package com.mailengine.mailengine.dto.response;

import com.mailengine.mailengine.entity.enums.UploadStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileUploadResponse {
    private Long id;
    private UploadStatus status;
    private Integer totalRows;
    private Integer importedRows;
    private Integer skippedRows;
    private Integer duplicateRows;

    public FileUploadResponse(Long id, UploadStatus status) {
        this.id = id;
        this.status = status;
        this.totalRows = null;
        this.importedRows = null;
        this.skippedRows = null;
        this.duplicateRows = null;
    }
}
