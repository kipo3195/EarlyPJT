package com.early.www.user.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class EarlyUser {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private String username;
	private String password;
	private String name;
	private String phoneNumber;
	private String birthDay;
	private String roles; // USER, ADMIN을 사용, Role 객체를 만들어도 됨.  

}
