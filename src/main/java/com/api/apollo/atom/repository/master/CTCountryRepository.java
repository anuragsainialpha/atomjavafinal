package com.api.apollo.atom.repository.master;

import com.api.apollo.atom.entity.master.CTCountry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface CTCountryRepository extends PagingAndSortingRepository<CTCountry, String> {

  @Query(nativeQuery = true,value = "(select ct.country_code as value, ct.country_name as description from ct_country ct " +
      "where LOWER(ct.country_name) LIKE LOWER(concat('%',concat(?1,'%'))) )")
  List<Object[]> findAllDestLocationsWithTypePORT(String id ,Pageable pageable);

  CTCountry findByCountryName(String countryName);

  boolean existsByCountryName(String countryName);

  boolean existsByCountryCodeAndIsExport(String countryCode, String isExport);

  CTCountry findByCountryCode(String countryCode);

  List<CTCountry> findAllByCountryCodeIn(List<String> ctCountryList);
}
