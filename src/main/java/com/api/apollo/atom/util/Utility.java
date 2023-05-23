package com.api.apollo.atom.util;

import com.api.apollo.atom.constant.Case;
import com.api.apollo.atom.constant.Constants;
import com.api.apollo.atom.constant.CaseType;
import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.entity.ops.Loadslip;
import com.api.apollo.atom.security.LoginUser;
import org.apache.tomcat.util.codec.binary.Base64;
import org.hibernate.type.CustomType;
import org.hibernate.type.EnumType;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Utility {

  public static String toString(InputStream is, String encoding) {
    final StringBuilder sw = new StringBuilder();
    try {
      final InputStreamReader in = new InputStreamReader(is, encoding);
      long count = 0;
      int n;
      char[] buffer = new char[4096];
      while (-1 != (n = in.read(buffer))) {
        sw.append(buffer, 0, n);
        count += n;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return sw.toString();
  }

  public static Timestamp currentTimestamp() {
    return new Timestamp(new Date().getTime());
  }

  public static boolean isNumeric(String strNum) {
    try {
      double d = Double.parseDouble(strNum);
    } catch (NumberFormatException | NullPointerException nfe) {
      return false;
    }
    return true;
  }

  public static ApplicationUser getApplicationUserFromAuthentication(Authentication authentication) {
    LoginUser loginUser = (LoginUser) authentication.getDetails();
    return loginUser.getUser();
  }

  public static <T> CompletableFuture<List<T>> joinAll(List<CompletableFuture<T>> pendingFutures) {
    return CompletableFuture.allOf(pendingFutures.toArray(new CompletableFuture<?>[pendingFutures.size()]))
        .thenApply(v -> pendingFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
  }

  public static String camelCase(String value) {
//		return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, value.replaceAll("_", "-"));
    return value;
  }

  public static String convertStringBase64Format(String text) {
    // Encode data on your side using BASE64
    byte[] bytesEncoded = Base64.encodeBase64(text.getBytes());
    return new String(bytesEncoded);
  }

  public static Map<String, String> deriveTubeAndFlapBatchCodes() {
    Map<String, String> marketSegmentMap = new HashMap<>();
//    For Tube and Flaps, the batch code will be defaulted to BOAW (market segment=REP), BOOE (market segment=OEM), BOEX (market segment=EXPORT). User will change where ever required.
    marketSegmentMap.put(Constants.MarketSegmentType.REP.name(), "BOAW");
    marketSegmentMap.put(Constants.MarketSegmentType.OE.name(), "BOOE");
    marketSegmentMap.put(Constants.MarketSegmentType.EXPORT.name(), "BOEX");
    return marketSegmentMap;
  }

  public static BigDecimal roundingNumbersOfterDecimal(double number) {
    BigDecimal bigDecimal = new BigDecimal(number);
    return bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
  }

  public static String validateExistingLoadslip(Optional<Loadslip> optionalLoadslip) {
    if (!optionalLoadslip.isPresent()) {
      return "Loadslip is not found ";
    } else if (optionalLoadslip.get().getStatus() == Constants.LoadslipStatus.CANCELLED) {
      return "Loadlsip is found as Cancelled ";
    }
    return null;
  }

  public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
    Set<Object> seen = ConcurrentHashMap.newKeySet();
    return t -> seen.add(keyExtractor.apply(t));
  }

  public static String getCurrentDateAndTime() {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    LocalDateTime now = LocalDateTime.now();
    return dtf.format(now);
  }

  public static CustomType getCustomEnumType(String enumClass){
    Properties params = new Properties();
    params.put("enumClass", enumClass);
    params.put("useNamed", true);

    EnumType enumType = new EnumType();
    enumType.setParameterValues(params);
    return new CustomType(enumType);
  }

  public static String join(List<String> namesList) {
    return String.join(",", namesList .stream() .map(name -> ("'" + name + "'")) .collect(Collectors.toList())); }

  public static String joinInt(List<Integer> namesList) {
    return String.join(",", namesList .stream().map(name -> name+"") .collect(Collectors.toList())); }


  public static Map<String, String> getBatchCodePrefixForPlant() {
    Map<String, String> batchCodePrefixForSource = new HashMap<>();
    batchCodePrefixForSource.put("1001", "PE");
    batchCodePrefixForSource.put("1002", "LI");
    batchCodePrefixForSource.put("1004", "KA");
    batchCodePrefixForSource.put("1007", "CH");
    return batchCodePrefixForSource;
  }

  public static void parse(Object obj) {
    Class<?> objectClass = (obj).getClass();

    for (Field field : objectClass.getDeclaredFields()) {
      field.setAccessible(true);
      if (field.isAnnotationPresent(Case.class)) {
        if (field.getAnnotation(Case.class).value().equals(CaseType.TO_UPPER)) {
          try {
            if (!StringUtils.isEmpty(field.get(obj)))
            field.set(obj, field.get(obj).toString().toUpperCase());
          } catch (IllegalAccessException e) {
            e.printStackTrace();
          }

        } else if (field.getAnnotation(Case.class).value().equals(CaseType.TO_LOWER)) {
          try {
            if (!StringUtils.isEmpty(field.get(obj)))
            field.set(obj, field.get(obj).toString().toLowerCase());
          } catch (IllegalAccessException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }
}
