package com.api.apollo.atom.dto.ops;

import com.api.apollo.atom.constant.UserRole;
import com.api.apollo.atom.entity.ApplicationUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ExportTrackerFilter {

  private String shipmentId;

  private String containerNum;

  private String truckNumber;

  private String sourceLoc;

  private String destLoc;

  private List<String> truckType;

  private String transporter;

  public boolean validateExportTracker(ApplicationUser loggedInUser,String transporter){
    List<UserRole> roleList = Arrays.asList(UserRole.CHA, UserRole.TRP);
    boolean isValidTransporter = roleList.contains(loggedInUser.getRole()) && StringUtils.isEmpty(transporter);
    if(!isValidTransporter ){
      this.setSourceLoc( !StringUtils.isEmpty(this.sourceLoc) ? this.sourceLoc:null );
    }else{
      this.setTransporter(transporter);
    }
    return isValidTransporter ;
  }


}
