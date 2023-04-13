package com.foresys.vacationAPI.biz.weekendWork.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.foresys.vacationAPI.biz.weekendWork.service.WeekendWorkService;


/**
 * 
 * 휴일근무 controller
 * @author Lim Kyoung Won
 * @date : 2023.01.19
 */ 
@RestController
public class WeekendWorkController {
	
	@Autowired
	WeekendWorkService weekendWorkService;
	
	
	/*
	 * 주말근무 결재자 조회
	 * */
	@PostMapping("/weekendWork/getApprovers")
	public Map<String, Object> getApprovers(@RequestBody Map<String, Object> params) {
		return weekendWorkService.getApprovers(params);
	}
	
	/*
	 * 유저의 신청, 승인, 반려된 주말근무 리스트 조회
	 * */
	@PostMapping("/weekendWork/getWeekendWorkList")
	public List<Map<String, Object>> getWeekendWorkList(@RequestBody Map<String, Object> params) {
		return weekendWorkService.getWeekendWorkList(params);
	}
	
	/*
	 * 주말근무 결재자 조회
	 * */
	@PostMapping("/weekendWork/insertWeekendWork")
	public int insertWeekendWork(@RequestBody Map<String, Object> params) {
		return weekendWorkService.insertWeekendWork(params);
	}
	
	/*
	 * 주말근무 신정 정보 조회
	 * */
	@PostMapping("/weekendWork/getWeekendWorkInfo")
	public Map<String, Object> getWeekendWorkInfo(@RequestBody Map<String, Object> params) {
		return weekendWorkService.getWeekendWorkInfo(params);
	}
	
	/*
	 * C05이상의 권한자의 주말근무승인 리스트 조회
	 * */
	@PostMapping("/weekendWork/getApproveList")
	public List<Map<String, Object>> getApproveList(@RequestBody Map<String, Object> params) {
		return weekendWorkService.getApproveList(params);
	}
	
	/*
	 * 주말근무 승인자가(C05권한 이상) 신청된 주말근무를 승인할 때의 프로세스
	 * */
	@PostMapping("/weekendWork/approveWeekendWork")
	public int approveWeekendWork(@RequestBody Map<String, Object> params) {
		return weekendWorkService.approveWeekendWork(params);
	}
	
	/*
	 * 주말근무 승인자가(C05권한 이상) 신청된 주말근무를 반려시킬 때의 프로세스
	 * */
	@PostMapping("/weekendWork/rejectWeekendWork")
	public int rejectVacation(@RequestBody Map<String, Object> params) {
		return weekendWorkService.rejectWeekendWork(params);
	}
	
	/*
	 * 유저가 주말근무를 취소할 때의 프로세스
	 * */
	@PostMapping("/weekendWork/cancelWeekendWork")
	public int cancelWeekendWork(@RequestBody Map<String, Object> params) {
		return weekendWorkService.cancelWeekendWork(params);
	}
	
	/*
	 * 유저의 주말근무 결재 리스트 조회 
	 * */
	@PostMapping("/weekendWork/getApproveCheckList")
	public List<Map<String, Object>> getApproveCheckList(@RequestBody Map<String, Object> params) {
		return weekendWorkService.getApproveCheckList(params);
	}
	
	/**
	 * C06 ~ C07권한자가 전체 휴가자 파악을 위해 가져오는 전체 휴가자 리스트 
	 */
	@PostMapping("/weekendWork/getWeekendWorkManageList")
	public List<Map<String, Object>> getWeekendWorkManageList(@RequestBody Map<String, Object> params) {
		return weekendWorkService.getWeekendWorkManageList(params);
	}	
	
	/**
	 * 선택 일자 주말근무자 목록 조회 
	 */
	@PostMapping("/weekendWork/getWeekendWorkManageDetail")
	public List<Map<String, Object>> getWeekendWorkManageDetail(@RequestBody Map<String, Object> params) {
		return weekendWorkService.getWeekendWorkManageDetail(params);
	}
	
	
}