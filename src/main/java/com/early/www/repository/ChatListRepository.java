package com.early.www.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.early.www.chat.model.ChatList;

public interface ChatListRepository extends JpaRepository<ChatList, Long>{
	
	

}
