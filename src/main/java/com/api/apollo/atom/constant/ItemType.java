package com.api.apollo.atom.constant;

public enum ItemType {

  ZRMS, ZSFG, ZFGS, XFIN, ZACC, ZTTF;

  public static ItemType toString(String value) {
    switch (value) {
      case "ZRMS":
        return ItemType.ZRMS;
      case "ZSFG":
        return ItemType.ZSFG;
      case "ZFGS":
        return ItemType.ZFGS;
      case "XFIN":
        return ItemType.XFIN;
      case "ZACC":
        return ItemType.ZACC;
      case "ZTTF":
        return ItemType.ZTTF;
    }
    return null;
  }
}
