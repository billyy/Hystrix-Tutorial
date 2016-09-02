package com.intuit;

import java.util.HashMap;
import java.util.Map;

public class Marshaller {
	   public static Map<String, Object> transform(Map<String, String> m) {
	   		Map<String, Object> map = new HashMap<String, Object>();
	   		map.put("symbol", m.get("t"));
	   		map.put("change", m.get("c"));
	   		return map;
	    }
}
