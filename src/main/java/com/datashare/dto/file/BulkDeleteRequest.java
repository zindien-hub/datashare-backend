package com.datashare.dto.file;

import java.util.List;

public record BulkDeleteRequest(List<Long> fileIds) {
}
