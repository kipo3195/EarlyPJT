package com.early.www.chat.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="tbl_chat_room")
public class ChatRoom {

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="chat_room_seq")
	private long chatRoomSeq;
	
	@Column(name="chat_room_key", nullable=false, length=255)
	private String chatRoomKey; 
	
	// 채팅방의 이름이 없으면 참여자 리스트이다.
	@Column(name="chat_room_title", nullable=true, length=255)
	private String chatRoomTitle; 
	
	@Column(name="chat_room_users", nullable=false, length=4000)
	private String chatRoomUsers;
	
	@Column(name="create_room_date", nullable=false, length=20)
	private String sendDate;
	

}
