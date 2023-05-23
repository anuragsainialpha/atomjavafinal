package com.api.apollo.atom.dto.core;

import com.api.apollo.atom.entity.master.MTItem;
import com.api.apollo.atom.entity.master.MTLocation;
import com.api.apollo.atom.entity.master.MTTruckTypeInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class MasterDataDto {

	private String destination;
	private String materialCode;
	private String batchCode;
	private String materialGrp;
	private String truckType;
	private String truckDescription;
	private String transporter;
	private String materialDesc;
	private String gpsProvider;
	private int index = 0;

	private List<SearchResultDto> searchResult = new ArrayList<>();
	private long total;

	public MasterDataDto(Page<MTLocation> locations, Page<MTItem> items, Page<String> batchCodes,
			Page<String> materialGrps, Page<MTTruckTypeInfo> truckTypes, Page<MTTruckTypeInfo> truckDescriptiones, Page<String> transporters, Page<String> gpsProviders ,List<Object[]> objects) {
		if (locations != null) {
			this.searchResult = locations.stream().parallel()
					.map(location -> new SearchResultDto(location.getId(), location.getDescription()))
					.collect(Collectors.toList());
			this.total = locations.getTotalElements();
		}
		if (items != null) {
			this.searchResult = items.stream().parallel()
					.map(item -> new SearchResultDto(item.getId(), item.getDescription())).collect(Collectors.toList());
			this.total = items.getTotalElements();
		}
		if (batchCodes != null) {
			this.searchResult = batchCodes.stream().parallel().distinct().map(batchCode -> new SearchResultDto(batchCode))
					.collect(Collectors.toList());
			this.total = batchCodes.getTotalElements();
		}
		if (materialGrps != null) {
			this.searchResult = materialGrps.stream().parallel().map(materialGrp -> new SearchResultDto(materialGrp))
					.collect(Collectors.toList());
			this.total = materialGrps.getTotalElements();
		}
		if (truckTypes != null) {
			this.searchResult = truckTypes.stream().parallel().filter(distinctByKey(truckType -> truckType.getType())).map(
					truckType -> new SearchResultDto(truckType.getType(), String.valueOf(truckType.getTteCapacity())))
					.collect(Collectors.toList());
			this.total = truckTypes.getTotalElements();
		}

		if (truckDescriptiones != null) {
//			this.searchResult = truckDescriptiones.stream().parallel().map(truckDescription -> new SearchResultDto(truckDescription))
//					.collect(Collectors.toList());
//			this.total = truckDescriptiones.getTotalElements();

			this.searchResult = truckDescriptiones.stream().parallel().filter(distinctByKey(truckDescription -> truckDescription.getType())).map(
					truckDescription -> new SearchResultDto(truckDescription.getType(), String.valueOf(truckDescription.getTteCapacity())))
					.collect(Collectors.toList());
			this.total = truckDescriptiones.getTotalElements();
		}

		if (transporters != null) {
			this.searchResult = transporters.stream().parallel().map(transporter -> new SearchResultDto(transporter))
					.collect(Collectors.toList());
			this.total = transporters.getTotalElements();
		}
		if (gpsProviders != null) {
			this.searchResult = gpsProviders.stream().parallel().map(provider -> new SearchResultDto(provider))
					.collect(Collectors.toList());
			this.total = gpsProviders.getTotalElements();
		}
		if (objects!=null){
			this.searchResult = objects.stream().parallel()
					.map(object -> new SearchResultDto(object[0]!=null?object[0].toString():null, object[1] != null ? object[1].toString(): null))
					.collect(Collectors.toList());
			this.total = objects.size();
		}
	}

	
	public MasterDataDto(List<Object[]> locations) {
		if (locations != null) {
			this.searchResult = locations.stream().parallel()
					.map(location -> new SearchResultDto(location[0]!=null?location[0].toString():null, location[1] != null ? location[1].toString(): null))
					.collect(Collectors.toList());
			this.total = locations.size();
		}
	}
	
	

	@Getter
	@Setter
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public class SearchResultDto {
		private String value;
		private String description;

		public SearchResultDto(String value) {
			this.value = value;
		}

		public SearchResultDto(String value, String description) {
			this.value = value;
			this.description = description;
		}
	}
	private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

}
