package com.early.www.common.jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.early.www.user.model.EarlyUser;

import lombok.Data;

@Data	
public class PrincipalDetails implements UserDetails, OAuth2User{

	private static final long serialVersionUID = -9148476874812730459L;
	
	private EarlyUser earlyUser;
	
	private Map<String, Object> attributes;
	
	public PrincipalDetails (EarlyUser earlyUser){
		this.earlyUser = earlyUser;
	}
	
	//OAuth2.0 추가 
	public PrincipalDetails(EarlyUser earlyUser, Map<String, Object> attributes) {
	    this.earlyUser = earlyUser;
	    this.attributes = attributes;
	}
 
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> authorities = new ArrayList<>();	
		earlyUser.getRoleList().forEach(r->{
			authorities.add(()->r);
		});
		
		return authorities;
	}

	//해당 user의 권한을 리턴하는 곳
//	@Override
//	public Collection<? extends GrantedAuthority> getAuthorities() {
//		Collection<GrantedAuthority> collect = new ArrayList<>();
//		collect.add(new GrantedAuthority() {
//			private static final long serialVersionUID = 1111639807443687288L;
//			@Override
//			public String getAuthority() {
//				return earlyUser.getRoles();
//			}
//		});
//		return collect;
//	}
	
	@Override
	public String getPassword() {
		return earlyUser.getPassword();
	}

	@Override
	public String getUsername() {
		return earlyUser.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	
}
