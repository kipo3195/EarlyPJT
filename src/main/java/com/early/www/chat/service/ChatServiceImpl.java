package com.early.www.chat.service;

import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import com.early.www.chat.VO.ChatReadVO;
import com.early.www.chat.model.ChatMain;
import com.early.www.chat.model.ChatRoom;
import com.early.www.repository.ChatMainRepository;
import com.early.www.repository.ChatRoomRepository;

@Service
public class ChatServiceImpl implements ChatService {

	@Autowired
	ChatRoomRepository chatRoomRepository;
	
	@Autowired
	ChatMainRepository chatMainRepository;
	
	@Autowired
	RedisTemplate<String, Object> redisTemplate;
	
	// 람다는 파라미터로 사용하는 변수와 로컬 변수를 구분을 하지 못하기 때문에 클래스 변수로 잡아줌
	double startLine = 0;
	
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
		
		main.setChatLineKey(main.getChatLineKey());
		main.setSendDate(main.getChatLineKey());
		main.setChatDelFlag("N");
		System.out.println("[ChatSericeImpl] main : " + main);
		
		chatMainRepository.save(main);
	}

	// 채팅방 데이터 조회 (최초)
	@Override
	public List<ChatMain> getChatRoomLine(String chatRoomKey) {
		
		// 라인 조회 DB
		List<ChatMain> chatMainList = chatMainRepository.findByChatRoomKey(chatRoomKey);
		
		RedisSerializer keySerializer = redisTemplate.getKeySerializer();
		redisTemplate.execute(new RedisCallback<Object>() {

			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				
				// 라인의 미확인 건수 추가
				for(int i = 0; i < chatMainList.size(); i++) {
					String key = "unreadLineUsers:"+chatRoomKey+":"+chatMainList.get(i).getChatLineKey();
					Long unreadCount = connection.sCard(keySerializer.serialize(key));
					chatMainList.get(i).setChatUnreadCount(String.valueOf(unreadCount));
				}
				
				if(connection != null) {
					connection.close();
				}
				return null;
			}
			
		});
		
		return chatMainList;
	}

	// 채팅방 데이터 조회 (최초 이후)
	@Override
	public List<ChatMain> getChatRoomLineAppend(String chatRoomKey, String lineKey) {
		// 라인의 미확인 건수 추가
		List<ChatMain> chatMainList = chatMainRepository.findByChatRoomKeyAndLineKey(chatRoomKey, lineKey);
	
		RedisSerializer keySerializer = redisTemplate.getKeySerializer();
		redisTemplate.execute(new RedisCallback<Object>() {

			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				
				// 라인의 미확인 건수 추가
				for(int i = 0; i < chatMainList.size(); i++) {
					String key = "unreadLineUsers:"+chatRoomKey+":"+chatMainList.get(i).getChatLineKey();
					Long unreadCount = connection.sCard(keySerializer.serialize(key));
					chatMainList.get(i).setChatUnreadCount(String.valueOf(unreadCount));
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
	public Map<String, JSONObject> getUnreadChatCount(String roomKey, String receiver, String sender, String lineKey) {
		
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
		
		return map;
		
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

	// 채팅 입장시 해당 채팅방 데이터 읽음처리
	@Override
	public Map<String, String> putChatRoomUnread(String roomKey, String username, String startLineKey) {
		
		// 클라이언트로 전달할 데이터 
		 Map<String, String> map = new HashMap<String, String>();
		
		if(!startLineKey.equals("0")) {
			startLine = Double.valueOf(startLineKey);
		}
		
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

				Object[] arr = myUnreadLineSet.toArray();
				for(int i = 0; i < arr.length; i++) {
					
					// 나의 미확인 건수 삭제 
					String unreadLine = (String) valueSerializer.deserialize((byte[]) arr[i]);
					key = "myUnreadLine:"+roomKey+":"+username;
					connection.zRem(keySerializer.serialize(key), valueSerializer.serialize(unreadLine));
					// System.out.println("나의 미확인 건수 삭제 key: " + key+", unreadLine : "+unreadLine);
					
					// 해당 라인의 미확인 사용자 중 나 삭제
					key = "unreadLineUsers:"+roomKey+":"+unreadLine;
					connection.sRem(keySerializer.serialize(key), keySerializer.serialize(username));
					
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
				
				if(connection != null) {
					connection.close();
				}
				return null;
			}
		});
		
		return map;
		
	}

	// 읽음 요청 
	@Override
	public Map<String, Object> getReadSuccessLines(String roomKey, String username, String startLineKey) {
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

				Object[] arr = myUnreadLineSet.toArray();
				for(int i = 0; i < arr.length; i++) {
					
					// 나의 미확인 건수 삭제 
					String unreadLine = (String) valueSerializer.deserialize((byte[]) arr[i]);
					key = "myUnreadLine:"+roomKey+":"+username;
					connection.zRem(keySerializer.serialize(key), valueSerializer.serialize(unreadLine));
					// System.out.println("나의 미확인 건수 삭제 key: " + key+", unreadLine : "+unreadLine);
					
					// 해당 라인의 미확인 사용자 중 나 삭제
					key = "unreadLineUsers:"+roomKey+":"+unreadLine;
					connection.sRem(keySerializer.serialize(key), keySerializer.serialize(username));
					
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
				map.put("result", json);
				if(connection != null) {
					connection.close();
				}
				return null;
			}
		});
		
		return map;
	}



}
