package com.early.www.chat.service;

import java.util.ArrayList;
import java.util.List;

import com.early.www.chat.model.ChatMain;
import com.early.www.chat.model.ChatRoom;

public interface ChatService {

	public List<ChatRoom> getMyChatList(String username);

	public void putChatMain(ChatMain main);

	public List<ChatMain> getChatRoomLine(String chatRoomKey);
	
	public List<ChatMain> getChatRoomLineAppend(String chatRoomKey, String lineKey);
	
	
}
