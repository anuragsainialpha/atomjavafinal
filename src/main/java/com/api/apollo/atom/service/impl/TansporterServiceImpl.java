package com.api.apollo.atom.service.impl;

import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.dto.core.ApiResponse;
import com.api.apollo.atom.dto.ops.IndentFilterDto;
import com.api.apollo.atom.dto.ops.IndentInfoDto;
import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.entity.master.CTCountry;
import com.api.apollo.atom.entity.ops.IndentSummary;
import com.api.apollo.atom.exception.InvalidException;
import com.api.apollo.atom.exception.UnAuthorisedException;
import com.api.apollo.atom.repository.UserRepository;
import com.api.apollo.atom.repository.master.CTCountryRepository;
import com.api.apollo.atom.repository.master.LocationRepository;
import com.api.apollo.atom.repository.ops.IndentSummaryRepository;
import com.api.apollo.atom.service.FilterService;
import com.api.apollo.atom.service.TransporterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TansporterServiceImpl implements TransporterService {

  @Autowired
  private IndentSummaryRepository indentSummaryRepository;

  @Autowired
  private FilterService filterService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private LocationRepository locationRepository;

  @Autowired
  private CTCountryRepository ctCountryRepository;

  @Override
  public ApiResponse getIndents(IndentFilterDto indentFilterDto, ApplicationUser loggedInUser) {
    Page<IndentSummary> pagableIndents;
    String transporter = (String) userRepository.findTransporterByUser(loggedInUser.getUserId());
    if (StringUtils.isEmpty(transporter)) {
      return new ApiResponse(HttpStatus.NOT_FOUND, "No Transporter is associated for this User");
    }
    indentFilterDto.setTransporter(transporter);
    if (!indentFilterDto.isIndentFilterByTransporter()) {
//      pagableIndents = indentSummaryRepository.findByTransporter(transporter, PageRequest.of(indentFilterDto.getIndex(), indentFilterDto.getPageLength(), Sort.Direction.DESC, "dispatchDate"));
      pagableIndents = indentSummaryRepository.findAllByTransporter(transporter, PageRequest.of(indentFilterDto.getIndex(), indentFilterDto.getPageLength(), Sort.Direction.DESC, "dispatchDate"));
    } else {
      pagableIndents = filterService.filterIndents(indentFilterDto, loggedInUser);
    }

    /*To get Country name from country code*/
    List<String> countryCodeList = pagableIndents.stream().parallel().map(page -> page.getDestCountry()).distinct().collect(Collectors.toList());
    List<CTCountry> ctCountryList = ctCountryRepository.findAllByCountryCodeIn(countryCodeList);
    if (ctCountryList.size() > 0) {
      pagableIndents.stream().parallel().forEach(indentSummary -> {
        ctCountryList.parallelStream().forEach(ctCountry -> {
          if (!StringUtils.isEmpty(ctCountry.getCountryCode()) && !StringUtils.isEmpty(indentSummary.getDestCountry())) {
            if (ctCountry.getCountryCode().equalsIgnoreCase(indentSummary.getDestCountry())) {
              indentSummary.setDestCountryName(ctCountry.getCountryName());
            }
          }
        });
      });
    }
    IndentFilterDto indentDto = new IndentFilterDto(pagableIndents, "");
//    if (!indentFilterDto.isIndentFilterByTransporter()){
//      indentDto =
//    }else {
//      indentDto = new IndentFilterDto(pagableIndents);
//    }
    List<String> uniqueDests = indentDto.getIndents().stream().map(IndentInfoDto::getDestination).distinct().collect(Collectors.toList());
    if(uniqueDests .size() > 0) {
      List<Map<String, String>> destWithDesc = locationRepository.findDestDescWtihDestinations(uniqueDests);
      indentDto.getIndents().parallelStream().forEach(indentInfoDto -> {
        indentInfoDto.setDestDis(destWithDesc.stream().filter(destDescMap -> destDescMap.get("destLoc").equals(indentInfoDto.getDestination())).findAny().get().get("DESTDESC"));
      });
    }
    return new ApiResponse(HttpStatus.OK, "", indentDto);
  }

  @Override
  public ApiResponse updateIndents(IndentInfoDto indentInfoDto, ApplicationUser loggedInUser) throws Exception {
    Optional<IndentSummary> indentSummaryOptional = indentSummaryRepository.findOneByIndentId(indentInfoDto.getIndentId());
    IndentSummary indentSummary = indentSummaryOptional.orElseThrow(() -> new EntityNotFoundException("Indent Not Found!"));
    String transporter = (String) userRepository.findTransporterByUser(loggedInUser.getUserId());

    if (indentSummary.getTransporter().equalsIgnoreCase(transporter)) {
      if(indentSummary.getStatus().equals(Constants.Status.CANCELLED)){
        throw new InvalidException(String.format("Not allowed to modify cancelled indent"));
      }
      if (Integer.sum(indentInfoDto.getDeclined(), indentInfoDto.getConfirmed()) > indentSummary.getNetRequested()) {
        throw new InvalidException("Sum of Confirmed Truck and Decline Truck Should not be More than Net Requested!");
      }
      indentSummary.setTransConfirmed(indentInfoDto.getConfirmed());
      indentSummary.setTransDeclined(indentInfoDto.getDeclined());
      if(indentSummary.getTransConfirmed() > 0  )
        indentSummary.setStatus(Constants.Status.PARTIALLY_CONFIRMED);
      if(indentSummary.getNetRequested()== indentSummary.getTransConfirmed())
        indentSummary.setStatus(Constants.Status.CONFIRMED);
       if(indentSummary.getIndented() == indentSummary.getTransDeclined() || indentSummary.getTransConfirmed() == 0)
         indentSummary.setStatus(Constants.Status.OPEN);
      if(indentSummary.getNetRequested() == indentInfoDto.getDeclined()) {
        indentSummary.setStatus(Constants.Status.DECLINED);
      }
      indentSummary.setUpdateUser(loggedInUser.getUserId());
      indentSummary.setUpdateDate(new Date());
      indentSummary = indentSummaryRepository.saveAndFlush(indentSummary);


    } else {
      throw new UnAuthorisedException("You are not Authorised To Do It");
    }
    return new ApiResponse(HttpStatus.OK,
        String.format("Details for indent %s updated successfully!", indentSummary.getIndentId()),
        new IndentInfoDto(indentSummary));
  }

}
