package com.esalagea.jwtsample.integrationtest;

import com.esalagea.SpringBootHelloWorldApplication;
import com.esalagea.controller.HelloWorldController;
import com.esalagea.controller.RegistrationController;
import com.esalagea.controller.UsersController;
import com.esalagea.dto.UserDto;
import com.esalagea.jwtsample.helper.Tools;
import com.esalagea.jwtsample.spring.TestDbConfig;
import com.esalagea.jwtsample.spring.TestIntegrationConfig;
import com.esalagea.persistence.entity.User;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.springframework.test.util.AssertionErrors.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = {SpringBootHelloWorldApplication.class, TestDbConfig.class, TestIntegrationConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MethodSecurityIntegrationTest {

    @Autowired
    Tools tools;

    @Autowired
    UsersController usersController;

    @Autowired
    HelloWorldController helloWorldController;

    @Autowired
    RegistrationController registrationController;


    @Configuration
    @ComponentScan("com.esalagea.*")
    public static class SpringConfig {

    }

    @BeforeEach
    public void init(){
        tools.createOrUpdateUser("Admin User", "admin", "admin@esalagea.com", "pwd", "ROLE_ADMIN");
        tools.createOrUpdateUser("Regular User", "user", "user@esalagea.com", "pwd", "ROLE_USER");
    }


    @Test
    @WithMockUser(username = "emil", roles = { "ADMIN" })
    public void givenRoleViewer_whenCallListUsers_thenReturnUsername() {
        ResponseEntity<List<User>> allUsers = usersController.listUsers();
        assertEquals("", 200, allUsers.getStatusCodeValue());
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(roles = "USER")
    public void givenRoleUser_whenCallListUsers_thenThrowAccessDeniedException() {
        usersController.listUsers();
    }


    @Test
    @WithMockUser(username = "emil", roles = "USER")
    public void givenRoleUserWhenCallHiThenCorrectResponse() {
        String hi = helloWorldController.hi();
        assertEquals("", "hi, emil", hi);
    }

    @Test
    @WithMockUser(username = "emil", roles = "ADMIN")
    public void givenRoleAdminWhenUpdateUserThenCorrectResponse()
    {
        ResponseEntity<User> response
                = usersController.updateUser("test",
                new UserDto( "test", "test", "test@method_securty_tests.com", "test"));
        assertEquals("Status 200", 200, response.getStatusCodeValue());

    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username = "emil", roles = "USER")
    public void givenRoleUserWhenUpdateOtherUserThenAccessDenied()
    {
        ResponseEntity<User> response
                = usersController.updateUser("test",
                new UserDto( "test", "test", "test@method_securty_tests.com", "test"));

    }

    @Test(expected = AccessDeniedException.class)
    @WithAnonymousUser
    public void givenNoLoggedInUserWhenCallHiThenAccessDenied() {
        helloWorldController.hi();
    }

}
