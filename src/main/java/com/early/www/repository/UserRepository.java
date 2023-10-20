package com.early.www.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.early.www.user.model.EarlyUser;

public interface UserRepository  extends JpaRepository<EarlyUser, Long>{

	
}
