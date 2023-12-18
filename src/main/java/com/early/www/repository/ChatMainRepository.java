package com.early.www.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.early.www.chat.model.ChatMain;

public interface ChatMainRepository extends JpaRepository<ChatMain, Long>{

}
