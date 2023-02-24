package com.esalagea.service;

import com.esalagea.dto.UserDto;
import com.esalagea.persistence.entity.User;

import java.util.Optional;

public interface IUserService {

    User findByUsername(String username);
    User registerNewUserAccount(UserDto user);
    User findByUserEmail(String email);
    String generatePasswordResetTokenForUser(User user);
    Optional<User> getUserByPasswordResetToken(String token);
    void changeUserPassword(User user, String newPassword);

}
