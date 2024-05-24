package com.early.www.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.early.www.file.model.FileEntity;

public interface FileRepository extends JpaRepository<FileEntity, Long>{

}
