package com.early.www.address.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.early.www.repository.AddressRepository;
import com.early.www.user.model.EarlyUser;

@Service
public class AddressServiceImpl implements AddressService {

	@Autowired
	AddressRepository addressRepositroy; 
	
	@Override
	public Map<String, Object> getAddressList(String userId, String limit) {
		Map<String, Object> map = new HashMap<>();
		
		// 친구 리스트
		List<EarlyUser> userList = addressRepositroy.findByMyAddressUserList(userId, Integer.parseInt(limit));
		map.put("friend_list", userList);
		
		// 내 정보
		EarlyUser myInfo = addressRepositroy.findByUserId(userId);
		map.put("my_info", myInfo);
		
		return map;
	}

}
