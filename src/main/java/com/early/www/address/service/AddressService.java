package com.early.www.address.service;

import java.util.Map;

import com.early.www.address.DTO.AddressSearchDTO;

public interface AddressService {

	Map<String, Object> getAddressList(String userId, String limit);
	
	Map<String, Object> getSearchUser(AddressSearchDTO addressSearchDTO);

}
