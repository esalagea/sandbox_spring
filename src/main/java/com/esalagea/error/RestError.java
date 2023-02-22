package com.esalagea.error;

import lombok.Data;

@Data
public class RestError {
    private String message;
    private String status;

    public RestError(String message, String status) {
        this.message = message;
        this.status = status;
    }
}
