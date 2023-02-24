package com.esalagea.jwtsample.test;

import com.esalagea.config.JwtTokenUtil;
import com.esalagea.controller.AuthenticationController;
import com.esalagea.dto.JwtTokenDto;
import com.esalagea.dto.LoginDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


@RunWith(SpringRunner.class)
@WebMvcTest(controllers = AuthenticationController.class,  excludeAutoConfiguration = {SecurityAutoConfiguration.class})
//@Import(SecurityConfig.class)
@Import(AuthenticationController.class)
@ContextConfiguration(classes = {TestConfig.class})
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    public void testCreateAuthenticationToken() throws Exception {
        String username = "testuser";
        String password = "testpassword";
        String token = "testtoken";

        // mock the authentication manager to return a successful authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);
        Mockito.when(authenticationManager.authenticate(Mockito.any())).thenReturn(authentication);

        // mock the user details service to return a UserDetails object
        UserDetails userDetails = new User(username, password, new ArrayList<>());
        Mockito.when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        // mock the JWT token util to return a token
        Mockito.when(jwtTokenUtil.generateToken(userDetails)).thenReturn(token);


        // create a request to the authentication endpoint with a JSON body
        LoginDto authenticationRequest = new LoginDto();
        authenticationRequest.setUsername(username);
        authenticationRequest.setPassword(password);
        String requestBody = new ObjectMapper().writeValueAsString(authenticationRequest);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/user/authentication").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody);

        // perform the request and assert the response
        MvcResult result = mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        JwtTokenDto jwtTokenDto = new ObjectMapper().readValue(responseContent, JwtTokenDto.class);
        Assert.assertEquals(token, jwtTokenDto.getJwttoken());
    }
}
