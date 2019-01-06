package com.rustero.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.rustero.R;
import com.rustero.activities.MainActivity;



public class RenameDialog extends DialogFragment {


	public interface Eventer {
		void onDone(String aPath, String aName);
	}

	private static boolean sDialogShown = false;
	private static String mPath, mName;


	public void ask(FragmentActivity aContext, String aPath, String aName) {
		if (sDialogShown) return;
		mPath = aPath;
		mName = aName;
		show(MainActivity.get().getSupportFragmentManager(), "RenameDialog");
	}



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View dialogLayout = inflater.inflate(R.layout.rename_dialog, container, false);
		final Dialog dialog = getDialog();
		dialog.setCanceledOnTouchOutside(false);

		final EditText nameView = (EditText)dialogLayout.findViewById(R.id.rename_dlg_edit_name_);
		nameView.setText(mName);
		nameView.setSelection(nameView.length());

		final Button cancelButton = (Button)dialogLayout.findViewById(R.id.rename_dlg_cancel);
		final Button doneButton = (Button)dialogLayout.findViewById(R.id.rename_dlg_ok);

		cancelButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				}
		);

		doneButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v)
					{
						String newName = nameView.getText().toString();
						if (newName.length() < 2) {
							nameView.setError(MainActivity.get().getString(R.string.too_short));
							return;
						}

						if (newName.substring(0,1).equals(" ") ) {
							nameView.setError("cannot start with a space");
							return;
						}

						dialog.dismiss();
						if (newName.equals(mName)) return;
						Eventer eventer = MainActivity.get().new RenameDialogEventer();
						eventer.onDone(mPath, newName);
					}
				}
		);

		return dialogLayout;
	}



	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		sDialogShown = false;
	}



}
