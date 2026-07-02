package com.yas.customer.repository;

import com.yas.customer.model.UserAddress;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findAllByUserId(String userId);

    UserAddress findOneByUserIdAndAddressId(String userId, Long id);

    List<UserAddress> findAllByUserIdAndIsActiveTrueOrderByIdAsc(String userId);
}
