package com.foresys.vacationAPI.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.foresys.vacationAPI.biz.login.service.LoginService;
import com.foresys.vacationAPI.security.model.UserInfoDetails;

@Component
public class UserInfoDetailService implements UserDetailsService{
	@Autowired
	private LoginService loginService;
	
	@Override
	public UserInfoDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserInfoDetails user = loginService.getUserName(username);
		return user;
	}
	
}
