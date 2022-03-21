package com.foresys.vacationAPI.biz.vacation.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VacationMapper {
	public double getRequestCount(Map<String, Object> params);
	public Map<String, Object> getVacationRemains(Map<String, Object> params);
	public List<Map<String, Object>> getApprovers(Map<String, Object> params);
	public List<Map<String, Object>> getHolidays(Map<String, Object> params);
	public List<Map<String, Object>> getVacationList(Map<String, Object> params);
	public List<Map<String, Object>> getApproveList(Map<String, Object> params);
	public Map<String, Object> getVacationInfo(Map<String, Object> params);
	public String getVacaSeq();
	public int insertVacationList(Map<String, Object> params);
	public int insertVacationDateList(Map<String, Object> params);
	public int insertVacationInsuList(Map<String, Object> params);
	public int deleteVacationList(Map<String, Object> params);
	public int deleteVacationDateList(Map<String, Object> params);
	public int deleteVacationInsuList(Map<String, Object> params);
	public int updateVacaUsedCount(Map<String, Object> params);
	public int rejectVacation(Map<String, Object> params);
	
}
