package com.early.www.address.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.early.www.address.model.AddressUserMapping;

public interface AddressMappingRepository extends JpaRepository<AddressUserMapping, Long>{

	@Query(nativeQuery = true, value= "select * from tbl_addr_user_mapping where my_id = :myId and friend_id = :friendId")
	AddressUserMapping findByMyIdAndFriendId(String myId, String friendId);

}
