
package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.entity.master.UserEntity;
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
public class UserInfoDto {

    @JsonProperty("userId")
    @SerializedName(value = "userId")
    private String userId;

    @JsonProperty("status")
    @SerializedName(value = "status")
    private String status;

    @JsonProperty("userRoleId")
    @SerializedName(value = "userRoleId")
    private String roleId;

    @JsonProperty("password")
    @SerializedName(value = "password")
    private String password;

    @JsonProperty("plantCode")
    @SerializedName(value = "plantCode")
    private String plantCode;

    @JsonProperty("firstName")
    @SerializedName(value = "firstName")
    private String firstName;

    @JsonProperty("lastName")
    @SerializedName(value = "lastName")
    private String lastName;

    @JsonProperty("emailId")
    @SerializedName(value = "emailId")
    private String emailId;

    @JsonProperty("lastLoginDate")
    @SerializedName(value = "lastLoginDate")
    private Date lastLoginDate;

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



    public UserInfoDto(UserEntity user) {

        this.userId = user.getUserId();
        this.emailId = user.getEmailId();
        this.firstName = user.getFirstName();
        this.lastLoginDate = user.getLastLoginDate();
        this.lastName = user.getLastName();
        this.password = user.getPassword();
        this.plantCode = user.getPlantCode();
        this.roleId = user.getRoleId();
        this.status = user.getStatus();
        this.insertUser = user.getInsertUser();
        this.updateUser = user.getUpdateUser();
        this.insertDate = user.getInsertDate();
        this.updateDate = user.getUpdateDate();

    }
}
