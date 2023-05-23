package com.api.apollo.atom.dto.ops;

import com.api.apollo.atom.constant.ItemType;
import com.api.apollo.atom.entity.ops.LoadslipDetail;
import com.api.apollo.atom.entity.ops.LoadslipDetailBom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
public class ExportInfoDto {

    private String invoiceNumber;

    private String itemId;

    private String classification;

    private String description;

    private ItemType type;


    private String group;

    private Double tte;

    private Double loadFactor;

    private Double grossWt;

    private String grossWtUom;

    private Double netWt;

    private String netWtUom;

    private Double volume;

    private String volUom;

    private Integer length;

    private String lenUom;

    private Integer width;

    private String wdUom;

    private Integer height;

    private String htUom;

    private Integer diameter;

    private String dmUom;

    private String category;

    private Integer lineNo;

    private Long sapLineNo;

    private Integer qty;

    private Double weight;

    private String weightUom;

    private String insertUser;

    private Date insertDate;

    private String updateUser;

    private Date updateDate;

    //--------------loadslip line details

    private double lineNumber;

    private String itemDesc;

    private String batchCode;
    private Object availableQty;
    private Integer loadedQty;


    private Double grossWtD;
    private Double grossVol;

    private String isScannable;
    private Integer scannedQty;

    private String tubeSKU;
    private String tubeBatch;
    private Integer tubeQty;
    private Object tubeCompQty;

    private String flapSKU;
    private String flapBatch;
    private Integer falpQty;
    private Object flapCompQty;

    private String valveSKU;
    private String valveBatch;
    private Integer valveQty;
    private Object valveCompQty;
    private Object itemCategory;
    private Object priority;

    private Object invoiceList;


    public ExportInfoDto(String invoiceNumber, String itemId, String classification, String description, ItemType type, String group, Double tte, Double loadFactor, Double grossWt, String grossWtUom, Double netWt, String netWtUom, Double volume, String volUom, Integer length, String lenUom, Integer width, String wdUom, Integer height, String htUom, Integer diameter, String dmUom, String category, Integer lineNo, Long sapLineNo, Integer qty, Double weight, String weightUom, String insertUser, Date insertDate, String updateUser, Date updateDate) {
        this.invoiceNumber = invoiceNumber;
        this.itemId = itemId;
        this.classification = classification;
        this.description = description;
        this.type = type;
        this.group = group;
        this.tte = tte;
        this.loadFactor = loadFactor;
        this.grossWt = grossWt;
        this.grossWtUom = grossWtUom;
        this.netWt = netWt;
        this.netWtUom = netWtUom;
        this.volume = volume;
        this.volUom = volUom;
        this.length = length;
        this.lenUom = lenUom;
        this.width = width;
        this.wdUom = wdUom;
        this.height = height;
        this.htUom = htUom;
        this.diameter = diameter;
        this.dmUom = dmUom;
        this.lineNo = lineNo;
        this.sapLineNo = sapLineNo;
        this.qty = qty;
        this.weight = weight;
        this.weightUom = weightUom;
        this.insertUser = insertUser;
        this.insertDate = insertDate;
        this.updateUser = updateUser;
        this.updateDate = updateDate;
        this.itemCategory = category;
        this.category = category;
        this.grossVol = volume;
    }


    public ExportInfoDto updateLoadSlipdetails(LoadslipDetail loadslipDetail, Optional<LoadslipDetailBom> optionalLoadslipDetailBom, Object avlQty, Object tubeCompQty, Object flapCompQty, Object valveCompQty) {
        this.invoiceNumber = loadslipDetail.getInvoiceNumber()!=null?loadslipDetail.getInvoiceNumber():this.getInvoiceNumber();
        this.lineNumber = loadslipDetail.getLoadslipDetailId().getLineNo();
        this.itemId = loadslipDetail.getLoadslipDetailId().getItemId();
        this.itemDesc = loadslipDetail.getItemDescription();
        this.batchCode = loadslipDetail.getBatchCode();
        this.loadedQty = loadslipDetail.getLoadQty();
        this.grossWtD = loadslipDetail.getGrossWt();
        this.weight = loadslipDetail.getGrossWt();
        this.grossVol = loadslipDetail.getGrossVol();
        this.tte = loadslipDetail.getTte();
        this.isScannable = loadslipDetail.getScannable();
        this.availableQty = avlQty;
        this.tubeCompQty = tubeCompQty;
        this.valveCompQty = valveCompQty;
        this.flapCompQty = flapCompQty;

        if (optionalLoadslipDetailBom.isPresent()) {
            this.tubeSKU = optionalLoadslipDetailBom.get().getTubeSku();
            this.tubeBatch = optionalLoadslipDetailBom.get().getTubeBatch();
            this.tubeQty = optionalLoadslipDetailBom.get().getTubeQty();
            this.flapSKU = optionalLoadslipDetailBom.get().getFlapSku();
            this.flapBatch = optionalLoadslipDetailBom.get().getFlapBatch();
            this.falpQty = optionalLoadslipDetailBom.get().getFlapQty();
            this.valveSKU = optionalLoadslipDetailBom.get().getValveSku();
            this.valveBatch = optionalLoadslipDetailBom.get().getValveBatch();
            this.valveQty = optionalLoadslipDetailBom.get().getValveQty();
        }
        return this;
    }



}
