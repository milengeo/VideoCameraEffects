package com.rustero.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.Html;

import com.rustero.App;
import com.rustero.R;
import com.rustero.activities.RecordActivity;
import com.rustero.coders.Encoder;
import com.rustero.tools.Tools;


public class RecordService extends Service {



	public static RecordActivity activity = null;


	public RecordService() {
	}



	@Override
	public IBinder onBind(Intent intent) {
		// Used only in case of bound services.
		return null;
	}





	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent.getAction().equals(App.INTENT_START_SERVICE)) {
			doStartService();
		} else if (intent.getAction().equals(App.INTENT_STOP_SERVICE)) {
			doStopService();
		}
		return START_STICKY;
	}



	private void doStartService() {
		App.log("doStartService");
		Encoder.get().attachEngine(ENCODER_EVENTER);
		startForeground(App.NOTIFICATION_BAR_RECORD, buildNotification("Recording..."));
	}



	private void doStopService() {
		App.log("doStopService");
		Encoder.get().detachEngine();
		stopForeground(true);
		stopSelf();
	}




	private Notification buildNotification(String aText) {
		Intent notificationIntent = new Intent(this, RecordActivity.class);
		notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.app_icon_96);

		String title = "" + Html.fromHtml("<b>" + App.resstr(R.string.app_name) + "</b>");
		Notification notification = new NotificationCompat.Builder(this)
				.setTicker(title)
				.setContentTitle(title)
				.setContentText(aText)
				.setSmallIcon(R.drawable.app_icon_96)
				.setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
				.setContentIntent(pendingIntent)
				.build();

		return notification;
	}



	private void updateNotification() {
		Encoder.StatusC status = Encoder.get().getStatus();
		if (null == status) return;

		String text = Encoder.get().getOutputName();
		text += "  " + String.format("%02d:%02d", status.secs/60, status.secs%60);
		text += "  " + Tools.formatSize(status.size);

		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(App.NOTIFICATION_BAR_RECORD, buildNotification(text));
	}





	private Encoder.Events ENCODER_EVENTER = new Encoder.Events() {


		public void onStateChanged() {
			if (null == activity) return;
			try {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						activity.updateUI();
					}
				});
			}
			catch (Exception ex) {
				App.log(" ***_ex ENCODER_EVENTER-onStateChanged: " + ex.getMessage());
			}
		}


		public void onProgress() {
			if (Encoder.get().getStatus().secs > App.getRecordMinutes() * 60)
				Encoder.get().cease();

			if (null == activity) {
				new Thread() {
					public void run() {
						updateNotification();
					}
				}.start();

			} else {
				try {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							activity.updateStatus();
						}
					});
				} catch(Exception ex){
						App.log(" ***_ex ENCODER_EVENTER-onProgress: " + ex.getMessage());
					}
				}
		}



		public void onFault(final String aMessage)
		{
			if (null == activity) return;
			try {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						new android.app.AlertDialog.Builder(activity)
								.setTitle("Recording error")
								.setMessage(aMessage)
								.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										// continue with click
										activity.finish();
									}
								})
								.setIcon(android.R.drawable.ic_dialog_alert)
								.show();
					}
				});
			}
			catch (Exception ex) {
				App.log(" ***_ex ENCODER_EVENTER-onFault: " + ex.getMessage());
			}
		}

	};


}
