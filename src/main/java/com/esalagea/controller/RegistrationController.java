package com.esalagea.controller;

import com.esalagea.dto.PasswordDto;
import com.esalagea.dto.UserDto;
import com.esalagea.error.UserAlreadyExistException;
import com.esalagea.persistence.entity.User;
import com.esalagea.service.IUserSecurityService;
import com.esalagea.service.IUserService;
import com.esalagea.service.UserSecurityService;
import com.esalagea.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;


@RestController
@CrossOrigin
public class RegistrationController {

    private final static Logger LOGGER = LoggerFactory.getLogger(RegistrationController.class);

    @Autowired
    private Environment env;

    @Autowired
    private UserService userDetailsService;

    @Autowired
    private IUserService userService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private IUserSecurityService securityUserService;

    /**
     * Updates the password from the current logged in user
     * @param passwordDto
     * @return
     */
    @PostMapping("/user/updatePassword")
    public ResponseEntity<?> changeUserPassword(@RequestBody PasswordDto passwordDto) {

        User userToUpdate = userService.findByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName());

        if (!userDetailsService.checkIfValidOldPassword(userToUpdate.getUsername(), passwordDto.getOldPassword())) {
            return new ResponseEntity<>("Invalid old password for user " + userToUpdate.getUsername(), HttpStatus.BAD_REQUEST);
        }

        userDetailsService.changeUserPassword(userToUpdate, passwordDto.getNewPassword());
        return ResponseEntity.ok("Password updated.");
    }


    @PostMapping(value = "/user/registration")
    public ResponseEntity<?> saveUser(@RequestBody @Valid UserDto user) throws Exception {
        try {
            User userEntity = userService.registerNewUserAccount(user);
            return ResponseEntity.ok(userEntity);
        } catch (UserAlreadyExistException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/user/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestParam("email") final String email) {
        try{
            final User user = userService.findByUserEmail(email);
            String token = userService.generatePasswordResetTokenForUser(user);
            SimpleMailMessage mailMessage = constructResetTokenEmail(getAppUrl(), token, user);
            // TODO: need to properly configure the google account to accept the request
            // mailSender.send(mailMessage);
            return ResponseEntity.ok(mailMessage.toString());
        } catch (UsernameNotFoundException e){
            LOGGER.debug("Email not found ", e);
        }
        return ResponseEntity.ok("Email sent.");
    }

    /**
     *
     * Saves a password with a reset token obtained by calling /user/resetPassword
     * @param passwordDto
     * @return
     */
    @PostMapping("/user/savePassword")
    public ResponseEntity<?> savePassword(@RequestBody PasswordDto passwordDto) {

        final String result = securityUserService.validatePasswordResetToken(passwordDto.getToken());

        if(result != null) {
            return new ResponseEntity("Invalid token", HttpStatus.BAD_REQUEST);
        }

        Optional<User> user = userService.getUserByPasswordResetToken(passwordDto.getToken());
        if(user.isPresent()) {
            userService.changeUserPassword(user.get(), passwordDto.getNewPassword());
            securityUserService.removePasswordResetToken(passwordDto.getToken());
            return ResponseEntity.ok("Password updated for user " + user.get().getUsername());
        } else {
            return new ResponseEntity("Invalid token", HttpStatus.BAD_REQUEST);
        }
    }



    private String getAppUrl() {
        return "http://localhost:8080/";
    }


    private SimpleMailMessage constructResetTokenEmail(final String contextPath, final String token, final User user) {
        final String url = contextPath + "/user/changePassword?token=" + token;
        final String message = "Click here to reset your password ";
        return constructEmail("Reset Password", message + " \r\n" + url, user);
    }

    private SimpleMailMessage constructEmail(String subject, String body, User user) {
        final SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(body);
        email.setTo(user.getEmail());
        email.setFrom(env.getProperty("support.email"));
        return email;
    }

}
