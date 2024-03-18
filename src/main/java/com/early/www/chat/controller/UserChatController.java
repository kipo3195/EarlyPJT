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
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.early.www.chat.VO.ChatLineEventVO;
import com.early.www.chat.VO.ChatReadVO;
import com.early.www.chat.VO.ChatRoomUserVO;
import com.early.www.chat.model.ChatMain;
import com.early.www.chat.model.ChatRoom;
import com.early.www.chat.service.ChatService;
import com.early.www.user.model.EarlyUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserChatController {

	@Autowired
	ChatService chatService;
	
	// 라인키 발급 로직 
	@GetMapping("/user/getLineKey")
	public Map<String, String> getLineKey(HttpServletRequest request, HttpServletResponse response){
		Map<String, String> resultMap = new HashMap<String, String>();
		
		String error = (String) response.getHeader("error_code");
		if(error != null) {
			resultMap.put("flag", "fail");
			resultMap.put("error_code", response.getHeader("error_code"));
		}else {
			String lineKey = chatService.getLineKey();
			resultMap.put("lineKey", lineKey);
		}
		
		return resultMap;
	}
	
	// 채팅방 생성 
	@PostMapping("/user/putChatRoom")
	public Map<String, String> putChatRoom(HttpServletRequest request, HttpServletResponse response, @RequestBody ChatRoom chatroom){
		
		Map<String, String> resultMap = new HashMap<String, String>();
		
		String error = (String) response.getHeader("error_code");
		if(error != null) {
			resultMap.put("flag", "fail");
			resultMap.put("error_code", response.getHeader("error_code"));
		}else {
			//System.out.println(chatroom);
			String result = chatService.putChatRoom(chatroom);
			
			if(result != null && result.equals("success")) {
				// 방 생성시 line key를 서버에서 발급하는 이유?
				// 라인키 발급 -> ~ 가 방을 생성하였습니다 라인저장 + 채팅방 last_line_key update
				String lineKey = chatService.getLineKey();
				chatService.putChatRoom(chatroom.getChatRoomKey(), lineKey);
			}
			
			resultMap.put("flag", result);
			
		}
		
		return resultMap;
	}
	
	// 채팅방 생성시 사용자 조회 
	@PostMapping("/user/getCreateChatRoomUsers")
	public Map<String, String> getCreateChatRoomUsers(HttpServletRequest request, HttpServletResponse response, @RequestBody String sender){
		Map<String, String> resultMap = new HashMap<String, String>();
		
		String error = (String) response.getHeader("error_code");
		if(error != null) {
			resultMap.put("flag", "fail");
			resultMap.put("error_code", response.getHeader("error_code"));
		}else {
			if(sender != null) {
				System.out.println("userchatController sender : " +sender);
				List<EarlyUser> EarlyUserList = chatService.getChatRoomUserList(sender);
				
				ObjectMapper mapper = new ObjectMapper();
				
				List<String> list = new ArrayList<String>();
				
				for(int i = 0; i < EarlyUserList.size(); i++) {
					EarlyUser earlyUser = EarlyUserList.get(i);
					
					try {
						list.add(mapper.writeValueAsString(earlyUser));
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}
				}
				
				resultMap.put("userList", list.toString());
			}else {
				System.out.println("getCreateChatRoomUsers sender is null !");
				resultMap.put("result", "error");
				return resultMap;
			}
			
		}
		
		return resultMap;
		
	}
	
	// 채팅방 참여자 조회 
	@PostMapping("/user/getChatRoomUsers") // @requestBody는 한번의 request에 하나의 Object만 받을 수 있다. @RequestBody String A,  @RequestBody String B 안됨.
	public Map<String, String> getChatRoomUsers(HttpServletRequest request, HttpServletResponse response, @RequestBody ChatRoomUserVO chatRoomUserVO){
		
		
		Map<String, String> resultMap = new HashMap<String, String>();
		String error = (String) response.getHeader("error_code");
		boolean result = false;
		if(error != null) {
			resultMap.put("flag", "fail");
			resultMap.put("error_code", response.getHeader("error_code"));
		}else {
			
			if(chatRoomUserVO != null) {
				
				String chatRoomKey = chatRoomUserVO.getChatRoomKey();
				int limitCnt = chatRoomUserVO.getLimitCnt();
				
				if(chatRoomKey != null && !chatRoomKey.isEmpty()) {
					List<EarlyUser> EarlyUserList = chatService.getChatRoomUsers(chatRoomKey, limitCnt);
					
					ObjectMapper mapper = new ObjectMapper();
					
					List<String> list = new ArrayList<String>();
					
					for(int i = 0; i < EarlyUserList.size(); i++) {
						EarlyUser earlyUser = EarlyUserList.get(i);
						
						try {
							list.add(mapper.writeValueAsString(earlyUser));
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						}
					}
					
					resultMap.put("result", list.toString());
					
				}else {
					System.out.println("getChatRoomUsers roomkey is null !");
					resultMap.put("result", "error");
					return resultMap;
				}
				
			}else {
				resultMap.put("result", "error");
			}
			
		}
		
		
		
		return resultMap;
		
	}
	
	
	// 라인 별 이벤트 사용자 조회
	@PostMapping("/user/getChatLineEventUser")
	public Map<String, String> getChatLineEventUser(HttpServletRequest request, HttpServletResponse response, @RequestBody ChatLineEventVO chatLineEventVO){
		
		Map<String, String> resultMap = new HashMap<String, String>();
		String error = (String) response.getHeader("error_code");
		boolean result = false;
		
		if(error != null) {
			resultMap.put("flag", "fail");
			resultMap.put("error_code", response.getHeader("error_code"));
		}else {
			
			String username = (String) request.getAttribute("username");
			if(username != null && chatLineEventVO != null) {
				
				JSONObject resultJson = chatService.getChatLineEventUser(chatLineEventVO.getRoomKey(), chatLineEventVO.getLineKey());
				if(resultJson != null) {
					result = true;
					resultMap.put("users", resultJson.toString());
				}
			}
			resultMap.put("result", String.valueOf(result));
		}
		return resultMap;
	}
	
	
	// 좋아요 이벤트 처리 
	// 프로젝트의 문서 참조
	@PostMapping("/user/putChatLineEvent")
	public Map<String, String> putChatLineEvent(HttpServletRequest request, HttpServletResponse response,  @RequestBody ChatLineEventVO chatLineEventVO){
		Map<String, String> resultMap = new HashMap<String, String>();
		
		String error = (String) response.getHeader("error_code");
		boolean result = false;
		if(error != null) {
			resultMap.put("flag", "fail");
			resultMap.put("error_code", response.getHeader("error_code"));
		}else {
			
			String username = (String) request.getAttribute("username");
			
			if(username != null && chatLineEventVO != null) {
				JSONObject resultJson = chatService.putLikeEvent(username, chatLineEventVO);
				// new로 객체 생성은함. 다만, 오류가 나서 담기지 않을 가능성이 있으므로 isEmpty로 비교 
				if(!resultJson.isEmpty()) {
					
					result = true;
					
					// 웹소켓 key
					resultJson.put("type", "lineEvent");
					resultJson.put("roomKey", chatLineEventVO.getRoomKey());
					resultJson.put("lineKey", chatLineEventVO.getLineKey());
					
					// 웹소켓으로 pub
					String dest = "/topic/room/"+chatLineEventVO.getRoomKey();
					//System.out.println(resultJson.toString());
					simpMessagingTemplate.convertAndSend(dest, resultJson.toJSONString());
					
				}
			}
			
			resultMap.put("result", String.valueOf(result));
 			
		}

		// 성공, 실패 여부만 처리하고 결과는 웹소켓으로 pub 함
		return resultMap;
		
	}
	
	
	// 읽음 처리 이벤트 수신
	// 읽음 처리 설계 20240130 기준
    // 서버의 user/readLines API를 호출
    // recvLine의 이후 라인키를 모두 조회 
    // 요청 사용자(myUnreadLine)의 모든 미확인 건수 삭제 + 해당 라인의 미확인 사용자(unreadLineUsers) 삭제
    // 라인 별 미확인 사용자 (unreadLineUsers)의 count 계산 sCard
    // 라인을 key로 건수를 value 저장
    // 모든 라인키 처리 후 요청 사용자의 해당 채팅방 미확인 건수 0으로 만듦
    // 나의 전체 채팅방의 미확인 건수를 다 더함
    // /user/readLines의 response를 통해 채팅 전체 건수, 해당 채팅방의 건수를 전달 (resultMap)
    // 웹소켓을 통해 해당 채팅방을 구독하는 모든 사용자에게 읽음 처리된 라인을 라인:건수의 형식으로 publish 하여 처리.(socketJson)
	@PostMapping("/user/readLines")
	public Map<String, String> readLines(HttpServletRequest request, HttpServletResponse response, @RequestBody ChatReadVO chatReadVO){
		Map<String, String> resultMap = new HashMap<String, String>();
		String error = (String) response.getHeader("error_code");
		if(error != null) {
			resultMap.put("flag", "fail");
			resultMap.put("error_code", response.getHeader("error_code"));
		}else {
			
			String username = (String) request.getAttribute("username");
			
			Map<String, Object> unreadJson = chatService.getReadSuccessLines(chatReadVO.getChatRoomKey(), username, chatReadVO.getRecvLine());
			// return 하면 이벤트 수신 요청자 (수신자) 처리
			
			if(unreadJson != null && !unreadJson.isEmpty()) {
				
				String type = (String) unreadJson.get("type");
				String chat = (String) unreadJson.get("chat");
				String room = (String) unreadJson.get("room");
				JSONObject result = (JSONObject) unreadJson.get("result");

				// 유효성 검사 체크 로직 추가 할 것 TODO
				
				/* linekey:count는 웹 소켓으로 전달 */
				// 보낼 경로 설정
				String dest = "/topic/room/"+chatReadVO.getChatRoomKey();
				// 발송
				if(result != null && !result.isEmpty()) {
					JSONObject socketJson = new JSONObject();
					socketJson.put("result", result);
					socketJson.put("type", "readLines");
					simpMessagingTemplate.convertAndSend(dest, socketJson.toJSONString());
				}
				
				/* 전체건수, 채팅방 건수는 response로 전달 */
				resultMap.put("type", type);
				resultMap.put("chat", chat);
				resultMap.put("room", room);
				
				return resultMap;
			}else {
				resultMap.put("flag", "fail");
				resultMap.put("error_code", response.getHeader("error_code"));
			}
		}
		return resultMap;
	}
	
	// 채팅방 리스트 조회 
	@PostMapping("/user/chatList")
	public Map<String, String> chatList(HttpServletRequest request, HttpServletResponse response, @RequestBody EarlyUser earlyUser){
		Map<String, String> resultMap = new HashMap<String, String>();
		
		String error = (String) response.getHeader("error_code");
		if(error != null) {
			resultMap.put("flag", "fail");
			resultMap.put("error_code", response.getHeader("error_code"));
		}else {
			
			String username = (String) request.getAttribute("username");
			
			// 토큰의 id와 클라이언트가 던진 id가 동일 한 경우에만 조회 20240204 -> 브라우저 2개 띄울경우 다른 토큰이 넘어오는 케이스 방지 
			if(username != null && !username.isEmpty() && earlyUser != null && username.equals(earlyUser.getUsername())) {
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
				System.out.println("chatlist 조회시 토큰 id와 넘긴 id가 일치 하지 않음");
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
		if(main == null 
		   || StringUtils.isEmpty(main.getChatReceiver()) || StringUtils.isEmpty(main.getChatRoomKey()) || StringUtils.isEmpty(main.getChatSender())
		   || StringUtils.isEmpty(main.getChatLineKey())) {
			System.out.println("[UserChatController] send data check !");
			return;
		}
		
		/* 검증된 데이터 */ 
		String roomKey = main.getChatRoomKey();
		String receiver = main.getChatReceiver();
		String sender = main.getChatSender();
		String data = main.getChatContents();
		String lineKey = main.getChatLineKey();
		
		/* Room의 LastLineKey update */
		// room이 line 보다 먼저 처리 되어야 하는 이유 20240318
		// 리액트에서 웹소켓으로 publish 하면 해당 메소드가 호출 된다.
		// 그와 동시에 리액트에서는 비동기로 리스트를 다시 불러오게끔 처리 되는데 리스트를 조회 하는 시점보다 room의 lastlinekey를 update하는 것이 늦는 케이스가 있었다.
		// room의 lastlineKey를 먼저 업데이트 하는 것으로 순서를 변경하여 조회 이전에 정렬 기준이 되는 lastlinekey를 update 하는 것으로 수정하였다. 
		chatService.putChatRoom(roomKey, lineKey);
		
		/* 라인키 생성 및 DB 저장 */
		// TODO 채팅 내용 암호화 및 QueueThread 방식으로 전환 */ 
		chatService.putChatMain(main); 
		
		
		/* redis 해당 라인의 읽지 않은 사용자 저장 및 조회 */
		String unreadCount = chatService.getUnreadLineCount(roomKey, lineKey, receiver, sender);
		
		// 전달 할 채팅 데이터 json 생성
		JSONObject sendData = new JSONObject();
		sendData.put("type", "chat");
		sendData.put("chatLineKey", lineKey);
		sendData.put("chatRoomKey", roomKey);
		sendData.put("chatContents", data);
		sendData.put("chatSender", sender);
		sendData.put("chatUnreadCount", unreadCount);
		
		// 보낼 경로 설정
		String dest = "/topic/room/"+roomKey;

		// 발송 - chatData + 라인의 미확인 건수
		simpMessagingTemplate.convertAndSend(dest, sendData.toJSONString());

		
		/* redis 수신자 별 라인 저장 -> 수신자의 채팅방 미확인 건수 저장 -> 수신자의 전체 채팅 미확인 건수 저장 및 전체 건수 조회*/ 
		Map<String, JSONObject> unreadMap = chatService.getUnreadChatCount(roomKey, receiver, sender, lineKey);
		
		//System.out.println(unreadMap);
		
		// 발송 - 채팅방의 수신자의 채팅 미확인 전체 건수 & 해당 채팅방의 건수
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

	
	// 채팅방의 라인 조회 (방 입장)
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
					
					/* 입장한 채팅방 읽음처리  "0" -> 모두 읽음 처리 하겠다. */
					Map<String, Object> unreadJson = chatService.getReadSuccessLines(chatRoomKey, username, "0");
					// 미확인 건수 갱신 & 전달(웹소켓)
					if(unreadJson != null && !unreadJson.isEmpty()) {
						
						String type = (String) unreadJson.get("type");
						String chat = (String) unreadJson.get("chat");
						String room = (String) unreadJson.get("room");
						JSONObject result = (JSONObject) unreadJson.get("result");

						// 유효성 검사 체크 로직 추가 할 것 TODO
						
						/* linekey:count는 웹 소켓으로 전달 */
						// 보낼 경로 설정
						String dest = "/topic/room/"+chatRoomKey;
						// 발송
						if(result != null && !result.isEmpty()) {
							JSONObject socketJson = new JSONObject();
							socketJson.put("result", result);
							socketJson.put("type", "readLines");
							simpMessagingTemplate.convertAndSend(dest, socketJson.toJSONString());
						}
						
					}else {}
					
					/* 라인 리스트 조회 */
					List<ChatMain> lineList = chatService.getChatRoomLine(chatRoomKey);
					System.out.println(lineList);
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


