package com.early.www.chat.service;

import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.early.www.chat.dto.ChatLineDTO;
import com.early.www.chat.dto.ChatLineEventDTO;
import com.early.www.chat.model.ChatMain;
import com.early.www.chat.model.ChatRoom;
import com.early.www.user.model.EarlyUser;

public interface ChatService {

	public String getLineKey();
	
	public List<ChatRoom> getMyChatList(String username);

	public void putChatMain(ChatMain main);
	
	public void putChatRoom(String roomKey, String lineKey);

	public List<ChatMain> getChatRoomLine(String chatRoomKey, String readLineKey);
	
	public List<ChatMain> getChatRoomLineAppend(String chatRoomKey, String lineKey);
	
	public Map<String, JSONObject> getUnreadChatCount(String roomKey, String receiver, String sender, String lineKey);
	
	public Map<String, String> getAllUnreadCnt(String username);

	public String getUnreadLineCount(String roomKey, String lineKey, String receiver, String sender);
	
	//public Map<String, String> putChatRoomUnread(String roomKey, String username, String startLineKey);
	
	public Map<String, Object> putChatUnreadLines(String roomKey, String username, SimpMessagingTemplate simpMessagingTemplate);

	public JSONObject putLikeEvent(String username, ChatLineEventDTO chatLineEventVO);
	
	public JSONObject getChatLineEventUser(String roomKey, String lineKey);
	
	public List<EarlyUser> getChatRoomUsers(String roomKey, int limitCnt);

	public List<EarlyUser> getChatRoomUserList(String sender);

	public String putChatRoom(ChatRoom chatroom);

	public JSONObject getAddrChatLine(ChatRoom chatRoom, String username, SimpMessagingTemplate simpMessagingTemplate);
	
	public JSONObject getChatLines(ChatLineDTO chatLineDTO, SimpMessagingTemplate simpMessagingTemplate);
	
	
	
	
}
