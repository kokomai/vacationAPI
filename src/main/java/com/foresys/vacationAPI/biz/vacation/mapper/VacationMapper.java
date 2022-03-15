package com.foresys.vacationAPI.biz.vacation.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VacationMapper {
	public Map<String, Object> getVacationRemains(Map<String, Object> params);
	public List<Map<String, Object>> getApprovers(Map<String, Object> params);
	public List<Map<String, Object>> getHolidays(Map<String, Object> params);
	public List<Map<String, Object>> getVacationList(Map<String, Object> params);
}
