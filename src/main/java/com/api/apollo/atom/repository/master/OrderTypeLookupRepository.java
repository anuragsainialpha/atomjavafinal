package com.api.apollo.atom.repository.master;

import com.api.apollo.atom.entity.master.OrderTypeLookup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface OrderTypeLookupRepository extends JpaRepository<OrderTypeLookup, String> {

  @Query(nativeQuery = true, value = "select o.order_type as orderType, o.market_segment as mktSeg from order_type_lookup o where o.order_type = ?1 ")
  Map<String, Object> findMktSegByLSType(String loadslipType);

  @Query(nativeQuery = true, value = "select DISTINCT o.market_segment from order_type_lookup o ")
  List<String> getAllDistinctMKTSEG();

}
