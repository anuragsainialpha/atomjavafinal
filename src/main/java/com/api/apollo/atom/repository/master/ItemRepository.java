package com.api.apollo.atom.repository.master;

import com.api.apollo.atom.entity.master.MTItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<MTItem, String> {
  @Query("SELECT I.id,I.tte from MTItem I")
  List<MTItem> findAllItemIdsAndTee();

  List<MTItem> findByIdIn(List<String> ids);

  @Query(nativeQuery = true, value = "select item_id as itemId, item_category as category from mt_item where item_id IN (?1) ")
  List<Map<String, String>> findCategoryByIdIn(List<String> itemIds);

  Optional<MTItem> findOneById(String id);

  @Query(nativeQuery = true, name = "findNotExistItems")
  List<MTItem> findNotExistItemIds(List<String> itemIds);

  @Query("select new com.api.apollo.atom.entity.master.MTItem(I.id,I.description) from MTItem I where LOWER(I.id) like LOWER(concat(:itemId,'%')) and I.tte IS NOT NULL and I.category IS NOT NULL")
  Page<MTItem> findAllItemInfoIdLike(@Param("itemId") String itemId, Pageable pageable);

  @Query("select new com.api.apollo.atom.entity.master.MTItem(I.id,I.description) from MTItem I where LOWER(I.description) like LOWER(concat('%', concat(:materialDesc,'%'))) and I.tte IS NOT NULL and I.category IS NOT NULL")
  Page<MTItem> findAllItemInfoDescriptionLike(@Param("materialDesc") String materialDesc, Pageable pageable);

  boolean existsById(String materialCode);

  boolean existsByIdAndClassification(String materialCode, String classification);

  @Query("select new com.api.apollo.atom.entity.master.MTItem(I.id,I.tte,I.category,I.description) from MTItem I where LOWER(I.id) = LOWER(:id)")
  MTItem findTteAndCategoryById(@Param("id") String id);

  @Query(nativeQuery = true, value = "select distinct item_category from (select item_category from mt_item order by case when item_type = 'ZFGS' then 1 else 2 end) where item_category like UPPER(concat(?1,'%'))")
  Page<String> findAllByMaterialGrp(String materialGrp, Pageable pageable);

  @Query("select distinct new com.api.apollo.atom.entity.master.MTItem(I.category) from MTItem I where I.category in :categories")
  List<MTItem> findByCategoryIn(@Param("categories") List<String> categories);

  @Query("SELECT distinct I.category FROM MTItem I where I.category is not null and  I.id in :itemIds")
  List<String> findDistinctItemCategoryByItemIdIn(@Param("itemIds") List<String> itemIds);

  @Query("SELECT distinct I.category FROM MTItem I where I.category is not null and  I.id = ?1")
  String findCategoryByItemID(String itemId);

  @Query("SELECT distinct I.description FROM MTItem I where I.id = ?1")
  String getItemDescriptionByItemId(String itemId);

  @Query("SELECT distinct I.category FROM MTItem I where I.category is not null order by  I.category ASC ")
  List<String> findDistinctItemCategoryByOrderByIdAsc();

  @Query(nativeQuery = true,value = "select item_description from mt_item where item_id=?1")
  String findDescriptionById(String item_id);

  @Query("select distinct new com.api.apollo.atom.entity.master.MTItem(I.id,I.tte,I.category,I.description) from MTItem I where I.id IN (?1)")
  List<MTItem> findIdTteCategoryAndDescriptionByIdIn(List<String> categories);

}
