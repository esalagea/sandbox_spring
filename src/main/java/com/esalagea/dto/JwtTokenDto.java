package com.esalagea.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class JwtTokenDto implements Serializable {
	private final String jwttoken;
}