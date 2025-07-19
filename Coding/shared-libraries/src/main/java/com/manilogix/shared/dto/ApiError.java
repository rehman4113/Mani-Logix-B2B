package com.manilogix.shared.dto;

import java.time.LocalDateTime;

public class ApiError {
    private int statusCode;
    private LocalDateTime timestamp;
    private String message;
    private String details;

    public ApiError() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiError(int statusCode, String message, String details) {
        this();
        this.statusCode = statusCode;
        this.message = message;
        this.details = details;
    }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
