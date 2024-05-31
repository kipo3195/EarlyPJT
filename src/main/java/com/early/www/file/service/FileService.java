package com.early.www.file.service;

import java.io.File;

import org.springframework.core.io.Resource;

import com.early.www.file.DTO.FileDTO;
import com.early.www.file.model.FileEntity;

public interface FileService {

	public FileEntity putFile(FileDTO dto);

	public String getFileHash();

	public File getFile(String dto);

}
