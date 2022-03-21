package com.foresys.vacationAPI.security.jwt;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {
	private final long atokenValidMilisecond = 1000L * 60 * 20; // 20분
	private final long rtokenValidMilisecond = 1000L * 60 * 60; // 60분
	
	// secert key 
	private static final String SECRET_KEY = "aefgjnkgerhuoq3rt5ohni245";
	
	// access 토큰 생성
	public String createAccessToken(String username, String role) {
		Claims claims = Jwts.claims();
		claims.put("username", username);
		claims.put("roles", role);
		Date now = new Date();
		
		return Jwts.builder()
				.setClaims(claims)
				.setIssuedAt(now)
				.setExpiration(new Date(now.getTime() + atokenValidMilisecond))
				.signWith(SignatureAlgorithm.HS256, SECRET_KEY)
				.compact();
	}
	
	// refresh 토큰 생성
	public String createRefreshToken(String username, String role) {
		Claims claims = Jwts.claims();
		claims.put("username", username);
		claims.put("roles", role);
		Date now = new Date();
		
		return Jwts.builder()
				.setClaims(claims)
				.setIssuedAt(now)
				.setExpiration(new Date(now.getTime() + rtokenValidMilisecond))
				.signWith(SignatureAlgorithm.HS256, SECRET_KEY)
				.compact();
	}
	
	// JWT 토큰에서 인증 정보 조회
	public Claims getClaims(String token) {
		return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
	}
	
	// JWT 토큰 만료 확인
	public boolean isTokenExpired(String token) {
		try {
			Jws<Claims> claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
			// 만료되었으면 true 반환
			return claims.getBody().getExpiration().before(new Date());
		} catch(Exception e) {
//			log.info("isTokenExpired  ::: ", e);
			// claims를 읽어오는 과정에서도 만료시 error를 뱉어냄
			return true;
		}
	}
	
	// Request header에 있는 access token 정보 가져오기
	public String resolveAToken(HttpServletRequest request) {
		return request.getHeader("X-AUTH-ATOKEN");
	}
	
	// Request header에 있는 refresh token 정보 가져오기
	public String resolveRToken(HttpServletRequest request) {
		return request.getHeader("X-AUTH-RTOKEN");
	}
}
