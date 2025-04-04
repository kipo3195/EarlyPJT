package com.early.www.chat.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.early.www.chat.controller.UserChatController;
import com.early.www.chat.dto.ChatLineDTO;
import com.early.www.chat.dto.ChatLineEventDTO;
import com.early.www.chat.dto.ChatRoomRecvDTO;
import com.early.www.chat.model.ChatList;
import com.early.www.chat.model.ChatMain;
import com.early.www.chat.model.ChatRoom;
import com.early.www.common.config.RabbitmqConfig;
import com.early.www.repository.ChatListRepository;
import com.early.www.repository.ChatMainRepository;
import com.early.www.repository.ChatRoomRepository;
import com.early.www.repository.EarlyUserRepository;
import com.early.www.user.model.EarlyUser;
import com.early.www.util.CommonConst;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

	@Autowired
	ChatRoomRepository chatRoomRepository;
	
	@Autowired
	ChatListRepository chatListRepository;
	
	@Autowired
	ChatMainRepository chatMainRepository;
	
	@Autowired
	EarlyUserRepository earlyUserRepository;
	
	@Autowired
	RabbitTemplate rabbitTemplate;
	
	@Autowired
	RedisTemplate<String, Object> redisTemplate;
	
	// 람다는 파라미터로 사용하는 변수와 로컬 변수를 구분을 하지 못하기 때문에 클래스 변수로 잡아줌
	double startLine = 0;
	
	// 위 이유와 동일함.
	boolean result = false;
	
	@Override
	public String getLineKey() {
		
		long time = System.currentTimeMillis();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYYMMddHHmmssSSS");
		Date date = new Date();
		date.setTime(time);
		
		return simpleDateFormat.format(date);
		
	}
	
	
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
		if(chatList != null && !chatList.isEmpty()) {
			
			for(int i=0; i < chatList.size(); i++) {
				String key = "myUnreadRoom:"+username;
				Object roomCnt = redisTemplate.opsForHash().get(key, chatList.get(i).getChatRoomKey());
				
				if(roomCnt == null) {
					chatList.get(i).setUnreadCount("0");
				}else {
					chatList.get(i).setUnreadCount(String.valueOf(roomCnt));
				}
			}
		}
		return chatList;
	}

	
	//채팅 라인 저장
	@Override
	public void putChatMain(ChatMain main) {
		//String lineKey = makeLineKey();
		
		// Queue thread 
		
		main.setChatLineKey(main.getChatLineKey());
		main.setSendDate(main.getChatLineKey());
		main.setChatDelFlag("N");
		
		chatMainRepository.save(main);
		
	}
	
	// 채팅방 lastLinkey update
	@Override
	public void putChatRoom(String roomKey, String lineKey) {
		
		chatRoomRepository.save(roomKey, lineKey);
		
	}

	// 채팅방 데이터 조회 (최초)
	@Override
	public List<ChatMain> getChatRoomLine(String chatRoomKey, String readLineKey) {
		
		// 라인 조회 DB
		List<ChatMain> chatMainList = chatMainRepository.findByChatRoomKey(chatRoomKey, readLineKey);
		
		RedisSerializer hashKeySerializer = redisTemplate.getHashKeySerializer();
		RedisSerializer hashValueSerializer = redisTemplate.getHashValueSerializer();
		RedisSerializer keySerializer = redisTemplate.getKeySerializer();
		redisTemplate.execute(new RedisCallback<Object>() {

			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				
				// 라인의 미확인 건수 추가
				for(int i = 0; i < chatMainList.size(); i++) {
					String key = "unreadLineUsers:"+chatRoomKey+":"+chatMainList.get(i).getChatLineKey();
					Long unreadCount = connection.sCard(keySerializer.serialize(key));
					chatMainList.get(i).setChatUnreadCount(String.valueOf(unreadCount));
					
					key = "allChatEvent:"+chatRoomKey+":"+chatMainList.get(i).getChatLineKey();
					Map<byte[], byte[]> allChatEvent = connection.hGetAll(hashKeySerializer.serialize(key));
					
					Iterator<byte[]> chatEvent = allChatEvent.keySet().iterator();

					// 클라이언트에서 없으면 null임 
					while(chatEvent.hasNext()) {
						// 채팅 라인에 대한 이벤트 추가 
						byte[] event = chatEvent.next();
						String eventKey = (String) hashKeySerializer.deserialize(event);
						String value = (String) hashValueSerializer.deserialize(allChatEvent.get(event));
						if(eventKey.equals(CommonConst.ChatLineEventCheck)) {
							chatMainList.get(i).setChatCheckCnt(value);
						}else if(eventKey.equals(CommonConst.ChatLineEventGood)) {
							chatMainList.get(i).setChatGoodCnt(value);
						}else if(eventKey.equals(CommonConst.ChatLineEventLike)) {
							chatMainList.get(i).setChatLikeCnt(value);
						}
					}
				}
				
				if(connection != null) {
					connection.close();
				}
				return null;
			}
			
		});
		
		return chatMainList;
	}


	// 채팅 발송시 unreadcount 전달 
	@Override
	public void sendUnreadChatCount(SimpMessagingTemplate simpMessagingTemplate, String roomKey, String receiver, String sender, String lineKey) {
		
		// 수신자 파싱
		String[] receivers = receiver.split("[|]");
		
		// 사용자 : {}
		Map<String, JSONObject> map = new HashMap<>();
		
		// 20240123
		RedisSerializer keySerializer = redisTemplate.getKeySerializer();
		RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
		RedisSerializer hashKeySerializer = redisTemplate.getHashKeySerializer();
		RedisSerializer hashValueSerializer = redisTemplate.getHashValueSerializer();
	
		redisTemplate.execute(new RedisCallback<Object>() {

			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				
				for(int i = 0 ;  i< receivers.length; i++) {
					if(receivers[i].equals(sender)) {
						continue;
					}
					// 나의 라인 저장 (ZSET)
					String unreadLine = "myUnreadLine:"+roomKey+":"+receivers[i];
					connection.zAdd(keySerializer.serialize(unreadLine), Double.parseDouble(lineKey), valueSerializer.serialize(lineKey));
					// 나의 해당 채팅방 건수 조회 
					Long lineCount = connection.zCard(keySerializer.serialize(unreadLine));
					
					// 나의 해당 채팅방 건수 저장 (HASH)
					String unreadRoom = "myUnreadRoom:"+receivers[i];
					connection.hSet(hashKeySerializer.serialize(unreadRoom), hashKeySerializer.serialize(roomKey), hashValueSerializer.serialize(String.valueOf(lineCount)));
					// 나의 전체 채팅방 건수 조회
					Map<byte[], byte[]> roomMap = connection.hGetAll(hashKeySerializer.serialize(unreadRoom));
					Iterator<byte[]> allRoom = roomMap.keySet().iterator();
					long allCount = 0;

					while(allRoom.hasNext()) {
						byte[] room = allRoom.next();
						String value = (String) hashValueSerializer.deserialize(roomMap.get(room));
						allCount =+ Long.valueOf(value);
					}
					
					String unreadAll = "myUnreadAll:"+receivers[i];
					// 나의 전체 채팅 건수 저장 (SET)
					connection.hSet(hashKeySerializer.serialize(unreadAll), hashKeySerializer.serialize("chat"),valueSerializer.serialize(String.valueOf(allCount)));
					
					// 클라이언트로 전달할 데이터 
					JSONObject json = new JSONObject();
					json.put("type", "chat");	
					json.put("chat", allCount);	// 채팅 전체 건수
					json.put("room", lineCount); // 해당 채팅방의 건수 

					map.put(receivers[i], json);
					
				}
				
				if(connection != null) {
					connection.close();
				}
				return null;
			}
			
		});
		
		// 발송 - 채팅방의 수신자의 채팅 미확인 전체 건수 & 해당 채팅방의 건수 TODO 비동기 로직으로 전환 
		Iterator<String> unreadIter = map.keySet().iterator();
		while(unreadIter.hasNext()) {
			String recvUser = unreadIter.next();
			simpMessagingTemplate.convertAndSend("/topic/user/"+recvUser, map.get(recvUser).toJSONString());
		}
		
	}

	// 로그인 시 사용자의 미확인 건수(채팅, 쪽지)를 전달함.
	@Override
	public Map<String, String> getAllUnreadCnt(String username) {
		
		Map<String, String> map = new HashMap<>();

		// hash 형식
		// hash  = userID
		// key   = roomKey : value = count(int)
		Map<Object, Object> userAllRooms = redisTemplate.opsForHash().entries(username);
		if(userAllRooms != null && !userAllRooms.isEmpty()) {
			Iterator<Object> iter = userAllRooms.keySet().iterator();
			int unreadCnt = 0;
			while(iter.hasNext()) {
				unreadCnt += Integer.parseInt((String) userAllRooms.get((String) iter.next()));
			}
			map.put("chat", String.valueOf(unreadCnt));
		}
		
		return map;
	}

	// 해당 라인의 미확인 건수 저장 및 조회 
	@Override
	public String getUnreadLineCount(String roomKey, String lineKey, String receiver, String sender) {
		
		String result = "0";
		
		String line = "unreadLineUsers:"+roomKey+":"+lineKey;
		
		String[] receivers = receiver.split("[|]");
		
		Long unreadCount = null;
		
		RedisSerializer keySerializer = redisTemplate.getKeySerializer();
		RedisSerializer valueRedisSerializer = redisTemplate.getValueSerializer();
		redisTemplate.execute(new RedisCallback<Object>() {

			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				
				if(receivers != null) {
					for(int i = 0; i < receivers.length; i++) {
						if(!receivers[i].equals(sender)) {
							connection.sAdd(keySerializer.serialize(line), valueRedisSerializer.serialize(receivers[i]));
						}
					}
				}
				
				if(connection != null) {
					connection.close();
				}
				return null;
			}
			
		});
		unreadCount = redisTemplate.opsForSet().size(line);
		result = String.valueOf(unreadCount);
		
		return result;
	}

	// 채팅 입장시 해당 채팅방 데이터 읽음처리 -> 20240131 getReadSuccessLines 로 대체함 
//	@Override
//	public Map<String, String> putChatRoomUnread(String roomKey, String username, String startLineKey) {
//		
//		// 클라이언트로 전달할 데이터 
//		 Map<String, String> map = new HashMap<String, String>();
//		
//		if(!startLineKey.equals("0")) {
//			startLine = Double.valueOf(startLineKey);
//		}
//		
//		RedisSerializer keySerializer = redisTemplate.getKeySerializer();
//		RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
//		RedisSerializer hashKeySerializer = redisTemplate.getHashKeySerializer();
//		RedisSerializer hashValueSerializer = redisTemplate.getHashValueSerializer();
//		redisTemplate.execute(new RedisCallback<Object>() {
//
//			@Override
//			public Object doInRedis(RedisConnection connection) throws DataAccessException {
//
//				// 해당 방의 전체 미확인 건수 조회
//				String key = "myUnreadLine:"+roomKey+":"+username;
//				Set<byte[]> myUnreadLineSet = connection.zRangeByScore(keySerializer.serialize(key), startLine, 99999999999999999L);
//
//				Object[] arr = myUnreadLineSet.toArray();
//				for(int i = 0; i < arr.length; i++) {
//					
//					// 나의 미확인 건수 삭제 
//					String unreadLine = (String) valueSerializer.deserialize((byte[]) arr[i]);
//					key = "myUnreadLine:"+roomKey+":"+username;
//					connection.zRem(keySerializer.serialize(key), valueSerializer.serialize(unreadLine));
//					
//					// 해당 라인의 미확인 사용자 중 나 삭제
//					key = "unreadLineUsers:"+roomKey+":"+unreadLine;
//					connection.sRem(keySerializer.serialize(key), keySerializer.serialize(username));
//					
//				}
//				
//				// 나의 해당 채팅방 미확인 건수 제거 
//				String unreadRoom = "myUnreadRoom:"+username;
//				connection.hDel(hashKeySerializer.serialize(unreadRoom), hashKeySerializer.serialize(roomKey));
//				
//				// 나의 전체 채팅방 건수 조회
//				Map<byte[], byte[]> roomMap = connection.hGetAll(hashKeySerializer.serialize(unreadRoom));
//				Iterator<byte[]> allRoom = roomMap.keySet().iterator();
//				long allCount = 0;
//				
//				while(allRoom.hasNext()) {
//					byte[] room = allRoom.next();
//					String value = (String) hashValueSerializer.deserialize(roomMap.get(room));
//					allCount =+ Long.valueOf(value);
//				}
//				
//				String unreadLine = "myUnreadLine:"+roomKey+":"+username;
//				Long lineCount = connection.zCard(keySerializer.serialize(unreadLine));
//				
//				map.put("type", "chat");	
//				map.put("chat", String.valueOf(allCount));	// 채팅 전체 건수
//				map.put("room", String.valueOf(lineCount)); // 해당 채팅방의 건수 
//				
//				if(connection != null) {
//					connection.close();
//				}
//				return null;
//			}
//		});
//		
//		return map;
//		
//	}

	// 읽음 요청 
	@Override
	public Map<String, Object> putChatUnreadLines(String roomKey, String username, SimpMessagingTemplate simpMessagingTemplate) {
		// 전달받은 라인 이후의 신규 수신 라인 읽음처리 
		Map<String, Object> map = new HashMap<String, Object>();
		
		JSONObject json = new JSONObject();
		
		RedisSerializer keySerializer = redisTemplate.getKeySerializer();
		RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
		RedisSerializer hashKeySerializer = redisTemplate.getHashKeySerializer();
		RedisSerializer hashValueSerializer = redisTemplate.getHashValueSerializer();
		
		redisTemplate.execute(new RedisCallback<Object>() {

			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {

				// 해당 방의 전체 미확인 건수 조회
				String key = "myUnreadLine:"+roomKey+":"+username;
				
				Set<byte[]> myUnreadLineSet = connection.zRangeByScore(keySerializer.serialize(key), startLine, 99999999999999999L);

				// System.out.println("신규로 읽는 라인의 숫자 : "+ myUnreadLineSet.size());
				Object[] arr = myUnreadLineSet.toArray();
				for(int i = 0; i < arr.length; i++) {
					
					// 나의 미확인 건수 삭제 
					String unreadLine = (String) valueSerializer.deserialize((byte[]) arr[i]);
					key = "myUnreadLine:"+roomKey+":"+username;
					connection.zRem(keySerializer.serialize(key), valueSerializer.serialize(unreadLine));
					
					// 해당 라인의 미확인 사용자 중 나 삭제
					key = "unreadLineUsers:"+roomKey+":"+unreadLine;
					connection.sRem(keySerializer.serialize(key), keySerializer.serialize(username));
					
					// 내가 읽음 처리 함으로 써 해당 라인의 남은 미확인 사용자 건수 전달
					long count = connection.sCard(keySerializer.serialize(key));
					
					json.put(unreadLine, String.valueOf(count));
				}
				
				// 나의 해당 채팅방 미확인 건수 제거 
				String unreadRoom = "myUnreadRoom:"+username;
				connection.hDel(hashKeySerializer.serialize(unreadRoom), hashKeySerializer.serialize(roomKey));
				
				// 나의 전체 채팅방 건수 조회
				Map<byte[], byte[]> roomMap = connection.hGetAll(hashKeySerializer.serialize(unreadRoom));
				Iterator<byte[]> allRoom = roomMap.keySet().iterator();
				long allCount = 0;
				
				while(allRoom.hasNext()) {
					byte[] room = allRoom.next();
					String value = (String) hashValueSerializer.deserialize(roomMap.get(room));
					allCount =+ Long.valueOf(value);
				}
				
				String unreadLine = "myUnreadLine:"+roomKey+":"+username;
				Long lineCount = connection.zCard(keySerializer.serialize(unreadLine));
				
				map.put("type", "chat");	
				map.put("chat", String.valueOf(allCount));	// 채팅 전체 건수
				map.put("room", String.valueOf(lineCount)); // 해당 채팅방의 건수 
				//map.put("result", json); // 읽음 처리된 라인 정보
				
				
				/* linekey:count는 웹 소켓으로 전달 */
				// 보낼 경로 설정
				String dest = "/topic/room/"+roomKey;
				
				JSONObject socketJson = new JSONObject();
				socketJson.put("result", json);
				socketJson.put("type", "readLines");
				simpMessagingTemplate.convertAndSend(dest, socketJson.toJSONString());
				
				if(connection != null) {
					connection.close();
				}
				return null;
			}
		});
		
		return map;
	}


	@Override
	public JSONObject putLikeEvent(String username, ChatLineEventDTO chatLineEventVO) {
		
		RedisSerializer keySerializer = redisTemplate.getKeySerializer();
		RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
		RedisSerializer hashKeySerializer = redisTemplate.getHashKeySerializer();
		RedisSerializer hashValueSerializer = redisTemplate.getHashValueSerializer();
		
		String type = chatLineEventVO.getType();
		String roomKey = chatLineEventVO.getRoomKey();
		String lineKey = chatLineEventVO.getLineKey();
		
		JSONObject json = new JSONObject();
		redisTemplate.execute(new RedisCallback<Object>() {
			
			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				
				String key = type+":"+roomKey+":"+lineKey;
				long saddresult = connection.sAdd(keySerializer.serialize(key), valueSerializer.serialize(username));
				
				if(saddresult == 0) {
					// 취소가 되어야함.
					long cancelCnt = connection.sRem(keySerializer.serialize(key), valueSerializer.serialize(username));
				}
				// 해당 방의 라인의 타입의 건수
				long cnt = connection.sCard(keySerializer.serialize(key));
				
				key = "allChatEvent:"+roomKey+":"+lineKey;
				result = connection.hSet(hashKeySerializer.serialize(key), hashKeySerializer.serialize(type),hashValueSerializer.serialize(String.valueOf(cnt)));

				// 바뀐것만 내려줌
				byte[] result = connection.hGet(hashKeySerializer.serialize(key), hashKeySerializer.serialize(type));
				String value = (String) hashValueSerializer.deserialize(result);
				
				json.put(type, value);
				
				return null;
			}
		});
		
		return json;
	}


	@Override
	public JSONObject getChatLineEventUser(String roomKey, String lineKey) {
		
		RedisSerializer keySerializer = redisTemplate.getKeySerializer();
		RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
		RedisSerializer hashKeySerializer = redisTemplate.getHashKeySerializer();
		RedisSerializer hashValueSerializer = redisTemplate.getHashValueSerializer();
		
		JSONObject json = new JSONObject();
		redisTemplate.execute(new RedisCallback<Object>() {
			
			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				
				String key = "good:"+roomKey+":"+lineKey;
				Set<byte[]> goodUsers = connection.sMembers(keySerializer.serialize(key));
				if(goodUsers != null && !goodUsers.isEmpty()) {
					JSONArray userJson = new JSONArray();
					Object[] arr = goodUsers.toArray();
					for(int i = 0; i < arr.length; i++) {
						String user = (String) valueSerializer.deserialize((byte[]) arr[i]);
						userJson.add(user);
					}
					json.put("good", userJson);
				}

				key = "check:"+roomKey+":"+lineKey;
				Set<byte[]> checkUsers = connection.sMembers(keySerializer.serialize(key));
				if(checkUsers != null && !checkUsers.isEmpty()) {
					JSONArray userJson = new JSONArray();
					Object[] arr = checkUsers.toArray();
					for(int i = 0; i < arr.length; i++) {
						String user = (String) valueSerializer.deserialize((byte[]) arr[i]);
						userJson.add(user);
					}
					json.put("check", userJson);
				}
				
				key = "like:"+roomKey+":"+lineKey;
				Set<byte[]> likeUsers = connection.sMembers(keySerializer.serialize(key));
				if(likeUsers != null && !likeUsers.isEmpty()) {
					JSONArray userJson = new JSONArray();
					Object[] arr = likeUsers.toArray();
					for(int i = 0; i < arr.length; i++) {
						String user = (String) valueSerializer.deserialize((byte[]) arr[i]);
						userJson.add(user);
					}
					json.put("like", userJson);
				}
				return null;
			}
		});
		
		
		return json;
	}


	@Override
	public List<EarlyUser> getChatRoomUsers(String roomKey, int limitCnt) {
		
		List<EarlyUser> list = null;
		
		if(roomKey != null) {
			list = earlyUserRepository.findByChatRoomKeyAndMin(roomKey, limitCnt);
		}
		return list;
	}


	@Override
	public List<EarlyUser> getChatRoomUserList(String sender) {
		List<EarlyUser> list = null;
		
		if(sender != null) {
			list = earlyUserRepository.findBySender(sender);
		}
		
		return list;
	}


	@Override
	public String putChatRoom(ChatRoom chatroom) {
		
		ChatRoom createRoom = chatRoomRepository.save(chatroom);
		// TODO 트랜잭션 처리 ROOM과 LIST
		if(createRoom != null) {
			if(chatroom.getChatRoomKey().equals(createRoom.getChatRoomKey())){
				
				String userList[] = createRoom.getChatRoomUsers().split("[|]");
				for(int i = 0 ; i < userList.length; i++) {
					ChatList chatList = new ChatList();
					chatList.setChatListUser(userList[i]);
					chatList.setChatRoomKey(createRoom.getChatRoomKey());
					chatListRepository.save(chatList);
				}
				return "success";
			}else {
				return "false";
			}
		}else {
			return "false";
		}
		
	}

	
	@Override
	public JSONObject getChatLines(ChatLineDTO dto, SimpMessagingTemplate simpMessagingTemplate) {
		JSONObject resultJson = new JSONObject();
		JSONObject data = new JSONObject();
		
		String roomKey = null;
		String userId = dto.getUserId();
		
		// 방이 생성되어 있는지 체크
		if(dto.getEnterType() != null) {
			if(dto.getEnterType().equals("a")) {
				// 주소록에서 입장
				ChatRoom chatRoom = chatRoomRepository.findByRoomKey(dto.getRoomKey());
				if(chatRoom != null) {
					roomKey = chatRoom.getChatRoomKey();
					// 주소록에서 입장한 방은 방 이름을 알 수 없음. 
					data.put("title", chatRoom.getChatRoomTitle());
				}else {
					// 생성되지 않은 방
					data.put("newChatRoomKey", dto.getRoomKey());
				}
			}else {
				roomKey = dto.getRoomKey();
			}
		}else {
			
			data.put(CommonConst.RESPONSE_DATA_ERROR_MSG, CommonConst.INVALID_BODY_DATA);

			resultJson.put("type", CommonConst.RESPONSE_TYPE_FAIL);
			resultJson.put("data", data);
			return resultJson;
		}
		
		// 읽음처리 - redis 미확인 건수 갱신 & 전달(웹소켓) TODO Queue thread로 처리할 수 있다면?
		Map<String, Object> unreadJson = putChatUnreadLines(roomKey, userId, simpMessagingTemplate);
				
		if(unreadJson != null && !unreadJson.isEmpty()) {
			
			List<ChatMain> lineList = getChatRoomLine(roomKey, dto.getReadLineKey());
			
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
				data.put("chatRoomLine", list.toString());
				data.put("chatRoomKey", roomKey);
			}
		}
		
		resultJson.put("type", CommonConst.RESPONSE_TYPE_SUCCESS);
		resultJson.put("data", data);
		
		return resultJson;
	}


	@Override
	public JSONObject getRecvUser(ChatRoomRecvDTO dto) {
		JSONObject resultJson = new JSONObject();
		JSONObject data = new JSONObject();
		
		String roomKey = dto.getRoomKey();
		String userId = dto.getUserId();
		
		if(roomKey != null && !roomKey.isEmpty()) {
			
			List<EarlyUser> recvUser = earlyUserRepository.findByRoomKeyAndUser(roomKey, userId);
			if(recvUser != null && recvUser.size() > 0) {
				data.put("recv_user", recvUser);
			}else {
				data.put("recv_user", "");
			}
			resultJson.put(CommonConst.RESPONSE_DATA, data);
			resultJson.put("type", CommonConst.RESPONSE_TYPE_SUCCESS);
		}else {
			data.put(CommonConst.RESPONSE_DATA_ERROR_MSG, CommonConst.INVALID_BODY_DATA);
			resultJson.put("type", CommonConst.RESPONSE_TYPE_FAIL);
			resultJson.put(CommonConst.RESPONSE_DATA, data);
		}
		
		return resultJson;
	}

	/* rabbitmq 발송 로직 */
	@Autowired
	RabbitmqConfig config;

	@Override
	public void sendMessageDeployment(JSONObject sendData) {
		String queueName = config.getQueueName();
		
		// 수신 측에서 보낸 pod와 구분처리 하기 위한 값
		sendData.put(CommonConst.QUEUE_NAME, queueName);
		
		log.info("[rabbitmq sendMessage] send data : {}", sendData.toJSONString());
		
		rabbitTemplate.convertAndSend("fanout.exchange", "", sendData.toJSONString());
		
	}


	@Override
	public void recvMessageDeployment(String msg, SimpMessagingTemplate simpMessagingTemplate) {
		
		log.info("[rabbitmq receiveMessage] recv data : {} ", msg);
	
		if(msg != null && !msg.isEmpty()) {
			
			JSONParser parser = new JSONParser();
			
			try {
				JSONObject recvJson = (JSONObject) parser.parse(msg);
				
				if(recvJson != null) {
					String sendQueueName = (String) recvJson.get(CommonConst.QUEUE_NAME);
					String chatRoomKey = (String) recvJson.get(CommonConst.CAHT_ROOM_KEY);
					String chatLineKey = (String) recvJson.get(CommonConst.CHAT_LINE_KEY);						
					String chatSender = (String) recvJson.get(CommonConst.CHAT_SENDER);
					String chatReceiver = (String) recvJson.get(CommonConst.CHAT_RECEIVER);
					
					//queuename 비교
					String queueName = config.getQueueName();
					if(sendQueueName != null && !sendQueueName.isEmpty()) {
						
						if(!sendQueueName.equals(queueName)) {
							// 다른 pod에서 보낸 데이터
							String dest = "/topic/room/"+chatRoomKey;
							
							if(recvJson.containsKey(CommonConst.QUEUE_NAME)) {
								recvJson.remove(CommonConst.QUEUE_NAME);
							}
							simpMessagingTemplate.convertAndSend(dest, recvJson.toJSONString());
							log.info("[rabbitmq receiveMessage] sending chatData ws subscribe dest : {} end!", dest);
							
							// redis 건수 전송
							sendUnreadChatCount(simpMessagingTemplate, chatRoomKey, chatReceiver, chatSender, chatLineKey);
							log.info("[rabbitmq receiveMessage] sending unreadData ws subscribe dest : {} end!", dest);
						}else {
							// 같은 파드에서 보낸 데이터 처리하지 않음. 
							return;
						}
					}
				}
				
			} catch (ParseException e) {
				log.error(e.getMessage());
			}
			
		}else {
			log.info("[rabbitmq receiveMessage] recv msg is invalid from other pods ! msg : {}", msg);
			return;
		}
		
	}


	@Override
	public void sendMessageWs(SimpMessagingTemplate simpMessagingTemplate, JSONObject sendData, String roomKey) {
		
		// 보낼 경로 설정
		String dest = "/topic/room/"+roomKey;
		
		log.info("[sendMessageWs - chatData] dest : " + dest);
		simpMessagingTemplate.convertAndSend(dest, sendData.toJSONString());
		
	}
	
	


}
