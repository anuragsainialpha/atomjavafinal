package com.api.apollo.atom.dto.ops;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.ops.IndentSummary;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class IndentFilterDto {

  private String fromDispatchDate;
  private String toDispatchDate;
  private String dispatchDate;
  private String destination;
  private List<String> truckType = new ArrayList<>();
  private String indentID;
  private String transporter;
  private List<String> materialGrp = new ArrayList<>();
  private String source;

  private String destCountry;
  /*Multi-selection of status in view/modify Indent*/
  /*private List<String> status;*/
  private List<String> status = new ArrayList<>();
  private int index = Constants.PAGE_INDEX;
  private int pageLength = Constants.PAGE_LIMIT;
  private long total;
  private List<IndentInfoDto> indents = new ArrayList<>();

  public IndentFilterDto(Page<IndentSummary> pagableIndents) {
    this.indents = pagableIndents.stream().parallel().map(IndentInfoDto::new).collect(Collectors.toList());
    this.total = pagableIndents.getTotalElements();
  }


  /*Used for Optimizing view-indents API*/
  public IndentFilterDto(Page<IndentSummary> pagableIndents, String dummyVariable) {
    this.indents = pagableIndents.stream().parallel().map(indentSummary -> new IndentInfoDto(indentSummary, "")).collect(Collectors.toList());
    this.total = pagableIndents.getTotalElements();
  }


  public boolean isIndentFilterByTransporter() {
    return (!StringUtils.isEmpty(this.fromDispatchDate) && !StringUtils.isEmpty(this.toDispatchDate)) || !StringUtils.isEmpty(this.indentID) ||
        !StringUtils.isEmpty(this.destination) || !StringUtils.isEmpty(this.source) || (this.truckType != null && this.truckType.size() > 0) || (this.status != null && this.status.size() > 0) ||
        (this.materialGrp != null && this.materialGrp.size() > 0);
  }

  public boolean isIndentFilterByFGS() {
    return !StringUtils.isEmpty(this.fromDispatchDate) || !StringUtils.isEmpty(this.toDispatchDate) || !StringUtils.isEmpty(this.indentID) || !StringUtils.isEmpty(this.transporter)
        || !StringUtils.isEmpty(this.destination) || (this.truckType != null && this.truckType.size()>0) ||  !(this.materialGrp != null && this.materialGrp.isEmpty())
        || !StringUtils.isEmpty(this.source) || (this.status != null && this.status.size()>0) || !StringUtils.isEmpty(this.dispatchDate) || !StringUtils.isEmpty(this.destCountry);
  }

}