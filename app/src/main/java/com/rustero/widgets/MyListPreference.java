package com.rustero.widgets;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.widget.ListView;


public class MyListPreference extends ListPreference {


	public MyListPreference(Context context) {
		super(context);
	}

	public MyListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	protected void showDialog(Bundle state) {
		super.showDialog(state);
		Dialog dlg = getDialog();
		if (null == dlg) return;
		ListView listView = ((AlertDialog) dlg).getListView();
		if (null == listView) return;
		listView.setScrollbarFadingEnabled(false);
	}

}
