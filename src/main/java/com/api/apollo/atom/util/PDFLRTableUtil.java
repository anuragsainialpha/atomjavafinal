package com.api.apollo.atom.util;

import com.api.apollo.atom.entity.ops.Loadslip;
import com.api.apollo.atom.entity.ops.LoadslipInvoiceHeader;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PDFLRTableUtil {

  private Font regularFont = FontFactory.getFont(FontFactory.TIMES, 10);
  private Font bold = FontFactory.getFont(FontFactory.TIMES_BOLD, 10);
  private Font boldLabel = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);
  private Font italicFont = FontFactory.getFont(FontFactory.TIMES_ITALIC, 10);
  private Font unitsFont = FontFactory.getFont(FontFactory.TIMES_ITALIC, 9);


  public PdfPTable addLabelTable(Document document, String copyName) throws DocumentException {
    PdfPTable labelTable = new PdfPTable(new float[]{50f, 25f, 25f}); // 3 columns.
    labelTable.setWidthPercentage(98);
    labelTable.setSplitLate(false);

    //Preparing cell1
    Paragraph cellLabelP1 = new Paragraph("");
    cellLabelP1.add(new Chunk("CONSIGNMENT NOTE", boldLabel));
    PdfPCell cellLabel1 = new PdfPCell(cellLabelP1);
    cellLabel1.setColspan(2);
    cellLabel1.setHorizontalAlignment(Element.ALIGN_CENTER);

    Paragraph cellLabelP2 = new Paragraph("");
    cellLabelP2.add(new Chunk(copyName, boldLabel));
    PdfPCell cellLabel2 = new PdfPCell(new Paragraph(cellLabelP2));
    cellLabel2.setHorizontalAlignment(Element.ALIGN_CENTER);

    //Add cells
    labelTable.addCell(cellLabel1);
    labelTable.addCell(cellLabel2);
    labelTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
    return labelTable;
//    document.add(labelTable);

  }

  public PdfPTable addStampTable(Document document, Loadslip loadslip, List<Map<String, Object>> lrStampDetails, String transGstNum, String transPanNum) throws DocumentException {

    String vPhone = null;
    StringBuilder address = new StringBuilder();
    for (Map<String, Object> map : lrStampDetails) {
      if (!StringUtils.isEmpty(map.get("TRANSPORTERDEC"))) {
        address.append(map.get("TRANSPORTERDEC"));
      }
      if (!StringUtils.isEmpty("TRANSPORTERADDRESS")) {
        address.append(", ").append(map.get("TRANSPORTERADDRESS"));
      }
      if (!StringUtils.isEmpty(map.get("CITY"))) {
        address.append(", ").append(map.get("CITY"));
      }
      if (!StringUtils.isEmpty(map.get("STATE"))) {
        address.append(", ").append(map.get("STATE"));
      }
      if (!StringUtils.isEmpty(map.get("POSTALCODE"))) {
        address.append(", ").append(map.get("POSTALCODE"));
      }
      if (!StringUtils.isEmpty(map.get("COUNTRY"))) {
        address.append(", ").append(map.get("COUNTRY"));
      }
      if (!StringUtils.isEmpty(map.get("PHONE"))) {
        vPhone = map.get("PHONE") + "";
      }
    }
    PdfPTable stampTable = new PdfPTable(new float[]{50f, 25f, 25f});
    stampTable.setWidthPercentage(98);
    stampTable.setSplitLate(false);

    //Preparing cell1
    Paragraph pStamp = new Paragraph();
    pStamp.add(new Chunk(address.toString(), regularFont));
    pStamp.add(Chunk.NEWLINE);
    pStamp.add(new Chunk("Phone : " + (vPhone != null ? vPhone : "-"), bold));
    pStamp.add(Chunk.NEWLINE);
    pStamp.add(new Chunk("PAN : ", bold));
    pStamp.add(new Chunk(transPanNum != null ? transPanNum : "-", regularFont));
    pStamp.add(Chunk.NEWLINE);
    pStamp.add(new Chunk("GST : ", bold));
    pStamp.add(new Chunk(transGstNum != null ? transGstNum : "-", regularFont));
    PdfPCell cell1 = new PdfPCell(pStamp);
    cell1.setRowspan(2);

    Paragraph p2 = new Paragraph("Truck Number: ", regularFont);
    p2.add(new Chunk(loadslip.getShipment().getTruckNumber() != null ? loadslip.getShipment().getTruckNumber() : "-", bold));
    PdfPCell cell2 = new PdfPCell(p2);

    Paragraph p3 = new Paragraph("LR No: ", regularFont);
    p3.add(new Chunk(loadslip.getLrNum(), bold));
    PdfPCell cell3 = new PdfPCell(p3);

    Paragraph p4 = new Paragraph("Truck Type: ", regularFont);

    String vTruckType = "-";
    if (loadslip.getShipment().getActualTruckType() != null) {
      vTruckType = loadslip.getShipment().getActualTruckType();
    } else {
      vTruckType = loadslip.getShipment().getTruckType();
    }
    p4.add(new Chunk(vTruckType, bold));
    PdfPCell cell4 = new PdfPCell(p4);

    Paragraph p5 = new Paragraph("Date: ", regularFont);
    p5.add(new Chunk(new SimpleDateFormat("dd-MMMM-yyyy HH:mm").format(new Date()), bold));
    PdfPCell cell5 = new PdfPCell(p5);

    //Add cells
    stampTable.addCell(cell1);
    stampTable.addCell(cell2);
    stampTable.addCell(cell3);
    stampTable.addCell(cell4);
    stampTable.addCell(cell5);

//    document.add(stampTable);
    stampTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
    return stampTable;
  }

  public PdfPTable addFromToTable(Document document, Loadslip loadslip, List<Map<String, Object>> lrFromAddress, String vDestLocDesc) throws DocumentException {
    PdfPTable fromToTable = new PdfPTable(new float[]{50f, 50f});
    fromToTable.setWidthPercentage(98);
    fromToTable.setSplitLate(false);
    String vSourceLocDesc = null;

    for (Map<String, Object> map : lrFromAddress) {
      if (!StringUtils.isEmpty(map.get("SOURCELOCDESC"))) {
        vSourceLocDesc = map.get("SOURCELOCDESC").toString();
      }
    }
    //Preparing cell1
    Paragraph p1 = new Paragraph("Consignor: ", regularFont);
    p1.setMultipliedLeading(50);
    p1.add(new Chunk(vSourceLocDesc != null ? vSourceLocDesc : "-", bold));
    PdfPCell cell1 = new PdfPCell(p1);
    cell1.setPadding(5);

    Paragraph p2 = new Paragraph("Consignee: ", regularFont);
    p2.add(new Chunk(vDestLocDesc != null ? vDestLocDesc : "-", bold));
    PdfPCell cell2 = new PdfPCell(p2);
    cell2.setPadding(5);
    //Add cells
    fromToTable.addCell(cell1);
    fromToTable.addCell(cell2);

    return fromToTable;

  }

  public PdfPTable addGapTable(Document document, List<Map<String, Object>> lrFromAddress, Map<String, Object> destAddress) throws DocumentException {

    String formGstNum = null;
    String toGstNum = null;
    StringBuilder fromAddress = new StringBuilder();

    for (Map<String, Object> map : lrFromAddress) {
      if (!StringUtils.isEmpty(map.get("SOURCELOCDESC"))) {
        fromAddress.append(map.get("SOURCELOCDESC"));
      }
      if (!StringUtils.isEmpty("SOURCELOCADRESS")) {
        fromAddress.append(", ").append(map.get("SOURCELOCADRESS"));
      }
      if (!StringUtils.isEmpty(map.get("SOURCECITY"))) {
        fromAddress.append(", ").append(map.get("SOURCECITY"));
      }
      if (!StringUtils.isEmpty(map.get("SOURCESTATE"))) {
        fromAddress.append(", ").append(map.get("SOURCESTATE"));
      }
      if (!StringUtils.isEmpty(map.get("SOURCEPOSTALCODE"))) {
        fromAddress.append(", ").append(map.get("SOURCEPOSTALCODE"));
      }
      if (!StringUtils.isEmpty(map.get("SOURCECOUNTRY"))) {
        fromAddress.append(", ").append(map.get("SOURCECOUNTRY"));
      }
      if (!StringUtils.isEmpty(map.get("GSTNUM"))) {
        formGstNum = map.get("GSTNUM").toString();
      }
    }


    StringBuilder toAddress = new StringBuilder();
    if (!StringUtils.isEmpty(destAddress.get("DESTDESC"))) {
      toAddress.append(destAddress.get("DESTDESC"));
    }
    if (!StringUtils.isEmpty("DESTLOCADRESS")) {
      toAddress.append(", ").append(destAddress.get("DESTLOCADRESS"));
    }
    if (!StringUtils.isEmpty(destAddress.get("DESTCITY"))) {
      toAddress.append(", ").append(destAddress.get("DESTCITY"));
    }
    if (!StringUtils.isEmpty(destAddress.get("DESTSTATE"))) {
      toAddress.append(", ").append(destAddress.get("DESTSTATE"));
    }
    if (!StringUtils.isEmpty(destAddress.get("DESTPOSTALCODE"))) {
      toAddress.append(", ").append(destAddress.get("DESTPOSTALCODE"));
    }
    if (!StringUtils.isEmpty(destAddress.get("DESTCOUNTRY"))) {
      toAddress.append(", ").append(destAddress.get("DESTCOUNTRY"));
    }
    if (!StringUtils.isEmpty(destAddress.get("GSTNUM"))) {
      toGstNum = destAddress.get("GSTNUM").toString();
    }


    PdfPTable gapTable = new PdfPTable(new float[]{50f, 50f});
    gapTable.setWidthPercentage(98);
    gapTable.setSplitLate(false);

    Paragraph pFromPara = new Paragraph();
    pFromPara.add(new Chunk(fromAddress.toString(), regularFont));
    PdfPCell fromAddressCell = new PdfPCell(pFromPara);
    fromAddressCell.disableBorderSide(Rectangle.BOTTOM);


    Paragraph pToPara = new Paragraph();
    pToPara.add(new Chunk(toAddress.toString(), regularFont));
    PdfPCell toAddressCell = new PdfPCell(pToPara);
    toAddressCell.disableBorderSide(Rectangle.BOTTOM);

    //from gst number
    Paragraph fromGstNumPara = new Paragraph();
    fromGstNumPara.add(new Chunk("GST NO :", bold));
    fromGstNumPara.add(new Chunk(formGstNum != null ? formGstNum : "-", regularFont));
    PdfPCell fromGstCell = new PdfPCell(fromGstNumPara);
    fromGstCell.setPaddingTop(-1);
    fromGstCell.disableBorderSide(Rectangle.TOP);
    fromAddressCell.setPaddingBottom(3);

    //to gst number
    Paragraph toGstNumPara = new Paragraph();
    toGstNumPara.add(new Chunk("GST NO :", bold));
    toGstNumPara.add(new Chunk(toGstNum != null ? toGstNum : "-", regularFont));
    PdfPCell toGstCell = new PdfPCell(toGstNumPara);
    toGstCell.setPaddingTop(-1);
    toGstCell.disableBorderSide(Rectangle.TOP);
    fromAddressCell.setPaddingBottom(3);

    //Add cells
    gapTable.addCell(fromAddressCell);
    gapTable.addCell(toAddressCell);
    gapTable.addCell(fromGstCell);
    gapTable.addCell(toGstCell);

    gapTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
    return gapTable;

  }

  public PdfPTable addReceivingTable(Document document, Loadslip loadslip) throws DocumentException {

    PdfPTable table = new PdfPTable(new float[]{50f, 25f, 25f});
    table.setWidthPercentage(98);
    table.setSplitLate(false);

    //Preparing cell1
    Paragraph paragraph = new Paragraph();
    paragraph.add(Chunk.NEWLINE);
    paragraph.add(new Chunk("Total Tyres - ", bold));
    paragraph.add(new Chunk(loadslip.getTotTyres() + " nos.", regularFont));
    paragraph.add(Chunk.NEWLINE);
    paragraph.add(new Chunk("Total Tubes - ", bold));
    paragraph.add(new Chunk(loadslip.getTotTubes() + " nos.", regularFont));
    paragraph.add(Chunk.NEWLINE);
    paragraph.add(new Chunk("Total Flaps - ", bold));
    paragraph.add(new Chunk(loadslip.getTotFlaps() + " nos.", regularFont));
    paragraph.add(Chunk.NEWLINE);
    paragraph.add(new Chunk("Total Valves - ", bold));
    paragraph.add(new Chunk(loadslip.getTotValve() + " nos.", regularFont));
    paragraph.add(Chunk.NEWLINE);
    paragraph.add(new Chunk("Total PCTR - ", bold));
    paragraph.add(new Chunk(loadslip.getTotPctr() + " nos.", regularFont));
    paragraph.add(Chunk.NEWLINE);
    paragraph.add(new Chunk("Others - ", bold));
    paragraph.add(new Chunk(loadslip.getOtherQty() + " nos.", regularFont));
    paragraph.add(Chunk.NEWLINE);
    paragraph.add(new Chunk("Total QTY - ", bold));
    paragraph.add(new Chunk(loadslip.getTotQty() + " nos.", regularFont));
    PdfPCell cell1 = new PdfPCell(paragraph);
    cell1.setRowspan(3);


    cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
    PdfPCell cell2 = new PdfPCell(new Paragraph("Receiving Details ", boldLabel));
    cell2.setColspan(2);
    cell2.setHorizontalAlignment(Element.ALIGN_LEFT);
    PdfPCell cell3 = new PdfPCell(new Paragraph("Truck Reporting Date ", italicFont));
    cell3.setMinimumHeight(40);
    PdfPCell cell4 = new PdfPCell(new Paragraph("Truck Reporting Time ", italicFont));
    cell4.setMinimumHeight(40);
    PdfPCell cell5 = new PdfPCell(new Paragraph("Truck Unloading Date ", italicFont));
    cell5.setMinimumHeight(40);
    PdfPCell cell6 = new PdfPCell(new Paragraph("Truck Unloading Time ", italicFont));
    cell6.setMinimumHeight(40);

    //Add cells
    table.addCell(cell1);
    table.addCell(cell2);
    table.addCell(cell3);
    table.addCell(cell4);
    table.addCell(cell5);
    table.addCell(cell6);
    table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

    return table;

  }

  public PdfPTable addInvSignTable(Document document, Loadslip loadslip, List<LoadslipInvoiceHeader> invoiceHeaders) throws DocumentException {

    PdfPTable table = new PdfPTable(2);
    table.setWidths(new int[]{1, 1});
    table.setWidthPercentage(98);
    table.setSplitLate(false);

    Paragraph paragraph = new Paragraph();
    paragraph.add(new Chunk("Weight : ", boldLabel));
    paragraph.add(new Chunk(invoiceHeaders.stream().mapToInt(LoadslipInvoiceHeader::getTotalWeight).sum() + " "));
    paragraph.add(new Chunk(invoiceHeaders.isEmpty() ? "-" :invoiceHeaders.get(0).getWeightUOM(), unitsFont));
    PdfPCell weightCell = new PdfPCell(paragraph);
    table.addCell(weightCell);

    paragraph = new Paragraph();
    paragraph.add(new Chunk("Invoice Value : ", boldLabel));
    String invoiceValue = rupeeFormat(Integer.toString(invoiceHeaders.stream().mapToInt(LoadslipInvoiceHeader::getSapInvoiceVolue).sum()));
    paragraph.add(new Chunk(invoiceValue));
    paragraph.add(new Chunk(" INR", unitsFont));
    PdfPCell invoiceValueCell = new PdfPCell(paragraph);
    table.addCell(invoiceValueCell);

    paragraph = new Paragraph();
    paragraph.add(new Chunk("Invoice Number/Date:", boldLabel));
    PdfPCell invoiceLabelCell = new PdfPCell(paragraph);
    invoiceLabelCell.disableBorderSide(Rectangle.BOTTOM);
    table.addCell(invoiceLabelCell);

    paragraph = new Paragraph();
    paragraph.add(new Chunk("E-Way Bill No/Date", boldLabel));
    PdfPCell eWayLabelCell = new PdfPCell(paragraph);
    eWayLabelCell.disableBorderSide(Rectangle.BOTTOM);
    table.addCell(eWayLabelCell);

    paragraph = new Paragraph();
    for (LoadslipInvoiceHeader invoiceHeader : invoiceHeaders) {
      paragraph.add(Chunk.NEWLINE);
      paragraph.add(new Chunk(invoiceHeader.getInvoiceHeader().getInvoiceNum() + " (" + (invoiceHeader.getInvoiceDate() != null ? (new SimpleDateFormat("dd-MMMM-yyyy").format(invoiceHeader.getInvoiceDate())) : "") + ")", regularFont));
    }
    PdfPCell invoiceCell = new PdfPCell(paragraph);
    invoiceCell.disableBorderSide(Rectangle.TOP);
    invoiceCell.setPaddingTop(-9);
    invoiceCell.setPaddingBottom(7);
    table.addCell(invoiceCell);

    paragraph = new Paragraph();
    for (LoadslipInvoiceHeader invoiceHeader : invoiceHeaders) {
      paragraph.add(Chunk.NEWLINE);
      StringBuilder eWay = new StringBuilder();
      if (!StringUtils.isEmpty(invoiceHeader.getEWayBillNum())) {
        eWay.append(invoiceHeader.getEWayBillNum());
        if (!StringUtils.isEmpty(invoiceHeader.getEWayBillDate())) {
          eWay.append(" (").append(invoiceHeader.getEWayBillDate() != null ? (new SimpleDateFormat("dd-MMMM-yyyy").format(invoiceHeader.getEWayBillDate())) : "").append(")");
        }
      }
      paragraph.add(new Chunk(eWay.toString(), regularFont));
    }
    PdfPCell eWayCell = new PdfPCell(paragraph);
    eWayCell.disableBorderSide(Rectangle.TOP);
    eWayCell.setPaddingTop(-9);
    eWayCell.setPaddingBottom(7);
    table.addCell(eWayCell);
    table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

    return table;

  }

  private String rupeeFormat(String value) {
    value = value.replace(",", "");
    char lastDigit = value.charAt(value.length() - 1);
    String result = "";
    int len = value.length() - 1;
    int nDigits = 0;

    for (int i = len - 1; i >= 0; i--) {
      result = value.charAt(i) + result;
      nDigits++;
      if (((nDigits % 2) == 0) && (i > 0)) {
        result = "," + result;
      }
    }
    return (result + lastDigit);
  }

  public PdfPTable addDeclTable(Document document, String vLrTransporterDesc) throws DocumentException {
    PdfPTable fromToTable = new PdfPTable(new float[]{50f, 50f});
    fromToTable.setWidthPercentage(98);
    fromToTable.setSplitLate(false);
    String unchecked= "[  ]";

    Paragraph p1 = new Paragraph("Declaration : ", boldLabel);
    p1.add(Chunk.NEWLINE);
    p1.add(new Chunk("GST to be paid by",bold));
    p1.add(Chunk.NEWLINE);

    //      Font arial_unicode_ms = FontFactory.getFont("Wingdings", BaseFont.IDENTITY_H, false);

    Font zapfdingbats = new Font(Font.FontFamily.ZAPFDINGBATS, 14, Font.NORMAL);

    Chunk bullet = new Chunk(String.valueOf((char) 111), zapfdingbats);

    Paragraph p = new Paragraph();
    /*Consignor*/
    p1.add(new Chunk(unchecked, bold));
    p1.add(new Chunk(" Consignor",bold));
    p1.add(Chunk.NEWLINE);

    /*Consignee*/
    p1.add(new Chunk(unchecked, bold));
    p1.add(new Chunk(" Consignee",bold));
    p1.add(Chunk.NEWLINE);

    /*Transporter*/
    p1.add(new Chunk(unchecked, bold));
    p1.add(new Chunk(" Transporter – GST under Forward Charge Mechanism (FCM) as per notification no. 20/2017 dt 22nd August 2017",bold));

//    p1.add(new Chunk("We hereby declare that we will charge GST under Forward Charge Mechanism ( FCM) as per notification No. 20/2017 ", regularFont));
/*    p1.add(Chunk.NEWLINE);
    //Preparing cell1
    p1.add(new Chunk("– GST under Forward Charge Mechanism (FCM) as per notification no. 20/2017 dt 22nd August 2017",bold));*/
    p1.add(Chunk.NEWLINE);
    p1.add(new Chunk("(tick whichever is applicable)", regularFont));
//    p1.add(new Chunk("Dated 27 Aug 2017", regularFont));
    PdfPCell cell1 = new PdfPCell(p1);
    cell1.setMinimumHeight(50);

    Paragraph p2 = new Paragraph(new Chunk("For : ", boldLabel));
    p2.add(new Chunk(vLrTransporterDesc != null ? vLrTransporterDesc : "-", regularFont));
    p2.add(Chunk.NEWLINE);
    p2.add(Chunk.NEWLINE);
    p2.add(Chunk.NEWLINE);
    p2.add(new Chunk("Booking Incharge Sign", bold));
    PdfPCell cell2 = new PdfPCell(p2);
    cell2.setMinimumHeight(50);

    //Add cells
    fromToTable.addCell(cell1);
    fromToTable.addCell(cell2);
    fromToTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

    return fromToTable;

  }
}
