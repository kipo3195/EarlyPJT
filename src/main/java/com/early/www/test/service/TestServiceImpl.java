package com.early.www.test.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.early.www.test.Model.TestDTO;
import com.early.www.test.repository.TestDTORepository;

@Service
public class TestServiceImpl implements TestService {

	@Autowired
	TestDTORepository testDtoRepository;
	
	@Override
	public void test(TestDTO testDto) {
		
		TestDTO result = testDtoRepository.save(testDto);
  		System.out.println(result);
	}

	
}
