package com.mailengine.mailengine.dto.response;

import com.mailengine.mailengine.entity.enums.UploadStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class UploadStatusResponse {

    private UploadStatus status;

    /** First 5 rows of the uploaded file for the column-mapping UI. */
    private List<Map<String, String>> preview;

    /** Detected column headers — frontend maps these to system fields. */
    private List<String> columns;

    private Integer totalRows;
    private Integer importedRows;
    private Integer skippedRows;
    private Integer duplicateRows;
}
