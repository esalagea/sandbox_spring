package com.esalagea.dto;

import lombok.Data;

@Data
public class PasswordDto {
    private String oldPassword;
    private  String token;
    private String newPassword;
}