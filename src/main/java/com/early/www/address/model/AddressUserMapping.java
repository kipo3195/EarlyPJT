package com.early.www.address.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="tbl_addr_user_mapping")
@IdClass(AddressUserMappingKey.class)
public class AddressUserMapping {

	@Id
	@Column(name="my_id", columnDefinition = "varchar(100) comment '내 id'")
	private String myId;
	
	@Id
	@Column(name="friend_id", columnDefinition = "varchar(100) comment '친구 id'")
	private String friendId;
	
	@Column(name="del_flag", columnDefinition = "varchar(1) default 'N'")
	private String flag;
	
}
