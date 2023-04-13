package com.foresys.vacationAPI.biz.vacation.service;


import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.foresys.vacationAPI.biz.login.mapper.LoginMapper;
import com.foresys.vacationAPI.biz.vacation.mapper.VacationMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VacationService {
	@Autowired
	VacationMapper vacationMapper;
	
	@Autowired
	LoginMapper loginMapper;
	
	@Autowired
    private JavaMailSender mailSender;
	
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
		
		if(result != null && result.get("VACA_USED_CNT") != null && result.get("remainCount_CNT") != null) {
			double usedCnt =  ((BigDecimal) result.get("VACA_USED_CNT")).doubleValue();
			double extraCnt = ((BigDecimal) result.get("remainCount_CNT")).doubleValue();
			// 사용자에게 표시될 때는 신청 갯수도 계산해서..
			result.put("VACA_USED_CNT", usedCnt + reqCnt);
			result.put("remainCount_CNT", extraCnt - reqCnt);
		}
		
		return result;
	}
	
	/**
	 * 휴가 승인자 리스트 권한별로 가져와서 셋팅
	 * C04 -> 1차 : C05 / 2차 : C06
	 * C04(마케팅) -> 1차 : C05 / 2차 : C06 / 3차 : C07  
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
			if("002".equals(params.get("department"))) {
				// 마케팅일 경우 C04권한자도 C07권한자의 승인을 받아야 함
				result.put("auth3", auth3);
			}
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
		
		if(params.get("auth3") != null && "03".equals(params.get("auth3") + "")) {
			params.put("vacaApprvId3", (String) params.get("auth3Nm"));
			params.put("vacaApprvReqId3", (String) params.get("auth3Id"));
		}
		
		if(params.get("auth1Id") != null && !"".equals(params.get("auth1Id"))) {
			// 1차 승인자 있을 시, 1차 승인자에게 메일 발송
			params.put("approverId", params.get("auth1Id"));
		} else if(params.get("auth2Id") != null && !"".equals(params.get("auth2Id"))){
			// 1차 승인자 없을 시, 2차 승인자에게 메일 발송
			params.put("approverId", params.get("auth2Id"));
		} else {
			// 1차 승인자 없을 시, 2차 승인자 없을시, 3차 승인자에게 메일 발송
			params.put("approverId", params.get("auth3Id"));
		}
		
		// 휴가 신청시 휴가 갯수 깎음
		// 년도 정보를 가져오기 위해..
		params.put("year", ((String) params.get("vacaSeq")).substring(0, 4));
		
		// 해당 휴가의 승인자(요청자가 아닌, 로그인해서 승인하는 사람)의 정보를 chngId에 넣어줌
		params.put("chngId", params.get("approverId"));
		
		// getVacationRemains는 휴가 대상자의 아이디를 "id"로 받으므로, 요청자의 아이디로
		params.put("id", params.get("id"));
		params.put("requesterId", params.get("id"));
		
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
				
				
				// mailKind = [02 : 신청], [03 : 휴가 승인되어 완료], [06 : 승인 후 휴가신청 취소] 휴가 상태 코드(C02)를 따름
				params.put("mailKind", "02");
				
				// 권한을 가져워야 하는데 getMembersForSms(이미 있는 항목)을 통해 가져올 수 있기에 용도는 다르지만 재사용..
				Map<String, Object> authResult = loginMapper.getMembersForSms(params);
				
				String myAuth = authResult.get("AUTH") + "";
				
				if("C06".equals(myAuth) || "C07".equals(myAuth)) {
					// 06,07권한이며
					if(params.get("approverId") == null || "".equals(params.get("approverId"))) {
						// 휴가 승인자를 안선택했을 경우 (원래 06, 07권한자들은 그래왔다고 함)
						// 03 (승인완료)상태로 메일 보내기
						params.put("mailKind", "03");
						// 바꾼 사람은 자기자신이므로..
						params.put("chngId", params.get("id"));
						
						vacationMapper.updateVacaUsedCount(params);
						// 승인상태로 업데이트..
						params.put("state", "03");
						vacationMapper.approveVacation(params);
						sendMail(params);
						return 1;
					}
				}
				
				vacationMapper.updateVacaUsedCount(params);
				sendMail(params);
				sendSms(params);
			}
			
		} catch(Exception e) {
			log.info("insertVacation error :::: ", e);
			cancelVacation(params);
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
			
			// 권한을 가져워야 하는데 getMembersForSms(이미 있는 항목)을 통해 가져올 수 있기에 용도는 다르지만 재사용..
			Map<String, Object> authResult = loginMapper.getMembersForSms(params);
			
			String myAuth = authResult.get("AUTH") + "";
			
			Map<String, Object> approverResult = vacationMapper.getMyApprovers(params);
			
			
			if("C06".equals(myAuth) || "C07".equals(myAuth)) {
				// 06,07권한이며
				if(approverResult == null || "".equals(approverResult.get("VACA_APPRV_REQ_ID3")+"")) {
					// 휴가 승인자를 안선택했을 경우 (원래 06, 07권한자들은 그래왔다고 함)
					// 취소 메일 보내기
					params.put("mailKind", "06");
					sendMail(params);
				}
			}
			
			//  취소하면 해당 신청 휴가일수를 돌려받아야함.
			// 년도 정보를 가져오기 위해..
			params.put("year", ((String) params.get("vacaSeq")).substring(0, 4));
			// 요청자 아이디가 곧 변경 아이디..
			params.put("chngId", params.get("id"));
			params.put("requesterId", params.get("id"));
			
			int result1 = vacationMapper.deleteVacationDateList(params);
			result1 += vacationMapper.deleteVacationInsuList(params);
			
			if(result1 > 0) {
				vacationMapper.deleteVacationList(params);
			}
			
			vacationMapper.updateVacaUsedCount(params);
		} catch(Exception e) {
			log.info("deleteVacation error :::: ", e);
			return 0;
		}
		
		return 1;
	}
	
	
	/**
	 * 결재자가 신청자의 휴가를 취소할 때의 프로세스
	 * @param params
	 * @return
	 */
	public int cancelRequsterVacation(Map<String, Object> params) {
		try {
			String state = vacationMapper.getVacationState(params);
			
			// 승인된 휴가 취소시 전체 메일 공지
			if("03".equals(state)) {
				params.put("mailKind", "06");
				sendMail(params);
			}
			
			//  취소하면 해당 신청 휴가일수를 돌려받아야함.
			// 년도 정보를 가져오기 위해..
			params.put("year", ((String) params.get("vacaSeq")).substring(0, 4));
			
			//vacaSeq 가지고 휴가자의 id를 추출 해야됨 ***
			
			// 요청자 아이디가 곧 변경 아이디..
			params.put("chngId", params.get("requesterId"));
			params.put("id", params.get("requesterId"));
			
			int result1 = vacationMapper.deleteVacationDateList(params);
			result1 += vacationMapper.deleteVacationInsuList(params);
			
			if(result1 > 0) {
				vacationMapper.deleteVacationList(params);
			}
			
			vacationMapper.updateVacaUsedCount(params);
		} catch(Exception e) {
			log.info("deleteRequsterVacation error :::: ", e);
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
			
			Map<String, Object> approvers = vacationMapper.getMyApprovers(params);
			String nowApproverId = params.get("id") + "";
			String nowApproverNm = vacationMapper.getMemberNm(params);	// 현재 승인자 이름
			String approverId1 = approvers.get("VACA_APPRV_REQ_ID") + "";	// 첫번째 승인자
			String approverId2 = approvers.get("VACA_APPRV_REQ_ID2") + "";	// 두번째 승인자
			String approverId3 = approvers.get("VACA_APPRV_REQ_ID3") + "";	// 세번째 승인자
			
			if("03".equals(state)) {
				// mailKind = [02 : 신청], [03 : 휴가 승인되어 완료], [06 : 승인 후 휴가신청 취소] 휴가 상태 코드(C02)를 따름
				params.put("mailKind", "03");
			} else {
				// 이 휴가승인이 중간 승인인 경우 다음 신청 메일을 보냄
				
				// mailKind = [02 : 신청], [03 : 휴가 승인되어 완료], [06 : 승인 후 휴가신청 취소] 휴가 상태 코드(C02)를 따름
				params.put("mailKind", "02");
				
				if(nowApproverId.equals(approverId1)) {
					// 현재 승인자가 첫번쨰 승인자일 경우 메일 전송 대상 id를 두번째 승인자로..
					params.put("approverId", approverId2);
					
				} else if(nowApproverId.equals(approverId2)) {
					// 현재 승인자가 두번쨰 승인자일 경우 메일 전송 대상 id를 세번째 승인자로..
					params.put("approverId", approverId3);
				}
				
				// 신청시에만 sms 전송
				sendSms(params);
			}
			
			if(nowApproverId.equals(approverId1)) {
				// 현재 승인자가 첫번쨰 승인자일 경우 1차 승인자 업데이트..
				params.put("VACA_APPRV_ID", nowApproverNm);
			} else if(nowApproverId.equals(approverId2)) {
				// 현재 승인자가 두번쨰 승인자일 경우 2차 승인자 업데이트..
				params.put("VACA_APPRV_ID2", nowApproverNm);
			} else if(nowApproverId.equals(approverId3)) {
				// 현재 승인자가 세번쨰 승인자일 경우 2차 승인자 업데이트..
				params.put("VACA_APPRV_ID3", nowApproverNm);
			}
			
			vacationMapper.approveVacation(params);
			sendMail(params);
		} catch(Exception e) {
			log.info("approveVacation error :::: ", e);
			return 0;
		}
		
		return 1;
	}
	
	
	/**
	 * 신청, 최종승인, 취소시 메일 보내기
	 * @param param
	 */
	public void sendMail(Map<String, Object> params)  { 
        String subj = "";
        String content = "";
        String targetEmail = "";
        
        String mailKind = params.get("mailKind").toString();
        
        // vacaSeq를 통해 해당 휴가정보를 가져옴
        List<Map<String, Object>> vacaList = vacationMapper.getRequestVacationList(params);
        
        String memberId = vacaList.get(0).get("MEMBER_NO") + "";
        params.put("id", memberId);
        String department = vacaList.get(0).get("MEMBER_POSITION_CD") + "";
        String memberNm = vacaList.get(0).get("MEMBER_NM") + "";
        
        String dateString = vacaList.get(0).get("VACA_DATE") + "";
        
        String allDateString = dateString + "(" + vacaList.get(0).get("VACA_DIV_NM") +")"; 
        
        if(vacaList.size() > 1) {
        	dateString += " 외 " + (vacaList.size() -1) + "일";
        	
        	for(int i = 1; i < vacaList.size(); i++) {
        		allDateString += ", " + vacaList.get(i).get("VACA_DATE") + "(" + vacaList.get(i).get("VACA_DIV_NM") +")";
        	}
        	
        } else {
        	if("02".equals(vacaList.get(0).get("VACA_DIV"))) {
            	// 하루인데 오전 반차만 있을 경우..
            	dateString += " 오전 반차";
            } else if("03".equals(vacaList.get(0).get("VACA_DIV"))) {
            	// 하루인데 오후 반차만 있을 경우..
            	dateString += " 오후 반차";
            }
        	
        }
        
        String memberClass = vacaList.get(0).get("MEMBER_CLASS_CD_NM") + "";
        String reason = vacaList.get(0).get("VACA_REASON") + "";
        String createdDate = vacaList.get(0).get("VACA_CRE_DATE") + "";
        params.put("id", vacaList.get(0).get("MEMBER_NO"));
        
        if("".equals(reason) || "null".equals(reason)) {
        	reason = "개인사유";
        }
        
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
        params.put("year", formatter.format(now));
        
        double useCount = 0.0;
        // 멤버 아이디와 현재 연도로 남은 휴가수 가져오기
        double remainCount = Double.parseDouble(vacationMapper.getVacationRemains(params).get("VACA_EXTRA_CNT") + "");
        
        // 이번에 사용한 휴가 갯수 구하기
        for(Map<String, Object> vacaItem : vacaList) {
        	if("01".equals(vacaItem.get("VACA_DIV"))) {
        		useCount++;
        	} else {
        		useCount += 0.5;
        	}
        }
        
        String vacaCountStr = "";
        
        if(useCount % 1 == 0) {
        	// 정수로 떨어질 경우 int로 해서 .0지워주기
        	vacaCountStr = "(" + (int) useCount + "/";
        } else {
        	vacaCountStr = "(" + useCount + "/";
        }
        
        if(remainCount % 1 == 0) {
        	// 정수로 떨어질 경우 int로 해서 .0지워주기
        	vacaCountStr += (int) remainCount + ")"; 
        } else {
        	vacaCountStr += remainCount + ")";
        }
       
       // mailKind = [02 : 신청], [03 : 휴가 승인되어 완료], [06 : 승인 후 휴가신청 취소] 휴가 상태 코드(C02)를 따름 
       if("02".equals(mailKind)) {
    	   subj = "[휴가 신청]"
           		+ memberNm + " "
               	+ dateString 
               	+ vacaCountStr;
//               	+ " TO." + params.get("approverId");
       	   content = "안녕하세요 <br/><br/>"
          		   		+ "포이시스 " + memberNm + " " + memberClass + "입니다 <br/>"
         		    	+ reason + "로 인한 휴가 신청합니다 <br/>"
         		    	+ "휴가일 : " + allDateString + "<br/>"
         		    	+ "감사합니다."
         		    	+ "<a href=\"http://222.112.9.173:3000?s=" + params.get("vacaSeq") 
         		    	+ "&m=" + params.get("approverId") + "\">휴가 신청서</a><br/>";
       	   try {
       		   targetEmail = vacationMapper.getApproverEmail(params);
       	   } catch (Exception e) {
       		   log.info("이메일주소찾기err "+e);
       		   params.put("approverId", memberId);
       		   targetEmail =vacationMapper.getApproverEmail(params);
       		   subj = "[휴가공지]발송실패";
       		   content = "관리자에게 문의하세요.";
       	   }
       } else if("03".equals(mailKind)) {
    	   subj  = "[휴가 공지]"
  				+ memberNm + " "
   				+ dateString 
   				+ vacaCountStr;
       	   content = "안녕하세요 <br/><br/>"
       			   + "포이시스 " + memberNm + " " + memberClass + "입니다 <br/>"
       			   + allDateString + "<br/>"
       			   + "휴가 사용을 알려드립니다 <br/>"
       			   +"감사합니다.";
       	   if("002".equals(department)) {
       		   //  신청자가 마케팅 소속일 경우
       		   targetEmail = "marketer@foresys.co.kr";
       	   } else {
       		   targetEmail = "solution_all@foresys.co.kr";
       	   }
       	   
       	   if(memberNm.contains("테스터")) {
	   		   targetEmail ="khw200302@foresys.co.kr";	   		   
	   	   }       	   
       } else if("06".equals(mailKind)) {
    	   subj = "[휴가 취소]"
        		+ memberNm + " "
            	+ dateString 
            	+ vacaCountStr;
    	   content =  "안녕하세요 <br/><br/>"
           		+ "포이시스 " + memberNm + " " + memberClass + "입니다 <br/>"
           		+ allDateString + "<br/>"
       		    + "휴가 취소를 알려드립니다 <br/>"
       		    +"감사합니다.";
	   	   if("002".equals(department)) {
			   //  신청자가 마케팅 소속일 경우
			   targetEmail = "marketer@foresys.co.kr";
		   } else {
			   targetEmail = "solution_all@foresys.co.kr";
		   }
	   	   
	   	   if(memberNm.contains("테스터")) {
	   		   targetEmail ="khw200302@foresys.co.kr";	   		   
	   	   }
       }
       
       try {
    	   MimeMessage mail = mailSender.createMimeMessage();
    	   MimeMessageHelper mailHelper = new MimeMessageHelper(mail, true, "UTF-8");
    	   String fromAddr = "foresys@foresys.co.kr";
    	   String[] toAddrs;
    	   
    	   if(!"02".equals(mailKind)) {
    		   // 신청이 아니라 공지, 취소의 경우 하이사님께도 보내기
    		   toAddrs = new String[2];
    		   toAddrs[1] = "bsha@foresys.co.kr";
    		   if(memberNm.contains("테스터")) {
        		   toAddrs[1] = "jin7691@foresys.co.kr";
    	   	   }
    	   } else {
    		   toAddrs = new String[1];
    	   }

    	   toAddrs[0] = targetEmail;
    	   mailHelper.setTo(toAddrs);
    	   mailHelper.setFrom(fromAddr, "포이시스 " + memberNm);
    	   mailHelper.setSubject(subj);
    	   mailHelper.setText(content, true);
    	   
    	   mailSender.send(mail);

    	   log.info("메일전송");

    	   if("03".equals(mailKind)) {
    		   // 승인되어 휴가 확정의 경우
    		   params.put("vacaEmailSendState", "04");
    		   vacationMapper.updateEmail(params);
    	   }
       } catch(Exception e) {
    	   log.error("메일 전송 에러 ::::: ", e);
    	   if("03".equals(mailKind)) {
    		   params.put("vacaEmailSendState", "05");
    	   } else if("02".equals(mailKind)) {
    		   params.put("vacaEmailSendState", "03");
    	   }
    	   
    	   vacationMapper.updateEmail(params);
        }
	}
	
	/**
	 * 신청시 승인자에게 sms 보내기
	 * @param param
	 * @param req
	 */
	public void sendSms(Map<String, Object> params)  {
		log.info("dao smssend param>>>"+params);
		
		Map<String, Object> smsData = new HashMap<String, Object>();
		
		// vacaSeq를 통해 해당 휴가정보를 가져옴
        List<Map<String, Object>> vacaList = vacationMapper.getRequestVacationList(params);
		
		String member_nm = vacaList.get(0).get("MEMBER_NM") + "";
		String vaca_cre_date = vacaList.get(0).get("VACA_CRE_DATE") + "";
		SimpleDateFormat sdf1,sdfm;

		String vaca_seq = params.get("vacaSeq") + "";
		
		sdfm= new SimpleDateFormat("MM.dd");
		sdf1= new SimpleDateFormat("yyyyMMddHHmmss");
		
		String msg;
		
		try {
			msg="["+sdfm.format(sdf1.parse(vaca_cre_date+"000000"))+"]" + member_nm + "\n"
					+"http://222.112.9.173:3000?s="+vaca_seq+"&m="+params.get("approverId");
		} catch (ParseException e) {
			e.printStackTrace();
			msg="";
		}
		
		smsData.put("rdate", "00000000");
		smsData.put("rtime", "000000");
		smsData.put("sphone1", "02");
		smsData.put("sphone2", "2102");
		smsData.put("sphone3", "7119");
		smsData.put("send_member", "vaca");
		
		String vaca_apprv_req_id_ph = vacationMapper.getApproverTelNo(params);
		
		vaca_apprv_req_id_ph=vaca_apprv_req_id_ph.replace("-", "");
   		smsData.put("msg", msg);
   		smsData.put("rphone1", vaca_apprv_req_id_ph.substring(0, 3));
   		smsData.put("rphone2", vaca_apprv_req_id_ph.substring(3, 7));
   		smsData.put("rphone3", vaca_apprv_req_id_ph.substring(7,11));
   		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
   		log.info("["+smsData+"]");
   		log.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
   		vacationMapper.smsInsert(smsData);
	}
	
	/**
	 * 결재자가 자신인 휴가 리스트 가져옴 (부하직원이 신청한 휴가 목록 승인/반려를 위해 가져옴) 
	 * @param params
	 * @return
	 */
	public List<Map<String, Object>> getApproveCheckList(Map<String, Object> params) {
		// 서버 현재 연도를 넣어줌
		params.put("year", Integer.toString(LocalDate.now().getYear()));
		return vacationMapper.getApproveCheckList(params);
	}
	
	/**
	 * C06 ~ C07권한자가 전체 휴가자 파악을 위해 가져오는 전체 휴가자 리스트 
	 * @param params
	 * @return
	 */
	public List<Map<String, Object>> getVacationManageList(Map<String, Object> params) {
		return vacationMapper.getVacationManageList(params);
	}
	
	/**
	 * C06 ~ C07권한자가 전체 휴가자 파악을 위해 가져오는 전체 휴가자 리스트 중 항목 선택시 보여주는 디테일 항목
	 * @param params
	 * @return
	 */
	public List<Map<String, Object>> getVacationManageDetail(Map<String, Object> params) {
		return vacationMapper.getVacationManageDetail(params);
	}
}
