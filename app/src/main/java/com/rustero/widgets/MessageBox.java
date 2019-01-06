package com.rustero.widgets;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.rustero.App;
import com.rustero.R;


public class MessageBox extends DialogFragment {

	private static String mTitle, mMessage;
	private static boolean mFinish;
	private static FragmentActivity mActivity;


	public MessageBox() {
		setStyle(DialogFragment.STYLE_NORMAL, R.style.MyDialogStyle);
	}




	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View dialogLayout = inflater.inflate(R.layout.message_box, container, false);
		final Dialog dialog = getDialog();
		dialog.setCanceledOnTouchOutside(false);

		final TextView tvTitle = (TextView) dialogLayout.findViewById(R.id.message_box_title);
		tvTitle.setText(mTitle);

		final TextView tvMessage = (TextView) dialogLayout.findViewById(R.id.message_box_message);
		tvMessage.setText(mMessage);

		final Button btnOk = (Button) dialogLayout.findViewById(R.id.message_box_ok);


		btnOk.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
				if (mFinish)
					mActivity.finish();

			}
		});

		dialog.show();
		return dialogLayout;
	}



	private static void doShow(String aTitle, String aMessage) {

		FragmentActivity mActivity = App.getActivity();
		if (null == mActivity) return;

		mTitle = aTitle;
		mMessage = aMessage;
		MessageBox messageBox = new MessageBox();
		messageBox.show(mActivity.getSupportFragmentManager(), "MessageBox");
	}


	public static void show(String aTitle, String aMessage) {
		mFinish = false;
		doShow(aTitle, aMessage);
	}


	public static void showFinish(FragmentActivity aActivity, String aTitle, String aMessage) {
		mFinish = true;
		doShow(aTitle, aMessage);
	}


}