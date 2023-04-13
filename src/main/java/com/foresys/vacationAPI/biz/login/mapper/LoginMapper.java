package com.foresys.vacationAPI.biz.login.mapper;

import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginMapper {
	public Map<String, Object> getMembers(Map<String, Object> params);
	public Map<String, Object> getMembersForSms(Map<String, Object> params);
}
