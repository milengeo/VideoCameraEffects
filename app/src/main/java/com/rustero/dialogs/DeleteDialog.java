package com.rustero.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.rustero.App;
import com.rustero.R;
import com.rustero.activities.MainActivity;


public class DeleteDialog extends DialogFragment {


	public interface Eventer {
		void onOk(String aPath);
	}

	private static boolean sDialogShown = false;
	private static String mPath;


	public void ask(String aPath) {
		if (sDialogShown) return;
		mPath = aPath;
		show(MainActivity.get().getSupportFragmentManager(), "DeleteDialog");
	}



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		sDialogShown = true;
		View dialogLayout = inflater.inflate(R.layout.delete_dialog, container, false);
		final Dialog dialog = getDialog();
		dialog.setCanceledOnTouchOutside(false);

		TextView tv_message = (TextView) dialogLayout.findViewById(R.id.delete_dlg_message);
		tv_message.setText(App.resstr(R.string.are_you_sure_you_want_to_delete) + mPath);

		final Button cancelButton = (Button)dialogLayout.findViewById(R.id.delete_dlg_cancel);
		final Button okButton = (Button)dialogLayout.findViewById(R.id.delete_dlg_ok);

		cancelButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				}
		);

		okButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v)
					{
						dialog.dismiss();
						Eventer eventer = MainActivity.get().new DeleteDialogEventer();
						eventer.onOk(mPath);
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
