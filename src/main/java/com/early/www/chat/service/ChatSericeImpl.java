package com.early.www.chat.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.early.www.chat.model.ChatRoom;
import com.early.www.repository.ChatRepository;

@Service
public class ChatSericeImpl implements ChatService {

	@Autowired
	ChatRepository chatRepository;
	
	@Override
	public List<ChatRoom> getMyChatList(String username) {
		
		List<ChatRoom> chatList = chatRepository.findByChatListUser(username);
		
		return chatList;
	}
	
	

}
