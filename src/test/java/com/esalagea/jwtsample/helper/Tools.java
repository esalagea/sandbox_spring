package com.esalagea.jwtsample.helper;


import com.esalagea.persistence.entity.Role;
import com.esalagea.persistence.entity.User;
import com.esalagea.persistence.repository.RoleRepository;
import com.esalagea.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class Tools {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    public void createOrUpdateUser(String name, String userName, String email, String password, String roleStr) {

        User user = new User();
        Optional<User> userOpt = userRepository.findByUsername(userName);
        if (userOpt.isPresent()) {
            user = userOpt.get();
        }

        user.setName(userName);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setUsername(userName);
        Role role = roleRepository.findByName(roleStr).get();
        user.setRoles(Arrays.asList(role));
        userRepository.save(user);

    }


}
