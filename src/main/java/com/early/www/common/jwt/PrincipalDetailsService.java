package com.early.www.common.jwt;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.early.www.repository.CommonRepository;
import com.early.www.user.model.EarlyUser;

import lombok.RequiredArgsConstructor;


// http://localhost:8080/login
// 
@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService{
	
	private final CommonRepository commonRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	
		System.out.println("PrincipalDetailsService의 loadUserByUsername");
		
		
		EarlyUser userEntity = commonRepository.findByusername(username);

		System.out.println("userEntity : "+userEntity);

		if(userEntity == null) {
			return null;
		}
		
		return new PrincipalDetails(userEntity);
	}

	
	
	
	
	
}
