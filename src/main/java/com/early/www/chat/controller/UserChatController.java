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
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.early.www.chat.dto.ChatLineDTO;
import com.early.www.chat.dto.ChatLineEventDTO;
import com.early.www.chat.dto.ChatReadDTO;
import com.early.www.chat.dto.ChatRoomRecvDTO;
import com.early.www.chat.dto.ChatRoomUserDTO;
import com.early.www.chat.model.ChatMain;
import com.early.www.chat.model.ChatRoom;
import com.early.www.chat.service.ChatService;
import com.early.www.common.config.RabbitmqConfig;
import com.early.www.user.model.EarlyUser;
import com.early.www.util.CommonConst;
import com.early.www.util.CommonRequestCheck;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserChatController {

	@Autowired
	ChatService chatService;
	
	@Autowired
	CommonRequestCheck commonRequestCheck;
	
	// 라인키 발급 로직 error check 하지않음 (빈번하게 발생함)
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
	
		boolean errorCheck = commonRequestCheck.errorCheck(request, response, chatroom);
		
		if(errorCheck) {
			resultMap.put("flag", "fail");
			resultMap.put("error_code", response.getHeader("error_code"));
		}else {
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
		
		boolean errorCheck = commonRequestCheck.errorCheck(request, response, sender);
		
		if(errorCheck) {
			resultMap.put("flag", "fail");
			resultMap.put("error_code", response.getHeader("error_code"));
		}else {
			if(sender != null) {
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
				log.info("[/user/getCreateChatRoomUsers] getCreateChatRoomUsers sender is null !");
				resultMap.put("result", "error");
				return resultMap;
			}
		}
		return resultMap;
		
	}
	
	// 채팅방 참여자 조회 
	@PostMapping("/user/getChatRoomUsers") // @requestBody는 한번의 request에 하나의 Object만 받을 수 있다. @RequestBody String A,  @RequestBody String B 안됨.
	public Map<String, String> getChatRoomUsers(HttpServletRequest request, HttpServletResponse response, @RequestBody ChatRoomUserDTO chatRoomUserDto){
		Map<String, String> resultMap = new HashMap<String, String>();
		
		boolean errorCheck = commonRequestCheck.errorCheck(request, response, chatRoomUserDto);
		
		if(errorCheck) {
			resultMap.put("flag", "fail");
			resultMap.put("error_code", response.getHeader("error_code"));
		}else {
			
			if(chatRoomUserDto != null) {
				
				String chatRoomKey = chatRoomUserDto.getChatRoomKey();
				int limitCnt = chatRoomUserDto.getLimitCnt();
				
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
					log.info("[/user/getChatRoomUsers] getChatRoomUsers roomkey is null !");
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
	public Map<String, String> getChatLineEventUser(HttpServletRequest request, HttpServletResponse response, @RequestBody ChatLineEventDTO chatLineEventVO){
		
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
	public Map<String, String> putChatLineEvent(HttpServletRequest request, HttpServletResponse response,  @RequestBody ChatLineEventDTO chatLineEventVO){
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
	public JSONObject readLines(HttpServletRequest request, HttpServletResponse response, @RequestBody ChatReadDTO chatReadDto){
		JSONObject resultJson = new JSONObject();
		
		boolean errorCheck = commonRequestCheck.errorCheck(request, response, chatReadDto);
		
		if(errorCheck) {
			resultJson.put(CommonConst.RESPONSE_TYPE, CommonConst.RESPONSE_TYPE_FAIL);
			resultJson.put(CommonConst.RESPONSE_DATA_ERROR_MSG, response.getHeader("error_code"));
		}else {
			
			String username = (String) request.getAttribute("username");
			if(username != null && !username.isEmpty()) {
				
				if(chatReadDto.getUserId() != null && username.equals(chatReadDto.getUserId())) {
					
					Map<String, Object> unreadJson = chatService.putChatUnreadLines(chatReadDto.getChatRoomKey(), username, simpMessagingTemplate);
					// return 하면 이벤트 수신 요청자 (수신자) 처리
					
					if(unreadJson != null && !unreadJson.isEmpty()) {
						/* 전체건수, 채팅방 건수는 response로 전달 */
						JSONObject data = new JSONObject();
						data.put("count_type", (String) unreadJson.get("type"));
						data.put("chat", (String) unreadJson.get("chat"));
						data.put("room", (String) unreadJson.get("room"));
						
						resultJson.put(CommonConst.RESPONSE_TYPE, CommonConst.RESPONSE_TYPE_SUCCESS);
						resultJson.put(CommonConst.RESPONSE_DATA, data);
						
					}else {
						resultJson.put(CommonConst.RESPONSE_TYPE, CommonConst.RESPONSE_TYPE_FAIL);
						resultJson.put(CommonConst.RESPONSE_DATA_ERROR_MSG, response.getHeader("error_code"));
					}
				}else {
					resultJson.put(CommonConst.RESPONSE_TYPE, CommonConst.RESPONSE_TYPE_FAIL);
					resultJson.put(CommonConst.RESPONSE_DATA_ERROR_MSG, CommonConst.INVALID_BODY_DATA);
				}
			}else {
				resultJson.put(CommonConst.RESPONSE_TYPE, CommonConst.RESPONSE_TYPE_FAIL);
				resultJson.put(CommonConst.RESPONSE_DATA_ERROR_MSG, CommonConst.INVALID_TOKEN_DATA);
			}
		}
		
		log.info("[{}] response, body : {}", request.getRequestURI(), resultJson);
		return resultJson;
	}
	
	// 채팅방 리스트 조회 -> TODO 추후 Ealry가 아닌 DTO로 변경.. 사용자 ID와 chat type만 받으면 될듯. 
	@PostMapping("/user/chatList")
	public JSONObject chatList(HttpServletRequest request, HttpServletResponse response, @RequestBody EarlyUser earlyUser){
		JSONObject resultJson = new JSONObject();
		JSONObject data = new JSONObject();
		
		boolean errorCheck = commonRequestCheck.errorCheck(request, response, earlyUser);
		
		if(errorCheck) {
			
			resultJson.put(CommonConst.RESPONSE_TYPE, CommonConst.RESPONSE_TYPE_FAIL);
			resultJson.put(CommonConst.RESPONSE_DATA_ERROR_MSG, response.getHeader("error_code"));
		
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
					
					resultJson.put(CommonConst.RESPONSE_TYPE, CommonConst.RESPONSE_TYPE_SUCCESS);
					
					data.put("chat_list", list.toString());
					resultJson.put(CommonConst.RESPONSE_DATA, data);
				
				}else {
					// 채팅리스트가 없음
					resultJson.put(CommonConst.RESPONSE_TYPE, CommonConst.RESPONSE_TYPE_SUCCESS);
					
					data.put("chat_list", "empty");
					resultJson.put(CommonConst.RESPONSE_DATA, data);
				}
			}else {
				resultJson.put(CommonConst.RESPONSE_TYPE, CommonConst.RESPONSE_TYPE_FAIL);
				resultJson.put(CommonConst.RESPONSE_DATA_ERROR_MSG, CommonConst.INVALID_TOKEN_DATA);
			}
		}
		
		return resultJson;
	}
	

	private final SimpMessagingTemplate simpMessagingTemplate;

	// 채팅 발송 - error 체크 하지 않는 이유? 웹소켓 연결 자체가 http 프로토콜이 아니기 때문에, 그리고 로그인이 검증되야 웹소켓을 연결 할 수 있으므로
	@MessageMapping("/user/chat")
	public void handle(ChatMain main) {
	
		
		String receiver = main.getChatReceiver();
		String roomKey = main.getChatRoomKey();
		String sender = main.getChatSender();
		String data = main.getChatContents();
		String lineKey = main.getChatLineKey();
		String senderName = main.getChatSenderName();
		
		/* 데이터 검증 */
		if(main == null 
		   || StringUtils.isEmpty(receiver) || StringUtils.isEmpty(roomKey) || StringUtils.isEmpty(sender)
		   || StringUtils.isEmpty(lineKey)|| StringUtils.isEmpty(senderName)) {
			log.info("[/user/chat] ChatMain data invalid !! ChatMain is ... {} ", main);
			return;
		}
		
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
		sendData.put("chatSenderName", senderName);
		
		
		// 채팅 데이터 WS 발송 로직
		chatService.sendMessageWs(simpMessagingTemplate, sendData, roomKey);
		
		/* redis 수신자 별 라인 저장 -> 수신자의 채팅방 미확인 건수 저장 -> 수신자의 전체 채팅 미확인 건수 저장 및 전체 건수 조회 후 WS 발송 처리 */ 
		chatService.sendUnreadChatCount(simpMessagingTemplate, roomKey, receiver, sender, lineKey);
		
		/* 
		 * 20240720 k8s 이후 버전 적용 
		 * */
		// 다른 pod로 데이터 전송처리
		
		sendData.put("chatReceiver", receiver); // 수신한 pod에서 redis 건수를 처리 sendUnreadChatCount 호출하기 위함. 
		chatService.sendMessageDeployment(sendData);		
		
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

	// 방 입장, 더 불러오기 API
	@PostMapping("/user/getChatLines")
	public JSONObject getChatLines (HttpServletRequest request, @RequestBody ChatLineDTO chatLineDTO, HttpServletResponse response) {
		JSONObject resultJson = new JSONObject();
		
		boolean errorCheck = commonRequestCheck.errorCheck(request, response, chatLineDTO);
		
		if(errorCheck) {
			
			resultJson.put(CommonConst.RESPONSE_TYPE, CommonConst.RESPONSE_TYPE_FAIL);
			resultJson.put(CommonConst.RESPONSE_DATA_ERROR_MSG, response.getHeader("error_code"));
			
		}else {
			// 토큰에 있는 사용자 ID
			String username = (String) request.getAttribute("username");
			
			if(username != null && chatLineDTO.getUserId() != null && username.equals(chatLineDTO.getUserId())) {
				resultJson = chatService.getChatLines(chatLineDTO, simpMessagingTemplate);
			}else {
				JSONObject data = new JSONObject();
				data.put(CommonConst.RESPONSE_DATA_ERROR_MSG, CommonConst.INVALID_USER_ID);
				
				resultJson.put("type", CommonConst.RESPONSE_TYPE_FAIL);
				resultJson.put("data", data);
			}
		}
		log.info("[{}] response, body : {}", request.getRequestURI(), resultJson);
		return resultJson;
	}
	
	
	// 방 입장, 더 불러오기 API
	@PostMapping("/user/getRecvUser")
	public JSONObject getRecvUser (HttpServletRequest request, @RequestBody ChatRoomRecvDTO chatRoomRecvDTO, HttpServletResponse response) {
		JSONObject resultJson = new JSONObject();
			
		boolean errorCheck = commonRequestCheck.errorCheck(request, response, chatRoomRecvDTO);
		
		if(errorCheck) {
			
			resultJson.put(CommonConst.RESPONSE_TYPE, CommonConst.RESPONSE_TYPE_FAIL);
			resultJson.put(CommonConst.RESPONSE_DATA_ERROR_MSG, response.getHeader("error_code"));
			
		}else {
			// 토큰에 있는 사용자 ID
			String username = (String) request.getAttribute("username");
			
			if(username != null && chatRoomRecvDTO.getUserId() != null && username.equals(chatRoomRecvDTO.getUserId())) {
				resultJson = chatService.getRecvUser(chatRoomRecvDTO);
			}else {
				JSONObject data = new JSONObject();
				data.put(CommonConst.RESPONSE_DATA_ERROR_MSG, CommonConst.INVALID_USER_ID);
				
				resultJson.put("type", CommonConst.RESPONSE_TYPE_FAIL);
				resultJson.put("data", data);
			}
		}
			log.info("[{}] response, body : {}", request.getRequestURI(), resultJson);
			return resultJson;
	}
		
	@Autowired
	RabbitmqConfig config;
	
	/* rabbitmq 수신 로직 */
	@RabbitListener(queues = "#{chatQueue.name}")
	public void receiveMessage(String msg) {
		
		chatService.recvMessageDeployment(msg, simpMessagingTemplate);
		
	}

	

}


