package com.early.www.common.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.early.www.repository.CommonRepository;
import com.early.www.user.model.EarlyUser;


@Service
public class CommonServiceImpl implements CommonService {

	@Autowired
	CommonRepository commonRepository;
	
	@Autowired
	BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	RedisTemplate<String, Object> redisTemplate;
	
	@Override
	public String userJoin(EarlyUser user) {
		String result = "fail";
		System.out.println("CommonServiceImpl 회원가입 요청 ! 사용자 정보 : "+user);
		EarlyUser earlyUser = null;
		// 회원가입 여부 체크 = 동일한 사용자 
		EarlyUser joinCheck = commonRepository.findByusername(user.getUsername());
		if(joinCheck != null) {
			// 이미 가입된 회원 
		}else {
			String encodedPw = passwordEncoder.encode(user.getPassword());
			user.setPassword(encodedPw);
			user.setRoles("ROLE_USER");
			
			// save메소드는 결과값으로 저장한 객체를 반환함. 
			earlyUser = commonRepository.save(user);
			if(earlyUser != null) {
				result = "success";
			}
		}
		
		
		return result;
	}

	@Override
	public boolean existsUsername(String username) {
		
		EarlyUser earlyUser = commonRepository.findByusername(username);
		
		if(earlyUser != null) {
			return true;
		}else {
			return false;
		}
	}

	@Override
	public boolean login(String username, String password) {
		
		EarlyUser earlyUser = commonRepository.findByusername(username);
		
		System.out.println(earlyUser);
		
		if(earlyUser != null ) {
			if(passwordEncoder.matches(password, earlyUser.getPassword())) {
				return true; // 입력한 비밀번호와 저장소의 비밀번호가 일치
			} else {
				return false;
			}	
		}else {
			return false;
		}
	}
	
	@Override
	public void testRedisSet(String data) {

		System.out.println("1 Commonservice testRedisSet data : " + data);
		
		long result = redisTemplate.opsForSet().add("test@naver.com|roomkey1", data);
		
		System.out.println("2 Commonservice testRedisSet data : " + data + ", result : " + result);
		
		long unreadCnt = redisTemplate.opsForSet().size("test@naver.com|roomkey1");
		
		System.out.println("3 Commonservice testRedisSet data : " + data + ", unreadCnt : " + unreadCnt);
		
	
		
	}

	@Override
	public void testRedisGet(String data) {

		System.out.println("Commonservice testRedisGet data : " + data);
		
		Set<Object> values = redisTemplate.opsForSet().members("bse3808@gmail.com|R_231212225204942");		

		for(int i = 0 ; i < values.size(); i++) {
			redisTemplate.opsForSet().pop("bse3808@gmail.com|R_231212225204942");
		}
		
		
		long result = redisTemplate.opsForSet().size("bse3808@gmail.com|R_231212225204942");		
		
		System.out.println("Commonservice testRedisGet result : " + result);
	}


}

