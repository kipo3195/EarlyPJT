package com.early.www.chat.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.early.www.chat.model.ChatMain;
import com.early.www.chat.model.ChatRoom;
import com.early.www.repository.ChatMainRepository;
import com.early.www.repository.ChatRoomRepository;

@Service
public class ChatSericeImpl implements ChatService {

	@Autowired
	ChatRoomRepository chatRoomRepository;
	
	@Autowired
	ChatMainRepository chatMainRepository;
	
	// 채팅 리스트 조회
	@Override
	public List<ChatRoom> getMyChatList(String username) {
		
		List<ChatRoom> chatList = chatRoomRepository.findByChatListUser(username);
		
		return chatList;
	}

	
	//채팅 라인 저장
	@Override
	public void putChatMain(ChatMain main) {
		
		main.setChatLineKey(makeLineKey());
		System.out.println("[ChatSericeImpl] main : " + main);
		chatMainRepository.save(main);
		
		
	}
	
	public String makeLineKey() {
		long time = System.currentTimeMillis();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYYMMddHHmmssSSS");
		Date date = new Date();
		date.setTime(time);
		
		return simpleDateFormat.format(date);
	}

}
