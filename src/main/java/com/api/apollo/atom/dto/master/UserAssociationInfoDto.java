
package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.entity.UmUserAssociation;
import com.api.apollo.atom.entity.master.UmUserAssociationEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class UserAssociationInfoDto {


    @JsonProperty("userId")
    @SerializedName(value = "userId")
    private String userId;

    @JsonProperty("associationIdentifier")
    @SerializedName(value = "associationIdentifier")
    private String associationIdentifier;

    @JsonProperty("associationValue")
    @SerializedName(value = "associationValue")
    private String associationValue;

    @JsonProperty("insertUser")
    @SerializedName(value = "insertUser")
    private String insertUser;

    @JsonProperty("updateUser")
    @SerializedName(value = "updateUser")
    private String updateUser;

    @JsonProperty("insertDate")
    @SerializedName(value = "insertDate")
    private Date insertDate;

    @JsonProperty("updateDate")
    @SerializedName(value = "updateDate")
    private Date updateDate;


    @JsonProperty("uaId")
    @SerializedName(value = "uaId")
    private Integer uaId;


    public UserAssociationInfoDto(UmUserAssociationEntity userAssociation) {
        this.userId = userAssociation.getUserId();
        this.associationIdentifier = userAssociation.getAssociationIdentifier();
        this.associationValue = userAssociation.getAssociationValue();
        this.insertUser = userAssociation.getInsertUser();
        this.updateUser = userAssociation.getUpdateUser();
        this.insertDate = userAssociation.getInsertDate();
        this.updateDate = userAssociation.getUpdateDate();
        this.uaId=userAssociation.getUaId();
    }
}
