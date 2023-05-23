package com.api.apollo.atom.util;

import com.api.apollo.atom.entity.ops.IndentSummary;
import com.api.apollo.atom.entity.ops.Loadslip;
import com.api.apollo.atom.entity.ops.TruckReport;
import com.api.apollo.atom.repository.ops.LoadslipRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PDFTableUtil {


  //for seconde page + adding date and loadslipId
  public PdfPTable headerTableForSecondPage(Loadslip loadslip, PdfPTable headerTable, PdfWriter pdfWriter, String destDis) throws DocumentException, IOException {

    ClassPathResource imgFile = new ClassPathResource("apolloNewLogo.jpg");
    byte[] bytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
    Font bannerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20f);
    bannerFont.setColor(new BaseColor(90, 48, 148));
    PdfPCell cell;
    headerTable.setWidthPercentage(100);
    headerTable.setWidths(new int[]{3, 5, 2});
    headerTable.getDefaultCell().setMinimumHeight(16);
    headerTable.setTotalWidth(500f);
    headerTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
    Font regular = FontFactory.getFont(FontFactory.TIMES, 12);
    Font bold = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);

    Paragraph p = new Paragraph();

    //Date cell
    p = new Paragraph("Date:", regular);
    p.add(new Chunk(new SimpleDateFormat("dd-MMMM-yyyy HH:mm").format(new Date()), bold));
    cell = new PdfPCell(p);
    cell.setPaddingTop(10);
    cell.disableBorderSide(Rectangle.BOX);
    headerTable.addCell(cell);

    //LoadslipId cell
    p = new Paragraph("Loadslip Id:", regular);
    p.add(new Chunk(loadslip.getLoadslipId(), bold));
    cell = new PdfPCell(p);
    cell.setPaddingTop(10);
    cell.disableBorderSide(Rectangle.BOX);
    headerTable.addCell(cell);

    // logo cell
    Image image = Image.getInstance(bytes);
    image.scaleAbsolute(90, 30);
    p = new Paragraph(new Chunk(image, 20, 20));
    cell = new PdfPCell(image);
    cell.setPaddingRight(10);
    cell.setPaddingTop(5);
    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
    cell.disableBorderSide(Rectangle.BOX);
    headerTable.addCell(cell);


    p = new Paragraph("Delivery Location:", regular);
    p.add(new Chunk(loadslip.getDestLoc() + "(" + destDis + ")", bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    cell.setColspan(8);
    cell.setPaddingTop(-2);
    headerTable.addCell(cell);

    cell = new PdfPCell();
    cell.disableBorderSide(Rectangle.BOX);
    cell.setPaddingTop(-2);
    headerTable.addCell(cell);

    cell = new PdfPCell();
    cell.disableBorderSide(Rectangle.BOX);
    cell.setPaddingTop(-2);
    headerTable.addCell(cell);

    return headerTable;


  }

  public PdfPTable setHeaderCellFirstTable(Loadslip loadslip, PdfPTable headercellTable, PdfWriter pdfWriter) throws DocumentException, IOException {
//        ClassPathResource imgFile = new ClassPathResource("atom_logo.jpg");
    ClassPathResource imgFile = new ClassPathResource("apolloNewLogo.jpg");
    byte[] bytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
    Font bannerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20f);
    bannerFont.setColor(new BaseColor(90, 48, 148));
    PdfPCell cell;
    headercellTable.setWidthPercentage(100);
    headercellTable.setWidths(new int[]{3, 5, 2});
    headercellTable.getDefaultCell().setMinimumHeight(16);
    headercellTable.setTotalWidth(500f);
    headercellTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
    Font regular = FontFactory.getFont(FontFactory.TIMES, 12);
    Font bold = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);

    Paragraph p = new Paragraph();

    p = new Paragraph("Date:", regular);
    p.add(new Chunk(new SimpleDateFormat("dd-MMMM-yyyy HH:mm").format(new Date()), bold));
    cell = new PdfPCell(p);
    cell.setPaddingTop(10);
    cell.disableBorderSide(Rectangle.BOX);
    headercellTable.addCell(cell);

    Barcode128 code128 = new Barcode128();
    code128.setGenerateChecksum(true);
    code128.setCode(loadslip.getLoadslipId());
    code128.setFont(null);
    Image barcodeImg = code128.createImageWithBarcode(pdfWriter.getDirectContent(), null, null);
    barcodeImg.scaleAbsolute(250, 30);
    PdfPCell barcodeCell = new PdfPCell(barcodeImg);
    barcodeCell.disableBorderSide(Rectangle.BOX);
    barcodeCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    barcodeCell.setPadding(5);
    headercellTable.addCell(barcodeCell);

    Image image = Image.getInstance(bytes);
    image.scaleAbsolute(90, 30);
    p = new Paragraph(new Chunk(image, 20, 20));
    cell = new PdfPCell(image);
    cell.setPaddingRight(10);
    cell.setPaddingTop(5);
    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
    cell.disableBorderSide(Rectangle.BOX);
    headercellTable.addCell(cell);
    return headercellTable;
  }

  public PdfPTable barcodeLoadslipId(Loadslip loadslip, PdfPTable headercellTable) throws DocumentException, IOException {
    Font regular = FontFactory.getFont(FontFactory.TIMES, 12);
    Font bold = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);
    headercellTable.setWidths(new int[]{2, 3, 1});
    PdfPCell cell;
    Paragraph p = new Paragraph();
    p = new Paragraph(" ", regular);


    p.add(new Chunk(" ", bold));
    cell = new PdfPCell(p);
    cell.setPaddingTop(-20);
    cell.disableBorderSide(Rectangle.BOX);
    headercellTable.addCell(cell);

    p = new Paragraph();
    p.add(new Chunk(loadslip.getLoadslipId(), bold));
    cell = new PdfPCell(p);
    cell.setPaddingTop(-5);
    cell.setPaddingLeft(30);
    cell.disableBorderSide(Rectangle.BOX);
    headercellTable.addCell(cell);

    p = new Paragraph(" ", regular);
    p.add(new Chunk(" ", bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    headercellTable.addCell(cell);
    return headercellTable;
  }

  public PdfPTable setHeaderCellSecondTable(Loadslip loadslip, PdfPTable headercellTable) throws DocumentException, IOException {
    Font regular = FontFactory.getFont(FontFactory.TIMES, 12);
    Font bold = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);
    headercellTable.setWidths(new int[]{2, 1, 1});
    PdfPCell cell;
    Paragraph p = new Paragraph("LR#  ", regular);
//        p=new Paragraph("LR#:",regular);
    p.add(new Chunk(loadslip.getLrNum() != null ? loadslip.getLrNum() : "-"));
    cell = new PdfPCell(p);
    cell.setPaddingTop(-5);
    cell.disableBorderSide(Rectangle.BOX);
    headercellTable.addCell(cell);

    p = new Paragraph(" ", regular);
    p.add(new Chunk(" ", bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    headercellTable.addCell(cell);

    p = new Paragraph(" ", regular);
    p.add(new Chunk(" ", bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    headercellTable.addCell(cell);
    return headercellTable;
  }

  public PdfPTable getLocationTable(Loadslip loadslip, PdfPTable locationTable, String destDis) throws DocumentException {
    PdfPCell cell;
    Paragraph p;
    Font regular = FontFactory.getFont(FontFactory.TIMES, 12);
    Font bold = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);
    locationTable.setWidths(new int[]{2, 1, 4});
    p = new Paragraph("Loading  Location:", regular);
    p.add(new Chunk(loadslip.getSourceLoc(), bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    locationTable.addCell(cell);

    p = new Paragraph(" Bay :", regular);
    p.add(new Chunk(loadslip.getBay() != null ? loadslip.getBay() : "-", bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    locationTable.addCell(cell);

    p = new Paragraph("Delivery Location:", regular);
    p.add(new Chunk(loadslip.getDestLoc() + "(" + destDis + ")", bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    locationTable.addCell(cell);
    return locationTable;
  }

  public PdfPTable setTransportTable(Loadslip loadslip, PdfPTable transportTable) throws DocumentException {
    PdfPCell cell;
    transportTable.setWidthPercentage(100);
    transportTable.setWidths(new int[]{2, 2, 3});
    transportTable.getDefaultCell().setMinimumHeight(16);
    transportTable.setTotalWidth(500f);
    transportTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
    Font regular = FontFactory.getFont(FontFactory.TIMES, 12);
    Font bold = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);

    Paragraph p = new Paragraph("Transport:", regular);
    p.add(new Chunk(loadslip.getShipment() != null && loadslip.getShipment().getServprov() != null ? loadslip.getShipment().getServprov() : "-", bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    transportTable.addCell(cell);
    p = new Paragraph("SAP Code:", regular);
    p.add(new Chunk(loadslip.getShipment() != null && loadslip.getShipment().getTransporterSapCode() != null ? loadslip.getShipment().getTransporterSapCode() : "-", bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    transportTable.addCell(cell);
    p = new Paragraph("Driver Details:", regular);
    String DriverName = (loadslip.getShipment() != null && loadslip.getShipment().getDriverName() != null) ? loadslip.getShipment().getDriverName() : "-";
    String driverMobile = (loadslip.getShipment() != null && loadslip.getShipment().getDriverMobile() != null) ? loadslip.getShipment().getDriverMobile() : "-";
    p.add(new Chunk(DriverName, bold));
    p.add(new Chunk("(M:" + driverMobile + ")", bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    transportTable.addCell(cell);

    p = new Paragraph("Truck#:", regular);
    p.add(new Chunk(loadslip.getShipment().getTruckNumber() != null ? loadslip.getShipment().getTruckNumber() : "-", bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    transportTable.addCell(cell);
    p = new Paragraph("Truck Type:", regular);
    String truckType = loadslip.getShipment().getActualTruckType() == null ? (loadslip.getShipment().getTruckType() != null ? loadslip.getShipment().getTruckType() : "-") :
        loadslip.getShipment().getActualTruckType() != null ? loadslip.getShipment().getActualTruckType() : "-";
    p.add(new Chunk(truckType, bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    transportTable.addCell(cell);
    p = new Paragraph("Truck Variant:", regular);
    p.add(new Chunk(loadslip.getShipment().getVariant1() != null ? loadslip.getShipment().getVariant1() : "-", bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    transportTable.addCell(cell);
    return transportTable;

  }

  public PdfPTable setMaterialDetailsTable(Loadslip loadslip, PdfPTable materialDetailsTable, LoadslipRepository loadslipRepository) throws DocumentException {
    PdfPCell cell;
    materialDetailsTable.setWidthPercentage(100);
    materialDetailsTable.setWidths(new int[]{1, 1, 1, 1, 1, 1,1});
    materialDetailsTable.getDefaultCell().setMinimumHeight(16);
    materialDetailsTable.setTotalWidth(500f);
    materialDetailsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
    Font regular = FontFactory.getFont(FontFactory.TIMES, 12);
    Font bold = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);
    //To get the TUBE_QTY,FLAP_QTY,VALVE_QTY  only scanable
    //   Map<String, Object> mapOfQtys = loadslipRepository.findFubeQtyFlapQtyValveQtySum(loadslip.getLoadslipId());
    Paragraph p = new Paragraph("Tyres: ", regular);
    p.add(new Chunk(loadslip.getTotTyres() + "", bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    materialDetailsTable.addCell(cell);
    p = new Paragraph("Tubes:", regular);
    p.add(new Chunk(loadslip.getTotTubes() != null ? loadslip.getTotTubes() + "" : "-", bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    materialDetailsTable.addCell(cell);
    p = new Paragraph("Flaps:", regular);
    p.add(new Chunk(loadslip.getTotFlaps() != null ? loadslip.getTotFlaps() + "" : "-", bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    materialDetailsTable.addCell(cell);
    p = new Paragraph("Valves:", regular);
    p.add(new Chunk(loadslip.getTotValve() != null ? loadslip.getTotValve() + "" : "-", bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    materialDetailsTable.addCell(cell);
    p = new Paragraph("PCTR:", regular);
    p.add(new Chunk(loadslip.getTotPctr() != null ? loadslip.getTotPctr()+"" :"", bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    materialDetailsTable.addCell(cell);

    p = new Paragraph("Others:", regular);
    p.add(new Chunk(StringUtils.isEmpty(loadslip.getOtherQty()) ?  "-" :loadslip.getOtherQty()+ "", bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    materialDetailsTable.addCell(cell);
    //Loadslip quantity
    p = new Paragraph("Total Qty :", regular);
    p.add(new Chunk(loadslip.getQty() != null ? loadslip.getTotQty() + "" : "-", bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    materialDetailsTable.addCell(cell);



    return materialDetailsTable;
  }

  public PdfPTable setTteDetailsTable(Loadslip loadslip, PdfPTable tteDetailsTable) throws DocumentException {
    PdfPCell cell;
    tteDetailsTable.setWidthPercentage(100);
    tteDetailsTable.setWidths(new int[]{1, 1, 1});
    tteDetailsTable.getDefaultCell().setMinimumHeight(16);
    tteDetailsTable.setTotalWidth(500f);
    tteDetailsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
    Font regular = FontFactory.getFont(FontFactory.TIMES, 12);
    Font mFont = FontFactory.getFont(FontFactory.TIMES, 9);
    Font bold = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);
    Paragraph p = new Paragraph("TTE: ", regular);

    p.add(new Chunk(loadslip.getTte() != null ? Utility.roundingNumbersOfterDecimal(loadslip.getTte()) + "" : "-", bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    tteDetailsTable.addCell(cell);
    p = new Paragraph("Weight:", regular);
    p.add(new Chunk(loadslip.getWeight() != null ? Utility.roundingNumbersOfterDecimal(loadslip.getWeight()) + " " : "-", bold));
    p.add(new Chunk("KG", mFont));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    tteDetailsTable.addCell(cell);
    p = new Paragraph("Volume:", regular);
    p.add(new Chunk(loadslip.getVolume() != null ? Utility.roundingNumbersOfterDecimal(loadslip.getVolume()) + " " : "-", bold));
    p.add(new Chunk("CUMTR", mFont));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    tteDetailsTable.addCell(cell);
    p = new Paragraph("TTE Util:", regular);
    p.add(new Chunk(loadslip.getShipment() != null && loadslip.getShipment().getTteUtil() != null ?
        Utility.roundingNumbersOfterDecimal(loadslip.getShipment().getTteUtil()) + " %" : "-", bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    tteDetailsTable.addCell(cell);
    p = new Paragraph("WeightUtil:", regular);
    p.add(new Chunk(loadslip.getShipment() != null && loadslip.getShipment().getWeightUtil() != null ?
        Utility.roundingNumbersOfterDecimal(loadslip.getShipment().getWeightUtil()) + " %" : "-", bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    tteDetailsTable.addCell(cell);
    p = new Paragraph("Volume Util:", regular);
    p.add(new Chunk(loadslip.getShipment() != null && loadslip.getShipment().getVolumeUtil() != null ?
        Utility.roundingNumbersOfterDecimal(loadslip.getShipment().getVolumeUtil()) + " %" : "-", bold));
    cell = new PdfPCell(p);
    cell.disableBorderSide(Rectangle.BOX);
    tteDetailsTable.addCell(cell);
    return tteDetailsTable;
  }

  public ByteArrayOutputStream generatePdfWithIndentData(List<IndentSummary> indentSummaries) {
    IndentSummary indentSummary = indentSummaries.get(0);
    Set<TruckReport> truckReports = indentSummary.getTruckReports().parallelStream().filter(truckReport -> truckReport.getReportLocation().equals(indentSummary.getSourceLocation())).collect(Collectors.toSet());
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ClassPathResource imgFile = new ClassPathResource("apolloNewLogo.jpg");
    try {
      byte[] bytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
      Image image = Image.getInstance(bytes);
      image.scaleAbsolute(100, 40);
      Font regular = FontFactory.getFont(FontFactory.TIMES, 12);
      Document document = new Document(PageSize.A4, 50, 50, 40, 50);
      PdfWriter pdfWriter = PdfWriter.getInstance(document, baos);
//            PdfWriter.getInstance(document, new FileOutputStream("src/main/resources/indentPDF.pdf"));
      document.open();
      int checkLastReport = 0;
      for (TruckReport truckReport : truckReports) {
        checkLastReport++;
        PdfPTable table = new PdfPTable(2);
        table.setKeepTogether(true);
        table.setSpacingBefore(5f);
        table.setWidthPercentage(100);
        PdfPCell cell;
        table.setWidths(new int[]{1, 1});
        //to Insert space between line and table using this table row
        cell = new PdfPCell(new Phrase(" "));
        cell.setPadding(10);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(" "));
        cell.setPadding(10);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
        //Logo
        cell = new PdfPCell(image);
        cell.setPaddingBottom(5);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
        cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
        //IndentId
        cell = new PdfPCell(new Phrase("Indent#", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(indentSummary.getIndentId(), regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Transporter
        cell = new PdfPCell(new Phrase("Transporter", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(indentSummary.getTransporter(), regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Transporter code
        cell = new PdfPCell(new Phrase("Transporter Code", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(" ", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Truck Type
        cell = new PdfPCell(new Phrase("Truck Type", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(truckReport.getReportedTruckType(), regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Plant
        cell = new PdfPCell(new Phrase("Plant", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(indentSummary.getSourceLocation(), regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Destination
        cell = new PdfPCell(new Phrase("Destination", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(indentSummary.getDestinationLocation(), regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Segment
        cell = new PdfPCell(new Phrase("Segment", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(" ", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Trcuk Number
        cell = new PdfPCell(new Phrase("Truck Number", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(truckReport.getTruck().getTruckNumber(), regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Driver Name
        cell = new PdfPCell(new Phrase("Driver Name", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(truckReport.getDriverName(), regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Driver Mobile
        cell = new PdfPCell(new Phrase("Driver Mobile", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(truckReport.getDriverMobile(), regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Driver license Number
        cell = new PdfPCell(new Phrase("Driver License Number", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(truckReport.getDriverLicense(), regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Truck Registered weight
        cell = new PdfPCell(new Phrase("Truck Registered Weight", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(truckReport.getInWeight() != null ? truckReport.getInWeight() + "" : " ", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Truck Dimensions
        cell = new PdfPCell(new Phrase("Truck Dimensions", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(" ", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //GPS Enable
          /*  cell = new PdfPCell(new Phrase("GPS Enabled", regular));
            cell.setVerticalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(3);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(" ", regular));
            cell.setVerticalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(3);
            table.addCell(cell);*/
        table.setSpacingAfter(50f);
        document.add(table);
        if (indentSummary.getNetRequested() > checkLastReport && checkLastReport % 2 != 0) {
          DottedLineSeparator line = new DottedLineSeparator();
          line.setLineWidth(2);
          document.add(line);
        }
      }

      for (int i = 0; i < indentSummary.getNetRequested() - truckReports.size(); i++) {
        checkLastReport++;
        PdfPTable table = new PdfPTable(2);
        table.setKeepTogether(true);
        table.setSpacingBefore(5f);
        table.setWidthPercentage(100);
        PdfPCell cell;
        table.setWidths(new int[]{1, 1});
        //to Insert space between line and table using this table row
        cell = new PdfPCell(new Phrase(" "));
        cell.setPadding(10);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(" "));
        cell.setPadding(10);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
        //Logo
        cell = new PdfPCell(image);
        cell.setPaddingBottom(5);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
        cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
        //IndentId
        cell = new PdfPCell(new Phrase("Indent#", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(indentSummary.getIndentId(), regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Transporter
        cell = new PdfPCell(new Phrase("Transporter", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(indentSummary.getTransporter(), regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Transporter code
        cell = new PdfPCell(new Phrase("Transporter Code", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(" ", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Truck Type
        cell = new PdfPCell(new Phrase("Truck Type", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(indentSummary.getTruckType(), regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Plant
        cell = new PdfPCell(new Phrase("Plant", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(indentSummary.getSourceLocation(), regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Destination
        cell = new PdfPCell(new Phrase("Destination", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(indentSummary.getDestinationLocation(), regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Segment
        cell = new PdfPCell(new Phrase("Segment", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(" ", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Trcuk Number
        cell = new PdfPCell(new Phrase("Truck Number", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(" ", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Driver Name
        cell = new PdfPCell(new Phrase("Driver Name", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(" ", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Driver Mobile
        cell = new PdfPCell(new Phrase("Driver Mobile", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(" ", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Driver license Number
        cell = new PdfPCell(new Phrase("Driver License Number", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(" ", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Truck Registered weight
        cell = new PdfPCell(new Phrase("Truck Registered Weight", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(" ", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //Truck Dimensions
        cell = new PdfPCell(new Phrase("Truck Dimensions", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(" ", regular));
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
        //GPS Enable
          /*    cell = new PdfPCell(new Phrase("GPS Enabled", regular));
              cell.setVerticalAlignment(Element.ALIGN_CENTER);
              cell.setPadding(3);
              table.addCell(cell);
              cell = new PdfPCell(new Phrase(" ", regular));
              cell.setVerticalAlignment(Element.ALIGN_CENTER);
              cell.setPadding(3);
              table.addCell(cell);*/
        table.setSpacingAfter(50f);
        document.add(table);
        if (indentSummary.getNetRequested() > checkLastReport && checkLastReport % 2 != 0) {
          DottedLineSeparator line = new DottedLineSeparator();
          line.setLineWidth(2);
          document.add(line);
        }
      }
      document.close();
    } catch (Exception ex) {
    }
    return baos;
  }

}
