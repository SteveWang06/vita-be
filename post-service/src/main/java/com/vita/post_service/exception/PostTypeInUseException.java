package com.vita.post_service.exception;

public class PostTypeInUseException extends RuntimeException {
    public PostTypeInUseException(String message) {
        super(message);
    }
    
    public PostTypeInUseException(String postTypeId, long postCount) {
        super(String.format("Cannot delete PostType with id: %s. It is being used by %d post(s)", postTypeId, postCount));
    }
}

