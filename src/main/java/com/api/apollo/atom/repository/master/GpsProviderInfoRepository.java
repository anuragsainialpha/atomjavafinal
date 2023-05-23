package com.api.apollo.atom.repository.master;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.api.apollo.atom.entity.master.GpsProviderInfo;


public interface GpsProviderInfoRepository extends JpaRepository<GpsProviderInfo, String> {

	GpsProviderInfo findOneByGpsProvider(String gpsProvider);

	@Query("SELECT G.gpsProvider from GpsProviderInfo G where LOWER(G.gpsProvider) LIKE lower(concat(:gpsProvider,'%'))")
	Page<String> findByGpsProviderLike(@Param("gpsProvider") String gpsProvider, Pageable pageable);


}
