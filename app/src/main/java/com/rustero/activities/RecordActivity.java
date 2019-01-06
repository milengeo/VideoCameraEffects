package com.rustero.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.rustero.App;
import com.rustero.R;
import com.rustero.coders.Encoder;
import com.rustero.effects.glEffect;
import com.rustero.themes.ThemeC;
import com.rustero.themes.Themer;
import com.rustero.tools.Tools;
import com.rustero.widgets.MyActivity;
import com.rustero.widgets.PanelManager;
import com.rustero.widgets.ToggleImageButton;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;


public class RecordActivity extends MyActivity {

    private final int FADE_TIMEOUT = 333;
	private final int LARGE_KNOB_SIZE = 120;
	private final int SMALL_KNOB_SIZE = 80;

	private ViewGroup mControlLayout;
    private ImageButton mBeginButton, mCeaseButton, mBackgroundButton, mSwitchButton;
    private ToggleImageButton mGridButton, mFlashButton;
    private TextView mFileName, mResoInfo, mZoomInfo, mTimeInfo;
    private ProgressBar mLoadingBar;

    private SurfaceView mSurfView;
    private ScaleGestureDetector mScaleDetector;
    private Handler mTimerHandler;
    private int mTimerCount = 0;

	private PanelManager mPanelManager;
	private View mPanelBack;
	private View mEffectPanel;
	private ImageButton mOpenEffectPanel, mCloseEffectPanel;
	private ListView mEffectPanelList;
	private EffecstPanelAdapter mEffectsAdapter;
	private ArrayList<EffectItem> mEffectPanelItems;
	private TextView mEffectsStatus;

	private View mThemePanel;
	private ImageButton mOpenThemePanel, mCloseThemePanel;
	private ListView mThemePanelList;
	private ThemePanelAdapter mThemeAdapter;
	private ArrayList<ThemeItem> mThemePanelItems;


	public class EffectItem {
		public int tag, icon;
		public String name;

		// Constructor.
		public EffectItem(int tag, int icon, String name) {
			this.tag = tag;
			this.icon = icon;
			this.name = name;
		}
	}


	public class ThemeItem {
		public int tag, icon;
		public String name;

		// Constructor.
		public ThemeItem(int tag, int icon, String name) {
			this.tag = tag;
			this.icon = icon;
			this.name = name;
		}
	}



	@Override
	protected void onResume() {
		super.onResume();
		RecordService.activity = this;
		mSurfView.getHolder().addCallback(new SurfaceEventer());

		startRecordService();

		Encoder.get().resetStatus();
		updateUI();
	}



	@Override
	protected void onPause() {
		super.onPause();
		RecordService.activity = null;

		if (isFinishing()) {
			stopRecordService();
		}
	}




	private void startRecordService() {
		Intent startIntent = new Intent(this, RecordService.class);
		startIntent.setAction(App.INTENT_START_SERVICE);
		startService(startIntent);
	}



	private void stopRecordService() {
		Intent stopIntent = new Intent(this, RecordService.class);
		stopIntent.setAction(App.INTENT_STOP_SERVICE);
		startService(stopIntent);
	}




	@Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.record_activity);

			mControlLayout = (ViewGroup) findViewById(R.id.record_layout_controls);
            mBeginButton = (ImageButton) findViewById(R.id.record_pubu_start);
            mBeginButton.setOnClickListener(new BeginClicker());
            App.screenScaled(mBeginButton, LARGE_KNOB_SIZE);

            mCeaseButton = (ImageButton) findViewById(R.id.record_pubu_stop);
            mCeaseButton.setOnClickListener(new CeaseClicker());
			App.screenScaled(mCeaseButton, LARGE_KNOB_SIZE);

			mBackgroundButton = (ImageButton) findViewById(R.id.record_pubu_background);
			mBackgroundButton.setOnClickListener(new BackgroundClicker());
			App.screenScaled(mBackgroundButton, SMALL_KNOB_SIZE);

			mGridButton = (ToggleImageButton) findViewById(R.id.record_btn_grid);
			mGridButton.setOnCheckedChangeListener(new GridClicker());
			App.screenScaled(mGridButton, SMALL_KNOB_SIZE);
			mGridButton.setChecked( App.getPrefBln("show_grid") );

			mFlashButton = (ToggleImageButton) findViewById(R.id.record_btn_flash);
			mFlashButton.setOnCheckedChangeListener(new FlashClicker());
			App.screenScaled(mFlashButton, SMALL_KNOB_SIZE);

			mSwitchButton = (ImageButton) findViewById(R.id.record_pubu_switch);
            mSwitchButton.setOnClickListener(new SwitchClicker());
			App.screenScaled(mSwitchButton, SMALL_KNOB_SIZE);

			mFileName = (TextView) findViewById(R.id.record_tevi_file_name);
            mZoomInfo = (TextView) findViewById(R.id.record_tevi_zoom_info);
            mTimeInfo = (TextView) findViewById(R.id.record_tevi_time_info);
            mResoInfo = (TextView) findViewById(R.id.record_tevi_reso_info);
			mLoadingBar = (ProgressBar) findViewById(R.id.record_loading_bar);
            mSurfView = (SurfaceView) findViewById(R.id.id_port_view);
            mScaleDetector = new ScaleGestureDetector(this, new ScaleListener());

			mPanelBack = findViewById(R.id.record_panel_back);
			mPanelManager = new PanelManager(222, mPanelBack, null);
			mEffectPanel = findViewById(R.id.panel_effects);

			mOpenEffectPanel = (ImageButton) findViewById(R.id.record_open_effect_panel);
			mOpenEffectPanel.setOnClickListener(OpenEffectClicker);
			App.screenScaled(mOpenEffectPanel, LARGE_KNOB_SIZE);

			mCloseEffectPanel = (ImageButton) findViewById(R.id.record_close_effect_panel);
			mCloseEffectPanel.setOnClickListener(ClosePanelClicker);

			mEffectsStatus = (TextView) findViewById(R.id.effects_panel_status);
			mEffectPanelItems = new ArrayList<EffectItem>();
			loadEffectList();

			mEffectPanelList = (ListView) findViewById(R.id.effects_panel_list);
			mEffectsAdapter = new EffecstPanelAdapter(this, R.layout.effect_row, mEffectPanelItems);
			mEffectPanelList.setAdapter(mEffectsAdapter);
			mEffectPanelList.setOnItemClickListener(new EffectItemClicker());


			mOpenThemePanel = (ImageButton) findViewById(R.id.record_open_theme_panel);
			mOpenThemePanel.setOnClickListener(OpenThemeClicker);
			App.screenScaled(mOpenThemePanel, LARGE_KNOB_SIZE);

			mCloseThemePanel = (ImageButton) findViewById(R.id.record_close_theme_panel);
			mCloseThemePanel.setOnClickListener(ClosePanelClicker);

			mThemePanel = findViewById(R.id.theme_panel);
			mThemePanelItems = new ArrayList<ThemeItem>();
			loadThemeList();
			mThemeAdapter = new ThemePanelAdapter(this, R.layout.theme_row, mThemePanelItems);
			mThemePanelList = (ListView) findViewById(R.id.theme_panel_list);
			mThemePanelList.setAdapter(mThemeAdapter);
			mThemePanelList.setOnItemClickListener(new ThemeItemClicker());

			Encoder.create();
            App.gRecordedPath = "";

            if (null == App.gFrontResoList || null == App.gBackResoList)
                mSwitchButton.setVisibility(View.INVISIBLE);

			mTimerHandler = new Handler();
            mTimerHandler.postDelayed(timerTack, 999);
        } catch (Exception ex) {
            App.log( " ***_ex RecordActivity_onCreate: " + ex.getMessage());
        };
    }





	private Runnable timerTack = new Runnable() {
        @Override
        public void run() {
            mTimerCount++;
            //App.showShortToast("timerTack: " + mTimerCount);
            if (1 == mTimerCount) firstTack();
            //controlsTack();
            mTimerHandler.postDelayed(this, 999);
        }
    };



    private void firstTack() {
        //App.showShortToast("firstTack");
    }



    // Animate the content view to 100% opacity, and clear any animation listener set on the view.
    private void showControls() {
		mControlLayout.setAlpha(0f);
		mControlLayout.setVisibility(View.VISIBLE);
		mControlLayout.animate()
            .alpha(1f)
            .setDuration(FADE_TIMEOUT)
            .setListener(null);
    }



    private void hideControls() {
		mControlLayout.setAlpha(1f);
		mControlLayout.animate()
            .alpha(0f)
            .setDuration(FADE_TIMEOUT)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
					mControlLayout.setVisibility(GONE);
                }
            });
    }



    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        ///App.log( "dispatchTouchEvent");
        mScaleDetector.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }






    public class ScaleListener extends  ScaleGestureDetector.SimpleOnScaleGestureListener {

        private float mFactor = 1.0f;


        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mFactor = detector.getScaleFactor();
            App.log( "onScale: " + mFactor);
            float prev = detector.getPreviousSpan();
            float curr = detector.getCurrentSpan();
            if (curr > prev) {
                App.log( "onScale inc");
				Encoder.get().incZoom();
            } else if (curr < prev) {
                App.log( "onScale dec");
                Encoder.get().decZoom();
            }
            String zoin = Encoder.get().getZoom() + "x";
            mZoomInfo.setText(zoin);
            App.log( "onScale: " + Encoder.get().getZoom());
            return true;
        }



        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            //App.log( "onScaleBegin");
            return true;
        }



        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            //App.log( "onScaleEnd: " + Encoder.get().getZoom());
        }

    }




	private class BeginClicker implements View.OnClickListener {

        public void onClick(View v) {
			String folder = App.getOutputFolder();
			if (!Tools.folderExists(folder)) {
				App.showAlert(RecordActivity.this, "Something is wrong!", "The output directory does not exist!");
				return;
			}

			Encoder.get().begin();
			App.gRecordedPath = Encoder.get().getOutputName();
        }
    }



	private class CeaseClicker implements View.OnClickListener {

        public void onClick(View v) {
			Encoder.get().cease();
        }
    }



	private class SwitchClicker implements View.OnClickListener {

        public void onClick(View v) {
			boolean nowFront = App.getPrefBln("now_front");
			nowFront = !nowFront;
			App.setPrefBln("now_front", nowFront);

			stopRecordService();
			startRecordService();
        }
    }





	private class BackgroundClicker implements View.OnClickListener {
		public void onClick(View v) {
			moveTaskToBack(true);
		}
	}



	public class GridClicker implements ToggleImageButton.OnCheckedChangeListener {
		public void onCheckedChanged(ToggleImageButton buttonView, boolean isChecked) {
			Encoder.get().setGrid(isChecked);
			App.setPrefBln("show_grid", isChecked);
		}
    }



	public class FlashClicker implements ToggleImageButton.OnCheckedChangeListener {
		public void onCheckedChanged(ToggleImageButton buttonView, boolean isChecked) {
			if (isChecked)
				Encoder.get().turnFlash(true);
			else
				Encoder.get().turnFlash(false);
		}
	}






	private boolean hasFlash() {
		boolean result = Encoder.get().hasFlash();
		return result;
	}



	public void updateUI() {
		if (Encoder.get().isRecording()) {
			mBeginButton.setVisibility(View.INVISIBLE);
			mCeaseButton.setVisibility(View.VISIBLE);
			mBackgroundButton.setVisibility(View.VISIBLE);
			mSwitchButton.setVisibility(View.INVISIBLE);
		} else {
			mBeginButton.setVisibility(View.VISIBLE);
			mCeaseButton.setVisibility(View.INVISIBLE);
			mBackgroundButton.setVisibility(View.INVISIBLE);
			mSwitchButton.setVisibility(View.VISIBLE);
		}
		mFileName.setText(Encoder.get().getOutputName());
		mResoInfo.setText(Encoder.get().getCameraSize().y + "p");

		if (hasFlash())
			mFlashButton.setVisibility(View.VISIBLE);
		else
			mFlashButton.setVisibility(View.INVISIBLE);

		updateStatus();
    }



	public void updateStatus() {
		Encoder.StatusC status = Encoder.get().getStatus();

		if (Encoder.get().isRecording()) {
			if (status.size == 0)
				mLoadingBar.setVisibility(View.VISIBLE);
			else
				mLoadingBar.setVisibility(View.INVISIBLE);
		}

		String text = String.format("%02d:%02d", status.secs/60, status.secs%60);
		mTimeInfo.setText(text);

		TextView tevi = (TextView) findViewById(R.id.record_tevi_size_info);
		tevi.setText( Tools.formatSize(status.size));
	}







	View.OnClickListener OpenEffectClicker = new View.OnClickListener() {
		@Override
		public void onClick(final View v) {
			if (null == v)
				mPanelManager.quick = true;
			mPanelManager.openLeft(mEffectPanel);
			updateEffectsStatus();
		}
	};


	View.OnClickListener OpenThemeClicker = new View.OnClickListener() {
		@Override
		public void onClick(final View v) {
			if (null == v)
				mPanelManager.quick = true;
			mPanelManager.openLeft(mThemePanel);
		}
	};


	View.OnClickListener ClosePanelClicker = new View.OnClickListener() {
		@Override
		public void onClick(final View v) {
			mPanelManager.clear();
		}
	};




	private void loadEffectList() {
		List<String> list = glEffect.getEffects();
		for (String name : list) {
			mEffectPanelItems.add(new EffectItem(1, R.drawable.settings_54, name));
		}
	}



	private void updateEffectsStatus() {
		mEffectsAdapter.notifyDataSetChanged();
		mEffectsStatus.setText("Selected: " + App.sMyEffects.size());
	}



	private class EffectItemClicker implements ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			EffectItem item = (EffectItem) view.getTag();
			if (null == item) return;
			boolean have = App.sMyEffects.contains(item.name);
			if (have)
				App.sMyEffects.remove(item.name);
			else
				App.sMyEffects.add(item.name);
			Encoder.get().setEffects(App.sMyEffects);
			updateEffectsStatus();
		}
	}






	public class EffecstPanelAdapter extends ArrayAdapter<EffectItem> {

		Context mContext;
		int layoutResourceId;
		ArrayList<EffectItem> mEffectList = null;

		public EffecstPanelAdapter(Context mContext, int layoutResourceId, ArrayList<EffectItem> data) {
			super(mContext, layoutResourceId, data);
			this.layoutResourceId = layoutResourceId;
			this.mContext = mContext;
			this.mEffectList = data;
		}


		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View itemView = convertView;
			if (itemView == null) {
				LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
				itemView = inflater.inflate(layoutResourceId, parent, false);
			}

			EffectItem item = mEffectList.get(position);
			itemView.setTag(item);

			TextView textViewName = (TextView) itemView.findViewById(R.id.effect_row_name);
			textViewName.setText(item.name);

			CheckBox cb = (CheckBox) itemView.findViewById(R.id.effect_row_check);
			if (App.sMyEffects.contains(item.name))
				cb.setChecked(true);
			else
				cb.setChecked(false);

			return itemView;
		}
	}





	private void loadThemeList() {
		List<String> list = Themer.get().getNames();
		for (String name : list) {
			mThemePanelItems.add(new ThemeItem(1, R.drawable.settings_54, name));
		}
	}



	private class ThemeItemClicker implements ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			ThemeItem item = (ThemeItem) view.getTag();
			if (null == item) return;
			boolean selected = App.sMyTheme.equals(item.name);
			if (selected)
				App.sMyTheme = "";
			else
				App.sMyTheme = item.name;
			ThemeC theme = Themer.get().getTheme(App.sMyTheme);
			Encoder.get().setTheme(theme);
			mThemeAdapter.notifyDataSetChanged();
		}
	}




	public class ThemePanelAdapter extends ArrayAdapter<ThemeItem> {
		Context mContext;
		int layoutResourceId;
		ArrayList<ThemeItem> mThemeList = null;

		public ThemePanelAdapter(Context mContext, int layoutResourceId, ArrayList<ThemeItem> data) {
			super(mContext, layoutResourceId, data);
			this.layoutResourceId = layoutResourceId;
			this.mContext = mContext;
			this.mThemeList = data;
		}


		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View itemView = convertView;
			if (itemView == null) {
				LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
				itemView = inflater.inflate(layoutResourceId, parent, false);
			}

			ThemeItem item = mThemeList.get(position);
			itemView.setTag(item);

			TextView textViewName = (TextView) itemView.findViewById(R.id.theme_row_name);
			textViewName.setText(item.name);

			RadioButton rabu = (RadioButton) itemView.findViewById(R.id.theme_row_radio);
			if (App.sMyTheme.equals(item.name))
				rabu.setChecked(true);
			else
				rabu.setChecked(false);

			return itemView;
		}
	}






	// * SurfaceHolder.Callback

	private class SurfaceEventer implements  SurfaceHolder.Callback {


		@Override
		public void surfaceCreated(SurfaceHolder aHolder) {
			App.log( "surfaceCreated");
			Encoder.get().attachScreen(aHolder);
		}



		@Override
		public void surfaceChanged(SurfaceHolder aHolder, int format, int aWidth, int aHeight) {
			App.log( "surfaceChanged fmt=" + format + " size=" + aWidth + "x" + aHeight);
			Encoder.get().changeScreen(aWidth, aHeight);
		}



		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			App.log( "Surface destroyed");
			Encoder.get().detachScreen();
		}
	}




}
