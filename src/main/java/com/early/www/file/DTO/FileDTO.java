package com.early.www.file.DTO;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class FileDTO {
	
	MultipartFile fileData;
	private String senderId;
	private String fileHash;
	
}
