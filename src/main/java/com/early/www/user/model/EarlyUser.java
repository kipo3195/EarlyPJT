package com.early.www.user.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
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
	@Column(name="id")
	private long id;
	
	@Column(name="username")
	private String username;
	
	@Column(name="password")
	private String password;
	
	@Column(name="name")
	private String name;
	
	@Column(name="phoneNumber")
	private String phoneNumber;
	
	@Column(name="birthDay")
	private String birthDay;
	
	@Column(name="roles")
	private String roles; // USER, ADMIN을 사용, Role 객체를 만들어도 됨.  
	
	// 롤이 여러개이며 구분하기 위해서 
	public List<String> getRoleList(){
		if(this.roles.length() > 0) {
			return Arrays.asList(this.roles.split(","));
		}
		return new ArrayList<>();
	}
	
	//OAuth2.0 추가 
	@Column(name="provider")
	private String provider;
	
	@Column(name="providerId")
	private String providerId;
	
	// 사용자 프로필 추가
	@Column(columnDefinition = "varchar(255) default '' ")
	private String userProfile;
	

}
