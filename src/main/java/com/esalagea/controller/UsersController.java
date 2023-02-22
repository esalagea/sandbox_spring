package com.esalagea.controller;

import com.esalagea.dto.UserDto;
import com.esalagea.persistence.entity.User;
import com.esalagea.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin
@Secured("ROLE_ADMIN")
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    @GetMapping("")
    public ResponseEntity<List<User>> listUsers() {
        try {
            List<User> allUsers = userService.findAllUsers();
            return ResponseEntity.ok(allUsers);
        } catch (Exception e) {
            return new ResponseEntity("You are not authorized to access this resource.", HttpStatus.FORBIDDEN);
        }
    }

    @PutMapping("{username}")
    public ResponseEntity<User> updateUser(@PathVariable String username, @RequestBody UserDto userDetails) {

        User userToUpdate = null;
        try {
            userToUpdate = userService.findByUsername(username);
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity("User not found " + username, HttpStatus.BAD_REQUEST);
        }

        if (userDetails.getName() != null) {
            userToUpdate.setName(userDetails.getName());
        }

        if (userDetails.getEmail() != null) {
            userToUpdate.setEmail(userDetails.getEmail());
        }

        if (userDetails.getPassword() != null) {
            userToUpdate.setPassword(bcryptEncoder.encode(userDetails.getPassword()));
        }

        userService.saveUser(userToUpdate);
        return ResponseEntity.ok(userToUpdate);
    }


    @DeleteMapping("{username}")
    public ResponseEntity<User> deleteUser(@PathVariable String username) {
        try {
            User userToDelete = userService.deleteUser(username);
            return  ResponseEntity.ok(userToDelete);
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity("User not found " + username, HttpStatus.BAD_REQUEST);
        }
    }

}
