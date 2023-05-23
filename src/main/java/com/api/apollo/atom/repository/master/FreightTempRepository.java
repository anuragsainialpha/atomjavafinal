package com.api.apollo.atom.repository.master;

import com.api.apollo.atom.entity.master.FreightTemp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FreightTempRepository extends JpaRepository<FreightTemp, Long> {

  Page<FreightTemp> findByStatus(String status, Pageable pageable);

}
