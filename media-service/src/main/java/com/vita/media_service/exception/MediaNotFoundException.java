package com.vita.media_service.exception;

public class MediaNotFoundException extends RuntimeException {
    
    public MediaNotFoundException(String message) {
        super(message);
    }
    
    public static MediaNotFoundException withId(String id) {
        return new MediaNotFoundException("Media not found with id: " + id);
    }
}

