package com.esalagea.service;

public interface IUserSecurityService {

        String validatePasswordResetToken(String token);

        void removePasswordResetToken(String token);
}
