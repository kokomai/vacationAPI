package com.foresys.vacationAPI.biz.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class HolidayAPI {
	
	@SuppressWarnings("unchecked")
	@PostMapping("/holidayApi/getHoliday")
	public Map<String, Object> getHoliday(@RequestBody Map<String, Object> params) {
		Map<String, Object> result = null;
		
		log.info("params {}", params);
		try {
			// 1. 받은 parameter 각자 담기 
			String month = (String) params.get("month");
			String year = (String) params.get("year");
			// 2. URL 객체 생성
			URL url = new URL(
				"http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo?_type=json" 
				+ "&solYear=" + year
				+ "&solMonth=" + month
				+ "&ServiceKey=HiF3goFMonMOJoeb6mvSFmDA3GiAjrSpGrTn7Lv/k7yRiWKvDZ2Lp6FsyUqHTqffa68ErMxnRH/IMNDgKoleLg=="
			);
			// 3. 요청하고자 하는 URL과 통신하기 위한 Connection 객체 생성.
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        // 4. 통신을 위한 메소드 SET.
	        conn.setRequestMethod("GET");
	        // 5. 통신을 위한 Content-type SET. 
	        conn.setRequestProperty("Content-type", "application/json");
	        // 6. 통신 응답 코드 확인.
	        log.info("Response code: " + conn.getResponseCode());
	        // 7. 전달받은 데이터를 BufferedReader 객체로 저장.
	        BufferedReader rd;
	        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
	            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        } else {
	            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
	        }
	        // 8. 저장된 데이터를 라인별로 읽어 StringBuilder 객체로 저장.
	        StringBuilder sb = new StringBuilder();
	        String line;
	        while ((line = rd.readLine()) != null) {
	            sb.append(line);
	        }
	        // 9. 객체 해제.
	        rd.close();
	        conn.disconnect();
	        // 10. 전달받은 데이터 확인.
	        log.info(sb.toString());
	        
	        ObjectMapper mapper = new ObjectMapper();
	        
	        try {
	        	result = mapper.readValue(sb.toString(), Map.class);
	        } catch(Exception e) {
	        	log.info("Mapping exception", e);
	        }
	        
		} catch(Exception e) {
			log.info("GET HOLIDAY API ERROR ::: ", e);
		}
		
		return result;
	}
}
