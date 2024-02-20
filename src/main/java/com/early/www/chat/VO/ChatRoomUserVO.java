package com.early.www.chat.VO;

import lombok.Data;


// 채팅방 참여자 조회시 사용
@Data
public class ChatRoomUserVO {

	private String chatRoomKey;
	private int limitCnt;
	
}
