package com.api.apollo.atom.repository.master;

import com.api.apollo.atom.entity.master.MtSapTruckType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SapTruckTypeRepository extends JpaRepository<MtSapTruckType, String> {
}
