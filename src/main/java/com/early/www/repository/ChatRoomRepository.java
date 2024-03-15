package com.early.www.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.early.www.chat.model.ChatList;
import com.early.www.chat.model.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>{

	// 테이블 기준으로 컬럼의 snake 표기법을 camel로 변경해야함. 매개변수는 상관없음.
	// 단 nativeQuery를 사용하면 일반 쿼리와 동일하게 처리 할 수 있음.  
	@Query(nativeQuery = true, value ="select * from tbl_chat_list list inner join tbl_chat_room room on list.chat_room_key = room.chat_room_key "
			+ " where list.chat_list_user = :username order by room.last_line_key desc limit 0, 10")
	public List<ChatRoom> findByChatListUser(String username);

	@Query(nativeQuery = true, value = "update tbl_chat_room set last_line_key = :lineKey where chat_room_key = :roomKey")
	public void save(String roomKey, String lineKey);


	
	
}
