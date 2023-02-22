package com.esalagea.jwtsample.integrationtest;

import com.esalagea.SpringBootHelloWorldApplication;
import com.esalagea.jwtsample.helper.Tools;
import com.esalagea.jwtsample.spring.TestDbConfig;
import com.esalagea.jwtsample.spring.TestIntegrationConfig;
import com.esalagea.persistence.entity.User;
import com.esalagea.persistence.repository.RoleRepository;
import com.esalagea.persistence.repository.UserRepository;
import com.esalagea.service.UserService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SpringBootHelloWorldApplication.class, TestDbConfig.class, TestIntegrationConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RestAPITest {

    @Value("${local.server.port}")
    int port;

    private static String USER_AUTH_BODY = "{    \"username\":\"user\",   \"password\":\"pwd\"}";
    private static String ADMIN_AUTH_BODY = "{    \"username\":\"admin\",   \"password\":\"pwd\"}";

    @Autowired
    UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    Tools tools;

    private String userJWTToken;
    private String adminJWTToken;

    @BeforeEach
    public void init() {

        tools.createOrUpdateUser("Admin User", "admin", "admin@esalagea.com", "pwd", "ROLE_ADMIN");
        tools.createOrUpdateUser("Regular User", "user", "user@esalagea.com", "pwd", "ROLE_USER");

        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        initUserAndAdminJWTTokens();
    }

    @AfterEach
    public void resetUserPassword() {
        tools.createOrUpdateUser("Admin User", "admin", "admin@esalagea.com", "pwd", "ROLE_ADMIN");
        tools.createOrUpdateUser("Regular User", "user", "user@esalagea.com", "pwd", "ROLE_USER");
    }

    @Test
    public void givenCorrectJWTToken_whenHi_thenCorrect() {
        Map<String, String> headers = new HashMap<String, String>() {
            {
                put("Authorization", "Bearer " + userJWTToken);
            }
        };

        given()
                .headers(headers)
                .when()
                .get("/hi")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void givenNoJWTToken_whenHi_thenUnauthorized() {
        Map<String, String> headers = new HashMap<>();

        given()
                .headers(headers)
                .when()
                .get("/hi")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void givenAdminJWTToken_whenListingUsers_thenCorrect() {
        Map<String, String> headers = new HashMap<String, String>() {
            {
                put("Authorization", "Bearer " + adminJWTToken);
            }
        };

        given()
                .headers(headers)
                .when()
                .get("/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void givenCorrectResetPasswordJWTToken_whenSavePassword_ThenCorrect(){
        Optional<User> userOpt = userRepository.findByUsername("test");
        String resetPasswordJWToken = userService.generatePasswordResetTokenForUser(userOpt.get());

        String changePasswordBody = "{    " +
                "\"token\":\""+resetPasswordJWToken+"\"," +
                "\"newPassword\":\"pwd_updated_with_token\"" +
                "}";

        given().contentType(ContentType.JSON)
                .body(changePasswordBody)
                .when()
                .post("/user/savePassword")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Login with new password
        String loginWithNewPasswordBody = "{    \"username\":\"test\",   \"password\":\"pwd_updated_with_token\"}";
        String newJWTToken = validateCredentialsAndGetJWTToken(loginWithNewPasswordBody);
        Assert.assertNotNull(newJWTToken);
    }

    @Test
    public void givenUserJWTToken_whenListingUsers_thenUnauthorized() {
        Map<String, String> headers = new HashMap<String, String>() {
            {
                put("Authorization", "Bearer " + userJWTToken);
            }
        };

        given()
                .headers(headers)
                .when()
                .get("/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void givenUserJWTToken_whenChangingPassword_thenCanLoginWithNewPassword() {

        String changePasswordBody = "{    \"oldPassword\":\"pwd\",   \"newPassword\":\"pwd_updated\"}";
        String loginWithNewPasswordBody = "{    \"username\":\"user\",   \"password\":\"pwd_updated\"}";

        Map<String, String> headers = new HashMap<String, String>() {
            {
                put("Authorization", "Bearer " + userJWTToken);
            }
        };
        // Change my password
        given()
                .contentType(ContentType.JSON)
                .headers(headers)
                .body(changePasswordBody)
                .when()
                .post("/user/updatePassword")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);


        // Login with the new password
        String newJWTToken = validateCredentialsAndGetJWTToken(loginWithNewPasswordBody);
        Assert.assertNotNull(newJWTToken);
    }


    @Test
    public void givenUserJWTToken_IfOldPasswordDoesNotMatchNewPassword_Then_CannotChangePassword() {

        String changePasswordBody = "{    \"oldPassword\":\"pwd_wrong\",   \"newPassword\":\"pwd_updated\"}";

        Map<String, String> headers = new HashMap<String, String>() {
            {
                put("Authorization", "Bearer " + userJWTToken);
            }
        };
        // Change my password
        given()
                .contentType(ContentType.JSON)
                .headers(headers)
                .body(changePasswordBody)
                .when()
                .post("/user/updatePassword")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void givenCorrectNewUserBody_WhenRegistering_ThenCanDeleteUser() {
        // Delete the user from the repository if exists
        final Optional<User> user = userRepository.findByUsername("newUser");
        if (user.isPresent()) {
            userRepository.delete(user.get());
        }

        String newUserBody = "{" +
                "\"name\":\"newUserToDelete\",   " +
                "\"username\":\"newUserToDelete\"," +
                "\"email\":\"newUserToDelete@test.com\"," +
                "\"password\":\"pwd\"}";

        Response response = given().contentType(ContentType.JSON)
                .body(newUserBody)
                .when()
                .post("user/registration");

        // Validate the response status code is 200 OK
        response.then().assertThat().statusCode(200);

        Map<String, String> headers = new HashMap<String, String>() {
            {
                put("Authorization", "Bearer " + adminJWTToken);
            }
        };

        Optional<User> userOpt = userRepository.findByUsername("newUserToDelete");
        Assert.assertTrue(userOpt.isPresent());

        // Delete the user
        given()
                .contentType(ContentType.JSON)
                .headers(headers)
                .when()
                .delete("users/newUserToDelete")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        userOpt = userRepository.findByUsername("newUserToDelete");
        Assert.assertFalse(userOpt.isPresent());

    }

    @Test
    public void givenCorrectNewUserBody_WhenRegisteringNewUser_Then_CanLoginWithNewUser() {
        // Delete the user from the repository if exists
        final Optional<User> user = userRepository.findByUsername("newUser");
        if (user.isPresent()) {
            userRepository.delete(user.get());
        }

        String newUserBody = "{" +
                             "\"name\":\"newUser\",   " +
                             "\"username\":\"newUser\"," +
                             "\"email\":\"newUser@test.com\"," +
                             "\"password\":\"pwd\"}";

        Response response = given().contentType(ContentType.JSON)
                .body(newUserBody)
                .when()
                .post("user/registration");

        // Validate the response status code is 200 OK
        response.then().assertThat().statusCode(200);

        // login with the new user
        String newJWTToken = validateCredentialsAndGetJWTToken("{    \"username\":\"newUser\",   \"password\":\"pwd\"}");
        Assert.assertNotNull(newJWTToken);

    }

    @Test
    public void givenExistingUserBody_CannotRegisterExistingUser() {
        String newUserBody = "{" +
                "\"name\":\"user\",   " +
                "\"username\":\"user\"," +
                "\"email\":\"user@test.com\"," +
                "\"password\":\"pwd\"}";

        Response response = given().contentType(ContentType.JSON)
                .body(newUserBody)
                .when()
                .post("user/registration").then().assertThat().statusCode(400).extract().response();

        String responseString = response.asString();
        Assert.assertTrue(responseString.contains("There is an account with that username"));

    }
    // ----- Private Methods. No tests -------


    private String validateCredentialsAndGetJWTToken(String body) {
        Response response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/user/authentication");

        // Validate the response status code is 200 OK
        response.then().statusCode(200);

        String jwtToken = response.jsonPath().get("jwttoken");
        return jwtToken;
    }

    public void initUserAndAdminJWTTokens() {
        userJWTToken = validateCredentialsAndGetJWTToken(USER_AUTH_BODY);
        adminJWTToken = validateCredentialsAndGetJWTToken(ADMIN_AUTH_BODY);
        Assert.assertNotNull(userJWTToken);
        Assert.assertNotNull(adminJWTToken);
    }




}
