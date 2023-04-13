package com.foresys.vacationAPI.system.error.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErrorReportMapper {
	
	public int insertError(Map<String, Object> params);
}
