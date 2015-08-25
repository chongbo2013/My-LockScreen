package com.lewa.lockscreen.util;

import java.util.HashMap;
import java.util.TreeMap;

public class LoadDataByLocalLaunage {

	private static LoadDataByLocalLaunage instance;

	public static LoadDataByLocalLaunage getInstance() {
		if (null == instance) {
			instance = new LoadDataByLocalLaunage();
		}
		return instance;
	}

	private LoadDataByLocalLaunage() {
		initSolarTermsTreeMap();
	}

	private HashMap<String, String> mLunaHashMap = new HashMap<String, String>();
	private HashMap<String, String> mSolarTerms = new HashMap<String, String>();
	private HashMap<String, String> mSolarHolidays_TW = new HashMap<String, String>();
	private HashMap<String, String> mSolarHolidays = new HashMap<String, String>();
	private HashMap<String, String> mLunarHolidays = new HashMap<String, String>();
	private TreeMap<Integer, String> mSolarTermsTreeMap = new TreeMap<Integer, String>();

	public void finish() {
		mSolarTermsTreeMap.clear();
		mLunarHolidays.clear();
		mSolarHolidays.clear();
		mSolarHolidays_TW.clear();
		mSolarTerms.clear();
		mLunaHashMap.clear();
		instance = null;
	}

	public String getlunar(String key) {
		return mLunaHashMap.get(key);
	}

	public String getSolarTerm(int index) {
		return mSolarTerms.get(mSolarTermsTreeMap.get(index));
	}

	public String getSolarTerm(String key) {
		return mSolarTerms.get(key);
	}

	public String getSolarHolidays_TW(String key) {
		return mSolarHolidays_TW.get(key);
	}

	public String getSolarHoliday(String key) {
		return mSolarHolidays.get(key);
	}

	public String getLunarHoliday(String key) {
		return mLunarHolidays.get(key);
	}

	public void init() {
		final int launage = getCurrentLaunage();
		switch (launage) {
		case 1:
			initForCN();
			break;
		case 2:
			initForTW();
			break;
		case 3:
			initForEng();
			break;
		default:
			initForEng();
			break;
		}
	}

	private void initSolarTermsTreeMap() {
		mSolarTermsTreeMap.clear();
		mSolarTermsTreeMap.put(0, "slight_cold");
		mSolarTermsTreeMap.put(1, "great_cold");
		mSolarTermsTreeMap.put(2, "spring_begins");
		mSolarTermsTreeMap.put(3, "the_rains");
		mSolarTermsTreeMap.put(4, "insects_awaken");
		mSolarTermsTreeMap.put(5, "vernal_equinox");
		mSolarTermsTreeMap.put(6, "clear_and_bright");
		mSolarTermsTreeMap.put(7, "grain_rain");
		mSolarTermsTreeMap.put(8, "summer_begins");
		mSolarTermsTreeMap.put(9, "grain_buds");
		mSolarTermsTreeMap.put(10, "grain_in_ear");
		mSolarTermsTreeMap.put(11, "summer_solstice");
		mSolarTermsTreeMap.put(12, "slight_heat");
		mSolarTermsTreeMap.put(13, "great_heat");
		mSolarTermsTreeMap.put(14, "autumn_begins");
		mSolarTermsTreeMap.put(15, "stopping_the_heat");
		mSolarTermsTreeMap.put(16, "white_dews");
		mSolarTermsTreeMap.put(17, "autumn_equinox");
		mSolarTermsTreeMap.put(18, "cold_dews");
		mSolarTermsTreeMap.put(19, "hoar_frost_falls");
		mSolarTermsTreeMap.put(20, "winter_begins");
		mSolarTermsTreeMap.put(21, "light_snow");
		mSolarTermsTreeMap.put(22, "heavy_snow");
		mSolarTermsTreeMap.put(23, "winter_solstice");
	}

	private void initForEng() {

		mLunarHolidays.clear();
		mLunarHolidays.put("the_spring_festival", "Spring Festival");
		mLunarHolidays.put("lantern_festival", "Lantern Festival");
		mLunarHolidays.put("the_dragon_boat_festival", "Dragon boat festival");
		mLunarHolidays.put("double_seventh_day", "Double seventh festival");
		mLunarHolidays.put("the_mid_autumn_festival", "Spring Festival");
		mLunarHolidays
				.put("the_double_ninth_festival", "Double Ninth Festival");
		mLunarHolidays.put("the_laba_rice_porridge_festival", "Laba");

		mSolarHolidays.clear();
		mSolarHolidays.put("new_years_day", "New Year's Day");
		mSolarHolidays.put("valentines_day", "Valentine's Day");
		mSolarHolidays.put("international_womens_day", "Women's Day");
		mSolarHolidays.put("arbor_day", "Arbor Day");
		mSolarHolidays.put("fools_day", "April Fool's Day");
		mSolarHolidays.put("labour_day", "May Day");
		mSolarHolidays.put("chinese_youth_day", "Youth Day");
		mSolarHolidays.put("childrens_day", "Children's Day");
		mSolarHolidays.put("partys_day", "CPC Founding Day");
		mSolarHolidays.put("the_armys_day", "Army Day");
		mSolarHolidays.put("teachers_day", "Teachers' Day");
		mSolarHolidays.put("national_day", "National Day");
		mSolarHolidays.put("christmas_day", "Christmas");

		mSolarHolidays_TW.clear();
		mSolarHolidays_TW.put("national_father_day", "Sun Yat-sen's Birthday");

		mSolarTerms.clear();
		mSolarTerms.put("slight_cold", "Slight Cold");
		mSolarTerms.put("great_cold", "Big Chill");
		mSolarTerms.put("spring_begins", "Beginning of spring");
		mSolarTerms.put("the_rains", "Rainwater");
		mSolarTerms.put("insects_awaken", "Waking of Insects");
		mSolarTerms.put("vernal_equinox", "Spring equinox");
		mSolarTerms.put("clear_and_bright", "Qingming");
		mSolarTerms.put("grain_rain", "Grain Rain");
		mSolarTerms.put("summer_begins", "Beginning of summer");
		mSolarTerms.put("grain_buds", "Grain Full");
		mSolarTerms.put("grain_in_ear", "Grain in Ear");
		mSolarTerms.put("summer_solstice", "Summer Solstice");
		mSolarTerms.put("slight_heat", "Slight Heat");
		mSolarTerms.put("great_heat", "Great Heat");
		mSolarTerms.put("autumn_begins", "Beginning of autumn");
		mSolarTerms.put("stopping_the_heat", "End of heat");
		mSolarTerms.put("white_dews", "White dew");
		mSolarTerms.put("autumn_equinox", "Autumnal Equinox");
		mSolarTerms.put("cold_dews", "Cold Dews");
		mSolarTerms.put("hoar_frost_falls", "First Frost");
		mSolarTerms.put("winter_begins", "Beginning of winter");
		mSolarTerms.put("light_snow", "Light Snow");
		mSolarTerms.put("heavy_snow", "Heavy Snow");
		mSolarTerms.put("winter_solstice", "Winter Solstice");

		mLunaHashMap.clear();
		mLunaHashMap.put("lunar_calendar", "Lunar calendar");
		mLunaHashMap.put("lunar_zheng", "First");
		mLunaHashMap.put("lunar_yi", "One");
		mLunaHashMap.put("lunar_er", "Two");
		mLunaHashMap.put("lunar_san", "Three");
		mLunaHashMap.put("lunar_si", "Four");
		mLunaHashMap.put("lunar_wu", "Five");
		mLunaHashMap.put("lunar_liu", "Six");
		mLunaHashMap.put("lunar_qi", "Seven");
		mLunaHashMap.put("lunar_ba", "Eight");
		mLunaHashMap.put("lunar_jiu", "Nine");
		mLunaHashMap.put("lunar_shi", "Ten");
		mLunaHashMap.put("lunar_shi_yi", "Winter");
		mLunaHashMap.put("lunar_shi_er", "December");
		mLunaHashMap.put("lunar_chu_shi", "Tenth");
		mLunaHashMap.put("lunar_er_shi", "Twenty");
		mLunaHashMap.put("lunar_san_shi", "Thirty");
		mLunaHashMap.put("lunar_chu", "Early");
		mLunaHashMap.put("lunar_leap", "Intercalary");
		mLunaHashMap.put("lunar_ling", "O");
		mLunaHashMap.put("lunar_nian", "Twenty");
		mLunaHashMap.put("lunar_year", "Years");
		mLunaHashMap.put("lunar_yue", "Month");
	}

	private void initForCN() {
		mLunarHolidays.clear();
		mSolarHolidays.clear();


		mSolarHolidays_TW.clear();

		mSolarTerms.clear();

		mLunaHashMap.clear();
	}

	private void initForTW() {
		mLunarHolidays.clear();

		mSolarHolidays.clear();


		mSolarHolidays_TW.clear();

		mSolarTerms.clear();
		mLunaHashMap.clear();

	}

	private int getCurrentLaunage() {
		return LanguageUtils.getCurrentLanguage();
	}
}
