package com.example.core.exceptions;

public class MapNotFoundException extends Exception {
    public MapNotFoundException(String message, Exception e) {
        super(message);
    }
}
