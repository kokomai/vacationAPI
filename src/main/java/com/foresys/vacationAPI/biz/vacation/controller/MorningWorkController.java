package com.foresys.vacationAPI.biz.vacation.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.foresys.vacationAPI.biz.vacation.service.MorningWorkService;
import com.foresys.vacationAPI.biz.vacation.service.VacationService;

@RestController
public class MorningWorkController {
	@Autowired
	MorningWorkService morningWorkService;
	
	@PostMapping("/vacation/getMorningWorkList")
	public List<Map<String, Object>> getMorningWorkList(@RequestBody Map<String, Object> params) {
		return morningWorkService.getMorningWorkList(params);
	}	
	
	@PostMapping("/vacation/chgMorningWorkMember")
	public Map<String, Object> chgMorningWorkMember(@RequestBody Map<String, Object> params) {
		return morningWorkService.chgMorningWorkMember(params);
	}
	
	@PostMapping("/vacation/getWorkMemberList")
	public List<Map<String, Object>> getWorkMemberList(@RequestBody Map<String, Object> params) {
		return morningWorkService.getWorkMemberList(params);
	}	
	
	@PostMapping("/vacation/getMorningMemoList")	
	public List<Map<String, Object>> getMorningMemoList(@RequestBody Map<String, Object> params) {
		return morningWorkService.getMorningMemoList(params);
	}
	
}
