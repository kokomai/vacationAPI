package com.foresys.vacationAPI.system.error.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foresys.vacationAPI.system.error.service.ErrorReportService;

@RestController
@RequestMapping("/error")
public class ErrorReportController {
	
	@Autowired
	ErrorReportService errorReportService;
	
	@PostMapping("/insert")
	public int insertError(@RequestBody Map<String, Object> params) {
		return errorReportService.insertError(params);
	}
}
