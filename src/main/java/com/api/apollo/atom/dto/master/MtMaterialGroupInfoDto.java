package com.api.apollo.atom.dto.master;


import com.api.apollo.atom.entity.master.MtMaterialGroup;
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
public class MtMaterialGroupInfoDto {

    @JsonProperty("materialGroupId")
    @SerializedName(value = "materialGroupId")
    private String materialGroupId;

    @JsonProperty("description_1")
    @SerializedName(value = "description_1")
    private String description_1;

    @JsonProperty("description_2")
    @SerializedName(value = "description_2")
    private String description_2;

    @JsonProperty("scmGroup")
    @SerializedName(value = "scmGroup")
    private String scmGroup;

    @JsonProperty("insertUser")
    @SerializedName(value = "insertUser")
    private String insertUser;

    @JsonProperty("insertDate")
    @SerializedName(value = "insertDate")
    private Date insertDate;

    @JsonProperty("updateUser")
    @SerializedName(value = "updateUser")
    private String updateUser;

    @JsonProperty("updateDate")
    @SerializedName(value = "updateDate")
    private Date updateDate;

    @JsonProperty("mgId")
    @SerializedName(value = "mgId")
    private Double mgId;



    public MtMaterialGroupInfoDto(MtMaterialGroup mtmaterialgroup) {
        this.materialGroupId = mtmaterialgroup.getMaterialGroupId();
        this.description_1 = mtmaterialgroup.getDescription_1();
        this.description_2 = mtmaterialgroup.getDescription_2();
        this.scmGroup = mtmaterialgroup.getScmGroup();
        this.insertUser = mtmaterialgroup.getInsertUser();
        this.insertDate = mtmaterialgroup.getInsertDate();
        this.updateUser = mtmaterialgroup.getUpdateUser();
        this.updateDate = mtmaterialgroup.getUpdateDate();
        this.mgId = mtmaterialgroup.getMgId();
    }
}
