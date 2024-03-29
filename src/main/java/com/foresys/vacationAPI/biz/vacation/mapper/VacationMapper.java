package com.foresys.vacationAPI.biz.vacation.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VacationMapper {
	
	// 유저가 휴가 신청한 갯수를 구해옴(남은 휴가일 수 계산 때 사용) 
	public double getRequestCount(Map<String, Object> params);
	// 남은 휴가일 수 가져옴
	public Map<String, Object> getVacationRemains(Map<String, Object> params);
	// 휴가 승인자(C05권한 이상) 목록을 가져옴
	public List<Map<String, Object>> getApprovers(Map<String, Object> params);
	// 휴일 목록을 가져옴
	public List<Map<String, Object>> getHolidays(Map<String, Object> params);
	// 유저의 휴가 목록을 가져옴
	public List<Map<String, Object>> getVacationList(Map<String, Object> params);
	// 휴가 승인자(C05권한 이상)에게 신청된 휴가 목록을 가져옴
	public List<Map<String, Object>> getApproveList(Map<String, Object> params);
	// 해당 휴가의 자세한 정보를 가져옴
	public Map<String, Object> getVacationInfo(Map<String, Object> params);
	// TB_FS_VACA_LIST에 INSERT시 시퀀스 입력을 위한 ROW의 MAX 갯수를 가져옴
	public String getVacaSeq();
	// TB_FS_VACA_LIST에 정보 INSERT (휴가 정보) (1순위 삽입 요망 PK - FK)
	public int insertVacationList(Map<String, Object> params);
	// TB_FS_VACA_DATE_LIST에 정보 INSERT (휴가 일자) (2순위 삽입 요망 PK - FK)
	public int insertVacationDateList(Map<String, Object> params);
	// TB_FS_VACA_INSU_LIST에 정보 INSERT (인수 인계) (2순위 삽입 요망 PK - FK)
	public int insertVacationInsuList(Map<String, Object> params);
	// TB_FS_VACA_LIST에 정보 DELETE (휴가 정보) (2순위 삭제 요망 PK - FK)
	public int deleteVacationList(Map<String, Object> params);
	// TB_FS_VACA_DATE_LIST에 정보 DELETE (휴가 일자) (1순위 삭제 요망 PK - FK)
	public int deleteVacationDateList(Map<String, Object> params);
	// TB_FS_VACA_INSU_LIST에 정보 DELETE (인수 인계) (1순위 삭제 요망 PK - FK)
	public int deleteVacationInsuList(Map<String, Object> params);
	// 휴가 사용 갯수를 갱신 (휴가 최종 승인, 승인된 휴가 취소시 변경됨)
	public int updateVacaUsedCount(Map<String, Object> params);
	// 신청된 휴가의 상태를 "반려"로 갱신
	public int rejectVacation(Map<String, Object> params);
	// 승인자의 해당 휴가에대한 권한을 조회하여 승인 코드를 받아옴 (1차승인 : 53 / 최종승인(2차승인) : 03)
	public String getApprovalState(Map<String, Object> params);
	// 해당 휴가신청시 사용한 연차 갯수를 가져옴
	public double getUsedVacationCount(Map<String, Object> params);
	// 신청된 휴가의 상태를 "승인, 1차 승인"등으로 갱신
	public int approveVacation(Map<String, Object> params);
	// 취소할 휴가의 상태(승인여부)를 가져옴
	public String getVacationState(Map<String, Object> params);
	// 승인자의 sms를 가져옴
	public String getApproverTelNo(Map<String, Object> params);
	// 승인자의 email을 가져옴
	public String getApproverEmail(Map<String, Object> params);
	// 이메일 상태 업데이트
	public int updateEmail(Map<String, Object> params);
	// 신청한 해당 휴가 정보들 가져오기
	public List<Map<String, Object>> getRequestVacationList(Map<String, Object> params);
	// 해당 휴가의 승인자 ID들을 가져옴
	public Map<String, Object> getMyApprovers(Map<String, Object> params);
	// sms 테이블에 데이터 넣기(Queue 방식)
	public int smsInsert(Map<String, Object> params);
	// 자신이 결재자인 경우 휴가 리스트 가져옴
	public List<Map<String, Object>> getApproveCheckList(Map<String, Object> params);
	// 유저 이름 가져오기(휴가 승인시 필요)
	public String getMemberNm(Map<String, Object> params);
	// 관리를 위한 전체 휴가 목록을 가져옴
	public List<Map<String, Object>> getVacationManageList(Map<String, Object> params);
	// 관리를 위한 전체 휴가 목록 중 선택시 디테일 항목을 가져옴
	public List<Map<String, Object>> getVacationManageDetail(Map<String, Object> params);
}
