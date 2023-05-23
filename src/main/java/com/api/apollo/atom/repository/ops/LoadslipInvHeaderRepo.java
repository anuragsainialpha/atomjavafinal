package com.api.apollo.atom.repository.ops;

import com.api.apollo.atom.entity.ops.InvoiceHederId;
import com.api.apollo.atom.entity.ops.LoadslipInvoiceHeader;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoadslipInvHeaderRepo extends JpaRepository<LoadslipInvoiceHeader, InvoiceHederId> {

  List<LoadslipInvoiceHeader> findAllByInvoiceHeaderLoadslipId(String loadslipId);
}
