package com.early.www.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.early.www.user.model.RefreshToken;

public interface TokenRepository extends JpaRepository<RefreshToken, Long>{

	// 얘는 기존에 지원함.. 
	//public int save(String username, long createTime, String refreshToken);
	
	public RefreshToken findByRefreshTokenAndCreateTime(String refreshToken, String createTime);
	
}
