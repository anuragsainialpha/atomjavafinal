package com.api.apollo.atom.repository.master;

import com.api.apollo.atom.entity.master.MTLane;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MTLaneRepository extends JpaRepository<MTLane,Long> {

   Double findBySourceLocationIdAndDestLocationId(String sourceLoc, String destinationLoc);
}
