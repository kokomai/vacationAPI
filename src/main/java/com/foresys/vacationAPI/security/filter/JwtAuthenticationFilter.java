package com.foresys.vacationAPI.security.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.foresys.vacationAPI.security.jwt.JwtUtil;
import com.foresys.vacationAPI.security.model.UserInfoDetails;
import com.foresys.vacationAPI.security.service.UserInfoDetailService;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter{
	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	private UserInfoDetailService userInfoDetailService;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		log.info("JwtAuthentificationfilter entered");
		
		String token;
		
		try {
			// access token 먼저 확인
			token = jwtUtil.resolveAToken(request);
			
			if(token != null) {
				
				// access token이 만료되었는지 체크
				if(!jwtUtil.isTokenExpired(token)) {
					// Acess Token 유효 
					
					log.info("ACCESS 토큰 유효");
					// access 토큰이 유효하면 토큰으로부터 유저 정보를 받아옵니다.
					
					Claims claims = jwtUtil.getClaims(token);
					String username = (String) claims.get("username");
					String password = (String) claims.get("password");
					
					// 새로운 Access 토큰 발행
					token = jwtUtil.createAccessToken(username, password);
					
					response.setHeader("X-AUTH-ATOKEN", token);
					UserInfoDetails user = userInfoDetailService.loadUserByUsername(username);
					
					UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(user.getUsername(), "", user.getAuthorities());
	                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
	                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
				} else {
					//Acess Token 유효 기간 만료
					log.info("ACCESS 토큰 기간 만료");
					
					//Refresh Token 확인
					token = jwtUtil.resolveRToken(request);
					//Refresh Token 유효기간 확인
					if (!jwtUtil.isTokenExpired(token)) {
						// Refresh Token 유효 
						
						// refresh 토큰이 유효하면 토큰으로부터 유저 정보를 받아옵니다.
						Claims claims = jwtUtil.getClaims(token);
						String username = (String) claims.get("username");
						String password = (String) claims.get("password");
						
						// 새로운 Access 토큰 발행
						token = jwtUtil.createAccessToken(username, password);
						
						response.setHeader("X-AUTH-ATOKEN", token);
						UserInfoDetails user = userInfoDetailService.loadUserByUsername(username);
						
						UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(user.getUsername(), "", user.getAuthorities());
		                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
					} else {
						log.info("REFRESH 토큰 유효 기간 만료");
					}
				}
			} else {
				log.info("토큰 값이 아예 없음");
			}
		} catch(Exception e) {
			log.info("auth filter error", e);
		}
		
		filterChain.doFilter(request, response);
	}
}
