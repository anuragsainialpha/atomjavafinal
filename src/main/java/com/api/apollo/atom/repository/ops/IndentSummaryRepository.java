package com.api.apollo.atom.repository.ops;

import com.api.apollo.atom.entity.ops.IndentSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IndentSummaryRepository extends JpaRepository<IndentSummary, Long> {

  @Query(nativeQuery = true, value = "select atl_util_pkg.generate_business_number(?1,?2,?3) from dual")
  String findIndentSequence(String prefix, String source, String destination);

  Optional<IndentSummary> findById(Long id);

  Optional<IndentSummary> findOneByIndentId(String indentId);

  Optional<IndentSummary> findOneByIndentIdAndSourceLocation(String indentId, String source);

  Page<IndentSummary> findByTransporter(String transporter, Pageable pageable);

  Page<IndentSummary> findBySourceLocation(String plantCode, Pageable pageable);

  //com.api.apollo.atom.entity.ops.IndentSummary(mt.description)
  @Query(value = "SELECT new com.api.apollo.atom.entity.ops.IndentSummary(indent.id,indent.indentId,indent.dispatchDate,indent.sourceLocation,indent.destinationLocation,indent.truckType,indent.loadFactor,indent.transporter,indent.category,indent.tte,indent.indented,indent.cancelled "
      + ",indent.netRequested,indent.transConfirmed,indent.transDeclined,indent.transAssigned,indent.reported,indent.rejected,indent.netPlaced,indent.netBalance,indent.status,indent.comments,indent.indentAging,indent.insertUser,indent.updateUser,indent.insertDate,indent.updateDate,indent.isFreightAvailable"
      + ", mt.description, indent.destCountry, indent.pod) FROM IndentSummary indent LEFT JOIN MTLocation mt ON indent.destinationLocation = mt.id where indent.sourceLocation=:plantCode")
  Page<IndentSummary> getIndentSummeryWithDesc(@Param("plantCode") String plantCode, Pageable pageable);

  @Query(value = "SELECT new com.api.apollo.atom.entity.ops.IndentSummary(indent.id,indent.indentId,indent.dispatchDate,indent.sourceLocation,indent.destinationLocation,indent.truckType,indent.loadFactor,indent.transporter,indent.category,indent.tte,indent.indented,indent.cancelled "
      + ",indent.netRequested,indent.transConfirmed,indent.transDeclined,indent.transAssigned,indent.reported,indent.rejected,indent.netPlaced,indent.netBalance,indent.status,indent.comments,indent.indentAging,indent.insertUser,indent.updateUser,indent.insertDate,indent.updateDate,indent.isFreightAvailable"
      + ", mt.description, indent.destCountry, indent.pod) FROM IndentSummary indent LEFT JOIN MTLocation mt ON indent.destinationLocation = mt.id")
  Page<IndentSummary> getAllIndentSummeryWithDescDPREP( Pageable pageable);

  List<IndentSummary> findByIndentIdIn(List<String> indentids);

  @Query(value = "select indent_id as indentNumber,net_balance as netBalance from indent_summary where source_loc = ?1 and dest_loc = ?2  and status = ?3",nativeQuery = true)
  List<Map<String,Integer>> getIndentsWithOpenStatusToTruckDestinationEdit(String sourceLocation, String Destination, String status);

  @Query(nativeQuery = true, value = "select i.dest_loc from indent_summary i where i.indent_id = ?1")
  String findIndentDestByIndentId(String indentId);

  /*For Transporter*/
  @Query(value = "SELECT new com.api.apollo.atom.entity.ops.IndentSummary(indent.id,indent.indentId,indent.dispatchDate,indent.sourceLocation,indent.destinationLocation,indent.truckType,indent.loadFactor,indent.transporter,indent.category,indent.tte,indent.indented,indent.cancelled "
      + ",indent.netRequested,indent.transConfirmed,indent.transDeclined,indent.transAssigned,indent.reported,indent.rejected,indent.netPlaced,indent.netBalance,indent.status,indent.comments,indent.indentAging,indent.insertUser,indent.updateUser,indent.insertDate,indent.updateDate,indent.isFreightAvailable"
      + ", mt.description, indent.destCountry, indent.pod) FROM IndentSummary indent LEFT JOIN MTLocation mt ON indent.destinationLocation = mt.id where indent.transporter = ?1 ")
  Page<IndentSummary> findAllByTransporter(String transporter, Pageable pageable);
}
