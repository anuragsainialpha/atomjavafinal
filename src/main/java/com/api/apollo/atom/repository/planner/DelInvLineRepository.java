package com.api.apollo.atom.repository.planner;

import com.api.apollo.atom.dto.ops.ExportInfoDto;
import com.api.apollo.atom.entity.ops.DelInvLine;
import com.api.apollo.atom.entity.ops.DelInvLineId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

public interface DelInvLineRepository extends JpaRepository<DelInvLine, DelInvLineId> {
/*

    @Query(nativeQuery = true, value = "select t from DelInvLine t where t.source")
    List<DelInvLine> findBySourceDestinationAndInvoice(String source, String dest, String invoice);
*/

    List<DelInvLine> findByDelInvLineIdInvoiceNumber(String invoiceNumber);

    List<DelInvLine> findByDelInvLineIdInvoiceNumberIn(List<String> invoiceNumberList);
    @Query("SELECT new com.api.apollo.atom.dto.ops.ExportInfoDto(d.delInvLineId.invoiceNumber,i.id,i.classification," +
        "i.description,i.type,i.group, i.tte, i.loadFactor, i.grossWt, i.grossWtUom, i.netWt, i.netWtUom, i.volume, " +
        "i.volUom, i.length, i.lenUom, i.width, i.wdUom, i.height, i.htUom, i.diameter, i.dmUom, i.category," +
        " d.lineNo,d.delInvLineId.sapLineNo,d.qty,d.weight,d.weightUom,d.insertUser,d.insertDate,d.updateUser," +
        "d.updateDate) FROM DelInvLine d, MTItem i WHERE d.delInvLineId.invoiceNumber IN :invoices " +
        "and i.id = d.delInvLineId.itemId")
    List<ExportInfoDto> getAllExportInfoForInvoices(@Param("invoices") List<String> invoicesList);

    /*@Modifying(clearAutomatically = true)
    @Transactional
    @Query("update DelInvLine set loadslipId =:loadSlipId where delInvLineId.invoiceNumber IN :invoices")
    void updateLoadSlipId(@Param("loadSlipId") String loadSlipId, @Param("invoices") List<String> invoices);
*/

    /*@Modifying(clearAutomatically = true)
    @Transactional
    @Query("update DelInvLine set loadslipId = null where delInvLineId.invoiceNumber IN :invoices")
    void setSlipIdNull(@Param("invoices") List<String> invoices);*/


}

