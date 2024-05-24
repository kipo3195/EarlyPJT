package com.early.www.file.service;


import com.early.www.file.DTO.FileDTO;
import com.early.www.file.model.FileEntity;

public interface FileService {

	public FileEntity putFile(FileDTO dto);

}
