package com.sample.utils;

public class Compute {
	public static Boolean affect(Double humidity, String change) {
		if ((humidity > 65) && (change.charAt(0) == '+'))
			return true;
		else
			return false;
	}
}
