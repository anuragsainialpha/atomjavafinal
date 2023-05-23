package com.api.apollo.atom.repository.ops;

import com.api.apollo.atom.entity.ops.Loadslip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Map;
import java.util.List;

public interface LoadReceiptRepository extends JpaRepository<Loadslip, String> {

    @Query(nativeQuery = true, value = "select get_loadslip_lr_num.get_lr_num(?1) from dual")
    String getLRNumber(String loadslipId);

    @Query(nativeQuery = true, value = "select ml.LOCATION_DESC as sourceLocDesc from loadslip ls,MT_LOCATION ml where ls.loadslip_id = ?1 and ls.source_loc = ml.location_id")
    String getLRFromLocation(String loadslipId);

    @Query(nativeQuery = true, value = "select mt.TRANSPORTER_DESC as transporterDec,mt.TRANSPORTER_ADDRESS as transporterAddress,mt.CITY as city,mt.STATE as state,mt.POSTAL_CODE as postalCode,mt.COUNTRY as country,mc.phone as phone,mc.mobile as mobile from loadslip ls, SHIPMENT sm,MT_TRANSPORTER mt,MT_CONTACT mc where ls.loadslip_id = ?1 and ls.SHIPMENT_ID = sm.shipment_id and sm.TRANSPORTER_SAP_CODE = mt.transporter_id and mc.contact_id(+) = mt.transporter_id")
    List<Map<String, Object>> getLrStampDetails(String loadslipId);

    @Query(nativeQuery = true, value = "select nvl(ml.LOCATION_DESC,mc.cust_name) as destLocDesc from loadslip ls,MT_LOCATION ml,MT_CUSTOMER mc where ls.loadslip_id = ?1 and ls.dest_loc = ml.location_id and ls.ship_to = mc.cust_name(+)")
    String getLRToLocation(String loadslipId);

    @Query(nativeQuery = true, value = "select ml.LOCATION_DESC as sourceLocDesc,ml.LOCATION_ADDRESS as sourceLocAdress, ml.CITY as sourceCity, ml.STATE as sourceState, ml.POSTAL_CODE as sourcePostalCode ,ml.COUNTRY as sourceCountry, ml.GST_NO as gstNum from loadslip ls,MT_LOCATION ml where ls.loadslip_id = ?1 and ls.source_loc = ml.location_id")
    List<Map<String, Object>> getLrFromAdress(String loadslipId);

    @Query(nativeQuery = true, value = "select nvl(ml.LOCATION_DESC,mc.cust_name) as destLocDesc,nvl(ml.LOCATION_ADDRESS,mc.CUST_ADDRESS) as destLocAdress, nvl(ml.CITY,mc.city) as destCity, nvl(ml.STATE,mc.state) as destState, nvl(ml.POSTAL_CODE,mc.postal_code) as destPostalCode ,nvl(ml.COUNTRY,mc.country) as destCountry, Nvl(ml.gst_no,mc.gst_no) As gstNum from loadslip ls,MT_LOCATION ml,MT_CUSTOMER mc where ls.loadslip_id = ?1 and ls.dest_loc = ml.location_id and ls.ship_to = mc.cust_name(+)")
    List<Map<String, Object>> getLrToAddress(String loadslipId);

    @Query(nativeQuery = true,value = "SELECT l.location_id AS destloc, l.location_desc    AS destdesc, l.location_address AS destlocadress, l.city AS destcity, l.state  AS deststate, l.postal_code  AS destpostalcode, l.country  AS destcountry, l.gst_no AS gstnum FROM  mt_location l" +
        " WHERE  l.location_id = ?1 UNION SELECT c.cust_id, c.cust_name,  c.cust_address,  c.city, c.state, c.postal_code,c.country,c.gst_no FROM   mt_customer c WHERE  c.cust_id = ?1")
    Map<String,Object> getToAddressToELr(String locationId);

    @Query(nativeQuery = true, value = "select ls.TOT_TYRES totTyres,ls.TOT_TUBES as totTubes, ls.TOT_FLAPS totFlaps from loadslip ls where ls.loadslip_id = ?1")
    List<Map<String, Object>> getLrUnits(String loadslipId);

    @Query(nativeQuery = true, value = "select ls.SAP_INVOICE as sapInvoice,ls.SAP_INVOICE_DATE as sapInvoiceDate from loadslip ls where ls.loadslip_id = ?1")
    List<Map<String, Object>> getLrInvoiceDetails(String loadslipId);

    @Query(nativeQuery = true, value = "select mt.transporter_desc as transporterDesc from loadslip ls, SHIPMENT sm, MT_TRANSPORTER mt where ls.loadslip_id = ?1 and ls.SHIPMENT_ID = sm.shipment_id and sm.TRANSPORTER_SAP_CODE = mt.transporter_id")
    String getLrTransporterDesc(String loadslipId);

    @Query(nativeQuery = true, value = "select " +
        "case " +
        "when mt_elr.elr_flag = 'N' then 'N'" +
        "when mt_elr.elr_flag = 'Y' then 'Y'" +
        "else 'N'" +
        "end as elr from" +
        " MT_ELR , (select ls.source_loc, ship.servprov from loadslip ls, shipment ship where ls.shipment_id = ship.shipment_id and ls.loadslip_id = ?1) " +
        "elr_data where MT_ELR.location_id = elr_data.source_loc and MT_ELR.servprov = elr_data.servprov")
    String getElrFlag(String loadslipId);
}
