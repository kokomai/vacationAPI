package com.foresys.vacationAPI.biz.vacation.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.foresys.vacationAPI.biz.vacation.service.VacationService;

@RestController
public class VacationController {
	@Autowired
	VacationService vacationService;
	
	@PostMapping("/vacation/getVacationRemains")
	public Map<String, Object> getVacationRemains(@RequestBody Map<String, Object> params) {
		return vacationService.getVacationRemains(params);
	}
	
	@PostMapping("/vacation/getApprovers")
	public Map<String, Object> getApprovers(@RequestBody Map<String, Object> params) {
		return vacationService.getApprovers(params);
	}
	
	@PostMapping("/vacation/getHolidays")
	public List<Map<String, Object>> getHolidays(@RequestBody Map<String, Object> params) {
		return vacationService.getHolidays(params);
	}
	
	@PostMapping("/vacation/getVacationList")
	public List<Map<String, Object>> getVacationList(@RequestBody Map<String, Object> params) {
		return vacationService.getVacationList(params);
	}
}
