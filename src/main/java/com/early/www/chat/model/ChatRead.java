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
@Table(name="tbl_chat_read")
public class ChatRead {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="chat_read_seq")
	private long chatReadSeq;
	
	@Column(name="chat_line_key", nullable=false, length=255)
	private String chatLineKey;
	
	@Column(name="chat_room_key", nullable=false, length=255)
	private String chatRoomKey;
	
	@Column(name="chat_reader", nullable=false, length=100)
	private String chatReader;
	
	@Column(name="chat_read_date", nullable=false, length=20)
	private String readDate;
	
	
}
