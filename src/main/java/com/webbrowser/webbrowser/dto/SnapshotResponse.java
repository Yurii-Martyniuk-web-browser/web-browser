package com.webbrowser.webbrowser.dto;
import java.util.List;

public record SnapshotResponse(
        int historyId,
        String mainHtml,
        List<ResourceDto> css,
        List<ResourceDto> js,
        List<ImageDto> images
) {

    public record ResourceDto(
            String url,
            String content) {}

    public record ImageDto(
            String url,
            byte[] content) {}
}