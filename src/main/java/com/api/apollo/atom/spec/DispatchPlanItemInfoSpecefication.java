package com.api.apollo.atom.spec;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.api.apollo.atom.dto.planner.DispatchPlanItemDto;
import com.api.apollo.atom.dto.planner.DispatchPlanFilterDto;
import com.api.apollo.atom.entity.plan.DispatchPlanItemInfo;

public class DispatchPlanItemInfoSpecefication implements Specification<DispatchPlanItemInfo>{
	
	private DispatchPlanFilterDto filterDto;
	
	public DispatchPlanItemInfoSpecefication(DispatchPlanFilterDto filterDto) {
		this.filterDto = filterDto;
	}
	
	

	@Override
	public Predicate toPredicate(Root<DispatchPlanItemInfo> root, CriteriaQuery<?> query,
			CriteriaBuilder criteriaBuilder) {
		
		Predicate predicate = criteriaBuilder.disjunction();
		
		/*if (!StringUtils.isEmpty(filterDto.getDestinationCode())) {
            predicate.getExpressions()
                    .add(criteriaBuilder.equal(root.get("name"), filter.getName()));
        }*/
		
		
		 List<Predicate> predicates = new ArrayList<Predicate>();
		// predicates.add(builder.like(builder.lower(root.<String>get("destinationLocation")), "%" + searchDto.getDestinationCode().toLowerCase() + "%"));
		return null;
	}

}
