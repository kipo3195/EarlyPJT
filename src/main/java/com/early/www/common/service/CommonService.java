package com.early.www.common.service;

import com.early.www.user.model.EarlyUser;

public interface CommonService {
	
	// 회원가입 
	public abstract String userJoin(EarlyUser user);
	
	// id 중복체크 
	public abstract boolean existsUsername(String userId);

	// 로그인 
	public abstract boolean login(String username, String password);

}
