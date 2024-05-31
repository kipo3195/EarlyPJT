package com.early.www.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.early.www.chat.model.ChatMain;

public interface ChatMainRepository extends JpaRepository<ChatMain, Long>{

//	@Query(nativeQuery = true, value = "select chat_seq, chat_contents, chat_del_flag, chat_encrypt_key, chat_line_key, chat_receiver,"
//			+ " chat_room_key, chat_sender, chat_title, chat_type, chat_send_date, chat_unread_count, chat_check_cnt, chat_good_cnt, chat_like_cnt,  name as chat_sender_name "
//			+ " from ("
//			+ " select a.*, e.name from tbl_chat_main as a join earlyuser e on a.chat_sender = e.username  where chat_room_key = :chatRoomKey and chat_type !='O' and chat_send_date < :readLineKey order by chat_seq desc limit 0, 20) as aa order by chat_seq asc")
	
	//20240526 채팅라인 + 사용자 + 파일 (  chatType이 I면 이미지, F면 파일, C면 일반 채팅
	@Query(nativeQuery = true, value = "select \r\n"
			+ "	chat_seq, \r\n"
			+ "	case when chat_type = 'C' then chat_contents when chat_type = 'F' then f.original_file_name else f.file_url end chat_contents,\r\n"
			+ "	chat_del_flag, chat_encrypt_key, chat_line_key, chat_receiver, chat_room_key,\r\n"
			+ "	chat_sender, chat_title, chat_type, chat_send_date, chat_unread_count, chat_check_cnt, chat_good_cnt, chat_like_cnt, name as chat_sender_name \r\n"
			+ " from (\r\n"
			+ " 	select a.*, e.name from tbl_chat_main as a join earlyuser e on a.chat_sender = e.username\r\n"
			+ " 	where chat_room_key = :chatRoomKey and chat_type !='O' and chat_line_key < :readLineKey order by chat_seq desc limit 0, 20) as aa\r\n"
			+ " left join tbl_file_info as f on aa.chat_line_key = f.file_hash order by chat_seq asc")
	List<ChatMain> findByChatRoomKey(String chatRoomKey, String readLineKey);
	
	
	
//	@Query(nativeQuery = true, value = "select chat_seq, chat_contents, chat_del_flag, chat_encrypt_key, chat_line_key, chat_receiver,"
//			+ " from ("
//			+ " chat_room_key, chat_sender, chat_title, chat_type, chat_send_date, chat_unread_count, chat_check_cnt, chat_good_cnt, chat_like_cnt,  name as chat_sender_name "
//			+ " select a.*, e.name from tbl_chat_main as a join earlyuser e on a.chat_sender = e.username where chat_room_key = :chatRoomKey and chat_type !='O' and chat_line_key < :lineKey order by chat_seq desc limit 0, 20) as aa order by chat_seq asc")

	@Query(nativeQuery = true, value="select "
			+ "	chat_seq, "
			+ "	case when chat_type = 'C' then chat_contents when chat_type = 'F' then f.original_file_name else f.file_url end chat_contents,"
			+ "	chat_del_flag, chat_encrypt_key, chat_line_key, chat_receiver, chat_room_key,"
			+ "	chat_sender, chat_title, chat_type, chat_send_date, chat_unread_count, chat_check_cnt, chat_good_cnt, chat_like_cnt, name as chat_sender_name "
			+ " from ("
			+ " 	select a.*, e.name from tbl_chat_main as a join earlyuser e on a.chat_sender = e.username"
			+ " 	where chat_room_key = :chatRoomKey and chat_type !='O' and chat_line_key < :lineKey order by chat_seq desc limit 0, 20) as aa"
			+ " left join tbl_file_info as f on aa.chat_line_key = f.file_hash order by chat_seq asc")
	List<ChatMain> findByChatRoomKeyAndLineKey(String chatRoomKey, String lineKey);
	
	

}
