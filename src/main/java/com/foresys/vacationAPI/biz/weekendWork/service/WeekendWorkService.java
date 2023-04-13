package com.foresys.vacationAPI.biz.weekendWork.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foresys.vacationAPI.biz.login.mapper.LoginMapper;
import com.foresys.vacationAPI.biz.weekendWork.mapper.WeekendWorkMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WeekendWorkService {
	@Autowired
	WeekendWorkMapper weekendWorkMapper;
	
	@Autowired
	LoginMapper loginMapper;

	@Autowired
	private JavaMailSender mailSender;

	/**
	 * 주말근무 결재자 목록 조회
	 * 
	 * @param params
	 * @return
	 */
	public Map<String, Object> getApprovers(Map<String, Object> params) {
		log.info("params ::: {}", params);

		List<Map<String, Object>> sqlResult = weekendWorkMapper.getApprovers(params);

		log.info("sqlResult ::: {}", sqlResult);
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> auth1 = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> auth2 = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> auth3 = new ArrayList<Map<String, Object>>();

		if (sqlResult.size() >= 1) {
			for (Map<String, Object> item : sqlResult) {
				if ("01".equals((String) item.get("AUTH"))) {
					auth1.add(item);
				} else if ("02".equals((String) item.get("AUTH"))) {
					auth2.add(item);
				} else {
					auth3.add(item);
				}
			}
		}

		String auth = (String) params.get("auth");

		if ("C04".equals(auth)) {
			result.put("auth1", auth1);
			result.put("auth2", auth2);
			if ("002".equals(params.get("department"))) {
				// 마케팅일 경우 C04권한자도 C07권한자의 승인을 받아야 함
				result.put("auth3", auth3);
			}
		} else if ("C05".equals(auth)) {
			result.put("auth1", auth2);
			result.put("auth2", auth3);
		} else {
			result.put("auth1", auth3);
		}

		return result;
	}

	/**
	 * 주말근무 신청 프로세스
	 * 
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor = Exception.class)
	public int insertWeekendWork(Map<String, Object> params) {
		ArrayList<Map<String, Object>> list = (ArrayList<Map<String, Object>>) params.get("selectedDate");
		String applySeq = weekendWorkMapper.getApplySeq();

		params.put("applySeq", applySeq);
		//String auth1 = (String) params.get("auth1");
		String auth2 = (String) params.get("auth2");

		if ("02".equals(auth2)) {
			params.put("apprvId2", (String) params.get("auth2Nm"));
			params.put("apprvReqId2", (String) params.get("auth2Id"));
		}

		/*
		 * if("01".equals(auth1)) { params.put("vacaApprvId", (String)
		 * params.get("auth1Nm")); params.put("vacaApprvReqId", (String)
		 * params.get("auth1Id")); } else if("02".equals(auth1)) {
		 * params.put("vacaApprvId2", (String) params.get("auth1Nm"));
		 * params.put("vacaApprvReqId2", (String) params.get("auth1Id")); } else {
		 * params.put("vacaApprvId3", (String) params.get("auth1Nm"));
		 * params.put("vacaApprvReqId3", (String) params.get("auth1Id")); }
		 * 
		 * if("02".equals(auth2)) { params.put("vacaApprvId2", (String)
		 * params.get("auth2Nm")); params.put("vacaApprvReqId2", (String)
		 * params.get("auth2Id")); } else if("03".equals(auth2)) {
		 * params.put("vacaApprvId3", (String) params.get("auth2Nm"));
		 * params.put("vacaApprvReqId3", (String) params.get("auth2Id")); }
		 * 
		 * if(params.get("auth3") != null && "03".equals(params.get("auth3") + "")) {
		 * params.put("vacaApprvId3", (String) params.get("auth3Nm"));
		 * params.put("vacaApprvReqId3", (String) params.get("auth3Id")); }
		 */

		log.info("params ::: {}", params);
		int insertResult = weekendWorkMapper.insertWeekendWorkList(params);

		if (insertResult > 0) {
			log.info("lists ::: {}", list);

			for (Map<String, Object> item : list) {
				item.put("applySeq", applySeq);
				weekendWorkMapper.insertWeekendWorkApplyList(item);
			}

			// vacationMapper.insertVacationInsuList(params);

//				if(params.get("auth1Id") != null && !"".equals(params.get("auth1Id"))) {
//					// 1차 승인자 있을 시, 1차 승인자에게 메일 발송
//					params.put("approverId", params.get("auth1Id"));
//				} else if(params.get("auth2Id") != null && !"".equals(params.get("auth2Id"))){
//					// 1차 승인자 없을 시, 2차 승인자에게 메일 발송
//					params.put("approverId", params.get("auth2Id"));
//				} else {
//					// 1차 승인자 없을 시, 2차 승인자 없을시, 3차 승인자에게 메일 발송
//					params.put("approverId", params.get("auth3Id"));
//				}

			// 휴가 신청시 휴가 갯수 깎음
			// 년도 정보를 가져오기 위해..
			// params.put("year", ((String) params.get("vacaSeq")).substring(0, 4));

			// 해당 휴가의 승인자(요청자가 아닌, 로그인해서 승인하는 사람)의 정보를 chngId에 넣어줌
			// params.put("chngId", params.get("approverId"));

			// getVacationRemains는 휴가 대상자의 아이디를 "id"로 받으므로, 요청자의 아이디로
			// params.put("id", params.get("id"));
			// params.put("requesterId", params.get("id"));

			// log.info("year PARAMS :::: {}", params);

			// vacationMapper.updateVacaUsedCount(params);

			// mailKind = [02 : 신청], [03 : 휴가 승인되어 완료], [06 : 승인 후 휴가신청 취소] 휴가 상태 코드(C02)를
			// 따름
			// params.put("mailKind", "02");
			// sendMail(params);
			// sendSms(params);
		}

		return 1;
	}

	
	/**
	 * 유저의 신청, 승인, 반려된 휴가 리스트들을 가져옴 
	 * @param params
	 * @return
	 */
	public List<Map<String, Object>> getWeekendWorkList(Map<String, Object> params) {
		return weekendWorkMapper.getWeekendWorkList(params);
	}

	/**
	 * 신청한 주말근무의 자세한 정보를 가져옴 
	 * @param params
	 * @return
	 */
	public Map<String, Object> getWeekendWorkInfo(Map<String, Object> params) {
		log.info("params ::: {}", params);
		return weekendWorkMapper.getWeekendWorkInfo(params);
	}

	/**
	 * C05이상의 권한자의 휴가승인 리스트를 가져옴(부하직원이 신청한 휴가 목록 승인/반려를 위해 가져옴) 
	 * @param params
	 * @return
	 */
	public List<Map<String, Object>> getApproveList(Map<String, Object> params) {
		// 서버 현재 연도를 넣어줌
		//params.put("year", Integer.toString(LocalDate.now().getYear()));
		return weekendWorkMapper.getApproveList(params);
	}

	/**
	 * 휴가 승인자가(C05권한 이상) 신청된 휴가를 승인할 때의 프로세스
	 * @param params
	 * @return
	 */
	public int approveWeekendWork(Map<String, Object> params) {
		try {
			// 몇차 결재자인지 조회해서 state값을 받아옴
			String state = weekendWorkMapper.getApprovalState(params);
			params.put("state", state);
			log.info("PARAMS :::: {}", params);
			
			//Map<String, Object> approvers = vacationMapper.getMyApprovers(params);
			//String nowApproverId = params.get("id") + "";
			String nowApproverNm = weekendWorkMapper.getMemberNm(params);	// 현재 승인자 이름
			params.put("nowApproverNm", nowApproverNm);
			
			weekendWorkMapper.approveWeekendWork(params);
			
			//String approverId1 = approvers.get("VACA_APPRV_REQ_ID") + "";	// 첫번째 승인자
			//String approverId2 = approvers.get("VACA_APPRV_REQ_ID2") + "";	// 두번째 승인자
			//String approverId3 = approvers.get("VACA_APPRV_REQ_ID3") + "";	// 세번째 승인자
			
//			if("03".equals(state)) {
//				// mailKind = [02 : 신청], [03 : 휴가 승인되어 완료], [06 : 승인 후 휴가신청 취소] 휴가 상태 코드(C02)를 따름
//				params.put("mailKind", "03");
//			} else {
//				// 이 휴가승인이 중간 승인인 경우 다음 신청 메일을 보냄
//				
//				// mailKind = [02 : 신청], [03 : 휴가 승인되어 완료], [06 : 승인 후 휴가신청 취소] 휴가 상태 코드(C02)를 따름
//				params.put("mailKind", "02");
//				
//				if(nowApproverId.equals(approverId1)) {
//					// 현재 승인자가 첫번쨰 승인자일 경우 메일 전송 대상 id를 두번째 승인자로..
//					params.put("approverId", approverId2);
//					
//				} else if(nowApproverId.equals(approverId2)) {
//					// 현재 승인자가 두번쨰 승인자일 경우 메일 전송 대상 id를 세번째 승인자로..
//					params.put("approverId", approverId3);
//				}
//				
//				// 신청시에만 sms 전송
//				sendSms(params);
//			}
			
//			if(nowApproverId.equals(approverId1)) {
//				// 현재 승인자가 첫번쨰 승인자일 경우 1차 승인자 업데이트..
//				params.put("VACA_APPRV_ID", nowApproverNm);
//			} else if(nowApproverId.equals(approverId2)) {
//				// 현재 승인자가 두번쨰 승인자일 경우 2차 승인자 업데이트..
//				params.put("VACA_APPRV_ID2", nowApproverNm);
//			} else if(nowApproverId.equals(approverId3)) {
//				// 현재 승인자가 세번쨰 승인자일 경우 2차 승인자 업데이트..
//				params.put("VACA_APPRV_ID3", nowApproverNm);
//			}
			
			
			//sendMail(params);
		} catch(Exception e) {
			log.info("approveWeekendWork error :::: ", e);
			return 0;
		}
		
		return 1;
	}

	/**
	 * 휴가 승인자가(C05권한 이상) 신청된 휴가를 반려시킬 때의 프로세스
	 * @param params
	 * @return
	 */
	public int rejectWeekendWork(Map<String, Object> params) {
		try {
			weekendWorkMapper.rejectWeekendwork(params);
		} catch(Exception e) {
			log.info("rejectWeekendWork error :::: ", e);
			return 0;
		}
		
		return 1;
	}

	/**
	 * 유저가 휴가를 취소할 때의 프로세스
	 * @param params
	 * @return
	 */
	public int cancelWeekendWork(Map<String, Object> params) {
		try {
			
			// 권한을 가져워야 하는데 getMembersForSms(이미 있는 항목)을 통해 가져올 수 있기에 용도는 다르지만 재사용..
			//Map<String, Object> authResult = loginMapper.getMembersForSms(params);
			
			//String myAuth = authResult.get("AUTH") + "";
			
			//Map<String, Object> approverResult = vacationMapper.getMyApprovers(params);
			
			
//			if("C06".equals(myAuth) || "C07".equals(myAuth)) {
//				// 06,07권한이며
//				if(approverResult == null || "".equals(approverResult.get("VACA_APPRV_REQ_ID3")+"")) {
//					// 휴가 승인자를 안선택했을 경우 (원래 06, 07권한자들은 그래왔다고 함)
//					// 취소 메일 보내기
//					params.put("mailKind", "06");
//					sendMail(params);
//				}
//			}
			
			//  취소하면 해당 신청 휴가일수를 돌려받아야함.
			// 년도 정보를 가져오기 위해..
			//params.put("year", ((String) params.get("vacaSeq")).substring(0, 4));
			// 요청자 아이디가 곧 변경 아이디..
			//params.put("chngId", params.get("id"));
			//params.put("requesterId", params.get("id"));
			
			int deleteResult = weekendWorkMapper.deleteWeekendWorkApplyList(params);
			
			if(deleteResult > 0) {
				weekendWorkMapper.deleteWeekendWorkList(params);
			}
			
			//vacationMapper.updateVacaUsedCount(params);
		} catch(Exception e) {
			log.info("deleteWeekendWork error :::: ", e);
			return 0;
		}
		
		return 1;
	}

	/**
	 * 유저가 결재한 주말근무 리스트 조회 
	 * @param params
	 * @return
	 */
	public List<Map<String, Object>> getApproveCheckList(Map<String, Object> params) {
		return weekendWorkMapper.getApproveCheckList(params);
	}

	/**
	 * 전체 주말근무자 리스트 조회
	 * @param params
	 * @return
	 */
	public List<Map<String, Object>> getWeekendWorkManageList(Map<String, Object> params) {
		log.info("params ::: {}", params);
		return weekendWorkMapper.getWeekendWorkManageList(params);
	}
	
	/**
	 * 선택 일자 주말근무자 목록 조회
	 * @param params
	 * @return
	 */
	public List<Map<String, Object>> getWeekendWorkManageDetail(Map<String, Object> params) {
		return weekendWorkMapper.getWeekendWorkManageDetail(params);
	}
}
