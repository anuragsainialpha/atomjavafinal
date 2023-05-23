package com.api.apollo.atom.projections;


public interface ShipmentLoadslipsProjection {

  String getLoadslipId();

  String getShipmentId();

  String getSourceLoc();

  String getDestLoc();

  String getStoSoNum();

  String getDelivery();

  String getSapInvoice();

  String getSapInvoiceDate();

  String getLrNum();

  Integer getTotTyres();

  Integer getTotTubes();

  Integer getTotFlaps();

  Integer getTotValve();

  String getGrn();

  String getStatus();

  Double getTteUtil();

  Double getWeightUtil();

  String getStopType();

  String getTruckType();

  String getTruckNumber();

  String getServprov();

  Integer getQty();

  String getGatedOutDate();

}
