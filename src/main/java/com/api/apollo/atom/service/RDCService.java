package com.api.apollo.atom.service;

import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.ops.TruckReportDto;
import com.api.apollo.atom.dto.ops.TruckReportFilterDto;
import com.api.apollo.atom.entity.ApplicationUser;

public interface RDCService {

  ApiResponse getIntransitTruckInfo(TruckReportFilterDto truckReportFilterDto, ApplicationUser loggedInUser);

  ApiResponse reportIntransitTruck(TruckReportDto reportDto, ApplicationUser loggedInUser);

  ApiResponse getLoadslipsWithShipment(String shipmentId, ApplicationUser loggedInUser);
}
