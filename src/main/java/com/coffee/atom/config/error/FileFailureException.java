package com.coffee.atom.config.error;

import lombok.Getter;

@Getter
public class FileFailureException extends RuntimeException{
    public FileFailureException(String message) {
        super(message);
    }
}
