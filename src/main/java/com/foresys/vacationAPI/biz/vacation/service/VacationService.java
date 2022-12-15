package com.foresys.vacationAPI.biz.vacation.service;


import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

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
				
				
				if(params.get("auth1Id") != null && !"".equals(params.get("auth1Id"))) {
					// 1차 승인자 있을 시, 1차 승인자에게 메일 발송
					params.put("approverId", params.get("auth1Id"));
				} else {
					// 1차 승인자 없을 시, 2차 승인자에게 메일 발송
					params.put("approverId", params.get("auth2Id"));
				}
				
				// mailKind = [02 : 신청], [03 : 휴가 승인되어 완료], [06 : 승인 후 휴가신청 취소] 휴가 상태 코드(C02)를 따름
				params.put("mailKind", "02");
				sendMail(params);
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
			String state = vacationMapper.getVacationState(params);
			
			// 이미 승인이 된 휴가일시, 취소하면 해당 신청 휴가일수를 돌려받아야함.
			if("03".equals(state)) {
				// 년도 정보를 가져오기 위해..
				params.put("year", ((String) params.get("vacaSeq")).substring(0, 4));
				// 요청자 아이디가 곧 변경 아이디..
				params.put("chngId", params.get("id"));
				params.put("requesterId", params.get("id"));
				
				// 취소할 휴가의 휴가 사용 갯수
				double requestCount = vacationMapper.getUsedVacationCount(params);
				Map<String, Object> remainsMap = vacationMapper.getVacationRemains(params);
				// 요청자의 현재 사용한 휴가 갯수
				double nowUsedCount = ((BigDecimal) remainsMap.get("VACA_USED_CNT")).doubleValue();
				// 현재 사용횟수에서 취소할 휴가갯수를 뺌
				double toBeUsedCount = nowUsedCount - requestCount;
				
				// 새로운 사용횟수로 업데이트
				params.put("usedCount", toBeUsedCount);
				
				log.info("usedCount PARAMS :::: {}", params);
				vacationMapper.updateVacaUsedCount(params);
				
				// TODO : 취소 전체 메일 공지
				
			}
			
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
			
			if("03".equals(state)) {
				// 이 휴가승인이 최종 승인인 경우 휴가 갯수를 깎고 공지 메일을 보냄
				
				// 년도 정보를 가져오기 위해..
				params.put("year", ((String) params.get("vacaSeq")).substring(0, 4));
				
				// 해당 휴가의 승인자(요청자가 아닌, 로그인해서 승인하는 사람)의 정보를 chngId에 넣어줌
				params.put("chngId", params.get("id"));
				
				// getVacationRemains는 휴가 대상자의 아이디를 "id"로 받으므로, 요청자의 아이디로 덮어씌워줌
				params.put("id", params.get("requesterId"));
				
				log.info("year PARAMS :::: {}", params);
				Map<String, Object> remainsMap = vacationMapper.getVacationRemains(params);
				// 요청자의 현재 사용한 휴가 갯수
				double nowUsedCount = ((BigDecimal) remainsMap.get("VACA_USED_CNT")).doubleValue();
				// 요청자가 신청한 휴가 갯수
				double requestCount = vacationMapper.getUsedVacationCount(params);
				// 휴가 승인 후 사용한 휴가 갯수
				double usedCount = nowUsedCount + requestCount;
				
				params.put("usedCount", usedCount);
				
				log.info("usedCount PARAMS :::: {}", params);
				vacationMapper.updateVacaUsedCount(params);
				
				// TODO : 전체 메일 공지
				// mailKind = [02 : 신청], [03 : 휴가 승인되어 완료], [06 : 승인 후 휴가신청 취소] 휴가 상태 코드(C02)를 따름
				params.put("mailKind", "03");
			} else {
				// 이 휴가승인이 중간 승인인 경우 다음 신청 메일을 보냄
				
				// TODO : 다음 2차 승인자에게 메일 공지
				// mailKind = [02 : 신청], [03 : 휴가 승인되어 완료], [06 : 승인 후 휴가신청 취소] 휴가 상태 코드(C02)를 따름
				params.put("mailKind", "02");
				params.put("approverId", params.get("auth1Id"));
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
	 * 메일 인증
	 * @author USER
	 *
	 */
	private static class SMTPAuthenticator extends javax.mail.Authenticator {
		  public PasswordAuthentication getPasswordAuthentication() {
		   return new PasswordAuthentication("foresys@foresys.co.kr", "foresys#@1"); 
		  }
	 }
	
	/**
	 * 신청, 최종승인, 취소시 메일 보내기
	 * @param param
	 */
	public void sendMail(Map<String, Object> params)  { 
    	Properties p = new Properties();
    	p.put("mail.smtp.user", "foresys00@gmail.com");
		p.put("mail.smtp.host", "smtp.gmail.com");
		p.put("mail.smtp.port", "465");
		p.put("mail.smtp.starttls.enable","true");
		p.put( "mail.smtp.auth", "true");
		
		p.put("mail.smtp.debug", "true");
		p.put("mail.smtp.socketFactory.port", "465"); 
		p.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); 
		p.put("mail.smtp.socketFactory.fallback", "false");
        
        String subj = "";
        String content = "";
        String targetEmail = "";
        
        String mailKind = params.get("mailKind").toString();
        
        // vacaSeq를 통해 해당 휴가정보를 가져옴
        List<Map<String, Object>> vacaList = vacationMapper.getRequestVacationList(params);
        
        String memberId = vacaList.get(0).get("MEMBER_NO") + "";
        String memberNm = vacaList.get(0).get("MEMBER_NM") + "";
        
        String dateString = vacaList.get(0).get("VACA_DATE") + "";
        
        if(vacaList.size() > 1) {
        	dateString += " 외 " + (vacaList.size() -1) + "일";
        } else {
        	if("02".equals(vacaList.get(0).get("VACA_DIV"))) {
            	// 하루인데 오전 반차만 있을 경우..
            	dateString += " 오전";
            } else if("03".equals(vacaList.get(0).get("VACA_DIV"))) {
            	// 하루인데 오후 반차만 있을 경우..
            	dateString += " 오후";
            }
        }
        
        String memberClass = vacaList.get(0).get("MEMBER_CLASS_CD_NM") + "";
        String reason = vacaList.get(0).get("VACA_REASON") + "";
        String createdDate = vacaList.get(0).get("VACA_CRE_DATE") + "";
        params.put("id", vacaList.get(0).get("MEMBER_NO"));
        
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
       
       // mailKind = [02 : 신청], [03 : 휴가 승인되어 완료], [06 : 승인 후 휴가신청 취소] 휴가 상태 코드(C02)를 따름 
       if("02".equals(mailKind)) {
    	   subj = "-TEST-[휴가 신청]"
           		+ memberNm + ""
               	+ dateString 
               	+ "(" + useCount + "/" + remainCount + ")";
       	   content  = "안녕하세요 <br/><br/>"
          		   		+ "포이시스 " + memberNm + memberClass + "입니다 <br/>"
         		    	+ reason + "로 인한 휴가 신청합니다 <br/>"
         		    	+ "감사합니다."
         		    	+ "<a href=\"support.foresys.co.kr/vacation_s.do?s=" + params.get("vacaSeq") + "&view=y\">휴가 신청서</a><br/>";
       	   try {
       		   targetEmail = vacationMapper.getApproverEmail(params);
//       		   targetEmail="khw200302@foresys.co.kr";
       	   } catch (Exception e) {
       		   log.info("이메일주소찾기err "+e);
       		   params.put("approverId", memberId);
       		   targetEmail =vacationMapper.getApproverEmail(params);
       		   subj = "[휴가공지]발송실패";
       		   content = "관리자에게 문의하세요.";
       	   }
       } else if("03".equals(mailKind)) {
    	   subj  = "-TEST-[휴가 공지]"
  				+ memberNm + ""
   				+ dateString 
   				+ "(" + useCount + "/" + remainCount + ")";
       	   content = "안녕하세요 <br/><br/>"
       			   + "포이시스 " + memberNm + memberClass + "입니다 <br/>"
       			   + reason + "로 인한 휴가 사용을 알려드립니다 <br/>"
       			   +"감사합니다.";
//       	   targetEmail="solution_all@foresys.co.kr";
       	   targetEmail="khw200302@foresys.co.kr";
       } else if("06".equals(mailKind)) {
    	   subj = "-TEST-[휴가 취소]"
        		+ memberNm + ""
            	+ dateString 
            	+ "(" + useCount + "/" + remainCount + ")";
    	   content =  "안녕하세요 <br/><br/>"
           		+ "포이시스 " + memberNm + memberClass + "입니다 <br/>"
           		+ dateString 
       		    + "휴가건 취소를 알려드립니다 <br/>"
       		    +"감사합니다.";
//    	   targetEmail="solution_all@foresys.co.kr";
    	   targetEmail="khw200302@foresys.co.kr";
       }
       
       try {
    	   Authenticator auth = new SMTPAuthenticator();
    	   Session mailSession = Session.getInstance(p,auth);
    	   MimeMessage message= new MimeMessage(mailSession);

    	   mailSession.setDebug(true);
    	   Address fromAddr;
    	   
    	   try {
    		   fromAddr = new InternetAddress("foresys@foresys.co.kr", "포이시스 "+ memberNm, "euc-kr");
    		   message.setFrom(fromAddr);
    	   } catch (UnsupportedEncodingException e) {
    		   log.error("---"+e);
    	   }
    	   
    	   Address[] toAddrs = {new InternetAddress(targetEmail)
//      		  				,new InternetAddress("bsha@foresys.co.kr") 
      		  				//,new InternetAddress("porollo@foresys.co.kr")
    	   };
        
        
        	message.addRecipients(Message.RecipientType.TO, toAddrs);
        	message.setSubject(subj,"euc-kr");//제목
        	message.setContent(content,"text/html;charset=euc-kr"); 	
        	Transport.send(message);
        	
        	log.info("메일전송");
        	
        	if("03".equals(mailKind)) {
        		// 승인되어 휴가 확정의 경우
        		params.put("vacaEmailSendState", "04");
        		vacationMapper.updateEmail(params);
        	}
        }catch (AddressException e) {
        	log.info("addr excep"+e);
        	if("03".equals(mailKind)) {
        		params.put("vacaEmailSendState", "05");
        	} else if("02".equals(mailKind)) {
        		params.put("vacaEmailSendState", "03");
        	}
        	vacationMapper.updateEmail(params);
        }catch(MessagingException me) {
        	if("03".equals(mailKind)) {
        		params.put("vacaEmailSendState", "05");
        	} else if("02".equals(mailKind)) {
        		params.put("vacaEmailSendState", "03");
        	}
        	vacationMapper.updateEmail(params);
        }
	}
	
}
