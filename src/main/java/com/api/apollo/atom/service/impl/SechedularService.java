package com.api.apollo.atom.service.impl;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.entity.ops.TruckReport;
import com.api.apollo.atom.entity.ops.TruckReportSummary;
import com.api.apollo.atom.repository.ops.TruckReportRepository;
import com.api.apollo.atom.repository.ops.TruckReportSummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;

@Component
public class SechedularService {
    private Long waitingMorethen3H;

    @Autowired
    private TruckReportRepository truckReportRepository;

    @Autowired
    private TruckReportSummaryRepository truckReportSummaryRepository;

    private static final long HOUR = 3600 * 1000; // in milli-seconds.


//    @Scheduled(cron = "0 0/10 * * * *")
//    @Scheduled(cron = "*/30 * * * * *")
    public void saveTrucksCountBasedOnStatus() {

        //find  distinct souce locations present in Truck_Reporting Table
        List<String> plantSouceList = truckReportRepository.getPlantSouce().stream().distinct().collect(Collectors.toList());
        plantSouceList.forEach(souceLocation -> {
            waitingMorethen3H = 0L;
            List<TruckReport> truckReportList = truckReportRepository.findAllBySourceLocation(souceLocation);
            Map<Constants.TruckReportStatus, Long> truckStatusMap = truckReportList.stream().collect(Collectors.groupingBy(TruckReport::getStatus, counting()));
            Long gatedInCount = truckStatusMap.keySet().contains(Constants.TruckReportStatus.GATED_IN) ? truckStatusMap.get(Constants.TruckReportStatus.GATED_IN) : 0L;
            Long gatedOutCount = truckStatusMap.keySet().contains(Constants.TruckReportStatus.INTRANSIT) ? truckStatusMap.get(Constants.TruckReportStatus.INTRANSIT) : 0L;
            Long reportedTrucksCount = truckStatusMap.keySet().contains(Constants.TruckReportStatus.REPORTED) ? truckStatusMap.get(Constants.TruckReportStatus.REPORTED) : 0L;
            truckReportList.forEach(truckStatus -> {
                if (truckStatus.getStatus().equals(Constants.TruckReportStatus.REPORTED)) {
                    Date exceededDate = new Date(truckStatus.getReportDate().getTime() + 3 * HOUR);
                    if (new Date().after(exceededDate))
                        waitingMorethen3H += 1;

                }

            });
            TruckReportSummary truckReportSummary = new TruckReportSummary();
            truckReportSummary.setLocation(souceLocation);
            truckReportSummary.setGatedInCount(gatedInCount.intValue());
            truckReportSummary.setReportedCount(reportedTrucksCount.intValue());
            truckReportSummary.setExcessWaitingCount(waitingMorethen3H.intValue());
            truckReportSummary.setInsertUser("SYSTEM");

            truckReportSummaryRepository.save(truckReportSummary);

        });

    }
}
