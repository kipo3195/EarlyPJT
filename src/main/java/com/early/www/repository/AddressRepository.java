package com.early.www.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.early.www.user.model.EarlyUser;

public interface AddressRepository extends JpaRepository<EarlyUser, Long>{
	
	@Query(nativeQuery = true, value = "select a.id, username, name, birthDay, '' as password, '' as roles, '' as phoneNumber, '' as provider, '' as providerId, userProfile from earlyuser as a "
			+ " join tbl_addr_user_mapping as b on a.username = b.friendId where b.myId = :userId order by a.name asc limit :min, 20")
	List<EarlyUser> findByMyAddressUserList(String userId, int min);
	
	@Query(nativeQuery = true, value = "select id, username, name, birthDay, '' as password, '' as roles, '' as phoneNumber, '' as provider, '' as providerId, userProfile  from earlyuser "
			+ "  where username = :userId ")
	EarlyUser findByUserId(String userId);
	

}
