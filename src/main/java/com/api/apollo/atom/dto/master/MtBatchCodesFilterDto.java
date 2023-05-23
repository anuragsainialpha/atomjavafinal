package com.api.apollo.atom.dto.master;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.entity.master.MTOeBom;
import com.api.apollo.atom.entity.master.MtBatchCodes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import javax.persistence.Column;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class MtBatchCodesFilterDto {

    private String batchCode;
    private String category;
    private String plantCode;

    private int index = Constants.PAGE_INDEX;
    private int pageLength = Constants.ADMIN_PAGE_LIMIT;
    private long total;

    public List<MtBatchCodesInfoDto> itemsList = new ArrayList<>();

    public MtBatchCodesFilterDto(Page<MtBatchCodes> pageableFreight){
       // System.out.println("****** SIZE: "+this.itemsList.size());
        this.itemsList = pageableFreight.stream().parallel().map(MtBatchCodesInfoDto::new).collect(Collectors.toList());
        this.total = pageableFreight.getTotalElements();
    }

    public MtBatchCodesFilterDto(MtBatchCodesFilterDto filterDto, Page<MtBatchCodes> pageableFreight){
        this.itemsList = pageableFreight.stream().parallel().map(MtBatchCodesInfoDto::new).collect(Collectors.toList());
        // System.out.println("itemId= "+mtOeBoms.get(0).getItemId());
        this.total = pageableFreight.getTotalElements();
    }
}
