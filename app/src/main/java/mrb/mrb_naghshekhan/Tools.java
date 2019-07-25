package mrb.mrb_naghshekhan;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by MRB on 20/07/2019.
 */
public class Tools {
    public static void fileCopy(String src, String dst) throws IOException {
        File src_ = new File(src);
        File dst_ = new File(dst);
        fileCopy(src_, dst_);
    }

    public static void fileCopy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    public static String getFileName(String filePath) {
        int p = filePath.lastIndexOf("/");
        return filePath.substring(p);
    }

    public static String getPath(Context context, Uri uri) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // DocumentProvider
            if (DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                    // TODO handle non-primary volumes
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static boolean unpackZip(String path, String zipname) {
        InputStream is;
        ZipInputStream zis;
        try {
            String filename;
            is = new FileInputStream(zipname);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null) {
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(path + filename);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(path + filename);

                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    public static MediaPlayer mp;

    public static boolean Download(String url,String params,String local_file,String method) {
        try {
            // Define the server endpoint to send the HTTP request to
            URL serverUrl = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) serverUrl.openConnection();

            // Indicate that we want to write to the HTTP request body
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod(method);

            // Writing the post data to the HTTP request body
            BufferedWriter httpRequestBodyWriter = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
            if (params != "")
                httpRequestBodyWriter.write(params);
            httpRequestBodyWriter.close();

            // Get a readable channel from url connection
            ReadableByteChannel readableChannelForHttpResponseBody = Channels.newChannel(urlConnection.getInputStream());

            // Create the file channel to save file
            FileOutputStream fosForDownloadedFile = new FileOutputStream(local_file);
            FileChannel fileChannelForDownloadedFile = fosForDownloadedFile.getChannel();

            // Save the contents of HTTP response to local file
            fileChannelForDownloadedFile.transferFrom(readableChannelForHttpResponseBody, 0, Long.MAX_VALUE);
            //http://daremco.com/content/files/products/12647/300.jpg
            return true;
        } catch (Exception ex) {

            return false;
        }
    }
    public static boolean GetBitmapFromURL(String url,String local_file) {
        try {
            java.net.URL url_ = new java.net.URL(url);
            HttpURLConnection connection = (HttpURLConnection) url_.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            savebitmap(myBitmap,local_file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static File savebitmap(Bitmap bmp , String filename) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 60, bytes);
        File f = new File(filename);
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.close();
        return f;
    }
    static String[] faNumbers = new String[]{ "۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹" };
    static String[] enNumbers = new String[]{ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
    public static String En2Fa(String str) {
        for (int i = 0; i < faNumbers.length; i++)
            str = str.replace(enNumbers[i], faNumbers[i]);
        return str;
    }
    public static String Fa2En(String str) {
        for (int i = 0; i < faNumbers.length; i++)
            str = str.replace(faNumbers[i], enNumbers[i]);
        return str;
    }

    public static boolean Speek(String str, Context context) {
        try {
            str = str.trim();
            while (str.startsWith("0"))
                str = str.substring(1);
            char[] STR = str.toCharArray();
            String[] D = new String[str.length()];
            int n = D.length;
            for (int i = 0; i < n; i++)
                if (STR[i] != '0')
                    D[i] = PadRight(STR[i] + "", n - i, '0');
            boolean add_and = false;
            for (int i = n - 1; i >= 0; i--)
                if (D[i] != null) {
                    if (add_and)
                        D[i] += "_";
                    add_and = true;
                }
            if (n >= 2 && D[n - 2] != null && D[n - 1] != null && D[n - 2].compareTo("10_") == 0)// 11~19
            {
                D[n - 1] = "1" + D[n - 1];
                D[n - 2] = null;
            }
            for (int i = 0; i < D.length; i++)
                if (D[i] != null)
                    if (!SpeekNum(D[i], context))
                        return false;

        } catch (Exception ex) {
        }
        //Toast.makeText(this, "صدا", Toast.LENGTH_SHORT).show();
        return true;
    }
    public  static  void  Silent() {
        if (mp != null) try {
            mp.stop();
            mp.release();
            mp=null;
        } catch (Exception ex) {
        }
    }
    public static String PadRight(String str,int len, char ch)
    {
        String res=str;
        while(res.length()<len)
            res = res+ch;
        return res;
    }
    public static boolean WaitForMP() {
        if (mp != null) {
            try {
                while (mp.isPlaying())
                    Thread.sleep(100);
                mp.release();
                mp = null;
            } catch (Exception ex) {
                return false;
            }
        }
        return true;
    }
    public static boolean SpeekRang(Context context) {
        if(!WaitForMP())return false;
        mp = MediaPlayer.create(context, R.raw.rang);
        mp.start();
        return true;
    }

    public static boolean SpeekRaj(Context context) {
        if(!WaitForMP())return false;
        mp = MediaPlayer.create(context, R.raw.raj);
        mp.start();
        return true;
    }

    public static boolean SpeekGereh(Context context) {
        if(!WaitForMP())return false;
        mp = MediaPlayer.create(context, R.raw.gereh);
        mp.start();
        return true;
    }

    static boolean SpeekNum(String num, Context context) {
        if(!WaitForMP())return false;
        int res_id = R.raw._0;
        if      (num.compareTo( "0")==0) res_id = R.raw._0;
        else if (num.compareTo( "1")==0) res_id = R.raw._1;
        else if (num.compareTo( "2")==0) res_id = R.raw._2;
        else if (num.compareTo( "3")==0) res_id = R.raw._3;
        else if (num.compareTo( "4")==0) res_id = R.raw._4;
        else if (num.compareTo( "5")==0) res_id = R.raw._5;
        else if (num.compareTo( "6")==0) res_id = R.raw._6;
        else if (num.compareTo( "7")==0) res_id = R.raw._7;
        else if (num.compareTo( "8")==0) res_id = R.raw._8;
        else if (num.compareTo( "9")==0) res_id = R.raw._9;
        else if (num.compareTo( "10")==0) res_id = R.raw._10;
        else if (num.compareTo( "11")==0) res_id = R.raw._11;
        else if (num.compareTo( "12")==0) res_id = R.raw._12;
        else if (num.compareTo( "13")==0) res_id = R.raw._13;
        else if (num.compareTo( "14")==0) res_id = R.raw._14;
        else if (num.compareTo( "15")==0) res_id = R.raw._15;
        else if (num.compareTo( "16")==0) res_id = R.raw._16;
        else if (num.compareTo( "17")==0) res_id = R.raw._17;
        else if (num.compareTo( "18")==0) res_id = R.raw._18;
        else if (num.compareTo( "19")==0) res_id = R.raw._19;
        else if (num.compareTo( "20")==0) res_id = R.raw._20;
        else if (num.compareTo( "30")==0) res_id = R.raw._30;
        else if (num.compareTo( "40")==0) res_id = R.raw._40;
        else if (num.compareTo( "50")==0) res_id = R.raw._50;
        else if (num.compareTo( "60")==0) res_id = R.raw._60;
        else if (num.compareTo( "70")==0) res_id = R.raw._70;
        else if (num.compareTo( "80")==0) res_id = R.raw._80;
        else if (num.compareTo( "90")==0) res_id = R.raw._90;
        else if (num.compareTo( "20_")==0) res_id = R.raw._20_;
        else if (num.compareTo( "30_")==0) res_id = R.raw._30_;
        else if (num.compareTo( "40_")==0) res_id = R.raw._40_;
        else if (num.compareTo( "50_")==0) res_id = R.raw._50_;
        else if (num.compareTo( "60_")==0) res_id = R.raw._60_;
        else if (num.compareTo( "70_")==0) res_id = R.raw._70_;
        else if (num.compareTo( "80_")==0) res_id = R.raw._80_;
        else if (num.compareTo( "90_")==0) res_id = R.raw._90_;
        else if (num.compareTo( "100")==0) res_id = R.raw._100;
        else if (num.compareTo( "200")==0) res_id = R.raw._200;
        else if (num.compareTo( "300")==0) res_id = R.raw._300;
        else if (num.compareTo( "400")==0) res_id = R.raw._400;
        else if (num.compareTo( "500")==0) res_id = R.raw._500;
        else if (num.compareTo( "600")==0) res_id = R.raw._600;
        else if (num.compareTo( "700")==0) res_id = R.raw._700;
        else if (num.compareTo( "800")==0) res_id = R.raw._800;
        else if (num.compareTo( "900")==0) res_id = R.raw._900;
        else if (num.compareTo( "100_")==0) res_id = R.raw._100_;
        else if (num.compareTo( "200_")==0) res_id = R.raw._200_;
        else if (num.compareTo( "300_")==0) res_id = R.raw._300_;
        else if (num.compareTo( "400_")==0) res_id = R.raw._400_;
        else if (num.compareTo( "500_")==0) res_id = R.raw._500_;
        else if (num.compareTo( "600_")==0) res_id = R.raw._600_;
        else if (num.compareTo( "700_")==0) res_id = R.raw._700_;
        else if (num.compareTo( "800_")==0) res_id = R.raw._800_;
        else if (num.compareTo( "900_")==0) res_id = R.raw._900_;
        mp = MediaPlayer.create(context, res_id);
        mp.start();
        return true;
    }
}
