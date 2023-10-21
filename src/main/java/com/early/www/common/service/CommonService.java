package com.early.www.common.service;

import com.early.www.user.model.EarlyUser;

public interface CommonService {
	
	public abstract void userJoin(EarlyUser user);
	
	public abstract boolean existsUsername(String userId);

}
