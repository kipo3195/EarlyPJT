package com.early.www.user.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	
	// 롤이 여러개이며 구분하기 위해서 
	public List<String> getRoleList(){
		if(this.roles.length() > 0) {
			return Arrays.asList(this.roles.split(","));
		}
		return new ArrayList<>();
	}
	
}
