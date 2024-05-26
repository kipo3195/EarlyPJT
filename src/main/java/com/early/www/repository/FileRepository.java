package com.early.www.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.early.www.file.model.FileEntity;

public interface FileRepository extends JpaRepository<FileEntity, Long>{

	@Query(nativeQuery = true, value = "select * from tbl_file_info where file_hash = :fileHash")
	FileEntity findByFileHash(String fileHash);

}
