package com.early.www.user.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class RefreshToken {

	@Id
	@GeneratedValue(strategy =  GenerationType.IDENTITY)
	private long id;
	private String username;
	private String createTime;
	private String refreshToken;
	
	
}
