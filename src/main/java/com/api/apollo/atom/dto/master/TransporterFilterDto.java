package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.MTTransporter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Getter
@Setter
@NoArgsConstructor
public class TransporterFilterDto {

    private String transporterId;
    private String servprov;
    private String transporterDesc;
    private String city;
    private String state;

    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.ADMIN_PAGE_LIMIT;
    private long total;

    public List<TransporterInfoDto> itemsList = new ArrayList<>();

    public TransporterFilterDto(Page<MTTransporter> pageableTransporter){
        System.out.println("****** "+this.itemsList.size());
        this.itemsList = pageableTransporter.stream().parallel().map(TransporterInfoDto::new).collect(Collectors.toList());
        this.total = pageableTransporter.getTotalElements();
    }

    public TransporterFilterDto(TransporterFilterDto transporterFilterDto, Page<MTTransporter> pageableTransporter){

        this.transporterId = transporterFilterDto.getTransporterId();
        this.servprov = transporterFilterDto.getServprov();
        this.transporterDesc = transporterFilterDto.getTransporterDesc();
        this.city = transporterFilterDto.getCity();
        this.state = transporterFilterDto.getState();
        this.itemsList= pageableTransporter.stream().parallel().map(TransporterInfoDto::new).collect(Collectors.toList());
        this.total = pageableTransporter.getTotalElements();
    }

}
