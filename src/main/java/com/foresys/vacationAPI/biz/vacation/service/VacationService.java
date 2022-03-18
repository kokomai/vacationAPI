package com.foresys.vacationAPI.biz.vacation.service;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.foresys.vacationAPI.biz.vacation.mapper.VacationMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VacationService {
	@Autowired
	VacationMapper vacationMapper;
	
	public Map<String, Object> getVacationRemains(Map<String, Object> params) {
		// 서버 현재 연도를 넣어줌
		params.put("year", Integer.toString(LocalDate.now().getYear()));
		
		// 신청 갯수 구해오기
		double reqCnt = vacationMapper.getRequestCount(params);
		
		Map<String, Object> result = vacationMapper.getVacationRemains(params);
		
		if(result != null && result.get("VACA_USED_CNT") != null && result.get("VACA_EXTRA_CNT") != null) {
			double usedCnt =  ((BigDecimal) result.get("VACA_USED_CNT")).doubleValue();
			double extraCnt = ((BigDecimal) result.get("VACA_EXTRA_CNT")).doubleValue();
			// 사용자에게 표시될 때는 신청 갯수도 계산해서..
			result.put("VACA_USED_CNT", usedCnt + reqCnt);
			result.put("VACA_EXTRA_CNT", extraCnt - reqCnt);
		}
		
		return result;
	}
	
	public Map<String, Object> getApprovers(Map<String, Object> params) {
		List<Map<String, Object>> sqlResult = vacationMapper.getApprovers(params);
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> auth1 = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> auth2 = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> auth3 = new ArrayList<Map<String, Object>>();
		
		if(sqlResult.size() >= 1) {
			for(Map<String, Object> item: sqlResult) {
				if("01".equals((String) item.get("AUTH"))) {
					auth1.add(item);
				} else if("02".equals((String) item.get("AUTH"))) {
					auth2.add(item);
				} else {
					auth3.add(item);
				}
			}
		}
		
		String auth = (String) params.get("auth");
		
		if("C04".equals(auth)) {
			result.put("auth1", auth1);
			result.put("auth2", auth2);
		} else if("C05".equals(auth)){
			result.put("auth1", auth2);
			result.put("auth2", auth3);
		} else {
			result.put("auth1", auth3);
		}
		
		return result;
	}
	
	public List<Map<String, Object>> getHolidays(Map<String, Object> params) {
		// 서버 현재 연도를 넣어줌
		params.put("year", Integer.toString(LocalDate.now().getYear()));
		return vacationMapper.getHolidays(params);
	}
	
	public List<Map<String, Object>> getVacationList(Map<String, Object> params) {
		return vacationMapper.getVacationList(params);
	}
	
	public List<Map<String, Object>> getApproveList(Map<String, Object> params) {
		// 서버 현재 연도를 넣어줌
		params.put("year", Integer.toString(LocalDate.now().getYear()));
		return vacationMapper.getApproveList(params);
	}
	
	public Map<String, Object> getVacationInfo(Map<String, Object> params) {
		return vacationMapper.getVacationInfo(params);
	}
	
	@SuppressWarnings("unchecked")
	public int insertVacation(Map<String, Object> params) {
		ArrayList<Map<String, Object>> list = (ArrayList<Map<String, Object>>) params.get("selectedDate");
		String vacaSeq =  vacationMapper.getVacaSeq();
		
		params.put("vacaSeq", vacaSeq);
		String auth1 = (String) params.get("auth1");
		String auth2 = (String) params.get("auth2");
		
		// 값이 안들어갈 경우도 있으므로 params 내에 빈 string으로 선언은 해준다.
		params.put("vacaApprvId", "");
		params.put("vacaApprvReqId", "");
		params.put("vacaApprvId2", "");
		params.put("vacaApprvReqId2", "");
		params.put("vacaApprvId3", "");
		params.put("vacaApprvReqId3", "");
		
		if("01".equals(auth1)) {
			params.put("vacaApprvId", (String) params.get("auth1Nm"));
			params.put("vacaApprvReqId", (String) params.get("auth1Id"));
		} else if("02".equals(auth1)) {
			params.put("vacaApprvId2", (String) params.get("auth1Nm"));
			params.put("vacaApprvReqId2", (String) params.get("auth1Id"));
		} else {
			params.put("vacaApprvId3", (String) params.get("auth1Nm"));
			params.put("vacaApprvReqId3", (String) params.get("auth1Id"));
		}
		
		if("02".equals(auth2)) {
			params.put("vacaApprvId2", (String) params.get("auth2Nm"));
			params.put("vacaApprvReqId2", (String) params.get("auth2Id"));
		} else if("03".equals(auth2)) {
			params.put("vacaApprvId3", (String) params.get("auth2Nm"));
			params.put("vacaApprvReqId3", (String) params.get("auth2Id"));
		}
		
		log.info("params ::: {}", params);
		try {
			int result1 = vacationMapper.insertVacationList(params);
			
			if(result1 > 0) {
				log.info("lists ::: {}", list);
				
				for(Map<String, Object> item : list) {
					item.put("vacaSeq", vacaSeq);
					vacationMapper.insertVacationDateList(item);
				}
				
				vacationMapper.insertVacationInsuList(params);
			}
			
		} catch(Exception e) {
			log.info("insertVacation error :::: ", e);
			return 0;
		}
		
		return 1;
	}
	
	public int cancelVacation(Map<String, Object> params) {
		try {
			int result1 = vacationMapper.deleteVacationDateList(params);
			result1 += vacationMapper.deleteVacationInsuList(params);
			
			if(result1 > 0) {
				vacationMapper.deleteVacationList(params);
			}
		} catch(Exception e) {
			log.info("deleteVacation error :::: ", e);
			return 0;
		}
		
		return 1;
	}
	
}
