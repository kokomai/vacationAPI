package com.foresys.vacationAPI.security.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class Token {

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static final class Response {
		private String accessToken;
		private String refreshToken;
	}
}
