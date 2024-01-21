package com.early.www.chat.controller;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.early.www.chat.model.ChatMain;
import com.early.www.chat.model.ChatRoom;
import com.early.www.chat.service.ChatService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserChatController {

	@Autowired
	ChatService chatService;
	
	// 채팅방 리스트 조회 
	@PostMapping("/user/chatList")
	public Map<String, String> chatList(HttpServletRequest request, HttpServletResponse response){
		Map<String, String> resultMap = new HashMap<String, String>();
		
		String error = (String) response.getHeader("error_code");
		if(error != null) {
			resultMap.put("flag", "fail");
			resultMap.put("error_code", response.getHeader("error_code"));
		}else {
			
			String username = (String) request.getAttribute("username");
			
			if(username != null && !username.isEmpty()) {
				
				// 읽지않은 건수 함께 조회
				List<ChatRoom> chatList = chatService.getMyChatList(username);
				if(chatList != null && !chatList.isEmpty()) {
					
					ObjectMapper mapper = new ObjectMapper();
					
					List<String> list = new ArrayList<String>();
					
					// chatList 만들때 chatRoom 객체에 읽지않은 건수 추가 TODO
					
					// 객체를 json형태의 String으로 변환 
					for(int i = 0; i < chatList.size(); i++) {
						ChatRoom chatObj = chatList.get(i);
						
						try {
							list.add(mapper.writeValueAsString(chatObj));
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						}
					}
					
					resultMap.put("chat_list", list.toString());
				}else {
					// 채팅리스트가 없음 
					resultMap.put("chat_list", "C404");
				}
			}else {
				resultMap.put("chat_list", "C403");
			}
		}
		
		return resultMap;
	}
	

	private final SimpMessagingTemplate simpMessagingTemplate;

	// 채팅 발송 - error 체크 하지 않는 이유? 웹소켓 연결 자체가 http 프로토콜이 아니기 때문에, 그리고 로그인이 검증되야 웹소켓을 연결 할 수 있으므로
	@MessageMapping("/user/chat")
	public void handle(ChatMain main) {
		
		
		/* 데이터 검증 */
		if(main == null || StringUtils.isEmpty(main.getChatReceiver()) || StringUtils.isEmpty(main.getChatRoomKey()) || StringUtils.isEmpty(main.getChatSender())) {
			System.out.println("[UserChatController] send data check !");
			return;
		}
		
		/* 검증된 데이터 */ 
		String roomKey = main.getChatRoomKey();
		String receiver = main.getChatReceiver();
		String sender = main.getChatSender();
		String data = main.getChatContents();
		
		/* 라인키 생성 및 DB 저장 --- TODO 채팅 내용 암호화  */ 
		String lineKey = chatService.putChatMain(main);
		
		/* redis 건수 저장 및 사용자 별 전체 건수 & 해당 방의 건수 조회 */ 
		Map<String, JSONObject> unreadMap = chatService.putChatUnreadCnt(roomKey, receiver, sender, lineKey);
		
		/* redis 해당 라인의 읽지않은 건수 조회 */
		String unreadCount = chatService.getUnreadCount(roomKey, lineKey);
		
		/* 웹소켓 발송 */
		// 전달 할 채팅 데이터 json 생성
		JSONObject sendData = new JSONObject();
		sendData.put("chatRoomKey", roomKey);
		sendData.put("chatContents", data);
		sendData.put("chatSender", sender);
		sendData.put("chatUnreadCount", unreadCount);
		

		// 보낼 경로 설정
		String dest = "/topic/room/"+roomKey;
		
		// 발송 - chatData 
		simpMessagingTemplate.convertAndSend(dest, sendData.toJSONString());
		
		
		// 발송 - unreadCount 
		// 채팅방의 수신자의 채팅 미확인 전체 건수 & 해당 채팅방의 건수 알려줌 
		Iterator<String> unreadIter = unreadMap.keySet().iterator();
		while(unreadIter.hasNext()) {
			String recvUser = unreadIter.next();
			simpMessagingTemplate.convertAndSend("/topic/user/"+recvUser, unreadMap.get(recvUser).toJSONString());
		}
		
		// 20231225 이전 방식 
		// 기존 채팅데이터를 보고 receiver를 구독하는 사용자에게 주도록 처리하던 것을 room을 구독하는 사용자에게 주도록 처리함. 
		// 이유는 프론트(리액트)에서 사용자를 구독하는 시점(로그인 직후)의 callback 함수에서는 roomkey 등을 알 수 없기 때문이다. 
		// 최초에 생각했던 방식은 채팅방 입장시 룸 키를 app.js에 상단에 useState를 이용하여 상태를 가지고 있으면서 내가 접속한 방의 정보를 
		// 가지고 있도록 하고, callBack 함수(채팅 수신)에서 비교 후 데이터를 주려고 하였으나, 데이터를 주더라도 callback의 시점에서는 방의 정보를 
		// 알 수 없었음으로, 방을 입장할때 구독하는 로직으로 수정함. 
		
		//수신자 ID 파싱
		//String recvIds[] = receiver.split("[|]");
		
		
		// STOMP를 통한 데이터 전송
		//for(int i = 0; i< recvIds.length; i++) {

		
//			System.out.println(roomKey);
//			String dest = "/topic/room/"+roomKey;
//
//			simpMessagingTemplate.convertAndSend(dest, chatData.toJSONString());
//			
			// 기존에 사용자 구독 url에 주던 방식 
//			if(!sender.toLowerCase().equals(recvIds[i].toLowerCase())) {
//				//String dest = "/topic/user/"+recvIds[i];
//				simpMessagingTemplate.convertAndSend(dest, chatData.toJSONString());
//			}
		//}
			
	}

	
	// 채팅방의 라인 조회
	@PostMapping("/user/chatRoomLine")										// body 데이터
	public Map<String, String> getChatRoomLine(HttpServletRequest request, @RequestBody ChatRoom chatRoom, HttpServletResponse response) {
		Map<String, String> resultMap = new HashMap<String, String>();
		
		String error = (String) response.getHeader("error_code");
		if(error != null) {
			resultMap.put("flag", "fail");
			resultMap.put("error_code", response.getHeader("error_code"));
		}else {
			
			// 토큰에서 가져온 데이터
			String username = (String) request.getAttribute("username");
			
			if(username != null && !username.isEmpty()) {
				
				String chatRoomKey = chatRoom.getChatRoomKey();
				
				System.out.println(username + " : " + chatRoomKey);
				
				if(!StringUtils.isEmpty(chatRoomKey)) { 
					
					List<ChatMain> lineList = chatService.getChatRoomLine(chatRoomKey);
					
					// chatList 만들때 ChatMain 객체에 읽지않은 건수 추가 TODO
					
					// System.out.println(lineList);
					if(lineList != null && !lineList.isEmpty()) {
						ObjectMapper mapper = new ObjectMapper();
						
						List<String> list = new ArrayList<String>();
						
						// 객체를 json형태의 String으로 변환 
						for(int i = 0; i < lineList.size(); i++) {
							ChatMain chatMain = lineList.get(i);
							
							try {
								list.add(mapper.writeValueAsString(chatMain));
							} catch (JsonProcessingException e) {
								e.printStackTrace();
							}
						}
						
						resultMap.put("chatRoomLine", list.toString());
						// 다음 요청의 기준이되는 라인키 생성
						if(list.size() > 0) {
							resultMap.put("nextLine", lineList.get(0).getChatLineKey());
						}else {
							resultMap.put("nextLine", "0");
						}
					}
				}
			}else {
				// 토큰은 검증했지만 username이 없는경우?
				
			}
			
		}
		
		
		return resultMap;
	}
	
	
		// 채팅방의 라인 추가조회
		@PostMapping("/user/chatRoomLineAppend")										// body 데이터
		public Map<String, String> getChatRoomLineAppend(HttpServletRequest request, @RequestBody ChatRoom chatRoom, HttpServletResponse response) {
			Map<String, String> resultMap = new HashMap<String, String>();
			
			String error = (String) response.getHeader("error_code");
			if(error != null) {
				resultMap.put("flag", "fail");
				resultMap.put("error_code", response.getHeader("error_code"));
			}else {
				// 토큰에서 가져온 데이터
				String username = (String) request.getAttribute("username");
				
				if(username != null && !username.isEmpty()) {
					
					String chatRoomKey = chatRoom.getChatRoomKey();
					String nextLine = chatRoom.getLastLineKey();
					
					System.out.println(username + " : " + chatRoomKey + " : " + nextLine);
					
					if(!StringUtils.isEmpty(chatRoomKey)) { 
						
						List<ChatMain> lineList = chatService.getChatRoomLineAppend(chatRoomKey, nextLine);
						
						if(lineList != null && !lineList.isEmpty()) {
							ObjectMapper mapper = new ObjectMapper();
							
							List<String> list = new ArrayList<String>();
							
							// chatList 만들때 ChatMain 객체에 읽지않은 건수 추가 TODO
							
							// 객체를 json형태의 String으로 변환 
							for(int i = 0; i < lineList.size(); i++) {
								ChatMain chatMain = lineList.get(i);
								
								try {
									list.add(mapper.writeValueAsString(chatMain));
								} catch (JsonProcessingException e) {
									e.printStackTrace();
								}
							}
							
							resultMap.put("chatRoomLine", list.toString());
							
							// 다음 요청의 기준이되는 라인키 생성
							if(list.size() > 0) {
								resultMap.put("nextLine", lineList.get(0).getChatLineKey());
							}else {
								resultMap.put("nextLine", "0");
							}
							
						}
						
					}
				}else {
					
				}
			}
			
			
			return resultMap;
		}
	
	
}
