package com.api.apollo.atom.util.func;

import com.api.apollo.atom.entity.ops.Loadslip;
import com.api.apollo.atom.entity.ops.LoadslipDetail;
import com.api.apollo.atom.entity.ops.LoadslipDetailBom;
import com.api.apollo.atom.repository.master.LocationRepository;
import com.api.apollo.atom.util.Utility;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ExcelUtil {

  public static void generateExcel(SXSSFWorkbook workbook, Sheet sheet, Map<String, Object[]> reportData) throws Exception {

    Row row;
    DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy, HH:mm ");
    DateFormat dF = new SimpleDateFormat("yyyy-MM-dd");
    Set<String> keyid = reportData.keySet();

    XSSFCellStyle headerStyle = getHeaderInBlue(workbook);
    XSSFCellStyle leftAlignCellStyle = getLeftAlignCellStyle(workbook);
    XSSFCellStyle rightAlignCellStyle = getRightAlignCellStyle(workbook);
    XSSFCellStyle rightAlignCellStyleInteger = getCellStyleForIntAndLong(workbook);
    DataFormat format = workbook.createDataFormat();

    int rowid = 0;
    for (String key : keyid) {
      row = sheet.createRow(rowid++);
      Object[] objectArr = reportData.get(key);
      int cellid = 0;
      for (Object obj : objectArr) {
        Cell cell = row.createCell(cellid++);

        if (row.getRowNum() == 0) {
          cell.setCellStyle(headerStyle);
          cell.setCellValue((String) obj);
        } else if (obj instanceof String) {
          cell.setCellStyle(leftAlignCellStyle);
          cell.setCellValue(obj == null ? "NULL" : (String) obj);
        } else if (obj instanceof Long) {
          cell.setCellStyle(getCellStyleForIntAndLong(workbook));
          cell.setCellValue(Long.parseLong(obj.toString()));
        } else if (obj instanceof Double) {
          rightAlignCellStyle.setDataFormat(format.getFormat("0.00"));
          cell.setCellStyle(rightAlignCellStyle);
          cell.setCellValue(ExcelUtil.roundTwoDecimals(Double.parseDouble(obj.toString())));
        } else if (obj instanceof Integer) {
          cell.setCellStyle(getCellStyleForIntAndLong(workbook));
          cell.setCellValue(Integer.parseInt(obj.toString()));
        } else if (obj instanceof Float) {
          rightAlignCellStyle.setDataFormat(format.getFormat("0.00"));
          cell.setCellStyle(rightAlignCellStyle);
          cell.setCellValue(ExcelUtil.roundTwoDecimals(Float.parseFloat(obj.toString())));
        } else if (obj instanceof Date) {
          cell.setCellValue(dateFormat.format((Date) obj));
          cell.setCellStyle(rightAlignCellStyle);
        }

      }
    }
  }


  public static Object[] setDataToMap(String[] fieldNames, Object dto) throws Exception {

    Object[] objArray = new Object[fieldNames.length];

    Class<? extends Object> class1 = dto.getClass();
    int objNumber = 0;
    for (String fieldName : fieldNames) {
      Field field = class1.getDeclaredField(fieldName);
      field.setAccessible(true);

      if (field.get(dto) == null) {
        objArray[objNumber] = " ";
      } else if (field.get(dto) instanceof Date) {
        objArray[objNumber] = (Date) field.get(dto) == null ? "" : field.get(dto);
      } else if (field.get(dto) instanceof Integer) {
        objArray[objNumber] = (Integer) field.get(dto);
      } else {
        if (field.get(dto) instanceof Float) {
          objArray[objNumber] = (Float) field.get(dto);
        } else if (field.get(dto) instanceof Long) {
          objArray[objNumber] = (Long) field.get(dto);
        } else if (field.get(dto) instanceof Double) {
          objArray[objNumber] = (Double) field.get(dto);
        } else if (field.get(dto) instanceof Long) {
          objArray[objNumber] = (Long) field.get(dto);
        } else if (field.get(dto) instanceof BigDecimal) {
          objArray[objNumber] = ((BigDecimal) field.get(dto));
        } else {
          objArray[objNumber] = field.get(dto).toString();
        }
      }
      objNumber++;
    }
    return objArray;
  }

  public static XSSFCellStyle getHeader(SXSSFWorkbook workbook) {

    final XSSFCellStyle hearderStyle = (XSSFCellStyle) workbook.createCellStyle();

    hearderStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
    hearderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    hearderStyle.setAlignment(HorizontalAlignment.CENTER);
    hearderStyle.setBorderBottom(BorderStyle.THIN);
    hearderStyle.setAlignment(HorizontalAlignment.CENTER);

    hearderStyle.setLocked(false);

    return hearderStyle;
  }

  public static XSSFCellStyle getRightAlignCellStyle(XSSFWorkbook workbook) {

    final XSSFCellStyle rightAlignCellStyle = workbook.createCellStyle();
    rightAlignCellStyle.setAlignment(HorizontalAlignment.RIGHT);

    ExcelUtil.setBordersToCell(rightAlignCellStyle);


    rightAlignCellStyle.setLocked(false);

    return rightAlignCellStyle;
  }

  public static XSSFCellStyle getRightAlignCellStyle(SXSSFWorkbook workbook) {

    final XSSFCellStyle rightAlignCellStyle = (XSSFCellStyle) workbook.createCellStyle();
    rightAlignCellStyle.setAlignment(HorizontalAlignment.RIGHT);

    ExcelUtil.setBordersToCell(rightAlignCellStyle);

    rightAlignCellStyle.setLocked(false);

    return rightAlignCellStyle;
  }


  public static XSSFCellStyle getLeftAlignCellStyle(XSSFWorkbook workbook) {

    final XSSFCellStyle leftAlignCellStyle = workbook.createCellStyle();
    leftAlignCellStyle.setAlignment(HorizontalAlignment.LEFT);
    ExcelUtil.setBordersToCell(leftAlignCellStyle);

    leftAlignCellStyle.setLocked(false);

    return leftAlignCellStyle;
  }

  public static XSSFCellStyle getLeftAlignCellStyle(SXSSFWorkbook workbook) {

    final XSSFCellStyle leftAlignCellStyle = (XSSFCellStyle) workbook.createCellStyle();
    leftAlignCellStyle.setAlignment(HorizontalAlignment.LEFT);
    leftAlignCellStyle.setAlignment(HorizontalAlignment.LEFT);

    ExcelUtil.setBordersToCell(leftAlignCellStyle);


    leftAlignCellStyle.setLocked(false);

    return leftAlignCellStyle;
  }

  public static void setBordersToCell(XSSFCellStyle cellStyle) {
    cellStyle.setBorderBottom(BorderStyle.THIN);
    cellStyle.setBorderTop(BorderStyle.THIN);
    cellStyle.setBorderRight(BorderStyle.THIN);
    cellStyle.setBorderLeft(BorderStyle.THIN);
  }

  public static XSSFCellStyle getHeaderInGreen(SXSSFWorkbook workbook) {

    final XSSFCellStyle hearderStyle = (XSSFCellStyle) workbook.createCellStyle();
    hearderStyle.setFillForegroundColor((IndexedColors.SEA_GREEN.getIndex()));
    hearderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    hearderStyle.setAlignment(HorizontalAlignment.CENTER);

    ExcelUtil.setBordersToCell(hearderStyle);


    hearderStyle.setLocked(false);

    return hearderStyle;
  }

  public static XSSFCellStyle getHeaderInBlue(SXSSFWorkbook workbook) {

    final XSSFCellStyle hearderStyle = (XSSFCellStyle) workbook.createCellStyle();
    hearderStyle.setFillForegroundColor((IndexedColors.LIGHT_BLUE.getIndex()));
    hearderStyle.setFillPattern(FillPatternType.BRICKS);
    hearderStyle.setAlignment(HorizontalAlignment.CENTER);

    ExcelUtil.setBordersToCell(hearderStyle);

    Font font = workbook.createFont();
    font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
    font.setBold(true);
    hearderStyle.setFont(font);

    hearderStyle.setLocked(true);

    return hearderStyle;
  }

  public ByteArrayOutputStream createLoadslipExcelSheet(Loadslip loadslip, LocationRepository locationRepository) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet sheet = workbook.createSheet(loadslip.getLoadslipId());
    sheet.setColumnWidth(0, 5000);
    sheet.setColumnWidth(1, 6000);
    sheet.setColumnWidth(2, 1500);
    sheet.setColumnWidth(3, 2000);
    sheet.setColumnWidth(4, 1500);
    sheet.setColumnWidth(5, 1800);

    int rownum = 0;

    //LoadslipDate row
    Row headerRow = sheet.createRow(rownum++);
    headerRow.setHeightInPoints((4 * sheet.getDefaultRowHeightInPoints()));
    Cell loadslipDateCell = headerRow.createCell(0);

    //Merging cells
    sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 2)); //barcode
    sheet.addMergedRegion(new CellRangeAddress(0, 0, 3, 5)); //logo
    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 1));
    sheet.addMergedRegion(new CellRangeAddress(1, 1, 2, 3));
    sheet.addMergedRegion(new CellRangeAddress(2, 2, 1, 5)); //destination desc
    sheet.addMergedRegion(new CellRangeAddress(4, 4, 2, 5)); //driver details
    sheet.addMergedRegion(new CellRangeAddress(3, 3, 2, 5)); //variant
    sheet.addMergedRegion(new CellRangeAddress(6,6, 2, 5));
    sheet.addMergedRegion(new CellRangeAddress(7,7, 2, 3));//other qty
    sheet.addMergedRegion(new CellRangeAddress(7,7, 4, 5));//total qty
    sheet.addMergedRegion(new CellRangeAddress(9, 9, 2, 5));//Volume
    sheet.addMergedRegion(new CellRangeAddress(10, 10, 2, 5));//Volume Util



    /* Center Align Cell Contents */
    XSSFCellStyle style = workbook.createCellStyle();
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    style.setWrapText(true);

    XSSFFont valueFont = workbook.createFont();
    XSSFRichTextString lsDate = new XSSFRichTextString("Date : " + new SimpleDateFormat("dd-MMMM-yyyy HH:mm").format(new Date()));
    valueFont.setFontHeight(10);
    valueFont.setBold(true);
    XSSFFont labelFont = workbook.createFont();
    labelFont.setFontHeight(10);
    labelFont.setBold(false);
    lsDate.applyFont(0, 6, labelFont);
    lsDate.applyFont(6, lsDate.length(), valueFont);

    loadslipDateCell.setCellValue(lsDate);
    loadslipDateCell.setCellStyle(style);

    //loadslipId cell
    Cell cell = headerRow.createCell(1);
    style = workbook.createCellStyle();
    style.setAlignment(HorizontalAlignment.CENTER);

    try {
      Code128Bean barcode128 = new Code128Bean();
      barcode128.setBarHeight(8);
      barcode128.setQuietZone(5);
      barcode128.setVerticalQuietZone(20);
      barcode128.setModuleWidth(0.3);
      barcode128.setFontSize(4);
      barcode128.doQuietZone(true);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      BitmapCanvasProvider canvas = new BitmapCanvasProvider(baos, "image/x-png", 300, BufferedImage.TYPE_BYTE_BINARY, false, 0);
      barcode128.generateBarcode(canvas, loadslip.getLoadslipId());
      canvas.finish();
      byte[] bytes = baos.toByteArray();
      int barcodePicId = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
      XSSFDrawing drawing = sheet.createDrawingPatriarch();
      XSSFClientAnchor barcodeAnchor = new XSSFClientAnchor();
      barcodeAnchor.setCol1(1);
      barcodeAnchor.setRow1(0);
      XSSFPicture barCodePic = drawing.createPicture(barcodeAnchor, barcodePicId);
      barCodePic.resize(1.3, 0.90);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    //Logo cell
    ClassPathResource imgFile = new ClassPathResource("apolloNewLogo.jpg");
    try {
      byte[] bytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
      int my_picture_id = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
      XSSFDrawing drawing = sheet.createDrawingPatriarch();
      XSSFClientAnchor my_anchor = new XSSFClientAnchor();
      my_anchor.setCol1(3);
      my_anchor.setRow1(0);
      XSSFPicture logoPic = drawing.createPicture(my_anchor, my_picture_id);
      logoPic.resize(2.5, 0.90);
    } catch (Exception e) {
      e.printStackTrace();
    }

    //LR number row
    Row lrNumRow = sheet.createRow(rownum++);
    Cell lrNumCell = lrNumRow.createCell(0);
    XSSFRichTextString lsLr = new XSSFRichTextString("LR# " + (loadslip.getLrNum() != null ? loadslip.getLrNum() : ""));
    lsLr.applyFont(0, 3, labelFont);
    lsLr.applyFont(3, lsLr.length(), valueFont);
    style.setWrapText(true);
    style.setAlignment(HorizontalAlignment.LEFT);
    lrNumCell.setCellValue(lsLr);
    lrNumCell.setCellStyle(style);
    if(lsLr.length() > 44){
      lrNumRow.setHeight((short) 700);
    }

    //Bay
    Cell bayCell = lrNumRow.createCell(2);
    XSSFRichTextString lsBay = new XSSFRichTextString("Bay : " + (loadslip.getBay() != null ? loadslip.getBay() : ""));
    lsBay.applyFont(0, 5, labelFont);
    lsBay.applyFont(5, lsBay.length(), valueFont);
    bayCell.setCellValue(lsBay);

    //LS location
    Row locationRow = sheet.createRow(rownum++);
    Cell locationCell = locationRow.createCell(0);
    XSSFRichTextString lsSource = new XSSFRichTextString("Loading Location :" + loadslip.getSourceLoc());
    lsSource.applyFont(0, "Loading Location :".length(), labelFont);
    lsSource.applyFont("Loading Location :".length(), lsSource.length(), valueFont);
    locationCell.setCellValue(lsSource);
    locationCell.setCellStyle(style);


    //LS Destination & Description
    List<String> destinationlist = new ArrayList<>();
    destinationlist.add(loadslip.getDestLoc());
    List<Map<String, String>> destdis = locationRepository.findDestDescWtihDestinations(destinationlist);
    String destDes = destdis.get(0).get("DESTDESC") != null ? destdis.get(0).get("DESTDESC") : " ";

    Cell delLocCell = locationRow.createCell(1);
    XSSFRichTextString destLoc = new XSSFRichTextString("Delivery Location : " + loadslip.getDestLoc() + "(" + destDes + ")");
    destLoc.applyFont(0, "Delivery Location  : ".length(), labelFont);
    destLoc.applyFont("Delivery Location : ".length(), destLoc.length(), valueFont);
    delLocCell.setCellValue(destLoc);
    if (destLoc.length() > 55) {
      locationRow.setHeight((short) 700);
    }
    delLocCell.setCellStyle(style);

    // Driver Details
    String driverName = (loadslip.getShipment() != null && loadslip.getShipment().getDriverName() != null) ? loadslip.getShipment().getDriverName() : "";
    String driverMobile = (loadslip.getShipment() != null && loadslip.getShipment().getDriverMobile() != null) ? loadslip.getShipment().getDriverMobile() : "";
    String driverDetail = "Driver Details: " + driverName + "  (" + driverMobile + ")";
    //Transporter Row
    Row transporterRow = sheet.createRow(rownum++);
    style = workbook.createCellStyle();
    style.setWrapText(true);
    Cell transporterCell = transporterRow.createCell(0);
    XSSFRichTextString servprov = new XSSFRichTextString("Transport : " + (loadslip.getShipment().getServprov() != null ? loadslip.getShipment().getServprov() : ""));
    servprov.applyFont(0, "Transport : ".length(), labelFont);
    servprov.applyFont("Transport : ".length(), servprov.length(), valueFont);
    transporterCell.setCellValue(servprov);
    transporterCell.setCellStyle(style);

    // Sap code cell
    Cell sapCodeCell = transporterRow.createCell(1);
    XSSFRichTextString sapcode = new XSSFRichTextString("SAP Code : " + (loadslip.getShipment() != null && loadslip.getShipment().getTransporterSapCode() != null ? loadslip.getShipment().getTransporterSapCode() : ""));
    sapcode.applyFont(0, "SAP Code : ".length(), labelFont);
    sapcode.applyFont("SAP Code : ".length(), sapcode.length(), valueFont);
    sapCodeCell.setCellValue(sapcode);
    sapCodeCell.setCellStyle(style);

    // Driver Details
    Cell driverDetailsCell = transporterRow.createCell(2);
    XSSFRichTextString driverDetails = new XSSFRichTextString(driverDetail);
    driverDetails.applyFont(0, "Driver Details:".length(), labelFont);
    driverDetails.applyFont("Driver Details:".length(), driverDetails.length(), valueFont);
    driverDetailsCell.setCellValue(driverDetails);
    style.setWrapText(true);
    driverDetailsCell.setCellStyle(style);

    //wraping  above row when row strings are more then the column in excel
    if (driverDetail.length()> 20 ||  servprov.length()> 25 || sapcode.length() > 30) {
      transporterRow.setHeight((short) 700);
    }

    // Truck Details Row
    Row truckDetailsRow = sheet.createRow(rownum++);
    Cell truckNumberCell = truckDetailsRow.createCell(0);
    XSSFRichTextString truck = new XSSFRichTextString("Truck : " + (loadslip.getShipment().getTruckNumber() != null ? loadslip.getShipment().getTruckNumber() : ""));
    truck.applyFont(0, "Truck : ".length(), labelFont);
    truck.applyFont("Truck : ".length(), truck.length(), valueFont);
    truckNumberCell.setCellValue(truck);
    truckNumberCell.setCellStyle(style);

    // Truck Type cell
    String truckType = loadslip.getShipment().getActualTruckType() == null ? (loadslip.getShipment().getTruckType() != null ? loadslip.getShipment().getTruckType() : "") :
        loadslip.getShipment().getActualTruckType() != null ? loadslip.getShipment().getActualTruckType() : "-";
    Cell truckTypeCell = truckDetailsRow.createCell(1);
    XSSFRichTextString truckTypeValue = new XSSFRichTextString("Truck Type:  " + truckType);
    truckTypeValue.applyFont(0, "Truck Type:".length(), labelFont);
    truckTypeValue.applyFont("Truck Type:".length(), truckTypeValue.length(), valueFont);
    truckTypeCell.setCellValue(truckTypeValue);
    truckTypeCell.setCellStyle(style);

    // Truck Variant cell
    Cell variantCell = truckDetailsRow.createCell(2);
    XSSFRichTextString variant = new XSSFRichTextString("Truck Variant :" + (loadslip.getShipment().getVariant1() != null ? loadslip.getShipment().getVariant1() : ""));
    variant.applyFont(0, "Truck Variant :".length(), labelFont);
    variant.applyFont("Truck Variant :".length(), variant.length(), valueFont);
    variantCell.setCellValue(variant);
    variantCell.setCellStyle(style);

    //wraping  above row when row strings are more then the column in excel
    if (truck.length()> 20 ||  truckTypeValue.length()> 25 || variant.length() > 30) {
      truckDetailsRow.setHeight((short) 700);
    }

    Row gaprow = sheet.createRow(rownum++);

    //Tyres row
    Row tyresRow = sheet.createRow(rownum++);
    Cell tyreCell = tyresRow.createCell(0);
    XSSFRichTextString tyres = new XSSFRichTextString("Tyres:" + (loadslip.getTotTyres() != null ? loadslip.getTotTyres() + "" : ""));
    tyres.applyFont(0, "Tyres:".length(), labelFont);
    tyres.applyFont("Tyres:".length(), tyres.length(), valueFont);
    tyreCell.setCellValue(tyres);

    //Tubes
    Cell tubeCell = tyresRow.createCell(1);
    XSSFRichTextString tubes = new XSSFRichTextString("Tubes:" + (loadslip.getTotTubes() != null ? loadslip.getTotTubes() + "" : ""));
    tubes.applyFont(0, "Tubes:".length(), labelFont);
    tubes.applyFont("Tubes:".length(), tubes.length(), valueFont);
    tubeCell.setCellValue(tubes);

    //Flaps
    Cell flapsCell = tyresRow.createCell(2);
    XSSFRichTextString flaps = new XSSFRichTextString("Flaps:" + (loadslip.getTotFlaps() != null ? loadslip.getTotFlaps() + "" : ""));
    flaps.applyFont(0, "Flaps:".length(), labelFont);
    flaps.applyFont("Flaps:".length(), flaps.length(), valueFont);
    flapsCell.setCellValue(flaps);


    //Valves
    Row tyreRow2 = sheet.createRow(rownum++);
    XSSFRichTextString valves = new XSSFRichTextString("Valves:" + (loadslip.getTotValve() != null ? loadslip.getTotValve() + "" : ""));
    valves.applyFont(0, "Valves:".length(), labelFont);
    valves.applyFont("Valves:".length(), valves.length(), valueFont);
    Cell valvesCell = tyreRow2.createCell(0);
    valvesCell.setCellValue(valves);

    //PCTR
    Cell pctrCell = tyreRow2.createCell(1);
    XSSFRichTextString pctr = new XSSFRichTextString("PCTR:" + (loadslip.getTotPctr() != null ? loadslip.getTotPctr() + "" : ""));
    pctr.applyFont(0, "PCTR:".length(), labelFont);
    pctr.applyFont("PCTR:".length(), pctr.length(), valueFont);
    pctrCell.setCellValue(pctr);

    //OTHERS
    Cell othersCell = tyreRow2.createCell(2);
    XSSFRichTextString others = new XSSFRichTextString("Others:" + (loadslip.getOtherQty() != null ? loadslip.getOtherQty() + "" : ""));
    others.applyFont(0, "Others:".length(), labelFont);
    others.applyFont("Others:".length(), others.length(), valueFont);
    othersCell.setCellValue(others);

    //Total quantity
    Cell totalQtyCell = tyreRow2.createCell(4);
    XSSFRichTextString totalQty = new XSSFRichTextString("Total Qty:" + (loadslip.getQty() != null ? loadslip.getTotQty() + "" : ""));
    totalQty.applyFont(0, "Total Qty:".length(), labelFont);
    totalQty.applyFont("Total Qty:".length(), totalQty.length(), valueFont);
    totalQtyCell.setCellValue(totalQty);

    Row gaprow2 = sheet.createRow(rownum++);

    //TTE
    Row tteRow = sheet.createRow(rownum++);
    Cell tteCell = tteRow.createCell(0);
    XSSFRichTextString tte = new XSSFRichTextString("TTE:" + (loadslip.getTte() != null ? Utility.roundingNumbersOfterDecimal(loadslip.getTte()) + "" : ""));
    tte.applyFont(0, "TTE:".length(), labelFont);
    tte.applyFont("TTE:".length(), tte.length(), valueFont);
    tteCell.setCellValue(tte);

    //Weight
    Cell weightCell = tteRow.createCell(1);
    XSSFRichTextString weight = new XSSFRichTextString("Weight:" + (loadslip.getWeight() != null ? Utility.roundingNumbersOfterDecimal(loadslip.getWeight()) + " KG" : ""));
    weight.applyFont(0, "Weight:".length(), labelFont);
    weight.applyFont("Weight:".length(), weight.length(), valueFont);
    weightCell.setCellValue(weight);


    //Volume
    Cell volumeCell = tteRow.createCell(2);
    XSSFRichTextString volume = new XSSFRichTextString("Volume:" + (loadslip.getVolume() != null ? Utility.roundingNumbersOfterDecimal(loadslip.getVolume()) + " CUMTR" : ""));
    volume.applyFont(0, "Volume:".length(), labelFont);
    volume.applyFont("Volume:".length(), volume.length(), valueFont);
    volumeCell.setCellValue(volume);

    //TTE Util Row
    Row tteUtilRow = sheet.createRow(rownum++);
    Cell tteUtilCell = tteUtilRow.createCell(0);
    XSSFRichTextString tteUtil = new XSSFRichTextString("TTE Util:" + (loadslip.getShipment() != null && loadslip.getShipment().getTteUtil() != null ?
        Utility.roundingNumbersOfterDecimal(loadslip.getShipment().getTteUtil()) + " %" : ""));
    tteUtil.applyFont(0, "TTE Util:".length(), labelFont);
    tteUtil.applyFont("TTE Util:".length(), tteUtil.length(), valueFont);
    tteUtilCell.setCellValue(tteUtil);

    //WeightUtil
    Cell weightUtilCell = tteUtilRow.createCell(1);
    XSSFRichTextString weightUtil = new XSSFRichTextString("WeightUtil:" + (loadslip.getShipment() != null && loadslip.getShipment().getWeightUtil() != null ?
        Utility.roundingNumbersOfterDecimal(loadslip.getShipment().getWeightUtil()) + " %" : "-"));
    weightUtil.applyFont(0, "WeightUtil:".length(), labelFont);
    weightUtil.applyFont("WeightUtil:".length(), weightUtil.length(), valueFont);
    weightUtilCell.setCellValue(weightUtil);

    //Volume Util
    Cell valumeUtilCell = tteUtilRow.createCell(2);
    XSSFRichTextString valumeUtil = new XSSFRichTextString("Volume Util:" + (loadslip.getShipment() != null && loadslip.getShipment().getVolumeUtil() != null ?
        Utility.roundingNumbersOfterDecimal(loadslip.getShipment().getVolumeUtil()) + " %" : ""));
    valumeUtil.applyFont(0, "Volume Util:".length(), labelFont);
    valumeUtil.applyFont("Volume Util:".length(), valumeUtil.length(), valueFont);
    valumeUtilCell.setCellValue(valumeUtil);

    List<LoadslipDetail> loadslipDetails = loadslip.getLoadslipDetails().stream().sorted(Comparator.comparing(LoadslipDetail::getScannable).reversed().thenComparing(ld -> ld.getItemCategory() != null ? ld.getItemCategory() : ld.getLoadslipDetailId().getItemId()).thenComparing(ld -> ld.getLoadslipDetailId().getItemId())).collect(Collectors.toList());

    XSSFCellStyle centerStyle = workbook.createCellStyle();
    centerStyle.setAlignment(HorizontalAlignment.CENTER);
    Row spaceRow = sheet.createRow(rownum++);
    //Material Details row

    style = workbook.createCellStyle();
    style.setAlignment(HorizontalAlignment.LEFT);
    Font font = workbook.createFont();
    font.setBold(true);
    style.setFont(font);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    style.setBorderBottom(BorderStyle.THIN);
    Row materialDeatailRow = sheet.createRow(rownum++);
    Cell materialDetailCell = materialDeatailRow.createCell(0);
    materialDetailCell.setCellValue("Material");
    materialDetailCell.setCellStyle(style);
    materialDetailCell = materialDeatailRow.createCell(1);
    materialDetailCell.setCellValue("Description");
    materialDetailCell.setCellStyle(style);
    materialDetailCell = materialDeatailRow.createCell(2);
    materialDetailCell.setCellValue("scan");
    materialDetailCell.setCellStyle(style);
    materialDetailCell = materialDeatailRow.createCell(3);
    materialDetailCell.setCellValue("Batch");
    materialDetailCell.setCellStyle(style);
    materialDetailCell = materialDeatailRow.createCell(4);
    materialDetailCell.setCellValue("Qty");
    materialDetailCell.setCellStyle(style);
    materialDetailCell = materialDeatailRow.createCell(5);
    materialDetailCell.setCellValue("Picked");
    materialDetailCell.setCellStyle(style);


    for (LoadslipDetail loadslipDetail : loadslipDetails) {
      Row lsDetailRow = sheet.createRow(rownum++);
      String itemDesc = loadslipDetail.getItemDescription() != null ? loadslipDetail.getItemDescription() : "";
      if (itemDesc != null && itemDesc.length() > 28) {
        lsDetailRow.setHeight((short) 700);
      }
      //Material
      Cell detailCell = lsDetailRow.createCell(0);
      style = workbook.createCellStyle();
      style.setVerticalAlignment(VerticalAlignment.CENTER);
      style.setBorderLeft(BorderStyle.THIN);
      style.setBorderTop(BorderStyle.THIN);
      style.setBorderRight(BorderStyle.THIN);
      style.setBorderBottom(BorderStyle.THIN);
      style.setWrapText(true);
      detailCell.setCellStyle(style);
      detailCell.setCellValue(loadslipDetail.getLoadslipDetailId() != null && loadslipDetail.getLoadslipDetailId().getItemId() != null ? loadslipDetail.getLoadslipDetailId().getItemId() : "");

      //item description
      detailCell = lsDetailRow.createCell(1);
      detailCell.setCellStyle(style);
      detailCell.setCellValue(itemDesc);

      //scan
      Cell scanCell = lsDetailRow.createCell(2);
      scanCell.setCellStyle(style);
      scanCell.setCellValue("      " + (loadslipDetail.getScannable() != null ? loadslipDetail.getScannable() : ""));

      //Batch
      detailCell = lsDetailRow.createCell(3);
      detailCell.setCellStyle(style);
      detailCell.setCellValue((loadslipDetail.getBatchCode() != null ? loadslipDetail.getBatchCode() : ""));

      //Qty
      detailCell = lsDetailRow.createCell(4);
      detailCell.setCellStyle(style);
      detailCell.setCellValue("   " + (loadslipDetail.getLoadQty() + ""));

      //picked cell
      delLocCell = lsDetailRow.createCell(5);
      delLocCell.setCellStyle(style);

      Optional<LoadslipDetailBom> detailBom = loadslip.getLoadslipDetailBoms().stream().filter(bom -> bom.getLoadslipDetailBomId().getLineNo().equals(loadslipDetail.getLoadslipDetailId().getLineNo())).findAny();
      if (detailBom.isPresent()) {
        LoadslipDetailBom loadslipDetailBom = detailBom.get();
        if (!StringUtils.isEmpty(loadslipDetailBom.getTubeSku())) {
          Row tubeRow = sheet.createRow(rownum++);
          String bomDesc = loadslipDetailBom.getTubeDesc() != null ? loadslipDetailBom.getTubeDesc() : " ";
          if (bomDesc != null && bomDesc.length() > 26) {
            tubeRow.setHeight((short) 700);
          }
          //Material
          Cell bomTubeCell = tubeRow.createCell(0);
          bomTubeCell.setCellStyle(style);
          bomTubeCell.setCellValue(loadslipDetailBom.getTubeSku() + "");

          //item description
          bomTubeCell = tubeRow.createCell(1);
          bomTubeCell.setCellStyle(style);
          bomTubeCell.setCellValue(bomDesc);

          //scan
          Cell bomScanCell = tubeRow.createCell(2);
          bomScanCell.setCellStyle(style);
          bomScanCell.setCellValue("");

          //Batch
          bomTubeCell = tubeRow.createCell(3);
          bomTubeCell.setCellStyle(style);
          bomTubeCell.setCellValue((loadslipDetailBom.getTubeBatch() != null ? loadslipDetailBom.getTubeBatch() : " "));

          //Qty
          bomTubeCell = tubeRow.createCell(4);
          bomTubeCell.setCellStyle(style);
          bomTubeCell.setCellValue("   " + (loadslipDetailBom.getTubeQty() != null ? loadslipDetailBom.getTubeQty() + "" : " "));

          //picked cell
          bomTubeCell = tubeRow.createCell(5);
          bomTubeCell.setCellStyle(style);

        }
        if (!StringUtils.isEmpty(loadslipDetailBom.getFlapSku())) {
          Row flopRow = sheet.createRow(rownum++);
          String flapDesc = loadslipDetailBom.getFlapDesc() != null ? loadslipDetailBom.getFlapDesc() : " ";
          if (flapDesc != null && flapDesc.length() > 26) {
            flopRow.setHeight((short) 700);
          }
          //Material
          Cell flapCell = flopRow.createCell(0);
          flapCell.setCellStyle(style);
          flapCell.setCellValue(loadslipDetailBom.getFlapSku() + "");

          //item description
          flapCell = flopRow.createCell(1);
          flapCell.setCellStyle(style);
          flapCell.setCellValue(flapDesc);

          //scan
          Cell flapScanCell = flopRow.createCell(2);
          flapScanCell.setCellStyle(style);
          flapScanCell.setCellValue("");

          //Batch
          flapCell = flopRow.createCell(3);
          flapCell.setCellStyle(style);
          flapCell.setCellValue((loadslipDetailBom.getFlapBatch() != null ? loadslipDetailBom.getFlapBatch() : " "));

          //Qty
          flapCell = flopRow.createCell(4);
          flapCell.setCellStyle(style);
          flapCell.setCellValue("   " + (loadslipDetailBom.getFlapQty() != null ? loadslipDetailBom.getFlapQty() + "" : " "));

          //picked cell
          flapCell = flopRow.createCell(5);
          flapCell.setCellStyle(style);

        }
        if (loadslipDetailBom.getValveQty() > 0) {

          Row flopRow = sheet.createRow(rownum++);
          String valveDesc = loadslipDetailBom.getValveDesc() != null ? loadslipDetailBom.getValveDesc() : " ";
          if (valveDesc != null && valveDesc.length() > 26) {
            flopRow.setHeight((short) 700);
          }
          //Material
          Cell flapCell = flopRow.createCell(0);
          flapCell.setCellStyle(style);
          flapCell.setCellValue(loadslipDetailBom.getValveSku() != null ? loadslipDetailBom.getValveSku() : " " + "");

          //item description
          flapCell = flopRow.createCell(1);
          flapCell.setCellStyle(style);
          flapCell.setCellValue(valveDesc);

          //scan
          Cell valveScanCell = flopRow.createCell(2);
          valveScanCell.setCellStyle(style);
          valveScanCell.setCellValue("");

          //Batch
          flapCell = flopRow.createCell(3);
          flapCell.setCellStyle(style);
          flapCell.setCellValue((loadslipDetailBom.getValveBatch() != null ? loadslipDetailBom.getValveBatch() : " "));

          //Qty
          flapCell = flopRow.createCell(4);
          flapCell.setCellStyle(style);
          flapCell.setCellValue("   " + (loadslipDetailBom.getValveQty() != null ? loadslipDetailBom.getValveQty() + "" : " "));


          //picked cell
          flapCell = flopRow.createCell(5);
          flapCell.setCellStyle(style);

        }
      }
    }
    Row spaceRow1 = sheet.createRow(rownum++);
    Row spaceRow2 = sheet.createRow(rownum++);

    //signature row
    Row signRow = sheet.createRow(rownum);

    sheet.addMergedRegion(new CellRangeAddress(rownum, rownum, 0, 5));//Volume Util

    Cell  supervisorCell = signRow.createCell(0);
    style = workbook.createCellStyle();
    font.setBold(true);
    style.setFont(font);
    supervisorCell.setCellValue("Loading Supervisor               Transporter               Security             Driver");
    supervisorCell.setCellStyle(style);

    try {
      workbook.write(bos);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return bos;
  }

  public static XSSFCellStyle getCellStyleForIntAndLong(SXSSFWorkbook workbook) {
    final XSSFCellStyle aslignRightForIntAndLong = (XSSFCellStyle) workbook.createCellStyle();
    setBordersToCell(aslignRightForIntAndLong);
    return aslignRightForIntAndLong;
  }

  public static short getCommaSeperatedFormat(DataFormat format) {
    return format.getFormat("#,##");
  }

  public static short getFloatFormat(DataFormat format) {
    return format.getFormat("0.00##%");
  }

  public static Float roundTwoDecimals(Float d) {
    DecimalFormat twoDForm = new DecimalFormat("#.##");
    return Float.valueOf(twoDForm.format(d));
  }

  public static Double roundTwoDecimals(Double d) {
    DecimalFormat twoDForm = new DecimalFormat("#.##");
    return Double.valueOf(twoDForm.format(d));
  }


}
