package com.api.apollo.atom.repository.planner;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.ops.DelInvHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface DelInvHeaderRepository extends JpaRepository<DelInvHeader, String> {

  List<DelInvHeader> findByLoadslipId(String loadslipId);

  List<DelInvHeader> findByShipmentId(String loadslipId);

  @Query("select distinct d.destLoc from DelInvHeader d where d.sourceLoc = ?1 AND d.type = 'JIT_OEM' AND  (d.loadslipId is null OR d.loadslipId LIKE 'JIT_LS') ORDER BY d.destLoc ASC")
  List<String> findOpenDestinations(String sourceLoc);

  @Query("select distinct d.invoiceNumber from DelInvHeader d where d.sourceLoc = ?1 and d.destLoc = ?2 AND d.loadslipId is null and d.type = ?3")
  List<String> findInCompleteExportInvoices(String sourceLoc, String destLocation, Constants.DelInvType type);

  @Query("select distinct d.invoiceNumber from DelInvHeader d where d.sourceLoc = ?1 and d.destLoc = ?2 and d.type = ?3")
  List<String> findExportInvoicesOld(String sourceLoc, String destLocation, Constants.DelInvType delInvType);

  @Query("select distinct d.invoiceNumber from DelInvHeader d where d.sourceLoc = ?1 and d.destLoc = ?2 and (d.loadslipId = ?3 or d.loadslipId is null)  and d.type = ?4")
  List<String> findExportInvoicesOld(String sourceLoc, String destLocation, String loadslipId, Constants.DelInvType delInvType);

  //Ignoring filter on invoices
  @Query("select distinct d.invoiceNumber from DelInvHeader d where d.sourceLoc = ?1 AND (d.loadslipId is null or d.loadslipId like '____LS' ) and d.type = ?2" +
      " and ((d.type = 'JIT_OEM' and d.destLoc = ?3) or (d.type = 'FGS_EXP'))")
  List<String> findInCompleteExportInvoices(String sourceLoc, Constants.DelInvType type, String destLoc);
  //Ignoring filter on invoices

  @Query("select distinct d.invoiceNumber from DelInvHeader d where d.sourceLoc = ?1 and (d.loadslipId = ?2 or d.loadslipId is null or d.loadslipId like '____LS')  and d.type = ?3 " +
      "and ((d.type = 'JIT_OEM' and d.destLoc = ?4) or (d.type = 'FGS_EXP'))")
  List<String> findExportInvoices(String sourceLoc, String loadslipId, Constants.DelInvType delInvType, String destLoc);

  @Query("select distinct d.invoiceNumber from DelInvHeader d where d.loadslipId = ?1 ")
  List<String> findAllInvoiceNumberByLoadslipId(String loadslipId);

  @Modifying(clearAutomatically = true)
  //@Transactional
  @Query("update DelInvHeader set loadslipId =:loadSlipId , containerNum =:containerNum where invoiceNumber IN :invoices")
  void updateLoadSlipId(@Param("containerNum") String containerNum, @Param("loadSlipId") String loadSlipId, @Param("invoices") List<String> invoices);

  @Modifying(clearAutomatically = true)
  //@Transactional
  @Query("update DelInvHeader set loadslipId = null , containerNum = null where invoiceNumber IN :invoices")
  void setSlipIdNull(@Param("invoices") List<String> invoices);


  @Query("SELECT t.loadslipId from DelInvHeader t where t.invoiceNumber = :invoice")
  String getLoadSlipIdForInvoice(@Param("invoice") String invoice);


  @Query("SELECT distinct t.containerNum from DelInvHeader t where t.loadslipId = :loadslipId and t.containerNum is not null")
  List<String> getContainerNum(@Param("loadslipId") String loadSlipId);

  List<DelInvHeader> findByInvoiceNumberInOrderByInsertDateDesc(List<String> invoices);
//DelInvHeader including LS_ID
  @Query("select d from DelInvHeader d where d.sourceLoc = ?1 and (d.loadslipId = ?2 or d.loadslipId is null or d.loadslipId like '____LS')  and d.type = ?3 " +
      "and ((d.type = 'JIT_OEM' and d.destLoc = ?4) or (d.type = 'FGS_EXP'))")
  List<DelInvHeader> findAllHearderBySourceLoadslipTypeAndDest(String plantCode, String loadslipId, Constants.DelInvType type, String destLocation);

//  DelInvHeader with LS_ID
  @Query("select d from DelInvHeader d where d.sourceLoc = ?1 AND (d.loadslipId is null or d.loadslipId like '____LS' ) and d.type = ?2" +
      " and ((d.type = 'JIT_OEM' and d.destLoc = ?3) or (d.type = 'FGS_EXP'))")
  List<DelInvHeader> findAllHearderBySourceTypeAndDest(String plantCode, Constants.DelInvType type, String destLocation);

//  Getting the loadslip id for the inv_number
  @Query("select new com.api.apollo.atom.entity.ops.DelInvHeader(d.invoiceNumber, d.loadslipId) from DelInvHeader d where d.invoiceNumber IN (?1)")
  List<DelInvHeader> findInvNumLoadslipId(List<String> sunInvList);
}
