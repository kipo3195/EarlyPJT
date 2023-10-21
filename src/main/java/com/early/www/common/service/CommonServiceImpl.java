package com.early.www.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.early.www.repository.UserRepository;
import com.early.www.user.model.EarlyUser;


@Service
public class CommonServiceImpl implements CommonService {

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	BCryptPasswordEncoder passwordEncoder;
	
	@Override
	public void userJoin(EarlyUser user) {
		String encodedPw = passwordEncoder.encode(user.getPassword());
		System.out.println("encodedPw : " + encodedPw);
		user.setPassword(encodedPw);
		userRepository.save(user);
	
	}

	@Override
	public boolean existsUsername(String username) {
		
		EarlyUser earlyUser = userRepository.findByusername(username);
		System.out.println("EalryUser : " + earlyUser);
		
		if(earlyUser != null) {
			return true;
		}else {
			return false;
		}
	}


}
