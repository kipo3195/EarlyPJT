package com.early.www.common.jwt;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.early.www.user.model.EarlyUser;

import lombok.Data;

@Data	
public class PrincipalDetails implements UserDetails{

	private static final long serialVersionUID = -9148476874812730459L;
	
	private EarlyUser earlyUser;
	
	public PrincipalDetails (EarlyUser earlyUser){
		this.earlyUser = earlyUser;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> authorities = new ArrayList<>();	
		System.out.println("role List : "+earlyUser.getRoleList());
		earlyUser.getRoleList().forEach(r->{
			authorities.add(()->r);
		});
		
		return authorities;
	}

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
	
}
