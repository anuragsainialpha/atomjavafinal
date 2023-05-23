package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.MTItem;
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
public class MtItemFilterDto {

    private String itemId;
    private String classification;
    private String description;
    private String type;
    private String group;
    private String category;
    private String tte;
    private String loadfactor;

    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.ADMIN_PAGE_LIMIT;
    private long total;

    public List<MTItemInfoDto> items = new ArrayList<>();

    public MtItemFilterDto(Page<MTItem> pageableItems){
        this.items = pageableItems.stream().parallel().map(MTItemInfoDto::new).collect(Collectors.toList());
        //List<MTItem> itemsList = pageableItems.getContent();
        //System.out.println("Item List Size --"+itemsList.size());
        //System.out.println("Item List 0 id --"+itemsList.get(0).toString());

        //System.out.println(":::::::::::::::::::"+pageableItems.getSize());
        //System.out.println(":::::::::::::::::::"+pageableItems.getTotalElements());
        this.total = pageableItems.getTotalElements();
    }

    public MtItemFilterDto(MtItemFilterDto itemFilterDto, Page<MTItem> pageableItems){
        this.itemId = itemFilterDto.getItemId();
        this.classification = itemFilterDto.getClassification();
        this.description = itemFilterDto.getDescription();
        this.type = itemFilterDto.getType();
        this.group = itemFilterDto.getGroup();
        this.category = itemFilterDto.getCategory();
        this.tte = itemFilterDto.getTte();
        this.loadfactor = itemFilterDto.getLoadfactor();
        this.items = pageableItems.stream().parallel().map(MTItemInfoDto::new).collect(Collectors.toList());
        //List<MTItem> itemsList = pageableItems.getContent();
        //System.out.println("Item List Size --"+itemsList.size());
        //System.out.println("Item List 0 id --"+itemsList.get(0).toString());

        //System.out.println(":::::::::::::::::::"+pageableItems.getSize());
        //System.out.println(":::::::::::::::::::"+pageableItems.getTotalElements());
        this.total = pageableItems.getTotalElements();
    }
}
