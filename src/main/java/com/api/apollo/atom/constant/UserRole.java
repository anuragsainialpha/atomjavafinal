package com.api.apollo.atom.constant;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum UserRole {

  /*---------------ADMIN -----------*/
  ADMIN,

  /* ------------- FGS--------------- */
  DP_EXP, DP_OEM, DP_REP, // FGS-planner
  TRP, // Transporter
  PLT_PLN, PLT_3PL, RDC_3PL, // FGS-Operations
  PLT_GAT, // Gate Security
  CHA,

  /* ------------ RDC ----------------- */
  RDC_PLN, // RDC Planner and Operations
  RDC_GAT,  // RDC GATE SECURITY


  /*------------- JIT ------------------*/
  JIT_PLN, // Jit Operations
  JIT_GAT, // Jit Gate Security


  /*------------- JIT ------------------*/
  ABU_PLN, // ABU Operations


  FPL, // Shipment Exports

  /*---------ROLES WITH DP_REP ACCESS----------*/
  L1_MGR ,
  L2_MGR ;

  public boolean isFGSUserRole() {
    //FGS Planner & Operations role
    return Stream.of(UserRole.DP_REP, UserRole.PLT_PLN).anyMatch(role -> role.equals(this));
  }

  public boolean isFGSOperationsOrFGSGate() {
    //FGS Operations roles and FGS Gate Security Role
    return Stream.of(UserRole.PLT_PLN, UserRole.PLT_3PL, UserRole.RDC_3PL, UserRole.PLT_GAT).anyMatch(role -> role.equals(this));
  }

  public boolean isRDCPlannerOrRDCGate() {
    return Stream.of(UserRole.RDC_PLN, UserRole.RDC_GAT).anyMatch(role -> role.equals(this));
  }

  public boolean isRoleGateSecurity() {
    return Stream.of(UserRole.PLT_GAT, UserRole.RDC_GAT, UserRole.JIT_GAT).anyMatch(role -> role.equals(this));
  }

  public boolean isFGSOrRDCOperationsRole() {
    //FGS Planner & Operations role
    return Stream.of(UserRole.PLT_PLN, UserRole.RDC_PLN).anyMatch(role -> role.equals(this));
  }

//  Roles which have DP_REP access
  public static List<UserRole> getDPREPAccessRoles() {
    return Stream.of(DP_REP, L1_MGR, L2_MGR).collect(Collectors.toList());
  }

  public static  List<UserRole> getADMINScreenRoles(){
    return Stream.of(ADMIN, L1_MGR, L2_MGR).collect(Collectors.toList());
  }

}
