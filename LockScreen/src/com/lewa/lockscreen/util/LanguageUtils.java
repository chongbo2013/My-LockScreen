package com.lewa.lockscreen.util;

import java.util.Locale;

public class LanguageUtils {

	private LanguageUtils() {
	}

	public static int getCurrentLanguage() {
		int languageFlag = 3;
		if (isLanguageZhCn()) {
			languageFlag = 1;
		} else if (isLanguageZhTw()) {
			languageFlag = 2;
		} else {
			languageFlag = 3;
		}
		return languageFlag;
	}

	public static boolean isLanguageZhCn() {
		String defaultLanguage = getLanguageHeader();
		return defaultLanguage.equalsIgnoreCase("zh-cn");
	}

	public static boolean isLanguageZhTw() {
		String defaultLanguage = getLanguageHeader();
		return defaultLanguage.equalsIgnoreCase("zh-tw");
	}

	public static boolean isLanguageEnUs() {
		String defaultLanguage = getLanguageHeader();
		return defaultLanguage.equalsIgnoreCase("en-us");
	}

	private static String getLanguageHeader() {
		StringBuilder builder = new StringBuilder();
		builder.append(Locale.getDefault().getLanguage());
		builder.append("-");
		builder.append(Locale.getDefault().getCountry());
		return builder.toString().toLowerCase();
	}
}
