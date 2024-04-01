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
	
	@GetMapping(value = "/{socialLoginType}/{clientType}")
    public void socialLoginType(@PathVariable String socialLoginType, @PathVariable String clientType) {
		log.info("Social Login request ! socialLoginType : {}, clientType : {}", socialLoginType, clientType);
        oauthService.request(socialLoginType, clientType);
    }
	
	@GetMapping(value = "/{socialLoginType}/callback/web")
    public void callbackWeb(
            @PathVariable(name = "socialLoginType") String socialLoginType,
            @RequestParam(name = "code") String code) {
		
        log.info(">> web 소셜 로그인 API 서버로부터 받은 code :: {}", code);
        
        String token = oauthService.requestAccessToken(socialLoginType, code, "web");
        if(token != null) {
        	oauthService.getUserInfo(socialLoginType, token, "web");
        }else {
        	oauthService.response();
        }
    }
	
	@GetMapping(value = "/{socialLoginType}/callback/ios")
    public void callbackIos(
            @PathVariable(name = "socialLoginType") String socialLoginType,
            @RequestParam(name = "code") String code) {
		
        log.info(">> ios 소셜 로그인 API 서버로부터 받은 code :: {}", code);
        
        String token = oauthService.requestAccessToken(socialLoginType, code, "ios");
        if(token != null) {
        	oauthService.getUserInfo(socialLoginType, token, "ios");
        }else {
        	oauthService.response();
        }
    }
	
	@GetMapping(value = "/{socialLoginType}/callback/aos")
    public void callbackAos(
            @PathVariable(name = "socialLoginType") String socialLoginType,
            @RequestParam(name = "code") String code) {
		
        log.info(">> aos 소셜 로그인 API 서버로부터 받은 code :: {}", code);
        
        String token = oauthService.requestAccessToken(socialLoginType, code, "aos");
        if(token != null) {
        	oauthService.getUserInfo(socialLoginType, token, "aos");
        }else {
        	oauthService.response();
        }
    }
	
	
}
