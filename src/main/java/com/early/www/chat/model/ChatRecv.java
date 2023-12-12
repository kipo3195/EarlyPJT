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
@Table(name="tbl_chat_recv")
public class ChatRecv {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="chat_recv_seq")
	private long chatRecvSeq;
	
	@Column(name="chat_line_key", nullable=false, length=255)
	private String chatLineKey;
	
	@Column(name="chat_room_key", nullable=false, length=255)
	private String chatRoomKey;
	
	@Column(name="chat_receiver", nullable=false, length=100)
	private String chatReceiver;
	
	@Column(name="chat_recv_date", nullable=false, length=20)
	private String recvDate;
	
}
