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
@Table(name="tbl_chat_send")
public class ChatSend {
	  
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="chat_send_seq")
	private long chatSendSeq;
	
	@Column(name="chat_room_key", nullable=false, length=255)
	private String chatLineKey;
	
	@Column(name="chat_sender", nullable=false, length=100)
	private String chatSender;
	
	@Column(name="chat_send_date", nullable=false, length=20)
	private String sendDate;
	
	
}
