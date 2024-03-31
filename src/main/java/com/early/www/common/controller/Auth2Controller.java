package com.early.www.common.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.early.www.common.service.OauthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/auth")
public class Auth2Controller {
	
    private final OauthService oauthService;
	
	@GetMapping(value = "/{socialLoginType}")
    public void socialLoginType(@PathVariable String socialLoginType) {
		log.info("Social Login request ! type : {}", socialLoginType);
        oauthService.request(socialLoginType);
    }
	
	@GetMapping(value = "/{socialLoginType}/callback")
    public void callback(
            @PathVariable(name = "socialLoginType") String socialLoginType,
            @RequestParam(name = "code") String code) {
        log.info(">> 소셜 로그인 API 서버로부터 받은 code :: {}", code);
        
        oauthService.requestAccessToken(socialLoginType, code);
        
    }
	
}
