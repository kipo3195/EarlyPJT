package com.early.www.file.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.early.www.file.DTO.FileDTO;

@RestController
@RequestMapping(value = "/file")
public class FileController {

	@PostMapping("/upload")
	public void fileUpload(HttpServletRequest request, HttpServletResponse response, FileDTO dto) {
		
		System.out.println(dto);
		// 서버경로에 파일 생성 후 절대경로 찾기 
	    File projectDir = new File("");
	    String projectPath = projectDir.getAbsolutePath();
	    
	    // 파일 저장할 temp 생성
	    File tempFile = new File(projectPath+"/upload/"+dto.getFileData().getOriginalFilename());
	    
	    // 파일 명은 hashcode로?
	    
	    // 실제 파일
	    MultipartFile file = dto.getFileData();
	    
	    try {
	    	// 파일을 주어진 목적지 파일로 이동시키는 메서드를 통해 파일 업로드.
			file.transferTo(tempFile);
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}


	}
	
}
