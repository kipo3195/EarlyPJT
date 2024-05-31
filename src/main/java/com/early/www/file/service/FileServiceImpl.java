package com.early.www.file.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.early.www.file.DTO.FileDTO;
import com.early.www.file.model.FileEntity;
import com.early.www.properties.DeploymentProperties;
import com.early.www.repository.FileRepository;
import com.early.www.util.CommonConst;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileServiceImpl implements FileService {
	
	@Autowired
	FileRepository fileRepository;
	
	@Autowired
	DeploymentProperties deploymentProperties;
	
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

	    String commonPath = null;
	    // 저장 경로 생성
	    if(deploymentProperties.getType().equals("local")) {
	    	// 로컬 일때 
	    	commonPath = CommonConst.LOCAL_SERVER_RESOURCE_PATH;
	    }else {
	    	// 서버 일때 
	    	commonPath = "";
	    }
	    
	    StringBuffer sb = new StringBuffer();
	    sb.append(projectPath).append(commonPath).append(yyyyMMdd);
	    
	    String makePath = sb.toString();
	    
	    // 날짜 경로 생성 체크
	    boolean makeDirFlag = createDirectoryIfNotExists(makePath);
	    
	    File tempFile = null;
	    String newFileName = null;
	    
	    if(makeDirFlag) {
    	    try {
    	    	String contentType = file.getContentType();
    	    	
    	    	MakeString makeNewFileName = null;
    	    	
    	    	if(contentType.startsWith("image")) {
    	    		// 이미지 일때 /EARLY_20240527_hash정보.jpg
    	    		makeNewFileName = () -> new StringBuffer()
    	    				.append(CommonConst.PROJECT_KEYWORD)
    	    				.append(CommonConst.UNDER_BAR)
    	    				.append(yyyyMMdd)
    	    				.append(CommonConst.UNDER_BAR)
    	    				.append(dto.getFileHash())
    	    				.append(CommonConst.DOT)
    	    				.append(CommonConst.FILE_TYPE_JPG).toString();
    	    	}else {
    	    		// 파일 일때 원본파일명.확장자_hash정보
    	    		makeNewFileName = () -> new StringBuffer()
    	    				.append(file.getOriginalFilename())
    	    				.append(CommonConst.UNDER_BAR)
    	    				.append(dto.getFileHash()).toString();
    	    	}
    	    	
    	    	newFileName = makeNewFileName.execute();
    	    	
    	    	tempFile = new File(makePath+"/"+newFileName);
    	    	
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
	    	temp.setOriginalFileName(file.getOriginalFilename());
	    	
	    	if(deploymentProperties.getType().equals("local")){
	    		// 채팅 라인 join시 파일, 이미지는 url데이터를 contents로 내려줌 
	    		// 이미지는 src 속성에 그대로 사용하기 때문에 파일의 경로를,
	    		// 파일은 contents에 파일 명을 보여줘야 하기 때문에 url에 파일 명 저장. 단, 다운로드 요청시 tbl_file_info에 매핑된 fileDir로 파일을 찾도록 처리
    			temp.setFileDir(tempFile.getPath());
    			temp.setFileUrl("http://localhost:8080/"+yyyyMMdd+"/"+newFileName);
	    	}else {
	    		// 서버 방식일때 url 변경해야함. 배포 후 정리
	    	}
	    	
	    	// DB 저장 
	    	// 이미지는 chatType이 I, chatContents은 이미지의 url
	    	// 파일은 F. chatContents는 파일의 원본파일명
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


	@Override
	public String getFileHash() {
		long time = System.currentTimeMillis();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYYMMddHHmmssSSS");
		Date date = new Date();
		date.setTime(time);
		
		return simpleDateFormat.format(date);
	}


	@Override
	public File getFile(String dto) {
		File file = null;
		FileEntity entity = fileRepository.findByFileHash(dto);
		if(entity != null) {
			file = new File(entity.getFileDir());
		}
		
		return file;
	} 

	
}
