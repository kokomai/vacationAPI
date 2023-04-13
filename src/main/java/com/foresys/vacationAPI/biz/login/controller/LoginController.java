package com.foresys.vacationAPI.biz.login.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.foresys.vacationAPI.biz.login.service.LoginService;

@RestController
public class LoginController {
	
	@Autowired
	LoginService loginService;
	
	
	@PostMapping("/vacation/login/loginCheck")
	public Map<String, Object>  loginCheck(@RequestBody Map<String, Object> params, HttpServletRequest request, HttpServletResponse response, Authentication authentication)throws Exception{
		return loginService.loginCheck(params, request, response, authentication);
	}
	
	@PostMapping("/vacation/login/loginForSms")
	public Map<String, Object>  loginForSms(@RequestBody Map<String, Object> params, HttpServletRequest request, HttpServletResponse response, Authentication authentication)throws Exception{
		return loginService.loginForSms(params, request, response, authentication);
	}
}
