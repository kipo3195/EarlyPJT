package com.early.www.address.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.early.www.address.DTO.AddressSearchDTO;
import com.early.www.address.model.AddressUserMapping;
import com.early.www.address.repo.AddressMappingRepository;
import com.early.www.repository.AddressRepository;
import com.early.www.user.model.EarlyUser;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AddressServiceImpl implements AddressService {

	@Autowired
	AddressRepository addressRepositroy; 
	
	@Autowired
	AddressMappingRepository addressMappingRepository; 
	
	@Override
	public JSONObject getAddressList(String userId, String limit) {
		JSONObject jsonObj = new JSONObject();
		JSONObject type = new JSONObject();
		JSONObject data = new JSONObject();
		
		if(userId != null && !userId.isEmpty() && limit != null && !limit.isEmpty()) {
			
			// 친구 리스트
			List<EarlyUser> userList = addressRepositroy.findByMyAddressUserList(userId, Integer.parseInt(limit));
			data.put("friend_list", userList);
			
			// 전체 친구 수 
			long count = addressRepositroy.countUsersByuserId(userId);
			data.put("friend_count", count);
			
			// 내 정보
			EarlyUser myInfo = addressRepositroy.findByUserId(userId);
			data.put("my_info", myInfo);
			
			type.put("result", "success");
			
		}else {
			type.put("result", "fail");
			data.put("error_msg", "data_invalid");
		}
		
		jsonObj.put("type", type);
		jsonObj.put("data", data);
		
		return jsonObj;
	}

	@Override
	public JSONObject getSearchUser(AddressSearchDTO addressSearchDTO) {
		JSONObject jsonObj = new JSONObject();
		JSONObject type = new JSONObject();
		JSONObject data = new JSONObject();
		
		if(addressSearchDTO != null) {
			
			log.debug("/searchUser addressSearchDTO : {}", addressSearchDTO);
			
			EarlyUser friendInfo = null;
			long count = 0;
			
			String searchType = addressSearchDTO.getType();
			String inputText = addressSearchDTO.getInputText();
			String phoneNumber = addressSearchDTO.getPhoneNumber();
			String myId = addressSearchDTO.getMyId();
			
			if(searchType == null || searchType.isEmpty() || inputText == null || inputText.isEmpty() || myId == null || myId.isEmpty()) {
				type.put("result", "fail");
				data.put("error_msg", "data_invalid");
				jsonObj.put("type", type);
				jsonObj.put("data", data);
				return jsonObj;
			}else {
				if(searchType.equals("id")) {
					friendInfo = addressRepositroy.findByUserId(inputText);
					
					if(friendInfo == null) {
						data.put("friend_info", "no_user");
					}else {
						// 이미 친구인지 조회
						count = addressRepositroy.countByUserId(friendInfo.getUsername(), myId);
						if(count > 0) {
							data.put("my_friend", "true");
						}
						data.put("friend_info", friendInfo);
					}
				}else {
					if(phoneNumber == null){
						type.put("result", "fail");
						data.put("error_msg", "data_invalid");
						jsonObj.put("type", type);
						jsonObj.put("data", data);
					}else {
						friendInfo = addressRepositroy.findByNameAndPhoneNumber(inputText, phoneNumber);
						
						if(friendInfo == null) {
							data.put("friend_info", "no_user");
						}else {
							// 이미 친구인지 조회
							count = addressRepositroy.countByUserId(friendInfo.getUsername(), myId);
							if(count > 0) {
								data.put("my_friend", "true");
							}
							data.put("friend_info", friendInfo);
						}
					}
				}
			}
			type.put("result", "success");
		}else {
			type.put("result", "fail");
			data.put("error_msg", "body_data_invalid");
		}
		
		jsonObj.put("type", type);
		jsonObj.put("data", data);
		
		return jsonObj;
	}

	@Override
	public JSONObject putUser(AddressUserMapping dto) {

		JSONObject jsonObj = new JSONObject();
		JSONObject type = new JSONObject();
		JSONObject data = new JSONObject();
		
		if(dto != null) {
			if(dto.getFriendId() != null && !dto.getFriendId().isEmpty() && dto.getMyId() != null && !dto.getMyId().isEmpty()) {
				
				// 중복 체크
				AddressUserMapping result = addressMappingRepository.findByMyIdAndFriendId(dto.getMyId(), dto.getFriendId());
				
				// 신규 추가 가능
				if(result == null || result.getFlag().toLowerCase().equals("y")) {
					dto.setFlag("N");
					result = addressMappingRepository.save(dto);
					if(result != null) {
						type.put("result", "success");
						data.put("friend_info", result);
					}
				}else {
					type.put("result", "fail");
					data.put("error_msg", "already_registered");
				}
			}else {
				type.put("result", "fail");
				data.put("error_msg", "data_invalid");
			}
		}else {
			type.put("result", "fail");
			data.put("error_msg", "data_invalid");
		}
		
		jsonObj.put("type", type);
		jsonObj.put("data", data);
		
		return jsonObj;
	}

}
