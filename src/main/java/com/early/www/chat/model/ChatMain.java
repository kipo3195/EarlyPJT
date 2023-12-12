package com.early.www.chat.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;

import lombok.Data;

// 채팅의 라인을 관리 
@Data
@Entity
@Table(name="tbl_chat_main")
public class ChatMain{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="chat_seq")
	private long chatSeq;
	
	@Column(name="chat_line_key", nullable=false, length=255)
	private String chatLineKey;
	
	@Column(name="chat_room_key", nullable=false, length=255)
	private String chatRoomKey; 
	
	@Column(name="chat_sender", nullable=false, length=100)
	private String chatSender;
	
	@Column(name="chat_title", nullable=false, length=500)
	private String chatTitle;
	
	@Column(name="chat_encrypt_key", nullable=false, length=255)
	private String chatEncryptKey;
	
	@Column(name="chat_contents", nullable=false, length=4000)
	private String chatContents;

	@Column(name="chat_type", nullable=false, length=3)
	private String chatType;
	
	@Column(name="chat_send_date", nullable=false, length=20)
	private String sendDate;
	
	@Column(name="chat_del_flag", length=1)
	@ColumnDefault("'N'")
	private String chatDelFlag;
	
	
}
