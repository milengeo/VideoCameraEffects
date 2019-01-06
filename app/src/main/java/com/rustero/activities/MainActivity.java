package com.rustero.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rustero.coders.Encoder;
import com.rustero.dialogs.DeleteDialog;
import com.rustero.dialogs.RenameDialog;
import com.rustero.App;
import com.rustero.BuildConfig;
import com.rustero.tools.Connectivity;
import com.rustero.tools.Tools;
import com.rustero.units.Cameras;
import com.rustero.units.FilmItem;
import com.rustero.units.FilmList;
import com.rustero.units.FilmMeta;
import com.rustero.units.MimeInfo;
import com.rustero.units.SoundPlayer;
import com.rustero.widgets.MessageBox;
import com.rustero.widgets.MyActivity;
import com.rustero.widgets.MyDragButton;
import com.rustero.widgets.PanelManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import com.rustero.R;

import static com.rustero.tools.Tools.roundedBitmap;


@SuppressWarnings("ResourceType")



public class MainActivity extends MyActivity implements AdapterView.OnItemClickListener {

	public static MainActivity self;

	private final int KNOB_SIZE = 160;
	private final int ACT_RESULT_SETTINGS = 	11;
	private final int ACT_RESULT_RECORD = 		12;

	private final int MAME_ITEM_SETTINGS 	= 1;
	private final int MAME_ITEM_ABOUT 		= 2;
	private final int MAME_ITEM_ENCOURAGE 	= 3;
	private final int MAME_ITEM_INVITE 		= 4;
	private final int MAME_ITEM_MENTION 	= 5;

	private PanelManager mPanelManager;
	private View mLeftPanel;
	private ImageButton mOpenPanel, mClosePanel;
	private ListView mPanelList;
	private ArrayList<MameItem> mMameItems;

	private ActionBar mActBar;
	private Toolbar mToolbar;
	private ListView mFilmView;
	private FilmAdapter mAdapter;
	private FilmList mFilmList;
	private MetaTask mMetaTask = new MetaTask();
	private MyDragButton btnRecord;
	private MenuItem mRenameMeit, mDeleteMeit, mShareMeit;
	private ArrayList<String> mFolders = new ArrayList<String>();
	private HashSet<String> mScannedFolders = new HashSet<>();

	private Handler mTackHandler;
	private boolean mTacked;

	private long mTotalFiles;
	private long mTotalBytes;
	private static int sScrollIndex;
	private int mListCount;







	public static MainActivity get() {
		return self;
	}



	public void onResume() {
		super.onResume();
		self = this;

		if (!BuildConfig.DEBUG) {
			String msg = "";
			if (App.DEVEL)
				msg = "Devel build!";
			if (Encoder.ACTOR_ID > 0)
				msg = "Actor build!";
			if (!msg.isEmpty()) {
				App.finishActivityAlert(this, "Error!", msg);
			}
		}

		scanFolders();
	}



	@Override
	protected void onPause() {
		super.onPause();
		self = null;

		App.aliveMeter.click();
		sScrollIndex = mFilmView.getFirstVisiblePosition();
	}





	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		onAttach();
		App.aliveMeter.click();
		App.log("onCreate");
		App.log(Tools.getDisplayInfo(this));

		mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
		setSupportActionBar(mToolbar);

		mActBar = getSupportActionBar();
		mActBar.setDisplayShowTitleEnabled(false); // Hide default toolbar title
		View container2 = findViewById(R.id.main_container_2);

		View panelBack = findViewById(R.id.main_panel_back);
		mPanelManager = new PanelManager(222, panelBack, null);

		mLeftPanel = findViewById(R.id.main_panel_left);
		mOpenPanel = (ImageButton) findViewById(R.id.main_open_panel);
		mOpenPanel.setOnClickListener(OpenPanelClicker);
		mClosePanel = (ImageButton) findViewById(R.id.main_close_panel);
		mClosePanel.setOnClickListener(ClosePanelClicker);

		mMameItems = new ArrayList<MameItem>();
		mMameItems.add(new MameItem(MAME_ITEM_SETTINGS, R.drawable.settings_54, App.resstr(R.string.settings)));
		mMameItems.add(new MameItem(MAME_ITEM_ENCOURAGE, R.drawable.star_54, App.resstr(R.string.encaurageus)));
		mMameItems.add(new MameItem(MAME_ITEM_INVITE, R.drawable.thumb_54, App.resstr(R.string.invite_friend)));
		mMameItems.add(new MameItem(MAME_ITEM_MENTION, R.drawable.facebook_54, App.resstr(R.string.mention_facebook)));
		mMameItems.add(new MameItem(MAME_ITEM_ABOUT, R.drawable.about_54, App.resstr(R.string.about)));

		mPanelList = (ListView) findViewById(R.id.main_panel_list);
		LeftPanelAdapter adapter = new LeftPanelAdapter(this, R.layout.menu_row, mMameItems);
		mPanelList.setAdapter(adapter);
		mPanelList.setOnItemClickListener(new LeftPanelItemClicker());

		btnRecord = (MyDragButton) findViewById(R.id.main_new_record);
		btnRecord.setAnchor(container2);
		btnRecord.setOnPressListener(new RecordClicker());
		App.screenScaled(btnRecord, KNOB_SIZE);

		mFilmView = (ListView) findViewById(R.id.main_film_view);
		mFilmView.setOnItemClickListener(this);
		//mAdView = (AdView) findViewById(R.id.main_adView);

		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_filmlock_1);
		if (!havePermissions()) {
			return;
		}

		loadCameras();
		mTackHandler = new Handler();
		mTackHandler.postDelayed(clickTack, 999);

		App.fbLog("maac_oncr");

		mFilmView.post(new Runnable() {
			@Override
			public void run() {
				onShow();
			}
		});
	}



	public void onShow() {
		if (PanelManager.type == PanelManager.TYPE.LEFT)
			OpenPanelClicker.onClick(null);
	}



	@Override
	protected void onDestroy() {
		super.onDestroy();
		self = null;
	}



	public void onAttach() {
		if (App.live) return; // already created
		App.live = true;
		App.log("onAttach");
		//App.gBitrateList = Arrays.asList(getResources().getStringArray(R.array.bitrate_list));

		SoundPlayer.create();
		SoundPlayer.get().addSound(this, R.raw.shutter);
	}



	@Override
	public void onBackPressed() {
		if (mPanelManager.clear()) {
			return;
		}
		super.onBackPressed();
	}




	private boolean havePermissions() {
		if (!App.selfPermissionGranted(this, Manifest.permission.INTERNET)) {
			App.finishActivityAlert(this, "Permission needed!", "You need to allow access to internet!");
			return false;
		}

		if (!App.selfPermissionGranted(this, Manifest.permission.CAMERA)) {
			App.finishActivityAlert(this, "Permission needed!", "You need to allow access to camera!");
			return false;
		}

		if (!App.selfPermissionGranted(this, Manifest.permission.RECORD_AUDIO)) {
			App.finishActivityAlert(this, "Permission needed!", "You need to allow audio recording!");
			return false;
		}

		if (!App.selfPermissionGranted(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
			App.finishActivityAlert(this, "Permission needed!", "You need to allow storage reading!");
			return false;
		}

		if (!App.selfPermissionGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			App.finishActivityAlert(this, "Permission needed!", "You need to allow storage writing!");
			return false;
		}

		return true;
	}



	private Runnable clickTack = new Runnable() {
		@Override
		public void run() {
			mTackHandler.postDelayed(this, 999);
			firstTack();
		}
	};



	void firstTack() {
		if (mTacked) return;
		mTacked = true;
	}







	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		App.log("onCreateOptionsMenu");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);

		mShareMeit = menu.findItem(R.id.main_meit_share);
		mRenameMeit = menu.findItem(R.id.main_meit_rename);
		mDeleteMeit = menu.findItem(R.id.main_meit_delete);

		return true;
	}



	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		updateMenu();
		return super.onPrepareOptionsMenu(menu);
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		App.log("onOptionsItemSelected");
		switch (item.getItemId()) {
			case R.id.main_meit_share:
				shareFilm();
				return true;
			case R.id.main_meit_rename:
				renameFilm();
				return true;
			case R.id.main_meit_delete:
				deleteFilm();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}






	private void shareFilm() {
		if (mFilmList.getSelected() == null) return;
		String path = mFilmList.getSelected().path;
		String name = mFilmList.getSelected().name;
		File inFile = new File(path);
		if (!inFile.exists()) return;
		final Uri uri = FileProvider.getUriForFile(this, "com.rustero.vicaef.fileprovider", inFile);

		final Intent intent = ShareCompat.IntentBuilder.from(this)
			.setType("video/mp4")
			.setSubject(name)
			.setStream(uri)
			.setChooserTitle("Share " + name + " with")
			.createChooserIntent()
			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
			.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

		this.startActivity(intent);
	}




	private void renameFilm() {
        //App.fbLog("renameFilm");
		if (mFilmList.getSelected() == null) return;
		String name = mFilmList.getSelected().name;
		name = name.substring(0, name.indexOf("."));

		new RenameDialog().ask(this, mFilmList.getSelected().path, name);
	}



	public class RenameDialogEventer implements RenameDialog.Eventer {

		public void onDone(String aPath, String aName) {

			FilmItem film = mFilmList.getFilm(aPath);
			if (film == null) return;
			File oldFile = new File(aPath);
			String newPath = oldFile.getParent() + "/" + aName + "." + film.ext;
			File newFile = new File(newPath);
			if (newFile.exists()) {
				MessageBox.show("Error renaming", "File already exists!");
				return;
			}

			oldFile.renameTo(newFile);
			film.path = newPath;
			film.name = newFile.getName();

			mAdapter.notifyDataSetChanged();
		}
	}


	private void deleteFilm() {
		if (mFilmList.getSelected() == null) return;
		final String path = mFilmList.getSelected().path;
		new DeleteDialog().ask(path);
	}


	public class DeleteDialogEventer implements DeleteDialog.Eventer {
		public void onOk(String aPath) {
			File file = new File(aPath);
			boolean deleted = file.delete();
			if (deleted) {
				FilmItem film = mFilmList.getFilm(aPath);
				if (null == film) return;
				mAdapter.remove(film);
				mFilmList.remove(film);
				mFilmList.setSelected(null);
				countTotals();
				updateUI();
			}
		}
	}








	private void showSettings() {
		Intent intent = new Intent(this, SettingsActivity.class);
		//Intent intent = new Intent(this, SetAct.class);

		startActivityForResult(intent, ACT_RESULT_SETTINGS);
	}



	private void showAbout() {
        //App.fbLog("showAbout");
		Intent intent = new Intent(this, AboutActivity.class);
		startActivityForResult(intent, 0);
	}



	private void showEncourage() {
		Uri uri = Uri.parse("market://details?id=" + getPackageName());
		Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
		try {
			startActivity(myAppLinkToMarket);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, " unable to find market app", Toast.LENGTH_LONG).show();
		}
	}




	private void showMention() {
		try {
			String urlToShare = "https://play.google.com/store/apps/details?id=com.rustero.vicaef";
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, urlToShare);

			// As fallback, launch sharer.php in a browser
			String fbPackage = getFacebookPackage(intent);
			if ("" == fbPackage) {
				String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=" + urlToShare;
				intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
			} else
				intent.setPackage(fbPackage);

			startActivity(intent);

		} catch (Exception e) {	}
	}



	private String getFacebookPackage(Intent intent) {
		String result = "";
		List<ResolveInfo> matches = getPackageManager().queryIntentActivities(intent, 0);
		for (ResolveInfo info : matches) {
			if (info.activityInfo.packageName.toLowerCase().startsWith("com.facebook.katana")) {
				result = info.activityInfo.packageName;
				break;
			}
		}
		return result;
	}



	private void showInvite() {
		try {
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, "Record video with cascading special effects in real-time.");
			intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.rustero.vicaef");
			startActivity(Intent.createChooser(intent, "Share with ..."));

            //App.fbLog("showInvite");
		} catch (Exception e) {
			//e.toString();
		}
	}






	private class RecordClicker implements MyDragButton.OnPressListener {

		public void onPress(MyDragButton aSender) {
			mListCount = mFilmView.getCount();
			sScrollIndex = mFilmView.getFirstVisiblePosition();
			Intent intent = new Intent(MainActivity.this, RecordActivity.class);
			startActivityForResult(intent, ACT_RESULT_RECORD);
		}
	};



	@Override
	protected void onActivityResult(int aSender, int resultCode, Intent data) {
		super.onActivityResult(aSender, resultCode, data);
		App.aliveMeter.click();

		switch (aSender) {
			case ACT_RESULT_SETTINGS:
				onSettingsIntentResult(resultCode);
				break;
			case ACT_RESULT_RECORD:
				onRecordIntentResult(resultCode);
				break;
		}
	}



	public void onSettingsIntentResult(int aResultCode) {
		int bitrate = App.getPrefAsInt("record_bitrate");
		App.log("bitrate: " + bitrate);
	}



	public void onRecordIntentResult(int aResultCode) {
		App.log("onRecordIntentResult: " + App.gRecordedPath);
		if (!App.gRecordedPath.isEmpty()) {
			String name = Tools.getFileNameExt(App.gRecordedPath);
			App.gMetaHeap.remove(name);
			//mNeedScroll = true;
		}
	}



	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		FilmItem item = mAdapter.getItem(position);
		if (mFilmList.getSelected() != item)
			mFilmList.setSelected(item);
		else
			mFilmList.setSelected(null);
		mAdapter.notifyDataSetChanged();
		updateUI();
	}



	private class PlayClicker implements View.OnClickListener {

		public void onClick(View view) {
			sScrollIndex = mFilmView.getFirstVisiblePosition();
			mFilmList.setSelected(null);
			FilmItem item = (FilmItem) view.getTag();
			Uri uri = Uri.parse(item.path);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			intent.setDataAndType(uri, "video/mp4");
			startActivity(intent);
		}

	}



	// take one folder content
    public void pickupFolder()
    {
		String path = mFolders.get(0);
		mFolders.remove(0);
		if (mScannedFolders.contains(path)) {
			return;
		}

		mScannedFolders.add(path);
        File folder = new File(path);
        File[] files = folder.listFiles();
        try {
            for(File file: files)
            {
                if (file.isDirectory()) {
					mFolders.add(file.getAbsolutePath());
					continue;
				}
                String ext = Tools.getFileExt(file.getName());
                if (!ext.equals("mp4")) continue;

                Date lastModDate = new Date(file.lastModified());
				SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				String date_modify = formater.format(lastModDate);

                FilmItem fiit = new FilmItem();
                mFilmList.add(fiit);
				fiit.path = file.getAbsolutePath();
                fiit.ext = ext;
                fiit.name = file.getName();
				fiit.folder = Tools.getFileFolder(fiit.path);
                fiit.bytes = Tools.formatSize(file.length());
                fiit.date = date_modify;
                fiit.size = file.length();
            }
        } catch(Exception e) {}
    }



    // load the folder content from the disk with icons and dates
    public void scanFolders() {
        mMetaTask.quit = true;

		mFilmList = new FilmList();
		mFolders.add(App.getOutputFolder());

		mScannedFolders.clear();
		while (mFolders.size() > 0) {
			pickupFolder();
		}
		countTotals();

		Collections.sort(mFilmList);
        mAdapter = new FilmAdapter(this, R.layout.film_row, mFilmList);
        mFilmView.setAdapter(mAdapter);
        updateUI();

        mMetaTask = new MetaTask();
        mMetaTask.execute();
    }



	private void countTotals() {
		mTotalFiles = 0;
		mTotalBytes = 0;
		for (FilmItem fiit : mFilmList) {
			mTotalFiles++;
			mTotalBytes += fiit.size;
		}
	}



    private void updateStatus() {
        TextView view = (TextView) findViewById(R.id.main_tevi_status);
        if (mTotalFiles == 0) {
            view.setText("No videos found");
        } else {
            String text;
            text = mTotalFiles + " " + App.resstr(R.string.videos) + ", " + App.resstr(R.string.total_size) + " " + Tools.formatSize(mTotalBytes);
            view.setText(text);
        }
    }



    private void updateUI() {
        updateStatus();
        updateMenu();
    }



    public void updateMenu() {
		if (null == mDeleteMeit) return;

		boolean haveWifi = Connectivity.isConnectedWifi(this);
		FilmItem seldFilm = mFilmList.getSelected();
		mShareMeit.setVisible((seldFilm != null));
		mRenameMeit.setVisible((seldFilm != null));
        mDeleteMeit.setVisible((seldFilm != null));
    }






    public class FilmAdapter extends ArrayAdapter<FilmItem> {

        private Context mContext;
        private int mLayout;
        List<FilmItem> mItems;

        public FilmAdapter(Context context, int resource, List<FilmItem> objects) {
            super(context, resource, objects);
            this.mContext = context;
            this.mLayout = resource;
            this.mItems = objects;
        }


        public FilmItem getItem(int i)
        {
            return mItems.get(i);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(mLayout, null);
            }
            // create a new view of my layout and inflate it in the row
            final FilmItem item = mItems.get(position);
			FilmItem seldFilm = mFilmList.getSelected();
            if ( (seldFilm != null) && seldFilm.path.equals(item.path))
                rowView.setBackgroundColor(0xffcccccc);
            else
                rowView.setBackgroundColor(0xffffffff);

            TextView tevi;
			tevi = (TextView) rowView.findViewById(R.id.film_row_size);
			tevi.setText(item.bytes);

			ImageView iv = (ImageView) rowView.findViewById(R.id.film_row_icon);
			tevi = (TextView) rowView.findViewById(R.id.film_row_duration);
			if (null != item.meta) {
				if (item.meta.icon1 != null)
					iv.setImageDrawable(item.meta.icon1);
				if (item.meta.duration > 0)
					tevi.setText( Tools.formatDuration(item.meta.duration));
			} else {
				iv.setImageDrawable(MimeInfo.GetIcon("mp4"));
			}

            tevi = (TextView) rowView.findViewById(R.id.film_row_name);
            tevi.setText(item.name);

            tevi = (TextView) rowView.findViewById(R.id.film_row_date);
            tevi.setText(item.date);

			iv = (ImageView) rowView.findViewById(R.id.film_row_play);
			iv.setTag(item);
			iv.setOnClickListener(new PlayClicker());

            return rowView;
        }
    }








    private void loadCameras() {
        String code = Tools.readPrivateFile(this, App.CAMERAS_XML);
        if (code.isEmpty()) {
            Cameras.findCameras();
        } else {
            Cameras.readCameras(code);
        }
		if ( ((null==App.gFrontCam) && (null==App.gBackCam)) ) {
			App.finishActivityAlert(this, App.self.getResources().getText(R.string.app_name).toString(), "No camera is found!");
		}
    }



	private void ask5stars() {
		if (!App.want5stars()) return;
		long nowSecs = System.currentTimeMillis() / 1000;
		App.setPrefLong("rate5secs", nowSecs);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Please encourage us with 5 stars rating! \n\n");

		builder.setNegativeButton(App.resstr(R.string.not_now), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.setPositiveButton(R.string.rate_5_stars, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				App.setPrefBln("rate5done", true);
				showEncourage();
			}
		});

		AlertDialog dialog = builder.create();
		dialog.setCancelable(false);
		dialog.show();
	}





	View.OnClickListener OpenPanelClicker = new View.OnClickListener() {
		@Override
		public void onClick(final View v) {
			if (null == v)
				mPanelManager.quick = true;
			mPanelManager.openLeft(mLeftPanel);
		}
	};


	View.OnClickListener ClosePanelClicker = new View.OnClickListener() {
		@Override
		public void onClick(final View v) {
			mPanelManager.clear();
		}
	};




	private class LeftPanelItemClicker implements ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			int tag = 0;
			if (position < mMameItems.size())
				tag = mMameItems.get(position).tag;
			mPanelManager.clear();
			mPanelList.setItemChecked(position, false);

			//App.showShortToast("tag: " + tag);
			switch (tag) {
				case MAME_ITEM_SETTINGS:
					showSettings();
					return;
				case MAME_ITEM_ABOUT:
					showAbout();
					return;
				case MAME_ITEM_ENCOURAGE:
					showEncourage();
					return;
				case MAME_ITEM_INVITE:
					showInvite();
					return;
				case MAME_ITEM_MENTION:
					showMention();
					return;
			}
		}

	}



	public class LeftPanelAdapter extends ArrayAdapter<MameItem> {

		Context mContext;
		int layoutResourceId;
		ArrayList<MameItem> mItems = null;

		public LeftPanelAdapter(Context mContext, int layoutResourceId, ArrayList<MameItem> data) {
			super(mContext, layoutResourceId, data);
			this.layoutResourceId = layoutResourceId;
			this.mContext = mContext;
			this.mItems = data;
		}



		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View listItem = convertView;
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			listItem = inflater.inflate(layoutResourceId, parent, false);

			ImageView imageViewIcon = (ImageView) listItem.findViewById(R.id.menu_row_icon);
			TextView textViewName = (TextView) listItem.findViewById(R.id.menu_row_name);

			MameItem item = mItems.get(position);
			imageViewIcon.setImageResource(item.icon);
			textViewName.setText(item.name);

			return listItem;
		}
	}



	public class MameItem {
		public int tag, icon;
		public String name;

		// Constructor.
		public MameItem(int tag, int icon, String name) {
			this.tag = tag;
			this.icon = icon;
			this.name = name;
		}
	}






	public class MetaTask extends AsyncTask<Void, Integer, String> {

		private volatile boolean quit;
		long mKick;
		private FilmList list = new FilmList();
		private App.MetaHeapC heap = new App.MetaHeapC();

		@Override
		protected String doInBackground(Void... params) {
			try {
				for (FilmItem fi : mFilmList) {
					if (quit) return "";
					FilmMeta meta = App.gMetaHeap.get(fi.name);
					if (null == meta)
						meta = getMeta(fi.path);
					fi.meta = meta;
					list.add(fi);
					heap.put(fi.name, meta);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "";
		}



		@Override
		protected void onPostExecute(String result) {
			if (quit) return;
			App.gMetaHeap = heap;
			mFilmList = list;
			mAdapter = new FilmAdapter(MainActivity.this, R.layout.film_row, mFilmList);
			mFilmView.setAdapter(mAdapter);

			if ( (mListCount > 0) && (mListCount != mAdapter.getCount()) ) {
				mFilmView.setSelection(mAdapter.getCount());
				mListCount = 0;
			} else if (sScrollIndex > 0) {
				mFilmView.setSelection(sScrollIndex);
				sScrollIndex = 0;
			}
		}



		private FilmMeta getMeta(String aPath) {
			mKick = System.currentTimeMillis();
			int iconSize = (int) getResources().getDimension(R.dimen.m_film_icon_size);
			FilmMeta result = new FilmMeta();
			Bitmap bitmap = null;
			MediaMetadataRetriever retriever = new MediaMetadataRetriever();

			took("11");
			try {
				retriever.setDataSource(aPath);
				String stime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
				result.duration = Long.parseLong(stime) / 1000;
				bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_NEXT_SYNC);
				took("22");
			} catch (IllegalArgumentException ex) {
				// Assume this is a corrupt video file
			} catch (RuntimeException ex) {
				// Assume this is a corrupt video file.
			} finally {
				try {
					retriever.release();
				} catch (RuntimeException ex) {
					// Ignore failures while cleaning up.
				}
			}
			if (null == bitmap) return result;

			took("77");
			float scale;
			if (bitmap.getWidth() < bitmap.getHeight()) {
				scale = iconSize / (float) bitmap.getWidth();
			} else {
				scale = iconSize / (float) bitmap.getHeight();
			}
			Matrix matrix = new Matrix();
			matrix.setScale(scale, scale);
			bitmap = transform(matrix, bitmap, iconSize, iconSize);

			took("88");
			if (null != bitmap) {
				bitmap = roundedBitmap(bitmap);
				result.icon1 = new BitmapDrawable(MainActivity.this.getResources(), bitmap);
			}

			took("99");
			return result;
		}


		private void took(String aTag) {
			long took = System.currentTimeMillis() - mKick;
			App.log("took " + aTag + ": " + took);
		}



		private Bitmap transform(Matrix scaler, Bitmap source, int targetWidth, int targetHeight) {
			boolean scaleUp = true;
			boolean recycle = true;

			int deltaX = source.getWidth() - targetWidth;
			int deltaY = source.getHeight() - targetHeight;
			if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
            /*
            * In this case the bitmap is smaller, at least in one dimension,
            * than the target.  Transform it by placing as much of the image
            * as possible into the target and leaving the top/bottom or
            * left/right (or both) black.
            */
				Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight,
						Bitmap.Config.ARGB_8888);
				Canvas c = new Canvas(b2);

				int deltaXHalf = Math.max(0, deltaX / 2);
				int deltaYHalf = Math.max(0, deltaY / 2);
				Rect src = new Rect(
						deltaXHalf,
						deltaYHalf,
						deltaXHalf + Math.min(targetWidth, source.getWidth()),
						deltaYHalf + Math.min(targetHeight, source.getHeight()));
				int dstX = (targetWidth  - src.width())  / 2;
				int dstY = (targetHeight - src.height()) / 2;
				Rect dst = new Rect(
						dstX,
						dstY,
						targetWidth - dstX,
						targetHeight - dstY);
				c.drawBitmap(source, src, dst, null);
				if (recycle) {
					source.recycle();
				}
				c.setBitmap(null);
				return b2;
			}
			float bitmapWidthF = source.getWidth();
			float bitmapHeightF = source.getHeight();

			float bitmapAspect = bitmapWidthF / bitmapHeightF;
			float viewAspect   = (float) targetWidth / targetHeight;

			if (bitmapAspect > viewAspect) {
				float scale = targetHeight / bitmapHeightF;
				if (scale < .9F || scale > 1F) {
					scaler.setScale(scale, scale);
				} else {
					scaler = null;
				}
			} else {
				float scale = targetWidth / bitmapWidthF;
				if (scale < .9F || scale > 1F) {
					scaler.setScale(scale, scale);
				} else {
					scaler = null;
				}
			}

			Bitmap b1;
			if (scaler != null) {
				// this is used for minithumb and crop, so we want to filter here.
				b1 = Bitmap.createBitmap(source, 0, 0,
						source.getWidth(), source.getHeight(), scaler, true);
			} else {
				b1 = source;
			}

			if (recycle && b1 != source) {
				source.recycle();
			}

			int dx1 = Math.max(0, b1.getWidth() - targetWidth);
			int dy1 = Math.max(0, b1.getHeight() - targetHeight);

			Bitmap b2 = Bitmap.createBitmap(
					b1,
					dx1 / 2,
					dy1 / 2,
					targetWidth,
					targetHeight);

			if (b2 != b1) {
				if (recycle || b1 != source) {
					b1.recycle();
				}
			}

			return b2;
		}

	}


}
