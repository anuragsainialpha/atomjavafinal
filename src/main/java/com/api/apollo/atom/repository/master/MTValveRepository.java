package com.api.apollo.atom.repository.master;

import com.api.apollo.atom.entity.master.MTValve;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MTValveRepository extends JpaRepository<MTValve, String> {


  Optional<MTValve> findByItemCategory(String itemCategory);

  List<MTValve> findAllByItemCategoryIn(List<String> itemCategoryList);

}
