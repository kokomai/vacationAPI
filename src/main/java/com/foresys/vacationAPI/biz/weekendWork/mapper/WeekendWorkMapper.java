package com.foresys.vacationAPI.biz.weekendWork.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WeekendWorkMapper {

	// 주말근무 결재자(C06권한) 목록 조회
	public List<Map<String, Object>> getApprovers(Map<String, Object> params);
	// 주말근무 신청NO
	public String getApplySeq();
	// 주말근무 신청정보 등록 
	public int insertWeekendWorkList(Map<String, Object> params);
	// 주말근무 신청일 등록
	public void insertWeekendWorkApplyList(Map<String, Object> item);
	// 유저의 주말근무 목록 조회
	public List<Map<String, Object>> getWeekendWorkList(Map<String, Object> params);
	// 유저가 신청한 주말근무 정보 조회
	public Map<String, Object> getWeekendWorkInfo(Map<String, Object> params);
	// 주말근무 승인자(C05권한 이상)에게 신청된 주말근무 목록을 가져옴
	public List<Map<String, Object>> getApproveList(Map<String, Object> params);
	// 승인자의 해당 주말근무에 대한 권한을 조회하여 승인 코드를 받아옴 (1차승인 : 53 / 최종승인(2차승인) : 03)
	public String getApprovalState(Map<String, Object> params);
	// 유저 이름 조회(주말근무 승인시 필요)
	public String getMemberNm(Map<String, Object> params);
	// 신청된 주말근무의 상태를 "승인"으로 갱신
	public void approveWeekendWork(Map<String, Object> params);
	// 신청된 주말근무의 상태를 "반려"로 갱신
	public void rejectWeekendwork(Map<String, Object> params);
	// 주말근무 신청날짜 데이터 삭제
	public int deleteWeekendWorkApplyList(Map<String, Object> params);
	// 주말근무 신청 데이터 삭제
	public void deleteWeekendWorkList(Map<String, Object> params);
	// 주말근무 결재 리스트 조회  
	public List<Map<String, Object>> getApproveCheckList(Map<String, Object> params);
	// 전체 주말근무자 리스트 조회
	public List<Map<String, Object>> getWeekendWorkManageList(Map<String, Object> params);
	// 선택 일자 주말근무자 목록 조회
	public List<Map<String, Object>> getWeekendWorkManageDetail(Map<String, Object> params);
}
