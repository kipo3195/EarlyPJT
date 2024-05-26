package com.early.www.file.DTO;

import org.springframework.core.io.Resource;

import lombok.Data;

@Data
public class FileResourceDTO {

	private String fileHash;
	private Resource resource;
	
}
