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
	
	/**
	 * 유저의 휴가 남은 일자 조회
	 * @param params
	 * @return
	 */
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
	
	/**
	 * 휴가 승인자 리스트 권한별로 가져와서 셋팅
	 * C04 -> 1차 : C05 / 2차 : C06 
	 * C05 -> 1차 : C06 / 2차 : C07 
	 * C06 -> 1차 : C07 
	 * @param params
	 * @return
	 */
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
	
	/**
	 * 휴일을 가져옴 (휴일은 휴가를 쓸 필요가 없기에 달력에 표시)
	 * @param params
	 * @return
	 */
	public List<Map<String, Object>> getHolidays(Map<String, Object> params) {
		// 서버 현재 연도를 넣어줌
		params.put("year", Integer.toString(LocalDate.now().getYear()));
		return vacationMapper.getHolidays(params);
	}
	
	/**
	 * 유저의 신청, 승인, 반려된 휴가 리스트들을 가져옴 
	 * @param params
	 * @return
	 */
	public List<Map<String, Object>> getVacationList(Map<String, Object> params) {
		return vacationMapper.getVacationList(params);
	}
	
	/**
	 * C05이상의 권한자의 휴가승인 리스트를 가져옴(부하직원이 신청한 휴가 목록 승인/반려를 위해 가져옴) 
	 * @param params
	 * @return
	 */
	public List<Map<String, Object>> getApproveList(Map<String, Object> params) {
		// 서버 현재 연도를 넣어줌
		params.put("year", Integer.toString(LocalDate.now().getYear()));
		return vacationMapper.getApproveList(params);
	}
	
	/**
	 * 신청한 휴가의 자세한 정보를 가져옴 
	 * @param params
	 * @return
	 */
	public Map<String, Object> getVacationInfo(Map<String, Object> params) {
		return vacationMapper.getVacationInfo(params);
	}
	
	/**
	 * 휴가 신청 프로세스
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public int insertVacation(Map<String, Object> params) {
		ArrayList<Map<String, Object>> list = (ArrayList<Map<String, Object>>) params.get("selectedDate");
		String vacaSeq =  vacationMapper.getVacaSeq();
		
		params.put("vacaSeq", vacaSeq);
		String auth1 = (String) params.get("auth1");
		String auth2 = (String) params.get("auth2");
				
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
	
	/**
	 * 유저가 휴가를 취소할 때의 프로세스
	 * @param params
	 * @return
	 */
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
	
	/**
	 * 휴가 승인자가(C05권한 이상) 신청된 휴가를 반려시킬 때의 프로세스
	 * @param params
	 * @return
	 */
	public int rejectVacation(Map<String, Object> params) {
		try {
			vacationMapper.rejectVacation(params);
		} catch(Exception e) {
			log.info("rejectVacation error :::: ", e);
			return 0;
		}
		
		return 1;
	}
	
	/**
	 * 휴가 승인자가(C05권한 이상) 신청된 휴가를 승인할 때의 프로세스
	 * @param params
	 * @return
	 */
	public int approveVacation(Map<String, Object> params) {
		try {
			
			// 몇차 결재자인지 조회해서 state값을 받아옴
			String state = vacationMapper.getApprovalState(params);
			params.put("state", state);
			log.info("PARAMS :::: {}", params);
			
			// 이 휴가승인이 최종 승인인 경우 휴가 갯수를 깎음
			if("03".equals(state)) {
				params.put("year", ((String) params.get("vacaSeq")).substring(0, 5));
				log.info("year PARAMS :::: {}", params);
				Map<String, Object> remainsMap = vacationMapper.getVacationRemains(params);
				// 요청자의 실제 남은 휴가 갯수
				double remainsCount = (double) remainsMap.get("VACA_USED_CNT");
				// 요청자가 신청한 휴가 갯수
				double requestCount = vacationMapper.getUsedVacationCount(params);
				double usedCount = remainsCount - requestCount;
				
				params.put("usedCount", usedCount);
				params.put("chngId", params.get("id"));
				
				// TODO 03 휴가 깎는것, insert 계쏙 구현 해야 함
				// TODO sms / mail 전송 또한..
				/*
				UPDATE TB_FS_VACA_MAST
				SET 
					VACA_USED_CNT=#{usedCount},
					VACA_CHG_ID=#{chngId},
					VACA_CHG_DT=TO_CHAR(sysdate, 'YYYYMMDDHH24MISS')
				WHERE
					MEMBER_NO=#{requesterId}
				AND 
					VACA_YEAR=#{year}
				*/
				
				log.info("usedCount PARAMS :::: {}", params);
//				vacationMapper.updateVacaUsedCount(params);
			}
			
//			vacationMapper.approveVacation(params);
		} catch(Exception e) {
			log.info("approveVacation error :::: ", e);
			return 0;
		}
		
		return 1;
	}
	
}
