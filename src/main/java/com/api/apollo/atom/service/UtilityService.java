package com.api.apollo.atom.service;

import com.api.apollo.atom.entity.ops.IndentDetails;
import com.api.apollo.atom.entity.ops.IndentSummary;
import com.api.apollo.atom.entity.ops.TruckReport;

import java.util.List;
import java.util.Map;

public interface UtilityService {


  void updateLoadslipAndShipmentStatus(TruckReport truckReport, String plantCode);

  String getLinkedPlntLoc(String source, String destination);

  List<String> getLinkedExtWareHouse(String plantSource);

  /*Generating TransporterSAPCode*/
  Map<String, Object> calculateTrasporterSapCode(IndentSummary indentSummary, IndentDetails indentDetails);
}
