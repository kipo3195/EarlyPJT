package com.early.www.chat.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
	
	// line key 생성
	public String makeLineKey() {
		long time = System.currentTimeMillis();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYYMMddHHmmssSSS");
		Date date = new Date();
		date.setTime(time);
		
		return simpleDateFormat.format(date);
	}

	
	// 채팅 리스트 조회
	@Override
	public List<ChatRoom> getMyChatList(String username) {
		
		List<ChatRoom> chatList = chatRoomRepository.findByChatListUser(username);
		
		return chatList;
	}

	
	//채팅 라인 저장
	@Override
	public void putChatMain(ChatMain main) {
		String lineKey = makeLineKey();
		main.setChatLineKey(lineKey);
		main.setChatDelFlag("N");
		main.setSendDate(lineKey);
		System.out.println("[ChatSericeImpl] main : " + main);
		chatMainRepository.save(main);
		
	}

	// 채팅방 데이터 조회 (최초)
	@Override
	public List<ChatMain> getChatRoomLine(String chatRoomKey) {
		
		List<ChatMain> chatMainList = chatMainRepository.findByChatRoomKey(chatRoomKey);
		
		return chatMainList;
	}

	// 채팅방 데이터 조회 (최초 이후)
	@Override
	public List<ChatMain> getChatRoomLineAppend(String chatRoomKey, String lineKey) {
		
		List<ChatMain> chatMainList = chatMainRepository.findByChatRoomKeyAndLineKey(chatRoomKey, lineKey);
		
		return chatMainList;
	}
	
	
	

}
