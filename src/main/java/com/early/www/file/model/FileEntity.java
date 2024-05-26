package com.early.www.file.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="tbl_file_info")
public class FileEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="file_seq")
	private long fileSeq;
	
	@Column(name="file_hash")
	private String fileHash;
	
	@Column(name="file_name")
	private String fileName;
	
	@Column(name="file_dir")
	private String fileDir;
	
	@Column(name="file_url")
	private String fileUrl;
	
	@Column(name="file_uploader")
	private String fileUploader;
	
	@Column(name="file_upload_date")
	private Date fileUploadDate;
	
	@Column(name="file_size")
	private long fileSize;
	
}
