package com.foresys.vacationAPI.biz.login.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.foresys.vacationAPI.security.model.UserInfoDetails;

@Service
public class LoginService {
	public UserInfoDetails getUserName(String username) {
		// TODO : DB 에서 username 실제로 조회 해오는 로직 추가 아래는 예시
		// return sqlSession.selectOne("login.selectUserById", username);
		
		UserInfoDetails user = new UserInfoDetails();
		user.setUSERNAME(username);
		user.setAUTHORITY("USER");
		
		return user;
	}
	
	public boolean checkUser(Map<String, Object> params) {
		// TODO : DB에서 실제 로그인 체크를 진행해야 함
		return true;
	}
}
