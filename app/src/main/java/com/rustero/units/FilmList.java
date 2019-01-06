package com.rustero.units;


import java.util.ArrayList;


public class FilmList extends ArrayList<FilmItem> {

	private FilmItem mSelectedFilm;
	//private String mSelectedPath = "";


	public void setSelected(FilmItem aItem) {
		mSelectedFilm = aItem;
	}


	public FilmItem getSelected() {
		return mSelectedFilm;
	}


	public FilmItem getFilm(String aPath) {
		if (null == aPath) return null;
		for (FilmItem fiit : this) {
			if (fiit.path.equals(aPath)) {
				return fiit;
			}
		}
		return null;
	}


	public String getSelectedPath() {
		if (null == mSelectedFilm)
			return "";
		else
			return mSelectedFilm.path;
	}

//
//	public void fetchSelected() {
//		if (mSelectedPath.isEmpty())
//			mSelectedFilm = null;
//		else
//			mSelectedFilm = getFilm(mSelectedPath);
//	}

}
