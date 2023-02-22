package com.esalagea.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin()
@Secured("ROLE_USER")
public class HelloWorldController {

	@RequestMapping({ "/hello" })
	public String hello() {
		return "Hello World";
	}

	@RequestMapping({ "/hi" })
	public String hi() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return "hi, " + authentication.getName();
	}

}
