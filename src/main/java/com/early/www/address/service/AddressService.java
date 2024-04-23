package com.early.www.address.service;

import java.util.Map;

public interface AddressService {

	Map<String, Object> getAddressList(String userId, String limit);

}
