package com.early.www.chat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import com.early.www.chat.model.ChatMain;
import com.early.www.chat.model.ChatRoom;

public interface ChatService {

	public List<ChatRoom> getMyChatList(String username);

	public String putChatMain(ChatMain main);

	public List<ChatMain> getChatRoomLine(String chatRoomKey);
	
	public List<ChatMain> getChatRoomLineAppend(String chatRoomKey, String lineKey);
	
	public Map<String, JSONObject> putChatUnreadCnt(String roomKey, String receiver, String sender, String lineKey);
	
	public Map<String, String> getAllUnreadCnt(String username);
}
