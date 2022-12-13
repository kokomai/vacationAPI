package com.foresys.vacationAPI.biz.vacation.service;


import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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
			
			// 이 휴가승인이 최종 승인인 경우 휴가 갯수를 깎음
			if("03".equals(state)) {
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
			}
			
			vacationMapper.approveVacation(params);
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
		   return new PasswordAuthentication("foresys00", "foresys1"); // Google id, pwd, 주의) @gmail.com 은 제외하세요
		  }
	 }
	
//	/**
//	 * 신청, 최종승인, 취소시 메일 보내기
//	 * @param param
//	 */
//	public void sendMail(HashMap<String, Object> params)  { 
//		//("foresys00", "foresys1");
//		String host = "smtp.gmail.com";  
//		String subj="";
//		String applysubj, completesubj;
//		String content="";
//		String applycontent, completecontent;
//		String vaca_date;
//		String vaca_half;
//		String CONFIRM_CHK_EMAIL;
//		String to_email = "";
//    	Properties p = new Properties();
//    	  p.put("mail.smtp.user", "foresys00@gmail.com");
//    	  p.put("mail.smtp.host", "smtp.gmail.com");
//    	  p.put("mail.smtp.port", "465");
//    	  p.put("mail.smtp.starttls.enable","true");
//    	  p.put( "mail.smtp.auth", "true");
//
//    	  p.put("mail.smtp.debug", "true");
//    	  p.put("mail.smtp.socketFactory.port", "465"); 
//    	  p.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); 
//    	  p.put("mail.smtp.socketFactory.fallback", "false");
//        
//        String member_no = params.get("MEMBER_NO") + "";
//        String member_nm = params.get("MEMBER_NM") + "";
//        String member_class_cd = params.get("MEMBER_CLASS_CD") + "";
//        String[] vaca_d = (params.get("VACA_DATE") == null) ? null : (String[]) params.get("VACA_DATE");
//    	vaca_date = Arrays.toString(vaca_d);
//        	
//        String[] vaca_h = (params.get("VACA_HALF") == null) ? null :(String[]) params.get("VACA_HALF");
//    	vaca_half = Arrays.toString(vaca_h);
//    	
//        String vaca_num = params.get("VACA_NUM") + "";
//        String vaca_extra = params.get("VACA_EXTRA_CAL") + "";
//        String vaca_reason = params.get("VACA_REASON") + "";
//        String vaca_credate = params.get("VACA_CREDATE") + "";
//        String confirm_chk_member = params.get("CONFIRM_CHK") + "";
//        double vaca_used = Double.parseDouble(params.get("VACA_USED") + "");
//		double vaca_date_num = (vaca_d == null)? 0 : vaca_d.length;
//		double vaca_half_num = (vaca_h == null)? 0 : 0.5 * vaca_h.length;
//		double newvaca_used = vaca_used + vaca_date_num + vaca_half_num;
//		double c = vaca_date_num + vaca_half_num;
//		
//		String d = "";
//		int cot = 0;
//		
//		if(vaca_date_num > 0) {
//			for(int i = 0; i < vaca_d.length; i++) {
//				String a = vaca_d[i].trim();
//				
//				if(cot != 0) {
//					d += ",";
//				}
//				
//				d += a.substring(0, 4) + ".";
//				d += a.substring(4, 6) + ".";
//				d += a.substring(6) + " ";
//				cot++;
//			}
//		}
//		
//		if(vaca_half_num > 0) {
//			for(int i = 0; i < vaca_h.length; i++) {
//				String b = vaca_h[i].trim();
//				
//				if(cot != 0) {
//					d += ",";
//				}
//				
//				d += b.substring(0, 4) + ".";
//				d += b.substring(4, 6) + ".";
//				d += b.substring(6) + " ";
//				cot++;
//			}
//		}
//		
//        if(vaca_d == null) {
//        	vaca_date = "";
//        }else {
//        	vaca_date = vaca_date.replace("[","").replace("]", "");
//        }
//        
//        if(vaca_h == null) {
//        	vaca_half = "";
//        }else {
//        	vaca_half = vaca_half.replace("[","").replace("]", "");
//        }
//        
//        completesubj = "-TEST-[휴가 공지]"
//    				+ member_nm + ""
//					+ d 
//					+ "(" + c + "/" + vaca_extra + ")";
//        
//        applysubj = "-TEST-[휴가 신청]"
//        		+ member_nm + ""
//            	+ d 
//            	+ "(" + c + "/" + vaca_extra + ")";
//        
//        completecontent = "안녕하세요 <br/><br/>"
//        		+ "포이시스 " + member_nm + member_class_cd + "입니다 <br/>"
//    		    +vaca_reason+"로 인한 휴가 사용을 알려드립니다 <br/>"
//    		    +"감사합니다.";
//       
//       applycontent = "안녕하세요 <br/><br/>"
//    		   	+ "포이시스 " + member_nm+member_class_cd + "입니다 <br/>"
//   		    	+ vaca_reason + "로 인한 휴가 신청합니다 <br/>"
//   		    	+ "감사합니다."
//   		    	+ "<a href=\"support.foresys.co.kr/vaca_test.do?MEMBER_NO=" + member_no 
//   		    	+ "&VACA_CREDATE=" + vaca_credate 
//   		    	+ "&confirm_chk_member=" + confirm_chk_member 
//   		    	+ "\">보기</a><br/>";
//   		 //+"<a href=\"test.co.kr/vaca_test.do?MEMBER_NO="+member_no+"&VACA_CREDATE="+vaca_credate+"&confirm_chk_member="+confirm_chk_member+"\">보기</a><br/>";
//       String mail_chk = params.get("mail_chk").toString();
//       if(mail_chk == "complete") {
//    	   subj = completesubj;
//    	   content = completecontent;
//    	   //to_email="solution_all@foresys.co.kr";
//    	   to_email="ajakorea@foresys.co.kr";
//       }else if(mail_chk == "apply") {
//    	   subj = applysubj;
//    	   content = applycontent;
//    	   try {
//    		   CONFIRM_CHK_EMAIL = vacationMapper.getApproverEmail(params);
//    		   to_email = CONFIRM_CHK_EMAIL;
//    	   } catch (Exception e) {
//    		   log.info("이메일주소찾기err "+e);
//    		   params.put("CONFIRM_CHK", member_no);
//    		   CONFIRM_CHK_EMAIL = vacationMapper.getApproverEmail(params);
//    		   to_email = CONFIRM_CHK_EMAIL;
//    		   subj = "[휴가공지]발송실패";
//    		   content = "관리자에게 문의하세요.";
//    	   }
//
//       }
//       
//       log.info("작성되고 날아갈 제목-------------"+subj+"내용 들어갈자리------------"+content);
//        
//        
//        
//       try {
//       Authenticator auth = new SMTPAuthenticator();
//        Session mailSession = Session.getInstance(p,auth);
//        MimeMessage message= new MimeMessage(mailSession);
//
//        mailSession.setDebug(true);
//       
//        
//        Address fromAddr;
//		try {
//			fromAddr = new InternetAddress("foresys00@gmail.com", "포이시스 "+member_nm, "euc-kr");
//			message.setFrom(fromAddr);
//		} catch (UnsupportedEncodingException e) {
//			log.info("---"+e);
//		}
//        Address[] toAddrs = {new InternetAddress(to_email)
//      		  				//,new InternetAddress("solution_all@foresys.co.kr"), 
//      		  				//,new InternetAddress("porollo@foresys.co.kr")
//        		};
//        
//        
//        	message.addRecipients(Message.RecipientType.TO, toAddrs);
//        	message.setSubject(subj,"euc-kr");//제목
//        	//message.setText(content,"euc-kr");//내용
//        	message.setContent(content,"text/html;charset=euc-kr"); 	
//        	Transport.send(message);
//        	log.info("메일전송");
//        	param.put("VACA_EMAILCHK", "02");
//        	getSqlMapClientTemplate().update("vaca.updateEmail",param);
//        }catch (AddressException e) {
//        	log.info("addr excep"+e);
//        	param.put("VACA_EMAILCHK", "03");
//        	getSqlMapClientTemplate().update("vaca.updateEmail",param);
//        }catch(MessagingException me) {
//        	log.info("messagee excep"+me);
//        	param.put("VACA_EMAILCHK", "03");
//        	getSqlMapClientTemplate().update("vaca.updateEmail",param);
//        }
//	}
	
}
