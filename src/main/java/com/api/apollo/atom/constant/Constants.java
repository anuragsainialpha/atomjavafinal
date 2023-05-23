package com.api.apollo.atom.constant;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Constants {

  public static final long VALIDITY_TIME_MS = 10 * 24 * 60 * 60 * 1000;// 10 days Validity
  public static final String AUTH_HEADER_NAME = "Authorization";
  public static final String AUTH_HEADER_TOKEN_PREFIX = "Bearer";
  public static final String PLAN_RECORD_DATE_FORMAT = "dd/MM/yyyy";
  public static final String DATE_FORMATE_WITH_HYPHEN = "dd-MM-yyyy";
  public static final String DATE_TIME_FORMAT = "dd/MM/yyyy hh:mm:ss a";
  public static final String INDENT_SEQ_PREFIX = "IND";
  public static final String SHIPMENT_SEQ_PREFIX = "SH";
  public static final String LOADSLIP_SEQ_PREFIX = "LS";
  public static final String INDENT_GATE_CONTROL_CODE_PREFIX = "GC";
  public static final String INDENT_GATE_CONTROL_CODE_SUFFIX = "XX";
  public static final Integer PAGE_LIMIT = 100;
  public static final Integer ADMIN_PAGE_LIMIT = 100;
  public static final Integer MASTER_PAGE_LIMIT = 10;
  public static final Integer FREIGHT_MASTER_PAGE_LIMIT = 1000;
  public static final Integer PAGE_INDEX = 0;
  public static final String GPS_ENABLED = "Y";
  public static final String GPS_DISABLED = "N";
  public static final String GAT_CALL_STATUS = "Calling";
  public static final String PLT_CALL_STATUS = "Called";
  public static final String API_AUTH = "api_auth";
  public static final String LOAD_SLIP_ID = "load_slip_id";
  public static final String Shipment_id = "Shipment_id";
  public static final String SAP_API_RESPONSE = "Api_response";
  public static final String SAP_RESPONSE = "Sap_response";
  public static final String SHIPMENT_STOP_PROCEDURE_NAME = "atl_business_flow_pkg.insert_shipment_stops";
  public static final Integer LOADSLIP_FREEZ_INDEX = 4;// CREATED(0), PRINTED(1), LOADING(2), LOADED(3), SENT_SAP(4)== CONFIRMED, INTRANSIT(5), UNLOADING(6), DELIVERED(7);
  public static HashMap<LoadslipStatus, Integer> loadslipEventsIndexMap;
  public static final String ORA_DATE_FORMAT = "yyyy-mm-dd hh:mm:ss.sss";
  public static final String ORA_EFF_DATE_FORMAT = "dd-MMM-yyyy";

  public static final List<String> formatStrings = Arrays.asList( DATE_FORMATE_WITH_HYPHEN, PLAN_RECORD_DATE_FORMAT);


  static {
    loadslipEventsIndexMap = new HashMap<LoadslipStatus, Integer>();
    for (int i = 0; i < LoadslipStatus.values().length; i++) {
      loadslipEventsIndexMap.put(LoadslipStatus.values()[i], i);
    }
  }

  public static boolean isDPREPRole(UserRole role) {
    if (UserRole.getDPREPAccessRoles().contains(role)){
      return true;
    }else {
      return false;
    }
  }

  public enum Status {
    OPEN, ERROR, CLOSED, CANCELLED, DECLINED, PARTIALLY_CONFIRMED, CONFIRMED, COMPLETED
  }

  public enum DelInvType {
    //Export and JIT Types
    FGS_EXP, JIT_OEM, RDC_EXP;

    public static boolean isExportOrJIT(String lsOrderType) {
      return Arrays.stream(DelInvType.values()).anyMatch(type -> type.name().equalsIgnoreCase(lsOrderType));
    }
  }


  public enum DispatchPlanItemAppStatus {
    APPROVAL_PENDING, APPROVED, APPROVED_PART;

    public static List<DispatchPlanItemAppStatus> getFGSPlannerStatus() {
      return Stream.of(DispatchPlanItemAppStatus.APPROVAL_PENDING, DispatchPlanItemAppStatus.APPROVED,
          DispatchPlanItemAppStatus.APPROVED_PART).collect(Collectors.toList());
    }

    public String getStatus() {
      return this.name().replaceAll("_", " ");
    }
  }

  public enum PlanStatus {
    SUCCESS, ERROR
	}

  public enum UploadPlanStatus {
    PLAN_SUCCESS, PLAN_PENDING, NO_DATA
	}

  public enum TruckReportStatus {
    BAY_ASSIGNED, CALL_TRUCK, RELEASED, REJECT,// this is for internal condition not for report status value

    REPORTED, GATED_IN, LOADING, UNLOADING, LOADED, INTRANSIT, GEOFENCE_IN, RECEIVED, DELIVERED, NORMAL, REJECTED,
    ASSIGN_LS, COMPLETED;

    public static List<TruckReportStatus> getFGSSecurityReportedStatuses() {
      return Stream.of(TruckReportStatus.REPORTED, TruckReportStatus.GATED_IN, TruckReportStatus.LOADING, TruckReportStatus.ASSIGN_LS,
          TruckReportStatus.LOADED).collect(Collectors.toList());
    }

    public static List<TruckReportStatus> getRDCSecurityReportedStatuses() {
      return Stream.of(TruckReportStatus.REPORTED, TruckReportStatus.GATED_IN, TruckReportStatus.UNLOADING, TruckReportStatus.DELIVERED).collect(Collectors.toList());
    }

    public static List<TruckReportStatus> getTruckHistoryStatus() {
      return Stream.of(TruckReportStatus.INTRANSIT, TruckReportStatus.COMPLETED).collect(Collectors.toList());
    }

    public static List<TruckReportStatus> getIntransitTruckStatus() {
      return Stream.of(TruckReportStatus.INTRANSIT).collect(Collectors.toList());
    }

    public static List<TruckReportStatus> getInventoryTrucks() {
      return Stream.of(TruckReportStatus.REPORTED, TruckReportStatus.GATED_IN, TruckReportStatus.ASSIGN_LS, TruckReportStatus.LOADING, TruckReportStatus.LOADED,
          TruckReportStatus.UNLOADING, TruckReportStatus.DELIVERED).collect(Collectors.toList());
    }

  }


  public enum TruckStatus {
    NORMAL, BLACKLISTED
	}

  public enum BayStatus {
    OPEN, // status for MTLocationBay bayStatus
    ARVD, WAIT, CALL, LSPB, LEPB, RELEASE; // statuses for TruckReport bayStatus

//    private String bStatus;
//
//    private BayStatus(String bStatus) {
//      this.bStatus = bStatus;
//    }
//
//    public String getBStaus() {
//      return bStatus;
//    }
  }


  public enum LoadslipStatus {
    CREATED, PRINTED, LOADING, LOADED, SENT_SAP, INTRANSIT, UNLOADING, DELIVERED, CANCELLED, COMPLETED;

    public static boolean isExist(String slipStatus) {
      return Arrays.stream(LoadslipStatus.values()).anyMatch(status -> status.name().equalsIgnoreCase(slipStatus));
    }
    public static List<LoadslipStatus> getCompletedLoadslipStatuses() {
      return Stream.of(LoadslipStatus.INTRANSIT, LoadslipStatus.COMPLETED, LoadslipStatus.CANCELLED).collect(Collectors.toList());
    }
  }

  public enum LoadslipEvent {
    ARRIVED_BAY, LOADING_START, LOADING_END, CONFIRM, RELEASED, RETRY
	}

  public enum SystemType {
    FGS, RMS
	}

  public enum StopType {
    S, MD, MP, MPMD, SHR
	}

  public enum TransportersMode {
    TL, FLATBED, EXPRESS, AIR
	}

  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public class CodesInfo {
    public final String LOCATION_CODE = "C1";
    public final String MATERIAL_CODE = "C2";
    public final String ITEM_TTE_CODE = "C3";
    public final String ITEM_CATEGORY_CODE = "C4";
    public final String BATCH_CODE = "C5";
    public final String DUPLICATE_RECORD_CODE = "C6";

  }

  public enum ErrorCode {
    C1("LOCATION_CODE"), C2("MATERIAL_CODE"), C3("ITEM_TTE"), C4("ITEM_CATEGORY"), C5("BATCH_CODE"),
    C6("DUPLICATE_RECORD");

    private String desc;

    public String getDesc() {
      return this.desc.replace("_", " ");
    }

    ErrorCode(String desc) {
      this.desc = desc;
    }
  }

  public enum MarketSegmentType {
    REP, OE, EXPORT
	}

  public enum ItemClassification {
    TYRE, TUBE, FLAP, VALVE
	}

  // TODO: Need to maintain same Values for ITEM classification in MT_ITEM table and category in MT_BATCH_CODES table
  public enum ItemCategory {
    Tyre, Tube, Flap, Valve
	}

















  ///ADMIN

//  public enum PlanStatus {
//    SUCCESS, ERROR;
//  }
//
//  public enum UploadPlanStatus {
//    PLAN_SUCCESS, PLAN_PENDING, NO_DATA;
//  }

  public enum MTItemStatus {
    MTITEM_SUCCESS, MTITEM_PENDING, NO_DATA
	}

  public enum FreightStatus {
    FREIGHT_STATUS, FREIGHT_PENDING, NO_DATA
	}

  public enum MTOEBOMStatus {
    FREIGHT_STATUS, FREIGHT_PENDING, NO_DATA
	}
  public enum MTREPBOMFreightStatus {
    FREIGHT_STATUS, FREIGHT_PENDING, NO_DATA
	}
  public enum MTPLANTITEMStatus {
    FREIGHT_STATUS, FREIGHT_PENDING, NO_DATA
	}


}
