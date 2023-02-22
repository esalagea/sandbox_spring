package com.esalagea.service;


import com.esalagea.error.UserAlreadyExistException;
import com.esalagea.dto.UserDto;
import com.esalagea.persistence.entity.PasswordResetToken;
import com.esalagea.persistence.repository.PasswordResetTokenRepository;
import com.esalagea.persistence.repository.RoleRepository;
import com.esalagea.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.esalagea.persistence.entity.User;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordResetTokenRepository passwordTokenRepository;


    @Autowired
    private PasswordEncoder bcryptEncoder;

    public boolean checkIfValidOldPassword(String username, String password) {
        Optional<User> userEntity = userRepository.findByUsername(username);
        if (!userEntity.isPresent()) {
            return false;
        }
        return bcryptEncoder.matches(password, userEntity.get().getPassword());
    }

    public void changeUserPassword(final User user, final String newPassword) {
        user.setPassword(bcryptEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public User registerNewUserAccount(UserDto user) throws UserAlreadyExistException {
        if (userExists(user.getUsername())) {
            throw new UserAlreadyExistException("There is an account with that username: " + user.getUsername());
        }

        if (emailExists(user.getEmail())) {
            throw new UserAlreadyExistException("There is an account with that email address: " + user.getEmail());
        }

        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(bcryptEncoder.encode(user.getPassword()));
        newUser.setEmail(user.getEmail());
        newUser.setName(user.getName());
        newUser.setRoles(Arrays.asList(roleRepository.findByName("ROLE_USER").get()));
        return userRepository.save(newUser);
    }

    public void saveUser(User user){
        userRepository.save(user);
    }


    public String generatePasswordResetTokenForUser(final User user) {
        final String token = UUID.randomUUID().toString();
        final PasswordResetToken myToken = new PasswordResetToken(token, user);
        passwordTokenRepository.save(myToken);
        return token;
    }


    public User findByUsername(final String username) {
        return userRepository.findByUsername(
                username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
    }

    public User findByUserEmail(final String email) {
        return userRepository.findByEmail(
                email)
                .orElseThrow(() -> new UsernameNotFoundException("Email not found!"));
    }

    public List<User> findAllUsers() {
        List<User> allUsers = userRepository.findAll();
        return allUsers;
    }

    public Optional<User> getUserByPasswordResetToken(final String token) {
        return Optional.ofNullable(passwordTokenRepository.findByToken(token) .getUser());
    }

    public User deleteUser(String username)  throws UsernameNotFoundException {
        User user = findByUsername(username);
        userRepository.delete(user);
        return user;
    }

    public void deleteUserIfExist(String username) {
        if (userExists(username)) {
            User user = findByUsername(username);
            userRepository.delete(user);
        }
    }

}

