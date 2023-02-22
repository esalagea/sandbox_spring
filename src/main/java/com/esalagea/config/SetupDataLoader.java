package com.esalagea.config;


import com.esalagea.persistence.entity.Role;
import com.esalagea.persistence.entity.User;
import com.esalagea.persistence.repository.RoleRepository;
import com.esalagea.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

@Component
public class SetupDataLoader implements
        ApplicationListener<ContextRefreshedEvent> {

    boolean alreadySetup = false;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (alreadySetup)
            return;

        createRoleIfNotFound("ROLE_ADMIN");

        createRoleIfNotFound("ROLE_USER");

        createRoleIfNotFound("ROLE_GUEST");


       createUserIfNotFound("Emil1", "admin", "admin@esalagea.com", "pwd", "ROLE_ADMIN");
        createUserIfNotFound("Emil11", "admin1", "admin1@esalagea.com", "pwd", "ROLE_ADMIN");

       createUserIfNotFound("Emil2", "user", "writer@esalagea.com", "pwd", "ROLE_USER");

       createUserIfNotFound("Emil3","guest", "guest@esalagea.com", "pwd", "ROLE_GUEST");




        alreadySetup = true;
    }


    private void createUserIfNotFound(String name, String userName, String email, String password, String roleStr){
        if (userRepository.findByUsername(userName).isPresent()){
            return;
        }


        User user = new User();
        user.setName(userName);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setUsername(userName);
        Role role = roleRepository.findByName(roleStr).get();
        user.setRoles(Arrays.asList(role));
        userRepository.save(user);

    }


    @Transactional
    Role createRoleIfNotFound(
            String name) {

        Optional<Role> role = roleRepository.findByName(name);
        if (role.isPresent()) {
            return role.get();
        }

        Role newRole = new Role(name);

        roleRepository.save(newRole);
        return newRole;

    }
}