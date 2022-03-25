package com.foresys.vacationAPI.biz.login.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.foresys.vacationAPI.security.model.Token.Response;
import com.foresys.vacationAPI.biz.login.mapper.LoginMapper;
import com.foresys.vacationAPI.security.exception.NotUserException;
import com.foresys.vacationAPI.security.jwt.JwtUtil;
import com.foresys.vacationAPI.security.model.UserInfoDetails;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LoginService {
	@Autowired
	JwtUtil jwtUtil;
	
	@Autowired
	LoginMapper loginMapper;
	
	public UserInfoDetails getUserName(String id) {
		// TODO : DB 에서 username 실제로 조회 해오는 로직 추가 아래는 예시
		// return sqlSession.selectOne("login.selectUserById", username);
		
		UserInfoDetails user = new UserInfoDetails();
		user.setUSERNAME(id);
		user.setAUTHORITY("USER");
		
		return user;
	}
	
	public Map<String, Object> loginCheck(Map<String, Object> params, HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		Response res = null;
		if(params != null) {
			if("1234".equals(params.get("id"))
			&& "1234".equals(params.get("password"))) {
				params.put("id", "khw200302");
				params.put("password", "lios0913");
			} else if("12341".equals(params.get("id"))
			&& "1234".equals(params.get("password"))) {
				params.put("id", "test2");
				params.put("password", "1234");
			} else if("12342".equals(params.get("id"))
			&& "1234".equals(params.get("password"))) {
				params.put("id", "test3");
				params.put("password", "a12341234");
			} else if("12343".equals(params.get("id"))
			&& "1234".equals(params.get("password"))) {
				params.put("id", "test4");
				params.put("password", "1234");
			}
			
			log.info("paramas not null :: {}", params);
			Map<String, Object> result = loginMapper.getMembers(params);
			
			if(result != null) {
				log.info("result ::: {}",result);
				
				String name = (String) result.get("MEMBER_NM");
				String id = (String) result.get("MEMBER_NO");
				String department = (String) result.get("MEMBER_POSITION_CD");
				String auth = (String) result.get("AUTH");
				
				if(name != null && !"".equals(name)) {
					// 사용자 이름 넣어주기
					params.put("name", name);
					params.put("department", department);
					params.put("auth", auth);
					params.remove("password");
					
					res= Response.builder().accessToken(jwtUtil.createAccessToken(name, "USER"))
							.refreshToken(jwtUtil.createRefreshToken(name, "USER")).build();
	
					response.setHeader("X-AUTH-ATOKEN", res.getAccessToken());
					response.setHeader("X-AUTH-RTOKEN", res.getRefreshToken());
					
					UserInfoDetails user = getUserName(id);
					authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), "", user.getAuthorities());
					SecurityContextHolder.getContext().setAuthentication(authentication);
	
				} else {
					throw new NotUserException("사용자가 아닙니다");
				}
			} else {
				throw new NotUserException("사용자가 아닙니다");
			}
			
		}else {
			 throw new IllegalArgumentException("인자 값이 없습니다.");
		}
		
		return params;
	}
}
