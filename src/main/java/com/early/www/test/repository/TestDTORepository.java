package com.early.www.test.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.early.www.test.Model.TestDTO;

public interface TestDTORepository extends JpaRepository<TestDTO, Long>{

}
