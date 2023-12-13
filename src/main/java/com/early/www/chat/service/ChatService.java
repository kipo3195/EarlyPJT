package com.early.www.chat.service;

import java.util.List;

import com.early.www.chat.model.ChatRoom;

public interface ChatService {

	public List<ChatRoom> getMyChatList(String username);
	
	
}
