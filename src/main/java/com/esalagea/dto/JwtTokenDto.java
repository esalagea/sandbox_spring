package com.esalagea.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class JwtTokenDto implements Serializable {
	private String jwttoken;

	public JwtTokenDto(String jwttoken) {
		this.jwttoken = jwttoken;
	}

	public JwtTokenDto() {

	}
}