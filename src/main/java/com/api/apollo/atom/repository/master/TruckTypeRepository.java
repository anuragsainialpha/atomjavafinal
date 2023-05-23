package com.api.apollo.atom.repository.master;

import com.api.apollo.atom.entity.master.MtTruckType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TruckTypeRepository extends JpaRepository<MtTruckType, String> {
}
