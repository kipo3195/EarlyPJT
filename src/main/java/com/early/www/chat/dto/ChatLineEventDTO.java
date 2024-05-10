package com.early.www.chat.dto;

import lombok.Data;

@Data
public class ChatLineEventDTO {
	
	private String roomKey;
	private String lineKey;
	private String type;
	
}
