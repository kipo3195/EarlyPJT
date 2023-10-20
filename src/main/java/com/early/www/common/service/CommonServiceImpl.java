package com.early.www.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.early.www.repository.UserRepository;
import com.early.www.user.model.EarlyUser;


@Service
public class CommonServiceImpl implements CommonService {

	@Autowired
	UserRepository userRepository;
	
	@Override
	public void userJoin(EarlyUser user) {
		
		userRepository.save(user);
	
	}

}
