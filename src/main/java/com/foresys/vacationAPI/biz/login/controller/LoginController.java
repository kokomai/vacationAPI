package com.foresys.vacationAPI.biz.login.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.foresys.vacationAPI.security.exception.NotUserException;
import com.foresys.vacationAPI.security.jwt.JwtUtil;
import com.foresys.vacationAPI.security.model.Token.Response;
import com.foresys.vacationAPI.security.model.UserInfoDetails;
import com.foresys.vacationAPI.security.service.UserInfoDetailService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class LoginController {
	
	@Autowired
	JwtUtil jwtUtil;
	
	@Autowired
	UserInfoDetailService userInfoDetailService;
	
	@PostMapping("/login/loginCheck")
	public Map<String, Object>  loginCheck(@RequestBody Map<String, Object> params, HttpServletRequest request, HttpServletResponse response, Authentication authentication)throws Exception{
		log.info("login entered ");
		log.info("params {}", params);
		
		//아이디 검증
		Response res = null;
		if(params != null) {
			String username = (String) params.get("username");
			String password = (String) params.get("password");
			
			log.info("paramas not null");
			if( ("foresys".equals(username) && "for2sys".equals(password)) 
			|| ("1234".equals(username) && "1234".equals(password))) {
				res= Response.builder().accessToken(jwtUtil.createAccessToken(username, "USER"))
										.refreshToken(jwtUtil.createRefreshToken(username, "USER")).build();
				
				response.setHeader("X-AUTH-ATOKEN", res.getAccessToken());
				response.setHeader("X-AUTH-RTOKEN", res.getRefreshToken());
				
				UserInfoDetails user = userInfoDetailService.loadUserByUsername(username);
				
				authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), "", user.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(authentication);
				
				log.info("res : {}", res);
			} else {
				throw new NotUserException("사용자가 아닙니다");
			}
			
		}else {
			 throw new IllegalArgumentException("인자 값이 없습니다.");
		}
		
		return  params;
	}
}
