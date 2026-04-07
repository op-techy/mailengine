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
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final FileUploadRepository fileUploadRepository;
    private final RecipientRepository recipientRepository;
    private final RecipientListRepository recipientListRepository;
    private final RecipientListMemberRepository recipientListMemberRepository;
    private final SuppressionListRepository suppressionListRepository;

    @Async
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
        int totalRows = 0, imported = 0, skipped = 0, duplicates = 0;

        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(file.getInputStream()))
                .withSkipLines(1)
                .build()) {

            String[] row;
            while ((row = reader.readNext()) != null) {
                totalRows++;
                if (row.length < 1 || row[0].isBlank()) { skipped++; continue; }

                String email = row[0].trim().toLowerCase();
                String firstName = row.length > 1 ? row[1].trim() : null;
                String lastName  = row.length > 2 ? row[2].trim() : null;

                if (suppressionListRepository.existsByAccountIdAndEmail(accountId, email)) {
                    skipped++; continue;
                }
                if (recipientRepository.existsByAccountIdAndEmail(accountId, email)) {
                    duplicates++; continue;
                }

                saveRecipientToList(email, firstName, lastName, accountId, listId, list);
                imported++;
            }

            list.setRecipientCount(list.getRecipientCount() + imported);
            recipientListRepository.save(list);
            upload.setStatus(UploadStatus.completed);

        } catch (Exception e) {
            log.error("CSV import failed for fileUploadId {}: {}", fileUploadId, e.getMessage());
            upload.setStatus(UploadStatus.failed);
        }

        saveUploadStats(upload, totalRows, imported, skipped, duplicates);
    }

    private void processExcel(Long fileUploadId, MultipartFile file, Long listId, Long accountId) {
        FileUpload upload = fileUploadRepository.findById(fileUploadId).orElseThrow();
        RecipientList list = recipientListRepository.findById(listId).orElseThrow();
        int totalRows = 0, imported = 0, skipped = 0, duplicates = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                totalRows++;

                Cell emailCell = row.getCell(0);
                if (emailCell == null || emailCell.getStringCellValue().isBlank()) {
                    skipped++; continue;
                }

                String email = emailCell.getStringCellValue().trim().toLowerCase();
                String firstName = row.getCell(1) != null ? row.getCell(1).getStringCellValue() : null;
                String lastName  = row.getCell(2) != null ? row.getCell(2).getStringCellValue() : null;

                if (suppressionListRepository.existsByAccountIdAndEmail(accountId, email)) {
                    skipped++; continue;
                }
                if (recipientRepository.existsByAccountIdAndEmail(accountId, email)) {
                    duplicates++; continue;
                }

                saveRecipientToList(email, firstName, lastName, accountId, listId, list);
                imported++;
            }

            list.setRecipientCount(list.getRecipientCount() + imported);
            recipientListRepository.save(list);
            upload.setStatus(UploadStatus.completed);

        } catch (Exception e) {
            log.error("Excel import failed for fileUploadId {}: {}", fileUploadId, e.getMessage());
            upload.setStatus(UploadStatus.failed);
        }

        saveUploadStats(upload, totalRows, imported, skipped, duplicates);
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
}
