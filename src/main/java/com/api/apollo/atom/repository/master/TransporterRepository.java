package com.api.apollo.atom.repository.master;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.api.apollo.atom.entity.master.MTTransporter;

public interface TransporterRepository extends JpaRepository<MTTransporter, String> {

	@Query("SELECT distinct T.servprov from MTTransporter T where LOWER(T.servprov) LIKE lower(concat(:servprov,'%'))")
	Page<String> findAllTransportersLike(@Param("servprov") String transporter, Pageable pageable);

	Optional<MTTransporter> findOneByServprov(String servprov);

	List<MTTransporter> findAll();


	boolean existsByServprov(String servprov);

	@Query("select distinct new com.api.apollo.atom.entity.master.MTTransporter(T.servprov) from MTTransporter T where T.servprov in :transporters")
	List<MTTransporter> findByServprovIn(@Param("transporters") List<String> transporters);

}
