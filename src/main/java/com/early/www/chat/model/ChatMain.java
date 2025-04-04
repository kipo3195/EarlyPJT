package com.early.www.chat.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;

import lombok.Data;

// 채팅의 라인을 관리 
@Data
@Entity
@Table(name="tbl_chat_main", indexes = @Index(name = "tbl_chat_main_idx_1", columnList = "chat_room_key, chat_type, chat_seq"))
public class ChatMain{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="chat_seq")
	private long chatSeq;
	
	@Column(name="chat_line_key", length=30)
	private String chatLineKey;
	
	@Column(name="chat_room_key",  length=255)
	private String chatRoomKey; 
	
	@Column(name="chat_sender", length=100)
	private String chatSender;
	
	@Column(name="chat_sender_name", length=100)
	private String chatSenderName;
	
	@Column(name="chat_receiver",  length=3000)
	private String chatReceiver;
	
	@Column(name="chat_title", length=500)
	private String chatTitle;
	
	@Column(name="chat_encrypt_key", length=255)
	private String chatEncryptKey;
	
	@Column(name="chat_contents", length=4000)
	private String chatContents;

	
	// chatType 20231227
	// C - CHAT 일반 채팅
	// F - FILE 파일
	// I - 이미지(캡쳐)
	// O - 채팅방 나가기
	@Column(name="chat_type", length=1)
	@ColumnDefault("'C'")
	private String chatType;
	
	@Column(name="chat_send_date", length=20)
	private String sendDate;
	
	@Column(name="chat_del_flag", length=1)
	@ColumnDefault("'N'")
	private String chatDelFlag;
	
	@Column(name="chat_unread_count", length=5)
	private String chatUnreadCount;
	
	@Column(name="chat_like_cnt", length=5)
	@ColumnDefault("'0'")
	private String chatLikeCnt;

	@Column(name="chat_check_cnt", length=5)
	@ColumnDefault("'0'")
	private String chatCheckCnt;

	@Column(name="chat_good_cnt", length=5)
	@ColumnDefault("'0'")
	private String chatGoodCnt;
	
	
	
	
}
