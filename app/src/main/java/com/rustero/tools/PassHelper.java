package com.rustero.tools;


import com.rustero.App;
import com.rustero.R;

import java.util.HashSet;
import java.util.Set;


public class PassHelper {

	private final int WEAK_HIGH =   16;
	private final int STRONG_LOW =  32;


	private String mStatus;
	private int mScore;
	private static final String LOG_TAG = "PassHelper";



	public String getStatus() {
		return mStatus;
	}


	public int getScore() {
		return mScore;
	}


	public boolean isWeak() {
		return mScore < WEAK_HIGH;
	}


	public boolean isReasonable() {
		return (mScore >= WEAK_HIGH) && (mScore < STRONG_LOW);
	}


	public boolean isStrong() {
		return mScore >= STRONG_LOW;
	}



	private String whitespaces() {

		String whitespace_chars = ""       /* dummy empty string for homogeneity */
				+ "\\u0009" // CHARACTER TABULATION
				+ "\\u000A" // LINE FEED (LF)
				+ "\\u000B" // LINE TABULATION
				+ "\\u000C" // FORM FEED (FF)
				+ "\\u000D" // CARRIAGE RETURN (CR)
				+ "\\u0020" // SPACE
				+ "\\u0085" // NEXT LINE (NEL)
				+ "\\u00A0" // NO-BREAK SPACE
				+ "\\u1680" // OGHAM SPACE MARK
				+ "\\u180E" // MONGOLIAN VOWEL SEPARATOR
				+ "\\u2000" // EN QUAD
				+ "\\u2001" // EM QUAD
				+ "\\u2002" // EN SPACE
				+ "\\u2003" // EM SPACE
				+ "\\u2004" // THREE-PER-EM SPACE
				+ "\\u2005" // FOUR-PER-EM SPACE
				+ "\\u2006" // SIX-PER-EM SPACE
				+ "\\u2007" // FIGURE SPACE
				+ "\\u2008" // PUNCTUATION SPACE
				+ "\\u2009" // THIN SPACE
				+ "\\u200A" // HAIR SPACE
				+ "\\u2028" // LINE SEPARATOR
				+ "\\u2029" // PARAGRAPH SEPARATOR
				+ "\\u202F" // NARROW NO-BREAK SPACE
				+ "\\u205F" // MEDIUM MATHEMATICAL SPACE
				+ "\\u3000" // IDEOGRAPHIC SPACE
				;
	/* A \s that actually works for Java’s native character set: Unicode */
		String result = "[" + whitespace_chars + "]";
		return result;
	}



//	private int countNuts(String aPass) {
//		String nuts = "";
//		String twins = "";
//		String deltas = "";
//		char lach = Character.toChars(9)[0];
//		for (int i=0; i<aPass.length(); i++) {
//			char ch = aPass.charAt(i);
//
//			int p = twins.indexOf(ch);
//			if (p < 0) {
//				twins += ch;
//				int id = ch - lach;
//				id += 'a';
//				char cd = Character.toChars(id)[0];
//				p = deltas.indexOf(cd);
//				if (p < 0) {
//					deltas += cd;
//					nuts += ch;
//				}
//			}
//			lach = ch;
//		}
//		return nuts.length();
//	}



	private int countNuts(String aPass) {
		int nuts = 0;
		Set<Integer> twins = new HashSet<Integer>();
		Set<Integer> deltas = new HashSet<Integer>();
		int lacp = 0;
		int length = aPass.length();
		for (int offset = 0; offset < length; ) {
			int copo = aPass.codePointAt(offset);

			if (!twins.contains(copo)) {
				twins.add(copo);
				int de = copo-lacp;
				if (!deltas.contains(de)) {
					deltas.add(de);
					nuts++;
				}
				lacp = copo;
			}
			offset += Character.charCount(copo);
		}
		return nuts;
	}



//	private boolean nonAscii(String aPass) {
//		int length = aPass.length();
//		for (int offset = 0; offset < length; ) {
//			int copo = aPass.codePointAt(offset);
//			if (copo > 127)
//				return true;
//			offset += Character.charCount(copo);
//		}
//		return false;
//	}


	private boolean hasSpecial(String aPass) {
		int len = aPass.length();
		for (int offset=0; offset<len; ) {
			int copo = aPass.codePointAt(offset);
			if ( !Character.isDigit(copo) && !Character.isLetter(copo))
				return true;
			offset += Character.charCount(copo);
		}
		return false;
	}


	private boolean hasDigit(String aPass) {
		int len = aPass.length();
		for (int offset=0; offset<len; ) {
			int copo = aPass.codePointAt(offset);
			if (Character.isDigit(copo))
				return true;
			offset += Character.charCount(copo);
		}
		return false;
	}


	private boolean hasLetter(String aPass) {
		int len = aPass.length();
		for (int offset=0; offset<len; ) {
			int copo = aPass.codePointAt(offset);
			if (Character.isLetter(copo))
				return true;
			offset += Character.charCount(copo);
		}
		return false;
	}


	private boolean hasUpper(String aPass) {
		int len = aPass.length();
		for (int offset=0; offset<len; ) {
			int copo = aPass.codePointAt(offset);
			if (Character.isUpperCase(copo))
				return true;
			offset += Character.charCount(copo);
		}
		return false;
	}



	private boolean hasWhite(String aPass) {
		int len = aPass.length();
		for (int offset=0; offset<len; ) {
			int copo = aPass.codePointAt(offset);
			if (Character.isWhitespace(copo))
				return true;
			offset += Character.charCount(copo);
		}
		return false;
	}


//	private int reckonMask(String aText, String aMask) {
//		int result = 0;
//
//		Pattern pattern = Pattern.compile(aMask);
//		Matcher matcher = pattern.matcher(aText);
//		while (matcher.find())
//			result++;
//
//		return result;
//	}





	private int countGroups(String aPass) {

		int groups = 0;
		if (hasDigit(aPass))
			groups += 1;
		if (hasLetter(aPass))
			groups += 1;
		if (hasUpper(aPass))
			groups += 1;
		if (hasSpecial(aPass))
			groups += 1;

		return groups;
	}



	public boolean validate(String aPass) {
		int count;

		if (hasWhite(aPass)) {
			mScore = 0;
			mStatus = App.resstr(R.string.white_spaces_not_allowed);
			return false;
		}

		count = aPass.length();
		if (count < 6) {
			mScore = 0;
			mStatus = App.resstr(R.string.six_characters_required);
			return false;
		}

		int nuts = countNuts(aPass);
		int groups = countGroups(aPass);
		mScore = groups * nuts;

		if (mScore < WEAK_HIGH) {
			mStatus = App.resstr(R.string.weak);
			return false;
		} else if (mScore < STRONG_LOW) {
			mStatus = App.resstr(R.string.moderate);
			return true;
		} else {
			mStatus = App.resstr(R.string.strong);
			return true;
		}
	}




	static public void selfTest() {
		String pass;
		Boolean ok;
		App.log( " ");

		PassHelper helper = new PassHelper();

		pass = "абвгдежз"; App.log( String.format("nuts for pass %s is: %d", pass, helper.countNuts(pass)));
		pass = "явертъуи"; App.log( String.format("nuts for pass %s is: %d", pass, helper.countNuts(pass)));

		pass = "qqqqqqqq"; App.log( String.format("nuts for pass %s is: %d", pass, helper.countNuts(pass)));
		pass = "abcdefgh"; App.log( String.format("nuts for pass %s is: %d", pass, helper.countNuts(pass)));
		pass = "qwertyui"; App.log( String.format("nuts for pass %s is: %d", pass, helper.countNuts(pass)));
		pass = "12345678"; App.log( String.format("nuts for pass %s is: %d", pass, helper.countNuts(pass)));
		pass = "balkan"; App.log( String.format("nuts for pass %s is: %d", pass, helper.countNuts(pass)));
		pass = "Balkan22"; App.log( String.format("nuts for pass %s is: %d", pass, helper.countNuts(pass)));
		pass = "13245768"; App.log( String.format("nuts for pass %s is: %d", pass, helper.countNuts(pass)));

		pass = " qqqqqqqq"; ok = helper.validate(pass);
		App.log( String.format("pass: %s, result: %b, score: %d, status: %s", pass, ok, helper.getScore(), helper.getStatus()));

		pass = "12345678"; ok = helper.validate(pass);
		App.log( String.format("pass: %s, result: %b, score: %d, status: %s", pass, ok, helper.getScore(), helper.getStatus()));

		pass = "balkan"; ok = helper.validate(pass);
		App.log( String.format("pass: %s, result: %b, score: %d, status: %s", pass, ok, helper.getScore(), helper.getStatus()));

		pass = "qwertyui"; ok = helper.validate(pass);
		App.log( String.format("pass: %s, result: %b, score: %d, status: %s", pass, ok, helper.getScore(), helper.getStatus()));

		pass = "Balkan22"; ok = helper.validate(pass);
		App.log( String.format("pass: %s, result: %b, score: %d, status: %s", pass, ok, helper.getScore(), helper.getStatus()));

		pass = "alabalanica"; ok = helper.validate(pass);
		App.log( String.format("pass: %s, result: %b, score: %d, status: %s", pass, ok, helper.getScore(), helper.getStatus()));

		pass = "nsdsiu48789437knsdn89u5498374"; ok = helper.validate(pass);
		App.log( String.format("pass: %s, result: %b, score: %d, status: %s", pass, ok, helper.getScore(), helper.getStatus()));
	}


}
