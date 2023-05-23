package com.api.apollo.atom.repository.master;

import com.api.apollo.atom.entity.master.CTScaleInvoiceWtDiff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CTScaleInvoiceWtDiffRepository extends JpaRepository<CTScaleInvoiceWtDiff,String> {

  List<CTScaleInvoiceWtDiff> findAllByLocationIdIn(List<String> locationIds);
}
