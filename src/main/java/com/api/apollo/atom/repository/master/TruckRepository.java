package com.api.apollo.atom.repository.master;

import com.api.apollo.atom.entity.master.MTTruck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TruckRepository extends JpaRepository<MTTruck, String> {
	
	Optional<MTTruck> findOneByTruckNumberIgnoreCase(String truckNumber);

}
