package com.early.www.chat.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
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
	
	@Autowired
	RedisTemplate<String, Object> redisTemplate;
	
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
				String room = username +"|"+chatList.get(i).getChatRoomKey();
				// 읽지 않은 건수 조회 하여 chatRoom에 set 처리
				long roomCnt = redisTemplate.opsForSet().size(room);
				chatList.get(i).setUnreadCount(String.valueOf(roomCnt));
//				System.out.println(chatList.get(i));
			}
			
		}
		
		return chatList;
	}

	
	//채팅 라인 저장
	@Override
	public String putChatMain(ChatMain main) {
		String lineKey = makeLineKey();
		main.setChatLineKey(lineKey);
		main.setSendDate(lineKey);
		main.setChatDelFlag("N");
		System.out.println("[ChatSericeImpl] main : " + main);
		chatMainRepository.save(main);
		return lineKey;
	}

	// 채팅방 데이터 조회 (최초)
	@Override
	public List<ChatMain> getChatRoomLine(String chatRoomKey) {
		
		List<ChatMain> chatMainList = chatMainRepository.findByChatRoomKey(chatRoomKey);
		
		RedisSerializer keySerializer = redisTemplate.getKeySerializer();
		redisTemplate.execute(new RedisCallback<Object>() {

			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				
				// 라인의 미확인 건수 추가
				for(int i = 0; i < chatMainList.size(); i++) {
					String line = chatRoomKey+"|"+chatMainList.get(i).getChatLineKey();
					Long unreadCount = connection.sCard(keySerializer.serialize(line));
					chatMainList.get(i).setChatUnreadCount(String.valueOf(unreadCount));
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
		for(int i = 0; i < chatMainList.size(); i++) {
			String line = chatRoomKey+"|"+chatMainList.get(i).getChatLineKey();
			long unreadCount = redisTemplate.opsForSet().size(line);
			chatMainList.get(i).setChatUnreadCount(String.valueOf(unreadCount));
		}
		
		return chatMainList;
	}


	// 채팅 발송시 unreadcount 전달 
	@Override
	public Map<String, JSONObject> putChatUnreadCnt(String roomKey, String receiver, String sender, String lineKey) {
		
		// 수신자 파싱
		String[] receivers = receiver.split("[|]");
		
		// 사용자 : {}
		Map<String, JSONObject> map = new HashMap<>();
		
		for(int i = 0; i < receivers.length; i++) {
			// 발신자는 빼고 
			if(receivers[i].equals(sender)) {
				continue;
			}
			
			JSONObject unreadJson = new JSONObject();
			
			// 20240107
			
			// 수신자의 해당 룸의 라인 별 저장 -> 해당 라인의 읽지않은 사용자 수 구할때 사용 
			String line = roomKey+"|"+lineKey;
			redisTemplate.opsForSet().add(line, receivers[i]);
			// cf. 라인 읽음 처리는 라인 조회시마다 아래 처리
			// redisTemplate.opsForSet().remove("roomKey|linekey", 읽은 사람 ID);		
			// long result = redisTemplate.opsForSet().size("roomKey|linekey");
			// 조회시 DB에서 linekey 뽑아서 읽음 처리 후 result(건수) 를 라인 객체에 넣어줘야함. 
			// 건수만 표시하고 누가 안읽었는지는 표시하지 않는다. 

			
			// 수신자의 특정방 미확인 건수 저장 
			String room = receivers[i]+"|"+roomKey;
			redisTemplate.opsForSet().add(room, lineKey);
			
			// 수신자의 특정방 미확인 건수 가져오기 
			long roomCnt = redisTemplate.opsForSet().size(room);
			unreadJson.put("room", roomKey+"|"+roomCnt);

			
			// 수신자의 특정방의 미확인 건수 저장 -> 수신자의 전체 미확인 건수 구할때 사용 
			redisTemplate.opsForHash().put(receivers[i], roomKey, String.valueOf(roomCnt));
			
			// 수신자의 전체 읽지 않은 건수 구하기
			Map<Object, Object> userAllRooms = redisTemplate.opsForHash().entries(receivers[i]);
			Iterator<Object> iter = userAllRooms.keySet().iterator();
			int unreadCnt = 0;
			while(iter.hasNext()) {
				unreadCnt += Integer.parseInt((String) userAllRooms.get((String) iter.next()));
			}
			unreadJson.put("chat", unreadCnt);
			unreadJson.put("type", "chat");
			
			// 결과 json { "type":"chat", "chat":"전체건수", "room":"신규 채팅 roomKey|미확인 건수"}
			map.put(receivers[i], unreadJson);
		}
		
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

	// 해당 라인의 미확인 건수 구함
	@Override
	public String getUnreadCount(String roomKey, String lineKey) {
		
		String result = "0";
		
		String line = roomKey+"|"+lineKey;
		long unreadCount = redisTemplate.opsForSet().size(line);
		
		result = String.valueOf(unreadCount);
		
		return result;
	}
	
	
	
	
	
	

}
