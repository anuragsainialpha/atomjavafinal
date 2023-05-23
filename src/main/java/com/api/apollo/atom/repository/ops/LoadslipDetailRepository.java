package com.api.apollo.atom.repository.ops;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.ops.LoadslipDetail;
import com.api.apollo.atom.entity.ops.LoadslipDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface LoadslipDetailRepository extends JpaRepository<LoadslipDetail, LoadslipDetailId> {

  List<LoadslipDetail> findAllByLoadslipDetailIdItemIdAndItemDescriptionAndBatchCodeAndLoadslipDestLocAndLoadslipSourceLoc(String itemId, String itemDesc, String batchCode, String destLoc, String sourceLoc);

  List<LoadslipDetail> findAllByLoadslipDetailIdItemIdAndItemDescriptionAndBatchCodeAndLoadslipDestLocAndLoadslipSourceLocInAndLoadslipStatusNotIn(String itemId, String itemDesc, String batchCode, String destLoc, List<String> sourceLoc, List<Constants.LoadslipStatus> statusList);

  List<LoadslipDetail> findAllByLoadslipDetailIdItemIdAndItemDescriptionAndLoadslipDestLocAndLoadslipSourceLoc(String itemId, String itemDesc, String destLoc, String sourceLoc);

  List<LoadslipDetail> findAllByLoadslipDetailIdItemIdAndItemDescriptionAndLoadslipDestLocAndLoadslipSourceLocInAndLoadslipStatusNotIn(String itemId, String itemDesc, String destLoc, List<String> sourceLoc, List<Constants.LoadslipStatus> statusList);


  @Query(nativeQuery = true, value = "SELECT scannable FROM location_scan WHERE item_category = ?1 AND location_id  = ?2")
  String isLoadslipItemScannable(String itemCategory, String plant);

  @Query(nativeQuery = true, value = "SELECT LINE_NO,LOAD_QTY,LOADSLIP_ID FROM LOADSLIP_DETAIL WHERE ITEM_ID = ?1 AND ITEM_DESCRIPTION = ?2 AND BATCH_CODE = ?3 ")
  List<Object[]> getItemQty(String itemid, String itemDesc, String batchCode);

  @Transactional
  @Modifying
  @Query(nativeQuery = true, value = "delete from loadslip_detail where loadslip_id = ?1")
  void deleteLoadslipDetailByLoadslipId(String loadslipId);

  List<LoadslipDetail> findAllByLoadslipDetailIdLoadslipId(String loadslipId);

  List<LoadslipDetail> findAllByLoadslipDetailIdLoadslipIdAndBatchCodeIsNotNull(String loadslipId);

  List<LoadslipDetail> findAllByLoadslipDetailIdItemIdInAndItemDescriptionInAndLoadslipDestLocAndLoadslipSourceLocInAndLoadslipStatusNotIn(List<String> itemList, List<String> itemDescList, String destination, List<String> sourceLocList, List<Constants.LoadslipStatus> statusList);

  List<LoadslipDetail> findAllByLoadslipDetailIdItemIdInAndItemDescriptionInAndBatchCodeInAndLoadslipDestLocAndLoadslipSourceLocInAndLoadslipStatusNotIn(List<String> itemList, List<String> itemDescList, List<String> itemBatchCodeList, String destination, List<String> sourceLocList, List<Constants.LoadslipStatus> statusList);

  /*New Queries*/
  //RDC
  List<LoadslipDetail> findAllByLoadslipDetailIdItemIdInAndItemDescriptionInAndLoadslipDestLocAndLoadslipSourceLocInAndLoadslipStatusNotInAndLoadslipLoadslipIdNot(List<String> itemList, List<String> itemDescList, String destination, List<String> sourceLocList, List<Constants.LoadslipStatus> statusList, String currentLoadslipId);

  //FGS
  List<LoadslipDetail> findAllByLoadslipDetailIdItemIdInAndItemDescriptionInAndBatchCodeInAndLoadslipDestLocAndLoadslipSourceLocInAndLoadslipStatusNotInAndLoadslipLoadslipIdNot(List<String> itemList, List<String> itemDescList, List<String> itemBatchCodeList, String destination, List<String> sourceLocList, List<Constants.LoadslipStatus> statusList, String currentLoadslipId);


}
