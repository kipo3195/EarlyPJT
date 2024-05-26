package com.early.www.file.controller;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.early.www.file.DTO.FileDTO;
import com.early.www.file.DTO.FileHashDTO;
import com.early.www.file.model.FileEntity;
import com.early.www.file.service.FileService;
import com.early.www.util.CommonConst;
import com.early.www.util.CommonRequestCheck;

@RestController
@RequestMapping(value = "/file")
public class FileController {
	
	@Autowired
	CommonRequestCheck commonRequestCheck;
	
	@Autowired
	FileService fileService;
	
	// 파일 hash 발급 로직 error check 하지않음 (빈번하게 발생함) = 라인키 발급로직과 동일함. 
	@GetMapping("/getFileHash")
	public Map<String, String> getLineKey(HttpServletRequest request, HttpServletResponse response){
		Map<String, String> resultMap = new HashMap<String, String>();
		
		String error = (String) response.getHeader("error_code");
		if(error != null) {
			resultMap.put("flag", "fail");
			resultMap.put("error_code", response.getHeader("error_code"));
		}else {
			String lineKey = fileService.getFileHash();
			resultMap.put("fileHash", lineKey);
		}
		
		return resultMap;
	}
	

//	http는 기본적으로 텍스트 데이터를 전송하는 기반의 프로토콜이기 때문에 application/json header 설정
//	으로 텍스트 데이터를 주고 받을 수 있다. 하지만 파일은 텍스트가 아닌 이진 데이터.
//	이진 데이터(바이너리)를 http로 보내려면 파일(이진데이터)를 텍스트로 변환하여야 한다. 하지만 이러한 작업은
//	비효율적이다. 그래서 http의 header의 속성에 multipart로 변경하고 데이터를 보내게 되면 이진데이터
//	자체를 전송 할 수 있다. 이때 파일을 키-밸류 형태의 formData객체에 담아서 보내고 서버에서는 멀티파트로
//	전달된 파일데이터를 파싱 할 수 있는 라이브러리가 필요한데, 스프링부트에서는 MultipartFile 인터페이스가 존재
//	하여 처리가 가능하다. 
	@PostMapping("/upload")
	public JSONObject fileUpload(HttpServletRequest request, HttpServletResponse response, FileDTO dto) {
		
		JSONObject resultJson = new JSONObject();
		boolean errorCheck = commonRequestCheck.errorCheck(request, response, dto);
		if(errorCheck) {
			resultJson.put(CommonConst.RESPONSE_TYPE, CommonConst.RESPONSE_TYPE_FAIL);
			resultJson.put(CommonConst.RESPONSE_DATA_ERROR_MSG, response.getHeader("error_code"));
		}else {
			// 트랜잭션 처리하기. 파일저장과 DB처리를 동일하게 함. 
			FileEntity result = fileService.putFile(dto);
			if(result != null) {
				resultJson.put(CommonConst.RESPONSE_TYPE, CommonConst.RESPONSE_TYPE_SUCCESS);
				resultJson.put(CommonConst.RESPONSE_DATA, CommonConst.FILE_SAVED);
			}else {
				resultJson.put(CommonConst.RESPONSE_TYPE, CommonConst.RESPONSE_TYPE_FAIL);
				resultJson.put(CommonConst.RESPONSE_DATA_ERROR_MSG, CommonConst.FILE_NOT_SAVED);
			}
		}
		
		return resultJson;
	}
	
	@PostMapping("/imageLoad")
	public ResponseEntity<Object> imageLoad(HttpServletRequest request, HttpServletResponse response, FileHashDTO dto) {
		boolean errorCheck = commonRequestCheck.errorCheck(request, response, dto);
		
		JSONObject resultJson = new JSONObject();
		Resource result = null;
		if(errorCheck) {
			resultJson.put(CommonConst.RESPONSE_TYPE, CommonConst.RESPONSE_TYPE_FAIL);
			resultJson.put(CommonConst.RESPONSE_DATA_ERROR_MSG, response.getHeader("error_code"));
			// 에러 처리 필요. 
		}else {
			result = fileService.getImage(dto.getFileHash());
			
			resultJson.put(CommonConst.RESPONSE_TYPE, CommonConst.RESPONSE_TYPE_SUCCESS);
			resultJson.put(CommonConst.RESPONSE_DATA, result);
			try {
				System.out.println(result.getURI());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			return ResponseEntity.ok().contentType(MediaType.MULTIPART_FORM_DATA).body(Base64.getEncoder().encode(result.getInputStream().readAllBytes()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		return null;
	}

}
