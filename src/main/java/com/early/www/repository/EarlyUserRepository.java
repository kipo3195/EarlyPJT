package com.early.www.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.early.www.user.model.EarlyUser;

public interface EarlyUserRepository extends JpaRepository<EarlyUser, Long>{
	

	@Query(nativeQuery=true, value="select id, username, name, '' as password, '' as phoneNumber, '' as birthDay, '' as roles from earlyuser as e left join tbl_chat_list as r on e.username = r.chat_list_user where r.chat_room_key = :chatRoomKey "
			+ " order by name asc limit :min, 20")
	List<EarlyUser> findByChatRoomKeyAndMin(String chatRoomKey, int min);
	
	

}
