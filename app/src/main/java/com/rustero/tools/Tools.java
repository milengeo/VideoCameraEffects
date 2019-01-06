package com.rustero.tools;


import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class Tools {

    static final String LOG_TAG = "Tools";



	static public long getFreeSpace() {
		long result = 0;
		File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		result = folder.getUsableSpace();
		return result;
	}


	public static String getSystemInfo() {
		String s = "\n---";
		s += "\n OS Version: " + System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
		s += "\n OS API Level: " + android.os.Build.VERSION.SDK_INT;
		s += "\n Device: " + android.os.Build.DEVICE;
		s += "\n Model: " + android.os.Build.MODEL + " (" + android.os.Build.PRODUCT + ")";
		s += "\n---";
		return s;
	}



	public static String getDisplayInfo(Activity activity) {
		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		String s = "\n---";
		s += "\n  pixels: " + metrics.widthPixels + " x " + metrics.heightPixels;
		s += "\n     dpi: " + metrics.densityDpi;        // 120, 160, 213, 240, 320, 480 or 640 dpi
		s += "\n density: " + metrics.density;
		s += "\n---";
		return s;
	}



	static public void delay(int aMills) {
        try {
            Thread.sleep(aMills);
        } catch (Exception ex) {}
    }


    static public boolean folderExists(String aPath) {
        File folder = new File(aPath);
        boolean result = folder.isDirectory();
        return result;
    }



    static public long getFileTime(String aPath) {
        File file = new File(aPath);
        long result = file.lastModified();
        return result;
    }



    static public String getFileExt(String aPath) {
        String ext = "";
        int p = aPath.lastIndexOf('.');
        if (p > 0)
            ext = aPath.substring(p + 1);
        return ext.toLowerCase();
    }



    static public String getFileName(String aPath) {
        String name = "";
        int p = aPath.lastIndexOf('.');
        if (p > -1)
            name = aPath.substring(0, p);
        else
            name = aPath;
        p = name.lastIndexOf('/');
        if (p > -1)
            name = name.substring(p+1);
        return name.toLowerCase();
    }


    static public String getFileNameExt(String aPath) {
        String result = aPath;
        int p = result.lastIndexOf('/');
        if (p > -1)
            result = result.substring(p+1);
        return result.toLowerCase();
    }


    static  public String getFileFolder(String aPath) {
        String result = "";
        String path = aPath.substring(0, aPath.lastIndexOf("/"));
		result = path.substring(path.lastIndexOf("/"));
        return result;
    }


    static public int getIntAttr(Context aContext, int aId) {
        TypedValue typedValue = new TypedValue();
        int[] attrIds= new int[] { android.R.attr.actionBarSize };
        int attrIndex = 0;
        TypedArray ta = aContext.obtainStyledAttributes(typedValue.data, attrIds);
        int result = ta.getDimensionPixelSize(attrIndex, -1);
        ta.recycle();
        return result;
    }


    static public boolean isBlank(String aValue) {
        if (aValue != null && !aValue.isEmpty())
            return false;
        else
            return true;
    }


    static public String addSlash(String aValue) {
        String result = aValue;
        if (result.length() > 0)
          if (result.charAt(result.length()-1) != '/')
              result += '/';
        return result;
    }


    static public String upperDir(String aValue) {
        String result = "";
        int p = aValue.lastIndexOf('/');
        if (p > 0)
            result = aValue.substring(0, p);
        if (result.length() == 0)
            result = "/";
        return result;
    }


    public static String formatSize(long size) {
        if(size <= 0) return "0KB";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/ Math.log10(1024));
        return new DecimalFormat("#,##0.0").format(size/ Math.pow(1024, digitGroups)) + "" + units[digitGroups];
    }


    public static String formatBitrate(long aByteRate) {
        if (aByteRate <= 0) return "0 kbps";
        String result = "";
        long kbps = aByteRate*8/1000;
        if (kbps >= 1000)
            result = new DecimalFormat("#,##0.0").format(kbps/1000.0) + "mbps";
        else
            result = new DecimalFormat("#,##0.0").format(kbps) + "kbps";
        return result;
    }


    public static String formatDuration(long aDura) {
        String result = "00:00";
        if (aDura <= 0) return result;
        if (aDura < 3600)
            result = String.format("%02d:%02d", aDura / 60, aDura % 60);
        else
            result = String.format("%d:%02d:%02d", aDura / 3600, (aDura % 3600) / 60, (aDura % 60));
        return result;
    }


    public static void writePrivateFile(Context aContext, String aName, String aText) {
        try {
            FileOutputStream fos = aContext.openFileOutput(aName, Context.MODE_PRIVATE);
            fos.write(aText.getBytes());
            fos.close();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }



    public static String readPrivateFile(Context aContext, String aName) {
        StringBuilder sb = new StringBuilder();
        try {
            FileInputStream fis = aContext.openFileInput(aName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
        } catch (Exception e) {
            ///Log.e(LOG_TAG, e.getMessage(), e);
        }
        return sb.toString();
    }



    public static int parseInt(String aStr) {
        int result = 0;
        try {
            result = Integer.parseInt(aStr);
        } catch (NumberFormatException e) {
            result = 0;
        }
        return result;
    }



    public static int getAboveInt(List<String> aList, int aValue) {
        int result = -1;
        for (int i=0; i<aList.size(); i++) {
            int val = parseInt(aList.get(i));
            if (val >= aValue) {
                result = i;
                break;
            }
        }

        if (-1 == result) {
            if (aList.size() > 0)
                result = aList.size()-1;
        }

        return result;
    }



    static public long getFreeInternal() {
//        long result = new File(ctx.getFilesDir().getAbsoluteFile().toString()).getFreeSpace();
//        long freeBytesExternal = new File(getExternalFilesDir(null).toString()).getFreeSpace();
        return 0;
    }



    static public Drawable scaleBitmap(Bitmap aBitmap, int aSize) {
        BitmapDrawable result = null;
        float scale = 1f;
        if (aBitmap.getWidth() < aBitmap.getHeight()) {
            scale = aSize / (float) aBitmap.getWidth();
        } else {
            scale = aSize / (float) aBitmap.getHeight();
        }
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        Bitmap bitmap = transformBitmap(matrix, aBitmap, aSize, aSize);

        if (null != bitmap) {
            bitmap = roundedBitmap(bitmap);
            result =  new BitmapDrawable(bitmap);
        }
        return result;
    }


    static public Bitmap transformBitmap(Matrix scaler, Bitmap source, int targetWidth, int targetHeight) {
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




    static public Bitmap roundedBitmap(Bitmap bitmap) {
        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 12;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return result;
    }



    public static void deepCopy(ByteBuffer aSource, ByteBuffer aTarget) {
        aSource.rewind();
        aTarget.clear();
        aTarget.put(aSource);
        aTarget.flip();
        aSource.flip();
    }


    public static ByteBuffer makeCopy(ByteBuffer aSource) {
        ByteBuffer result = ByteBuffer.allocate(aSource.remaining());
        aSource.rewind();
        result.put(aSource);
        result.flip();
        aSource.flip();
        return result;
    }



	public static ByteBuffer copyDirect(ByteBuffer aSource) {
        ByteBuffer result = ByteBuffer.allocateDirect(aSource.remaining());
        aSource.rewind();
        result.put(aSource);
        result.flip();
        aSource.flip();
        return result;
    }


    public static void appendFile(ByteBuffer aBuffer, String aPath) {
        try {
            File file = new File(aPath);
            FileChannel channel = new FileOutputStream(file, true).getChannel();
            aBuffer.flip();
            channel.write(aBuffer);
            channel.close();
        } catch (Exception e) { }
    }



    public void unzipToFolder(String zipPath, String targetPath) {
		File targetFolder = new File(targetPath);
		targetFolder.mkdirs();
		try {
			FileInputStream fin = new FileInputStream(zipPath);
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry ze = null;
			while ((ze = zin.getNextEntry()) != null) {
				FileOutputStream fout = new FileOutputStream(targetPath + "/" + ze.getName());
				for (int c = zin.read(); c != -1; c = zin.read()) {
					fout.write(c);
				}
				zin.closeEntry();
				fout.close();
			}
			zin.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}



    public static void dumpVideos(Context context) {
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Video.VideoColumns.DATA };
        Cursor c = context.getContentResolver().query(uri, projection, null, null, null);
        int vidsCount = 0;
        if (c != null) {
            vidsCount = c.getCount();
            while (c.moveToNext()) {
                Log.d(" * VIDEO", c.getString(0));
            }
            c.close();
        }
        Log.d("Tools", "Total count of videos: " + vidsCount);
    }


    public static void registerVideo(Context context, String path) {
        MediaScannerConnection.scanFile(context, new String[]{path}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.d("Tools", "Finished scanning " + path + " New row: " + uri);
                    }
                });
    }



    public static int nextPowerOf2(final int a)
    {
        int result = 1;
        while (result < a)
			result = result << 1;
        return result;
    }

}
