package com.api.apollo.atom.dto.ops;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.MTItem;
import com.api.apollo.atom.entity.master.MTTransporter;
import com.api.apollo.atom.entity.master.MTTruckTypeInfo;
import com.api.apollo.atom.util.DateUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class IndentDto {

  private List<UploadIndentDto> indents = new ArrayList<UploadIndentDto>();

  public List<String> validateIndents() {
    List<String> errorMessages = new ArrayList<String>();
    if (indents.size() == 0)
      return Collections.singletonList("Please create atleast one indent !");
    this.indents.parallelStream().forEach(indent -> {
      int index = indents.indexOf(indent);
      if (StringUtils.isEmpty(indent.getDispatchDate())) {
        errorMessages.add("dispatch date is missing ! at row:"+(index+2));
      }
      if (StringUtils.isEmpty(indent.getSource()))
        errorMessages.add("source location is missing ! at row:"+(index+2));
      if (StringUtils.isEmpty(indent.getDestination()))
        errorMessages.add("destination location is missing ! at row:"+(index+2));
      if (StringUtils.isEmpty(indent.getTruckType()))
        errorMessages.add("truck type is missing ! at row:"+(index+2));
      if (StringUtils.isEmpty(indent.getTransporter()))
        errorMessages.add("transporter is missing ! at row:"+(index+2));
			/*if (StringUtils.isEmpty(indent.getMaterailGrp()))
				errorMessages.add("material group is missing !");*/
      if (StringUtils.isEmpty(indent.getIndented()))
        errorMessages.add("trucks count is missing ! at row:"+(index+2));
      if (indent.getIndented() == null || indent.getIndented() == 0 )
        errorMessages.add("please enter valid required trucks ! at row:"+(index+2));
      //check date formart
      /*if (!(DateUtils.isDateValid(indent.getDispatchDate(), Constants.PLAN_RECORD_DATE_FORMAT)))
        errorMessages.add(String.format("Dispatch data %s not valid date", indent.getDispatchDate()));*/
      if(!StringUtils.isEmpty(indent.getDispatchDate()) && DateUtils.constantDateFormat(indent.getDispatchDate()) == null){
        errorMessages.add(String.format("Dispatch data %s not valid date", indent.getDispatchDate()));
      }
      /*if (DateUtils.formatDate(indent.getDispatchDate(),
          Constants.PLAN_RECORD_DATE_FORMAT).before(DateUtils.atStartOfDay(new Date()))) {
        errorMessages.add(String.format("Dispatch date %s should not be past date", indent.getDispatchDate()));
      }*/
      if (DateUtils.constantDateFormat(indent.getDispatchDate()) != null && DateUtils.constantDateFormat(indent.getDispatchDate()).before(DateUtils.atStartOfDay(new Date()))) {
        errorMessages.add(String.format("Dispatch date %s should not be past date", indent.getDispatchDate()));
      }
      if(!StringUtils.isEmpty(indent.getSource()) && !StringUtils.isEmpty(indent.getDestination())){
        if(indent.getSource().equalsIgnoreCase(indent.getDestination())){
          errorMessages.add("Destination location should not be same as source location at row "+(index+2));
        }
      }
    });
    return errorMessages;
  }

  public List<String> validateTruckTypes(List<MTTruckTypeInfo> masterTruckTypes, List<String> uniqueTruckTypes) {
    return uniqueTruckTypes.stream().parallel()
        .filter(type -> !(masterTruckTypes.stream().parallel().anyMatch(mt -> mt.getType().equals(type))))
        .collect(Collectors.toList());
  }

  public List<String> validateMaterialGrps(List<MTItem> masterMaterialGrpItems, List<String> uniqueMaterialGrps) {
    return uniqueMaterialGrps.stream().parallel()
        .filter(materialGrp -> !(masterMaterialGrpItems.stream().anyMatch(masterItem -> masterItem.getCategory().equals(materialGrp))))
        .collect(Collectors.toList());
  }

  public List<String> validateMaterialscmGrps(List<String> masterMtMaterialGrpScmGroups, List<String> uniqueMaterialGrps) {
    return uniqueMaterialGrps.stream().parallel()
        .filter(materialGrp -> !(masterMtMaterialGrpScmGroups.stream().anyMatch(scmgroup -> scmgroup.equals(materialGrp))))
        .collect(Collectors.toList());
  }

  public List<String> validateTransporters(List<MTTransporter> masterTransporters, List<String> uniqueTransporters) {
    return uniqueTransporters.stream().parallel()
        .filter(transporter -> !(masterTransporters.stream().anyMatch(mt -> mt.getServprov().equals(transporter))))
        .collect(Collectors.toList());
  }


  public List<String> validateDestination(List<String> masterDestinations, List<String> uniqueDestinations) {
    return uniqueDestinations.stream().parallel()
        .filter(destination -> !(masterDestinations.stream().anyMatch(destLoc -> destLoc.equals(destination))))
        .collect(Collectors.toList());
  }
}
