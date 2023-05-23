
package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.UserRole;
import com.api.apollo.atom.entity.master.UmUserRole;
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
public class UserRoleInfoDto {

    @JsonProperty("userRoleId")
    @SerializedName(value = "userRoleId")
    private String userRoleId;

    @JsonProperty("description")
    @SerializedName(value = "description")
    private String description;

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

    public UserRoleInfoDto(UmUserRole userRole) {
        this.userRoleId = userRole.getUserRoleId();
        this.description = userRole.getDescription();
        this.insertUser = userRole.getInsertUser();
        this.updateUser = userRole.getUpdateUser();
        this.insertDate = userRole.getInsertDate();
        this.updateDate = userRole.getUpdateDate();
    }
}
