package com.early.www.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.early.www.chat.model.ChatMain;

public interface ChatMainRepository extends JpaRepository<ChatMain, Long>{

	@Query(nativeQuery = true, value = "select * from ("
			+ "select * from tbl_chat_main where chat_room_key = :chatRoomKey and chat_type !='O' order by chat_seq desc limit 0, 20) as aa order by chat_seq asc")
	List<ChatMain> findByChatRoomKey(String chatRoomKey);
	
	@Query(nativeQuery = true, value = "select * from ("
			+ "select * from tbl_chat_main where chat_room_key = :chatRoomKey and chat_type !='O' and chat_line_key < :lineKey order by chat_seq desc limit 0, 20) as aa order by chat_seq asc")
	List<ChatMain> findByChatRoomKeyAndLineKey(String chatRoomKey, String lineKey);

}
