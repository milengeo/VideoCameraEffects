package com.rustero.widgets;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.rustero.App;
import com.rustero.R;
import com.rustero.tools.Tools;

import java.io.File;


public class OutputFolderPrefdlg extends DialogPreference {

	private Context mContext;
	private EditText mEdit;
	private String mText;


	public OutputFolderPrefdlg(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mText = App.getPrefStr("output_folder");
	}


	@Override
	protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
		super.onPrepareDialogBuilder(builder);    //To change body of overridden methods use File | Settings | File Templates.
	}


	@Override
	protected View onCreateView(ViewGroup parent) {
		setSummary(mText);
		return super.onCreateView(parent);
	}


	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		mEdit = (EditText) view.findViewById(R.id.outputfolder_edit);
		mEdit.setText(mText);
		mEdit.setSelection(mText.length());
	}


	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (!positiveResult) return;

		mText = validate();
		App.setPrefStr("output_folder", mText);
		setSummary(mText);
	}


	private String validate() {
		String result = mText;

		String path = mEdit.getText().toString();
		if (path.length() < 2) {
			App.showAlert(mContext, "Error creating folder!", "Folder name is not valid.");
			return result;
		}

		File folder = new File(path);
		folder.mkdirs();
		if (!Tools.folderExists(path))
		{
			App.showAlert(mContext, "Error creating folder!", "Invalid folder path!");
			return result;
		}

		File file = new File(path + "/test");
		try {
			file.createNewFile();
		} catch (Exception ex) {
		}
		if (!file.isFile()) {
			App.showAlert(mContext, "No write access!", "Output folder is not writable!");
			return result;
		} else
			file.delete();

		result = path;
		return result;
	}



}
