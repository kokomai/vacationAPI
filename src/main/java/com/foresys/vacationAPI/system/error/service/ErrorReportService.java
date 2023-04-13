package com.foresys.vacationAPI.system.error.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.foresys.vacationAPI.system.error.mapper.ErrorReportMapper;

@Service
public class ErrorReportService {
	
	@Autowired
	ErrorReportMapper errorReportMapper;
	
	public int insertError(Map<String, Object> params) {
		return errorReportMapper.insertError(params);
	}
}
