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
import org.springframework.util.StringUtils;
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

    @Async
    @Transactional
    public void processImport(Long fileUploadId, MultipartFile file, Long listId, Long accountId) {
        String filename = file.getOriginalFilename();
        boolean isExcel = filename != null &&
                (filename.endsWith(".xlsx") || filename.endsWith(".xls"));

        if (isExcel) {
            processExcel(fileUploadId, file, listId, accountId);
        } else {
            processCsv(fileUploadId, file, listId, accountId);
        }
    }

    private void processCsv(Long fileUploadId, MultipartFile file, Long listId, Long accountId) {
        FileUpload upload = fileUploadRepository.findById(fileUploadId).orElseThrow();
        RecipientList list = recipientListRepository.findById(listId).orElseThrow();
        ImportStats stats = new ImportStats();

        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(file.getInputStream()))
                .withSkipLines(1)
                .build()) {

            String[] row;
            while ((row = reader.readNext()) != null) {
                stats.totalRows++;

                String email = getValue(row, 0);
                String firstName = getValue(row, 1);
                String lastName = getValue(row, 2);

                processRecipientRow(email, firstName, lastName, accountId, listId, list, stats);
            }

            completeUpload(upload, list, stats);
            saveUploadStats(upload, stats.totalRows, stats.imported, stats.skipped, stats.duplicates);

        } catch (Exception e) {
            log.error("CSV import failed for fileUploadId {}", fileUploadId, e);
            upload.setStatus(UploadStatus.failed);
            fileUploadRepository.save(upload);
            saveUploadStats(upload, stats.totalRows, stats.imported, stats.skipped, stats.duplicates);
        }


    }

    private void processExcel(Long fileUploadId, MultipartFile file, Long listId, Long accountId) {
        FileUpload upload = fileUploadRepository.findById(fileUploadId).orElseThrow();
        RecipientList list = recipientListRepository.findById(listId).orElseThrow();
        ImportStats stats = new ImportStats();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException("Uploaded Excel file does not contain any sheets");
            }

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                stats.totalRows++;

                String email = getCellValue(row, 0);
                String firstName = getCellValue(row, 1);
                String lastName = getCellValue(row, 2);

                processRecipientRow(email, firstName, lastName, accountId, listId, list, stats);
                saveUploadStats(upload, stats.totalRows, stats.imported, stats.skipped, stats.duplicates);
            }

            completeUpload(upload, list, stats);

        } catch (Exception e) {
            log.error("Excel import failed for fileUploadId {}", fileUploadId, e);
            upload.setStatus(UploadStatus.failed);
            fileUploadRepository.save(upload);
            saveUploadStats(upload, stats.totalRows, stats.imported, stats.skipped, stats.duplicates);
        }
    }

    private void processRecipientRow(String email, String firstName, String lastName,
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
            stats.duplicates++;
            return;
        }

        saveRecipientToList(normalizedEmail, firstName, lastName, accountId, listId, list);
        stats.imported++;
    }

    private void completeUpload(FileUpload upload, RecipientList list, ImportStats stats) {
        list.setRecipientCount(list.getRecipientCount() + stats.imported);
        recipientListRepository.save(list);
        upload.setStatus(UploadStatus.completed);
        fileUploadRepository.save(upload);
    }

    private String getValue(String[] row, int index) {
        if (row.length <= index || row[index] == null) {
            return null;
        }
        String value = row[index].trim();
        return value.isBlank() ? null : value;
    }

    private String getCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return null;
        }
        String value = dataFormatter.formatCellValue(cell);
        return value.isBlank() ? null : value.trim();
    }

    private void saveRecipientToList(String email, String firstName, String lastName,
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

        RecipientListMemberId memberId = new RecipientListMemberId();
        memberId.setRecipientListId(listId);
        memberId.setRecipientId(recipient.getId());

        RecipientListMember member = new RecipientListMember();
        member.setId(memberId);
        member.setRecipientList(list);
        member.setRecipient(recipient);

        recipientListMemberRepository.save(member);
    }

    private void saveUploadStats(FileUpload upload, int total, int imported,
                                 int skipped, int duplicates) {
        upload.setTotalRows(total);
        upload.setImportedRows(imported);
        upload.setSkippedRows(skipped);
        upload.setDuplicateRows(duplicates);
        fileUploadRepository.save(upload);
    }

    private static class ImportStats {
        int totalRows;
        int imported;
        int skipped;
        int duplicates;
    }
}
