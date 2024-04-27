package com.early.www.address.service;

import org.json.simple.JSONObject;

import com.early.www.address.DTO.AddressSearchDTO;
import com.early.www.address.model.AddressUserMapping;

public interface AddressService {

	JSONObject getAddressList(String userId, String limit);
	
	JSONObject getSearchUser(AddressSearchDTO addressSearchDTO);

	JSONObject putUser(AddressUserMapping earlyUser);

}
