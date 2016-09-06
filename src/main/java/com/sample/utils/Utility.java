package com.sample.utils;

import java.util.Map;

public class Utility {
    public static void  print(Map<String, Object> data) {
        System.out.println(data.get("symbol") + " is Correlated = " + Compute.affect(Double.valueOf(data.get("humidity").toString()), data.get("change").toString()));
         
     }
}
