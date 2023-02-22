package com.esalagea.service;


import com.esalagea.persistence.entity.PasswordResetToken;
import com.esalagea.persistence.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Calendar;

@Service
@Transactional
public class UserSecurityService {

    @Autowired
    private PasswordResetTokenRepository passwordTokenRepository;


    public String validatePasswordResetToken(String token) {
        final PasswordResetToken passToken = passwordTokenRepository.findByToken(token);

        return passToken == null ? "invalidToken"
                : isTokenExpired(passToken) ? "expired"
                : null;
    }

    public void removePasswordResetToken(String token){
        passwordTokenRepository.deleteByToken(token);
    }

    private boolean isTokenExpired(PasswordResetToken passToken) {
        final Calendar cal = Calendar.getInstance();
        return passToken.getExpiryDate().before(cal.getTime());
    }
}