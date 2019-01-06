
package com.rustero;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

//import com.google.android.gms.ads.MobileAds;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.applinks.AppLinkData;
import com.rustero.tools.Caminf;
import com.rustero.tools.SinceMeter;
import com.rustero.tools.Size2D;
import com.rustero.tools.Tools;
import com.rustero.units.FilmMeta;
import com.rustero.units.MimeInfo;
import com.rustero.units.NewPass;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.support.v4.content.*;



public class App extends Application {

	static final public boolean DEVEL = false;

    public static App self;
	private static WeakReference<AppCompatActivity> sWeakActivity = null;

	public static boolean fb_logging = !BuildConfig.DEBUG;
    public static AppEventsLogger sFBLogger;
	public static DisplayMetrics gDisplayMetrics;
	public static int sDisplayRotation = 0;
	public static SinceMeter aliveMeter = new SinceMeter();

	public static int gTargetSdkVersion = 18;
    public static boolean live;
    public static long gFirstRunSecs;

    public static Caminf gFrontCam = new Caminf();
    public static Caminf gBackCam = new Caminf();
	public static String[] gFrontResoList = null;
	public static String[] gBackResoList = null;
    public static String gRecordedPath="";

    static public ProgressDialog gWaitDlg;
    static public MetaHeapC gMetaHeap = new MetaHeapC();
    static public class MetaHeapC extends HashMap<String, FilmMeta> {}
	static public List<String> sMyEffects = new ArrayList<>();
	static public String sMyTheme = "";

    static final public String FILM_PREFIX = "vce";
    static final public String CAMERAS_XML = "cameras.xml";

	public static int NOTIFICATION_BAR_RECORD = 7492 + 222;
	public static String INTENT_START_SERVICE = "com.rustero.vicaef.intent.start.service";
	public static String INTENT_STOP_SERVICE = "com.rustero.vicaef.intent.stop.service";

	static final private int NEWBIE_HOURS = 2;
	static final private String FIRST_RUN_SECONDS  = "firuse";


    @Override
    public void onCreate() {
        super.onCreate();
        self = this;

		gFirstRunSecs = getPrefLong(FIRST_RUN_SECONDS);
		doFirstRun();
		setPrefLong(FIRST_RUN_SECONDS, gFirstRunSecs);

		gDisplayMetrics = getResources().getDisplayMetrics();
		gTargetSdkVersion = getTargetSdkVersion(self);
		new MimeInfo(this);

		if (fb_logging) {
			FacebookSdk.sdkInitialize(getApplicationContext());
			AppEventsLogger.activateApp(this);
			if (BuildConfig.DEBUG) {
				FacebookSdk.setIsDebugEnabled(true);
				FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);
			}
			sFBLogger = AppEventsLogger.newLogger(this);
		}
        App.log(Tools.getSystemInfo());

//        if (android.os.Debug.isDebuggerConnected()) {
//            Tools.delay(999);
//        }
	}




    public static void fbLog(String aEvent) {
		if (fb_logging) {
			sFBLogger.logEvent(aEvent);
		}
    }



	private  AppLinkData.CompletionHandler FBCompletionHandler = new AppLinkData.CompletionHandler() {

		public void onDeferredAppLinkDataFetched(AppLinkData appLinkData) {

		}
	};





	public static void setActivity(AppCompatActivity aActivity){
		if (null == aActivity)
			sWeakActivity = null;
		else
			sWeakActivity = new WeakReference<AppCompatActivity>(aActivity);
	}


	public static AppCompatActivity getActivity(){
		if (null == sWeakActivity)
			return null;
		else
			return sWeakActivity.get();
	}



	public static void log(String aLine) {
		if (!BuildConfig.DEBUG) return;
		Log.d("ViCaEf", aLine);
	}



	public static WindowManager getWindowManager() {
		WindowManager windowManager = (WindowManager) self.getSystemService(Context.WINDOW_SERVICE);
		return windowManager;
	}


	public static int getDeviceRotation() {
		int rotation = getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;
		switch(rotation){
			case Surface.ROTATION_0:
				degrees = 0;
				break;

			case Surface.ROTATION_90:
				degrees = 90;
				break;

			case Surface.ROTATION_180:
				degrees = 180;
				break;

			case Surface.ROTATION_270:
				degrees = 270;
				break;
		}
		return degrees;
	}



	public static DisplayMetrics getDisplayMetrics() {
		return gDisplayMetrics;
	}



	public static float getDensity() {
		return gDisplayMetrics.density;
	}



	public static int DipsToPixels(int aDips) {
		final float scale = gDisplayMetrics.density;
		int result = (int) (aDips * scale + 0.5f);
		return result;
	}



	public static void screenScaled(View aView, int a1000Size) {
		int ruler = Math.min(gDisplayMetrics.heightPixels, gDisplayMetrics.widthPixels);
		if (ruler < 400) return;
		float scale = 1.0f * ruler / 1000;
		ViewGroup.LayoutParams lapa = aView.getLayoutParams();
		lapa.width = (int) (scale * a1000Size);
		lapa.height = (int) (scale * a1000Size);
		aView.setLayoutParams(lapa);
	}



	public static void lockOrientation(Activity aActivity) {
		if (aActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			aActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else
			aActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}


	public static void unlockOrientation(Activity aActivity) {
		aActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}


	public static String resstr(int aId) {
		return self.getResources().getText(aId).toString();
	}


	public static Bitmap resbmp(int aId) {
		Bitmap bitmap = BitmapFactory.decodeResource(self.getResources(), aId);
		return bitmap;
	}


	private void doFirstRun() {
		if (gFirstRunSecs > 0) return;
		gFirstRunSecs = System.currentTimeMillis() / 1000;

		String path = getDefaultFolder();
		File folder = new File(path);
		if (folder.isDirectory()) {
			gFirstRunSecs -= (NEWBIE_HOURS+1) * 3600;
		}

		setDefaultPrefs();
		App.log(" ************* doFirstRun");
	}



	public static String readAssetFile(String aPath) {
		String result = "";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(self.getAssets().open(aPath)));
			StringBuilder sb = new StringBuilder();
			String mLine = reader.readLine();
			while (mLine != null) {
				sb.append(mLine); // process line
				mLine = reader.readLine();
			}
			reader.close();
			result = sb.toString();
		} catch (Exception ex) {}
		return result;
	}



	static public boolean isPe() {
//		String edpa =  gSavi2Lib.gep();
//		Lmx xep = new Lmx(edpa);
//		int pe = xep.getInt("pe");
//		if (pe > 0)
//			return true;
//		else
			return false;
	}



	static public String getLote() {
		String result = "r"+"u"+"s"+"t"+"e"+"r"+"o"+"."+"c"+"o"+"m";
		return result;
	}



    static public boolean isNewbie() {
		long hours = getInstallHours();
        if (hours < NEWBIE_HOURS) return true;
        return false;
    }



	static public boolean wantWatermark() {
		if (isPe()) return false;
		if (isNewbie()) return false;
//todo		return true;
		return false;
	}



	static public boolean want5stars() {
		if (isPe()) return false;
		if (getPrefBln("rate5done")) return false;
		if (getInstallHours() < 24*7) return false;
		long nowSecs = System.currentTimeMillis()/1000;
		long askSecs = getPrefLong("rate5secs");
		long sinceHours = (nowSecs - askSecs) / 60/ 60;
		if (sinceHours < 24*7 ) return false;
		setPrefLong("rate5secs", nowSecs);

		return true;
	}



    public static long getInstallHours() {
        long nowSecs = System.currentTimeMillis()/1000;
        long sinceSecs = (nowSecs - gFirstRunSecs);
        long result = sinceSecs/60/60;
        return result;
    }



    static public String getOutputFolder() {
        if (null == self) return "";
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(self);
        String result = preferences.getString("output_folder", "");

        if ((result != null) && (result.length() > 0)) {
            File folder = new File(result);
			if (folder.isDirectory())
   	            return result;
        }

        result = getDefaultFolder();
        File folder = new File(result);
        folder.mkdirs();
        if (folder.isDirectory())
            return result;

        result = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
		return result;
    }



    static public String getDefaultFolder() {
        String result = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
        result += "/video_camera_effects";
        return result;
    }



	public static String takeNameCount() {
		int count = getPrefInt("film_name_count");
		count++;
		setPrefInt("film_name_count", count);
		String result = String.format("%04d", count);
		return result;
	}



    static public int CamNumber() {
        int result = 0;
        if (gFrontCam != null)
            result++;
        if (gBackCam != null)
            result++;
        return result;
    }



    static public Caminf getCaminf() {
        Caminf result;
        if (App.getPrefBln("now_front")) {
            result = gFrontCam;
        } else {
            result = gBackCam;
        }
        return result;
    }



	public static boolean hasFlash() {
		boolean result = self.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
		return result;
	}



    public static void showShortToast(String aText) {
        if (null == self) return;
        Toast.makeText(self, aText, Toast.LENGTH_SHORT).show();
    }



    public static void showLongToast(String aText) {
        if (null == self) return;
        Toast.makeText(self, aText, Toast.LENGTH_LONG).show();
    }



    static public class TipC {
        int Count, Total, Duration;
        String Text;

        public TipC(String aText) {
            Count = 0;
            Total = 1;
            Duration = Toast.LENGTH_LONG;
            Text = aText;
        }

        public void show() {
            if (Count >= Total) return;
            Count++;
            Toast.makeText(self.getApplicationContext(), Text, Duration).show();
        }

        public void cease() {
            Count = Total;
        }
    }



    static public void showAlert(Context aContext, String aTitle, String aText) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(aContext);
        alertDialog.setIcon(R.drawable.app_icon_96);
        alertDialog.setPositiveButton("Ok", null);
        alertDialog.setMessage(aText);
        alertDialog.setTitle(aTitle);
        alertDialog.show();
    }



    static public void finishActivityAlert(final Activity aActivity, String aTitle, String aText) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(aActivity);
        alertDialog.setIcon(R.drawable.app_icon_96);
        alertDialog.setMessage(aText);
        alertDialog.setTitle(aTitle);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                aActivity.finish();
            }
        });
        alertDialog.show();
    }



    static public void showWaitDlg(Context aContext, String aMessage) {
        gWaitDlg = new ProgressDialog(aContext);
        gWaitDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        gWaitDlg.setMessage(aMessage);
        gWaitDlg.setCanceledOnTouchOutside(false);
        gWaitDlg.setCancelable(false);
        gWaitDlg.show();
    }


    static public void hideWaitDlg() {
        if (null == gWaitDlg) return;
        gWaitDlg.dismiss();
        gWaitDlg = null;
    }



    static public int getRecordMinutes() {
        int result = 5;
        if (null == self) return result;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(self);
        result = preferences.getInt("record_minutes", 5);
        if (result < 1) result = 1;
        return result;
    }



	static  private void setDefaultPrefs() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(self);

        String text = getDefaultFolder();
        preferences.edit().putString("output_folder", text).commit();

        int maxMinutes = getRecordMinutes();
        preferences.edit().putInt("record_minutes", maxMinutes).commit();

   }



	static public void setPrefStr(String aName, String aValue) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(self);
		preferences.edit().putString(aName, aValue).commit();
	}



    static public void setPrefBln(String aName, boolean aValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(self);
        preferences.edit().putBoolean(aName, aValue).commit();
    }



	static public void setPrefInt(String aName, int aValue) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(self);
		preferences.edit().putInt(aName, aValue).commit();
	}



	static public void setPrefLong(String aName, long aValue) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(self);
		preferences.edit().putLong(aName, aValue).commit();
	}



	static public String getPrefStr(String aName)
    {
        String result = "";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(self);
        result = prefs.getString(aName, "");
        return result;
    }




    static public int getPrefInt(String aName)
    {
        int result = 0;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(self);
        result = prefs.getInt(aName, 0);
        return result;
    }


	static public long getPrefLong(String aName)
	{
		long result = 0;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(self);
		result = prefs.getLong(aName, 0);
		return result;
	}


	static public boolean getPrefBln(String aName)
    {
        boolean result = false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(self);
        result = prefs.getBoolean(aName, false);
        return result;
    }


    static public int getPrefAsInt(String aName)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(self);
        String text = prefs.getString(aName, "");
        int result = 0;
        try {
            result = Integer.parseInt(text);
        } catch(Exception ex) {}
        return result;
    }



    public static boolean selfPermissionGranted(Context context, String permission) {
        // For Android < Android M, self permissions are always granted.
        boolean result = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (gTargetSdkVersion >= Build.VERSION_CODES.M) {
                // targetSdkVersion >= Android M, we can use Context#checkSelfPermission
                result = (context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            } else {
                // targetSdkVersion < Android M, we have to use PermissionChecker
                result = (PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED);
            }
        }
        return result;
    }



	public static int getTargetSdkVersion(Context context)
	{
		int result = 18;
		try {
			final PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			result = info.applicationInfo.targetSdkVersion;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return result;
	}





	public static String[] LoadResolutions(String[] aResoList) {
		if (null == aResoList) return null;
		int mrh = 777;

		// first, count
		int count = 0;
		for (int i=0; i<aResoList.length; i++) {
			String resstr = aResoList[i];
			Size2D size2 = Size2D.parseText(resstr);
			if (size2.y > mrh) break;
			count++;
		}

		// second, build
		String[] result = new String[count];
		for (int i=0; i<count; i++) {
			String resstr = aResoList[i];
			result[i] = resstr;
		}

		return result;
	}




}
