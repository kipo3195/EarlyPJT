package com.early.www.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.early.www.chat.model.ChatList;

public interface ChatListRepository extends JpaRepository<ChatList, Long>{
	
}
