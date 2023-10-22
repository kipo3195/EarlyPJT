package com.early.www.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.early.www.user.model.EarlyUser;

public interface CommonRepository  extends JpaRepository<EarlyUser, Long>{

	public EarlyUser findByusername(String username);

}
