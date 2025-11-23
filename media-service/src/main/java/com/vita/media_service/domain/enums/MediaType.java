package com.vita.media_service.domain.enums;

import lombok.Getter;

@Getter
public enum MediaType {
    IMAGE("image"),
    VIDEO("video");

    private final String type;

    MediaType(String type) {
        this.type = type;
    }

    public static MediaType fromMimeType(String mimeType) {
        if (mimeType == null) {
            return null;
        }
        
        if (mimeType.startsWith("image/")) {
            return IMAGE;
        } else if (mimeType.startsWith("video/")) {
            return VIDEO;
        }
        
        return null;
    }
}

