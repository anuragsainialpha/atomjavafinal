package com.api.apollo.atom.repository.master;


import com.api.apollo.atom.entity.master.MTPlantBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface MTPlantBatchRepository extends JpaRepository<MTPlantBatch, String> {

  @Query(nativeQuery = true, value = "select mt.plant_id as locationId, mt.batch_code as batchCodePrefix, mt.item_classification as itemClassification from mt_plant_batch mt")
  List<Map<String, Object>> findAllBatchCodePrefix();

}
