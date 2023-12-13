package com.early.www.chat.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;


// 내가 가진 채팅 리스트 조회
@Data
@Entity
@Table(name="tbl_chat_list")
public class ChatList {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="chat_list_seq")
	private long chatListSeq;
	
	@Column(name="chat_room_key", nullable=false, length=255)
	private String chatRoomKey; 
	
	// 내 ID
	@Column(name="chat_list_user", nullable=false, length=100)
	private String chatListUser;
	
}
