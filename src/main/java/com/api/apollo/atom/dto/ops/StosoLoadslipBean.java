package com.api.apollo.atom.dto.ops;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StosoLoadslipBean {

     private String loadslipId;

     private String username;

     private String password;

     private String invoiceNum;

     private boolean toDispatch;

     private List<StosoItemBean> itemsData = new ArrayList<>();

}
