package com.rustero.units;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.rustero.tools.Tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MimeInfo {
    private static MimeInfo Instance = null;
    public static Application mApp;
    private static Map<String, Drawable> mIcons = new HashMap<>();



    public MimeInfo(Application aApp) {
        Instance = this;
        mApp = aApp;
        Context ctx = mApp.getApplicationContext();
    }

    public static MimeInfo Get() {
        return Instance;
    }



    public static Drawable GetIcon(String aExt){
        Drawable result = null;
        try {
            MimeTypeMap mmt = MimeTypeMap.getSingleton();
            String mime = "";
                mime = mmt.getMimeTypeFromExtension(aExt);
                if (Tools.isBlank(mime)) return null;
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setType(mime);

            final List<ResolveInfo> matches = mApp.getPackageManager().queryIntentActivities(intent, 0);
            if (!matches.isEmpty()) {
                ResolveInfo rein = matches.get(0);
                ApplicationInfo app = rein.activityInfo.applicationInfo;
                result = mApp.getPackageManager().getApplicationIcon(app);
            }
        } catch (Exception  ex)
        { Log. d("*** EXCEPTION: ", "MimeInfo.GetIcon> " + ex.getMessage()); }
        return result;
    }
}
