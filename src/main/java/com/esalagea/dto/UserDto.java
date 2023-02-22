package com.esalagea.dto;

import com.esalagea.validation.ValidEmail;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class UserDto {
    @NotNull

    @Size(min = 1, message = "{Size.userDto.name}")
    private String name;

    @NotNull
    private String username;

    @NotNull
    @ValidEmail
    private String email;

    @NotNull
    private String password;

    // constructor
    public UserDto( String name, String username, String email, String password) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
    }


}