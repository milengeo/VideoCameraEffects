package com.rustero.themes;


import com.rustero.R;
import com.rustero.effects.glEffect;
import com.rustero.tools.Size2D;

import java.util.ArrayList;
import java.util.List;

public class Themer {

	public static final String NAME_BEACH = 		"Beach";
	public static final String NAME_BIRTHDAY_1 = 	"Birthday 1";
	public static final String NAME_BIRTHDAY_2 = 	"Birthday 2";
	public static final String NAME_CHRISTMAS_1 = 	"Christmas 1";
	public static final String NAME_CHRISTMAS_2 = 	"Christmas 2";
	public static final String NAME_PARROT = 		"Parrot";
	public static final String NAME_RAIN = 			"Rain";
	public static final String NAME_SNOW = 			"Snow";
	public static final String NAME_SNOWMAN = 		"Snowman";



	private static Themer self;

	private List<ThemeC> mList;
	private int mIndex;



	public static Themer get() {
		if (null == self)
			self = new Themer();
		return self;
	}



	private Themer() {
		mList = new ArrayList<>();
		mList.add(new ThemeC(NAME_BEACH, R.raw.beach_h, R.raw.beach_v));
		mList.add(new ThemeC(NAME_BIRTHDAY_1, R.raw.birthday_h_1, R.raw.birthday_v_1));
		mList.add(new ThemeC(NAME_BIRTHDAY_2, R.raw.birthday_h_2, R.raw.birthday_v_2));
		mList.add(new ThemeC(NAME_CHRISTMAS_1, R.raw.christmas_h_1, R.raw.christmas_v_1));
		mList.add(new ThemeC(NAME_CHRISTMAS_2, R.raw.christmas_h_2, R.raw.christmas_v_2));
		mList.add(new ThemeC(NAME_PARROT, R.raw.parrot_h, R.raw.parrot_v));
		mList.add(new ThemeC(NAME_RAIN, R.raw.rain_h, R.raw.rain_v));
		mList.add(new ThemeC(NAME_SNOW, R.raw.snow_h, R.raw.snow_v));
		mList.add(new ThemeC(NAME_SNOWMAN, R.raw.snowman_h, R.raw.snowman_v));
	}



	public List<String> getNames() {
		List<String> result = new ArrayList<>();
		for (ThemeC theme : mList) {
			result.add(theme.name);
		}
		return result;
	}


	public ThemeC getTheme(String aName) {
		if (aName.isEmpty()) return null;
		ThemeC result = null;
		for (ThemeC theme : mList) {
			if (theme.name.equals(aName)) {
				result = theme;
				break;
			}
		}
		return result;
	}


}
