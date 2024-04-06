package com.early.www.common.service;

import com.early.www.user.model.EarlyUser;

public interface CommonService {
	
	// 회원가입 
	public abstract String userJoin(EarlyUser user);
	
	// OAuth2.0 회원가입 
	public abstract boolean userJoinOAuth(String userId, String name, String socialLoginType);
	
	// id 중복체크 
	public abstract boolean existsUsername(String userId);

	// 로그인 
	public abstract boolean login(String username, String password);

	// redis 테스트 - set
	public abstract void testRedisSet(String data);
	
	// redis 테스트 get
	public abstract void testRedisGet(String data);
	
}
