package com.mailengine.mailengine.service;

import com.mailengine.mailengine.entity.*;
import com.mailengine.mailengine.entity.enums.RecipientStatus;
import com.mailengine.mailengine.entity.enums.UploadStatus;
import com.mailengine.mailengine.repository.*;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.util.Locale;

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
     * Async entry point — detects file type and delegates.
     * Columns are assumed to be: 0=email, 1=firstName, 2=lastName.
     * Full column-mapping support (PRD Flow 3 step 4) is a post-MVP enhancement;
     * the file_uploads.column_mapping JSONB field is ready to store the mapping
     * once that wizard step is built.
     */
    @Async
    @Transactional
    public void processImport(Long fileUploadId, MultipartFile file, Long listId, Long accountId) {
        String filename = file.getOriginalFilename();
        boolean isExcel = filename != null &&
                (filename.toLowerCase().endsWith(".xlsx") || filename.toLowerCase().endsWith(".xls"));
        if (isExcel) {
            processExcel(fileUploadId, file, listId, accountId);
        } else {
            processCsv(fileUploadId, file, listId, accountId);
        }
    }

    // ── CSV ───────────────────────────────────────────────────────────────────

    private void processCsv(Long fileUploadId, MultipartFile file, Long listId, Long accountId) {
        FileUpload upload = fileUploadRepository.findById(fileUploadId).orElseThrow();
        RecipientList list = recipientListRepository.findById(listId).orElseThrow();
        ImportStats stats = new ImportStats();

        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(file.getInputStream()))
                .withSkipLines(1)   // skip header row
                .build()) {

            String[] row;
            while ((row = reader.readNext()) != null) {
                stats.totalRows++;
                processRow(
                        getValue(row, 0),
                        getValue(row, 1),
                        getValue(row, 2),
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

    private void processExcel(Long fileUploadId, MultipartFile file, Long listId, Long accountId) {
        FileUpload upload = fileUploadRepository.findById(fileUploadId).orElseThrow();
        RecipientList list = recipientListRepository.findById(listId).orElseThrow();
        ImportStats stats = new ImportStats();

        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            if (wb.getNumberOfSheets() == 0) throw new IllegalArgumentException("No sheets found");
            Sheet sheet = wb.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                stats.totalRows++;
                processRow(
                        getCellValue(row, 0),
                        getCellValue(row, 1),
                        getCellValue(row, 2),
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

        // Check suppression list (covers hard bounces, complaints, unsubscribes)
        if (suppressionListRepository.existsByAccountIdAndEmail(accountId, normalizedEmail)) {
            stats.skipped++;
            return;
        }

        // Check for existing active recipient in this account
        if (recipientRepository.existsByAccountIdAndEmail(accountId, normalizedEmail)) {
            // They exist — check if already in this list
            recipientRepository.findByAccountIdAndEmail(accountId, normalizedEmail)
                    .ifPresent(existing -> {
                        if (existing.getStatus() == RecipientStatus.unsubscribed ||
                                existing.getStatus() == RecipientStatus.bounced) {
                            stats.skipped++;
                        } else if (!recipientListMemberRepository
                                .existsByRecipientListIdAndRecipientId(listId, existing.getId())) {
                            // Valid recipient not yet in this list — add membership
                            addMembership(existing, listId, list);
                            stats.imported++;
                        } else {
                            stats.duplicates++;
                        }
                    });
            return;
        }

        // Brand new recipient
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

    // ── Cell helpers ──────────────────────────────────────────────────────────

    private String getValue(String[] row, int index) {
        if (row.length <= index || row[index] == null) return null;
        String v = row[index].trim();
        return v.isBlank() ? null : v;
    }

    private String getCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        String v = dataFormatter.formatCellValue(cell).trim();
        return v.isBlank() ? null : v;
    }

    private static class ImportStats {
        int totalRows, imported, skipped, duplicates;
    }
}
