package com.early.www.address.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.early.www.address.DTO.AddressSearchDTO;
import com.early.www.repository.AddressRepository;
import com.early.www.user.model.EarlyUser;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AddressServiceImpl implements AddressService {

	@Autowired
	AddressRepository addressRepositroy; 
	
	@Override
	public Map<String, Object> getAddressList(String userId, String limit) {
		Map<String, Object> map = new HashMap<>();
		
		
		
		if(userId != null && !userId.isEmpty() && limit != null && !limit.isEmpty()) {
			
			log.debug("/address/list userId : {}, limit : {}", userId, limit);
			
			// 친구 리스트
			List<EarlyUser> userList = addressRepositroy.findByMyAddressUserList(userId, Integer.parseInt(limit));
			map.put("friend_list", userList);
			
			// 전체 친구 수 
			long count = addressRepositroy.countUsersByuserId(userId);
			map.put("friend_count", count);
			
			// 내 정보
			EarlyUser myInfo = addressRepositroy.findByUserId(userId);
			map.put("my_info", myInfo);
		}else {
			map.put("error", "data_invalid");
		}
		return map;
	}

	@Override
	public Map<String, Object> getSearchUser(AddressSearchDTO addressSearchDTO) {
		Map<String, Object> map = new HashMap<>();
		
		if(addressSearchDTO != null) {
			
			log.debug("/searchUser addressSearchDTO : {}", addressSearchDTO);
			
			EarlyUser friendInfo = null;
			long count = 0;
			
			String type = addressSearchDTO.getType();
			String inputText = addressSearchDTO.getInputText();
			String phoneNumber = addressSearchDTO.getPhoneNumber();
			String myId = addressSearchDTO.getMyId();
			
			if(type == null || type.isEmpty() || inputText == null || inputText.isEmpty() || myId == null || myId.isEmpty()) {
				map.put("error", "data_invalid");
			}else {
				if(type.equals("id")) {
					friendInfo = addressRepositroy.findByUserId(inputText);
					
					if(friendInfo == null) {
						map.put("friend_info", "no_user");
					}else {
						count = addressRepositroy.countByUserId(friendInfo.getUsername(), myId);
						if(count > 0) {
							map.put("my_friend", "true");
						}
						map.put("friend_info", friendInfo);
					}
				}else {
					if(phoneNumber == null){
						map.put("error", "phone_number_invalid");
					}else {
						friendInfo = addressRepositroy.findByNameAndPhoneNumber(inputText, phoneNumber);
						
						if(friendInfo == null) {
							map.put("friend_info", "no_user");
						}else {
							count = addressRepositroy.countByUserId(friendInfo.getUsername(), myId);
							if(count > 0) {
								map.put("my_friend", "true");
							}
							map.put("friend_info", friendInfo);
						}
					}
				}
			}
		}else {
			map.put("error", "body_data_invalid");
		}
		
		return map;
	}

}
