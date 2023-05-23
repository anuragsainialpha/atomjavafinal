package com.api.apollo.atom.repository.master;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.api.apollo.atom.entity.master.MTLocationBay;

public interface LocationBayRepository extends JpaRepository<MTLocationBay, String> {
	
	@Query(nativeQuery = true, value = "SELECT mlb.bay_id FROM mt_location_bay mlb WHERE mlb.location_id = ?1 and mlb.bay_status = ?2 order by length(mlb.bay_id), mlb.bay_id")
	List<String> findByLocationIdAndBayStatus(String locationId,String bayStatus);

}
