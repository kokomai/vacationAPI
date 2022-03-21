package com.foresys.vacationAPI.security.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class UserInfoDetails implements UserDetails {

	private static final long serialVersionUID = 1L;
		

	 private String accesToken;
	 private String represhToken;
	 private String USERNAME;
	 private String PASSWORD;
	 private String AUTHORITY;
	 private boolean ENABLED;
	
	 @Override
	 public Collection<? extends GrantedAuthority> getAuthorities() {
		 List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		 authorities.add(new SimpleGrantedAuthority(AUTHORITY));
		 
		 return authorities;
	 }
	 
	@Override
	public String getPassword() {
		return USERNAME;
	}

	@Override
	public String getUsername() {
		return PASSWORD;
	}

	@Override
	public boolean isAccountNonExpired() {
		return false;
	}

	@Override
	public boolean isAccountNonLocked() {
		return false;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return this.isCredentialsNonExpired();
	}

	@Override
	public boolean isEnabled() {
		return ENABLED;
	}



}
