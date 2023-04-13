package com.foresys.vacationAPI.biz.vacation.service;


import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.foresys.vacationAPI.biz.login.mapper.LoginMapper;
import com.foresys.vacationAPI.biz.vacation.mapper.MorningWorkMapper;
import com.foresys.vacationAPI.biz.vacation.mapper.VacationMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MorningWorkService {	
	
	@Autowired
	MorningWorkMapper morningWorkMapper;
	

	/**
	 * 아침당번 리스트를 불러옴 
	 * @param params
	 * @return 
	 */
	public List<Map<String, Object>> getMorningWorkList(Map<String, Object> params) {		
		return morningWorkMapper.getMorningWorkList(params);
	}
	
	
	/**
	 * 아침당번 변경 
	 * @param params
	 * @return resultMap
	 */
	public Map<String, Object> chgMorningWorkMember(Map<String, Object> params){ 
		
		Map<String, Object> resultMap = new HashMap<String, Object>(); 
		String chgCd = "";
		String chkDate = (String) params.get("check_date");
		String memTel = (String) params.get("member_tel_no");
		
		params.put("check_year", chkDate.substring(0,4));
		params.put("check_month", chkDate.substring(4,6));
		//params.put("member_tel_no", memTel.replaceAll("-", ""));
		
		//todo 중복체크 ( 당일에 아당으로 기록되어 있는지)
		log.info("아당변경 params::::::::>>>>"+params);
		
		
		try {
			//아당 삭제
			morningWorkMapper.deleteMorningMember(params);
			//아당 추가
			morningWorkMapper.insertMorningMember(params);					
			
			chgCd = "0000";			
		}catch(Exception e) {
			chgCd = "9999";
			e.printStackTrace();
		}
		
		resultMap.put("chgCd", chgCd);		
		
		if(chgCd == "0000") {
			resultMap.put("chgMsg", "아침당번 변경이 완료되었습니다.");
		}else {
			resultMap.put("chgMsg", "아침당번 변경 중 오류가 발생했습니다.");
		}
		
		return resultMap;
	}
	
	
	/**
	 * 아침당번 멤버 리스트 조회 
	 * @param params
	 * @return 
	 */
	public List<Map<String, Object>> getWorkMemberList(Map<String, Object> params) {		
		return morningWorkMapper.getWorkMemberList(params);
	}
	
	/**
	 * 아당 점검 목록 리스트 조회 
	 * @param params
	 * @return 
	 */
		
	public List<Map<String, Object>> getMorningMemoList(Map<String, Object> params) {				
		return morningWorkMapper.morningMemoList(params);
	}
	
	
}
