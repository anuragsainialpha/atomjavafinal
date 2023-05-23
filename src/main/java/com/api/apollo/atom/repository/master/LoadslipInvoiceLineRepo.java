package com.api.apollo.atom.repository.master;

import com.api.apollo.atom.entity.ops.LoadslipDetailId;
import com.api.apollo.atom.entity.ops.LoadslipInvoiceLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoadslipInvoiceLineRepo extends JpaRepository<LoadslipInvoiceLine, LoadslipDetailId> {

  List<LoadslipInvoiceLine> findByLoadslipInvLineIdLoadslipIdAndLoadslipInvLineIdInvoiceNumNotAndLoadslipInvLineIdItemIdAndBatchCode(String loadslipId,String invoiceNum,String itemId,String batchCode);

  List<LoadslipInvoiceLine> findByLoadslipInvLineIdLoadslipIdAndLoadslipInvLineIdInvoiceNum(String loadslipId, String invoiceNum);



}
