package com.early.www.util;

import lombok.Data;

@Data
public class CommonConst {
	
	// response data
	public static final String RESPONSE_TYPE_SUCCESS = "success";
	
	public static final String RESPONSE_TYPE_FAIL = "fail";
	public static final String RESPONSE_DATA_ERROR_MSG = "error_msg";
	
	public static final String INVALID_USER_ID = "invalid_user_id";
	
	public static final String INVALID_BODY_DATA = "invalid_body_data";


	
	
	
	// 채팅 라인 이벤트 
	public static String allChatEvent = "allChatEvent";
	public static String ChatLineEventCheck = "check";
	public static String ChatLineEventGood = "good";
	public static String ChatLineEventLike = "like";
	

}
