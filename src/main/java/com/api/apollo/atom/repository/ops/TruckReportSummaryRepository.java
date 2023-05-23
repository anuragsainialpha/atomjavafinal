package com.api.apollo.atom.repository.ops;

import com.api.apollo.atom.entity.ops.TruckReportSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TruckReportSummaryRepository extends JpaRepository<TruckReportSummary, String> {

    Optional<TruckReportSummary> findById(String location);

    @Query(nativeQuery = true, value = "select mt.excess_time as excessWaitTime from mt_excess_waiting_loc_limit mt where mt.reporting_loc = ?1")
    Long findExcessWaitingTimeForLoc(String locationId);

    @Query(nativeQuery = true, value = "select mt.excess_time as excessReportWaitTime from mt_excess_waiting_rep_limit mt where mt.reporting_loc = ?1 ")
    Long findExcessReportAndWaitTime(String locationId);

    @Query(nativeQuery = true, value = "select DISTINCT tr.reporting_loc as reportLoc from truck_reporting_summary tr ORDER by tr.reporting_loc asc")
    List<String> findAllTruckStatusLocs();
}
