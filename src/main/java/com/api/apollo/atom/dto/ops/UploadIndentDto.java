package com.api.apollo.atom.dto.ops;

import com.api.apollo.atom.constant.Case;
import com.api.apollo.atom.constant.CaseType;
import com.api.apollo.atom.util.Utility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
public class UploadIndentDto {

  @JsonProperty("Dispatch Date")
  @SerializedName(value = "DispatchDate")
  private String dispatchDate;

  @JsonProperty("Source Location")
  @Case(CaseType.TO_UPPER)
  @SerializedName(value = "SouceLocation")
  private String source;

  @JsonProperty("Destination Location")
  @Case(CaseType.TO_UPPER)
  private String destination;

  @JsonProperty("Truck Type")
  @Case(CaseType.TO_UPPER)
  @SerializedName(value = "TruckType")
  private String truckType;

  @JsonProperty("Transporter")
  @Case(CaseType.TO_UPPER)
  @SerializedName(value = "Transporter")
  private String transporter;

  @JsonProperty("Category")
  @Case(CaseType.TO_UPPER)
  @SerializedName(value = "Category")
  private String materailGrp;

  @JsonProperty("Count")
  @SerializedName(value = "Count")
  private Integer indented;

  @JsonProperty("comments")
  @Case(CaseType.TO_UPPER)
  @SerializedName(value = "comments")
  private String comments;

  @JsonProperty("TTE")
  @SerializedName(value = "TTE")
  private Double tte;

  @JsonProperty("Port Of Discharge")
  @Case(CaseType.TO_UPPER)
  @SerializedName(value = "POD")
  private String pod;

  @JsonProperty("DEST_COUNTRY")
  @Case(CaseType.TO_UPPER)
  @SerializedName(value = "DEST_COUNTRY")
  private String destCountry;

  @JsonProperty("Country Of Destination")
  @Case(CaseType.TO_UPPER)
  @SerializedName(value = "DestCountryName")
  private String destCountryName;

  @JsonProperty("DEST DESCRIPTION")
  @Case(CaseType.TO_UPPER)
  private String destinationDescription;

  @JsonCreator
  public UploadIndentDto() {
  }

  @JsonCreator
  public UploadIndentDto(@JsonProperty("Dispatch Date")String dispatchDate, @JsonProperty("Source Location")String source, @JsonProperty("Destination Location")String destination, @JsonProperty("Truck Type")String truckType, @JsonProperty("Transporter")String transporter,
                         @JsonProperty("Category")String materailGrp, @JsonProperty("Count")Integer indented, @JsonProperty("comments")String comments, @JsonProperty("TTE")Double tte, @JsonProperty("Port Of Discharge")String pod,  @JsonProperty("DEST_COUNTRY")String destCountry,
                         @JsonProperty("Country Of Destination")String destCountryName,  @JsonProperty("DEST DESCRIPTION")String destinationDescription) {
    this.dispatchDate = dispatchDate;
    this.source = source;
    this.destination = destination;
    this.truckType = truckType;
    this.transporter = transporter;
    this.materailGrp = materailGrp;
    this.indented = indented;
    this.comments = comments;
    this.tte = tte;
    this.pod = pod;
    this.destCountry = destCountry;
    this.destCountryName = destCountryName;
    this.destinationDescription = destinationDescription;

    Utility.parse(this);
  }
}
