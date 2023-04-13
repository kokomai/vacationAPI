package com.foresys.vacationAPI.biz.vacation.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MorningWorkMapper {
	
	// 유저의 휴가 목록을 가져옴
	public List<Map<String, Object>> getMorningWorkList(Map<String, Object> params);
	
	//아당 추가
	public void insertMorningMember(Map<String, Object> params);
	//아당 삭제
	public void deleteMorningMember(Map<String, Object> params);
	
	//아당 멤버 조회
	public List<Map<String, Object>> getWorkMemberList(Map<String, Object> params);
	
	//아당 점검목록
	public List<Map<String, Object>> morningMemoList(Map<String, Object> params);
	//public Map<String, Object> morningMemoList(Map<String, Object> params);	
	
	
}
