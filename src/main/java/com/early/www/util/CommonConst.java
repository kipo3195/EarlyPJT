package com.early.www.util;

import lombok.Data;

@Data
public class CommonConst {
	
	public static final String PROJECT_KEYWORD ="EARLY";
	public static final String UNDER_BAR ="_";
	public static final String DOT = ".";
	
	// 공통 
	public static final String RESPONSE_TYPE = "type";
	public static final String RESPONSE_TYPE_SUCCESS = "success";
	public static final String RESPONSE_TYPE_FAIL = "fail";
	
	public static final String RESPONSE_DATA = "data";
	public static final String RESPONSE_DATA_ERROR_MSG = "error_msg";
	
	public static final String INVALID_USER_ID = "invalid_user_id";
	public static final String INVALID_BODY_DATA = "invalid_body_data";
	public static final String INVALID_TOKEN_DATA = "invalid_token_data";
	
	// 로컬 path
	public static final String LOCAL_SERVER_RESOURCE_PATH = "/src/main/resources/static/";
	
	// 파일
	public static final String FILE_SAVED = "file_saved";
	public static final String FILE_NOT_SAVED = "file_not_saved";
	public static final String FILE_NOT_LOADED = "file_not_loaded";
	
	public static final String FILE_TYPE_JPG ="jpg";


	
	// 채팅 라인 이벤트 
	public static String allChatEvent = "allChatEvent";
	public static String ChatLineEventCheck = "check";
	public static String ChatLineEventGood = "good";
	public static String ChatLineEventLike = "like";
	
	// rabbitmq 
	public static String QUEUE_NAME = "queue_name";
	public static String CHAT_SENDER = "chatSender";
	public static String CHAT_LINE_KEY = "chatLineKey";
	public static String CAHT_ROOM_KEY = "chatRoomKey";
	public static String CHAT_CONTENTS = "chatContents";
	public static String CHAT_RECEIVER = "chatReceiver";
	public static String TYPE = "type";
	
	

}
