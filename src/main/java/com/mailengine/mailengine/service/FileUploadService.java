package com.mailengine.mailengine.service;

import com.mailengine.mailengine.entity.*;
import com.mailengine.mailengine.entity.enums.RecipientStatus;
import com.mailengine.mailengine.entity.enums.UploadStatus;
import com.mailengine.mailengine.exception.BadRequestException;
import com.mailengine.mailengine.repository.*;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final FileUploadRepository fileUploadRepository;
    private final RecipientRepository recipientRepository;
    private final RecipientListRepository recipientListRepository;
    private final RecipientListMemberRepository recipientListMemberRepository;
    private final SuppressionListRepository suppressionListRepository;
    private final DataFormatter dataFormatter = new DataFormatter();

    /**
     Async entry point — detects file type and delegates to the transactional import method.
     * {@code @Async} and @Transactional cannot be on the same method because Spring's async proxy
     * wraps the call before the transaction proxy can intercept it. The fix is this
     * thin @Async dispatcher calling a separate @Transactional method.
     */
    @Async
    public void processImport(Long fileUploadId, MultipartFile file, Long listId, Long accountId) {
        String filename = file.getOriginalFilename();
        boolean isExcel = filename != null &&
                (filename.toLowerCase().endsWith(".xlsx") || filename.toLowerCase().endsWith(".xls"));
        if (isExcel) {
            doProcessExcel(fileUploadId, file, listId, accountId);
        } else {
            doProcessCsv(fileUploadId, file, listId, accountId);
        }
    }

    @Transactional
    public ImportResult applyMapping(Long uploadId, Map<String, String> mapping) {
        FileUpload upload = fileUploadRepository.findById(uploadId)
                .orElseThrow(() -> new BadRequestException("Upload not found"));

        // Persist the mapping for audit / future reference
        upload.setColumnMapping(new java.util.HashMap<>(mapping));
        fileUploadRepository.save(upload);

        // Return the current stats — full re-import with custom mapping is a post-MVP feature.
        // The mapping is stored and will be used by the sending pipeline for merge tags.
        return new ImportResult(
                upload.getTotalRows(),
                upload.getImportedRows(),
                upload.getSkippedRows(),
                upload.getDuplicateRows()
        );
    }

    // ── CSV ───────────────────────────────────────────────────────────────────

    private void doProcessCsv(Long fileUploadId, MultipartFile file, Long listId, Long accountId) {
        FileUpload upload = fileUploadRepository.findById(fileUploadId).orElseThrow();
        RecipientList list = recipientListRepository.findById(listId).orElseThrow();
        ImportStats stats = new ImportStats();

        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(file.getInputStream()))
                .build()) {

            String[] headers = reader.readNext(); // first row is headers
            if (headers == null) {
                fail(upload, stats);
                return;
            }

            // Build a header→index map for flexible column ordering
            Map<String, Integer> colIndex = buildColumnIndex(headers);

            // Store the column preview on the upload record
            Map<String, Object> preview = new LinkedHashMap<>();
            for (String h : headers) preview.put(h, "");
            upload.setColumnPreview(preview);

            String[] row;
            while ((row = reader.readNext()) != null) {
                stats.totalRows++;
                processRow(
                        getByName(row, colIndex, "email"),
                        getByName(row, colIndex, "first_name"),
                        getByName(row, colIndex, "last_name"),
                        accountId, listId, list, stats
                );
            }
            complete(upload, list, stats);

        } catch (Exception e) {
            log.error("CSV import failed for upload {}", fileUploadId, e);
            fail(upload, stats);
        }
    }

    // ── Excel ─────────────────────────────────────────────────────────────────

    @Transactional
    public void doProcessExcel(Long fileUploadId, MultipartFile file, Long listId, Long accountId) {
        FileUpload upload = fileUploadRepository.findById(fileUploadId).orElseThrow();
        RecipientList list = recipientListRepository.findById(listId).orElseThrow();
        ImportStats stats = new ImportStats();

        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            if (wb.getNumberOfSheets() == 0) throw new IllegalArgumentException("No sheets found");
            Sheet sheet = wb.getSheetAt(0);

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                fail(upload, stats);
                return;
            }

            // Build column index from header row
            Map<String, Integer> colIndex = new LinkedHashMap<>();
            Map<String, Object> preview = new LinkedHashMap<>();
            for (Cell cell : headerRow) {
                String h = dataFormatter.formatCellValue(cell).trim().toLowerCase(Locale.ROOT);
                colIndex.put(h, cell.getColumnIndex());
                preview.put(h, "");
            }
            upload.setColumnPreview(preview);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                stats.totalRows++;
                processRow(
                        getExcelCell(row, colIndex, "email"),
                        getExcelCell(row, colIndex, "first_name"),
                        getExcelCell(row, colIndex, "last_name"),
                        accountId, listId, list, stats
                );
            }
            complete(upload, list, stats);

        } catch (Exception e) {
            log.error("Excel import failed for upload {}", fileUploadId, e);
            fail(upload, stats);
        }
    }

    // ── Per-row logic ─────────────────────────────────────────────────────────

    private void processRow(String email, String firstName, String lastName,
                            Long accountId, Long listId, RecipientList list,
                            ImportStats stats) {
        if (email == null || email.isBlank()) {
            stats.skipped++;
            return;
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (suppressionListRepository.existsByAccountIdAndEmail(accountId, normalizedEmail)) {
            stats.skipped++;
            return;
        }

        if (recipientRepository.existsByAccountIdAndEmail(accountId, normalizedEmail)) {
            recipientRepository.findByAccountIdAndEmail(accountId, normalizedEmail)
                    .ifPresent(existing -> {
                        if (existing.getStatus() == RecipientStatus.unsubscribed ||
                                existing.getStatus() == RecipientStatus.bounced) {
                            stats.skipped++;
                        } else if (!recipientListMemberRepository
                                .existsByRecipientListIdAndRecipientId(listId, existing.getId())) {
                            addMembership(existing, listId, list);
                            stats.imported++;
                        } else {
                            stats.duplicates++;
                        }
                    });
            return;
        }

        saveNewRecipientToList(normalizedEmail, firstName, lastName, accountId, listId, list);
        stats.imported++;
    }

    private void saveNewRecipientToList(String email, String firstName, String lastName,
                                        Long accountId, Long listId, RecipientList list) {
        Recipient recipient = new Recipient();
        recipient.setEmail(email);
        recipient.setFirstName(firstName);
        recipient.setLastName(lastName);
        recipient.setStatus(RecipientStatus.active);

        Account account = new Account();
        account.setId(accountId);
        recipient.setAccount(account);

        recipientRepository.save(recipient);
        addMembership(recipient, listId, list);
    }

    private void addMembership(Recipient recipient, Long listId, RecipientList list) {
        RecipientListMemberId memberId = new RecipientListMemberId();
        memberId.setRecipientListId(listId);
        memberId.setRecipientId(recipient.getId());

        RecipientListMember member = new RecipientListMember();
        member.setId(memberId);
        member.setRecipientList(list);
        member.setRecipient(recipient);

        recipientListMemberRepository.save(member);
    }

    // ── Completion ────────────────────────────────────────────────────────────

    private void complete(FileUpload upload, RecipientList list, ImportStats stats) {
        list.setRecipientCount(list.getRecipientCount() + stats.imported);
        recipientListRepository.save(list);
        upload.setStatus(UploadStatus.completed);
        saveStats(upload, stats);
    }

    private void fail(FileUpload upload, ImportStats stats) {
        upload.setStatus(UploadStatus.failed);
        saveStats(upload, stats);
    }

    private void saveStats(FileUpload upload, ImportStats stats) {
        upload.setTotalRows(stats.totalRows);
        upload.setImportedRows(stats.imported);
        upload.setSkippedRows(stats.skipped);
        upload.setDuplicateRows(stats.duplicates);
        fileUploadRepository.save(upload);
    }

    // ── Column helpers ──────────────────────────────────────────────────────────

    private Map<String, Integer> buildColumnIndex(String[] headers) {
        Map<String, Integer> idx = new LinkedHashMap<>();
        for (int i = 0; i < headers.length; i++) {
            idx.put(headers[i].trim().toLowerCase(Locale.ROOT), i);
        }
        return idx;
    }

    private String getByName(String[] row, Map<String, Integer> colIndex, String name) {
        Integer idx = colIndex.get(name);
        if (idx == null || idx >= row.length || row[idx] == null) return null;
        String v = row[idx].trim();
        return v.isBlank() ? null : v;
    }

    private String getExcelCell(Row row, Map<String, Integer> colIndex, String name) {
        Integer idx = colIndex.get(name);
        if (idx == null) return null;
        Cell cell = row.getCell(idx);
        if (cell == null) return null;
        String v = dataFormatter.formatCellValue(cell).trim();
        return v.isBlank() ? null : v;
    }


    // ── Result types ──────────────────────────────────────────────────────────
    private static class ImportStats {
        int totalRows;
        int imported;
        int skipped;
        int duplicates;
    }

    public record ImportResult(Integer totalRows, Integer imported, Integer skipped, Integer duplicates) {}
}
