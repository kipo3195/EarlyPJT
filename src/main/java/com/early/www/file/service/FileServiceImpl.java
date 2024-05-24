package com.early.www.file.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.early.www.file.DTO.FileDTO;
import com.early.www.file.model.FileEntity;
import com.early.www.repository.FileRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileServiceImpl implements FileService {
	
	@Autowired
	FileRepository fileRepository;
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	
	interface MakeString {
	    String execute();
	}
	
	@Override
	@Transactional
	public FileEntity putFile(FileDTO dto) {
		// 이하 파일 저장 로직
		
		MultipartFile file = dto.getFileData();
		
		// 서버 경로에 파일 생성 후 절대경로 찾기 
	    File projectDir = new File("");
	    String projectPath = projectDir.getAbsolutePath();
	    String yyyyMMdd = yyyymmdd();
	    
	    MakeString makePath = () -> projectPath+"/"+yyyyMMdd;
	    
	    // 날짜 경로 생성 체크
	    boolean makeDirFlag = createDirectoryIfNotExists(makePath.execute());
	    
	    File tempFile = null;
	    String newFileName = null;
	    if(makeDirFlag) {
    	    try {
    	    	String contentType = file.getContentType();
    	    	if(contentType.startsWith("image")) {
    	    		contentType = "jpg";
    	    	}
    	    	MakeString makeNewFileName = () -> "Early_"+yyyyMMdd+"_"+dto.getFileHash();
    	    	
    	    	newFileName = makeNewFileName.execute()+"."+contentType;
    	    	
    	    	tempFile = new File(projectPath+"/"+yyyyMMdd+"/"+newFileName);
		    	// 파일을 주어진 목적지 파일로 이동시키는 메서드를 통해 파일 업로드.
				file.transferTo(tempFile);
			} catch (IllegalStateException | IOException e) {
				log.error("Exception e : {}", e);
				return null;
			}
	    }
	    
	    FileEntity result = null;
	    
	    if(tempFile != null) {
	    	
	    	FileEntity temp = new FileEntity();
	    	
	    	temp.setFileName(newFileName);
	    	temp.setFileUploader(dto.getSenderId());
	    	temp.setFileSize(file.getSize());
	    	temp.setFileUploadDate(new Date());
	    	temp.setFileHash(dto.getFileHash());
	    	temp.setFileDir(tempFile.getPath());
	    	
	    	// DB 저장 
	    	result = fileRepository.save(temp);
	    }
	    
		return result;
	}
	
	
	public String yyyymmdd() {
		return sdf.format(new Date());
	}
	
	private boolean createDirectoryIfNotExists(String directoryPath) {
		boolean result = false;
		
		File directory = new File(directoryPath);
		        
        // 디렉터리 경로가 유효한지 확인
        if (directoryPath!= null &&!directoryPath.isEmpty()) {
            // 디렉터리가 존재하지 않으면 생성
            if (!directory.exists()) {
                boolean success = directory.mkdirs();
                if (success) {
                    System.out.println("디렉터리 '" + directoryPath + "'가 성공적으로 생성되었습니다.");
                    result = true;
                } else {
                    System.out.println("디렉터리 '" + directoryPath + "'의 생성에 실패했습니다.");
                }
            } else {
                System.out.println("디렉터리 '" + directoryPath + "'는 이미 존재합니다.");
                result = true;
            }
        } else {
            System.out.println("디렉터리 경로는 유효하지 않습니다.");
        }
        return result;
	}

	
}
