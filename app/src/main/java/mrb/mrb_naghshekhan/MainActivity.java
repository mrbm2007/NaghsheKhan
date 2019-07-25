package mrb.mrb_naghshekhan;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Spinner sp = (Spinner) findViewById(R.id.spinner_map);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                UpdateView();
                RelativeLayout im = (RelativeLayout) findViewById(R.id.back_panel);
                try {
                    im.setBackgroundDrawable(Drawable.createFromPath(currentMap().file.getPath() + ".jpg"));
                    /*int sdk = android.os.Build.VERSION.SDK_INT;
                    if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        im.setBackgroundDrawable(Drawable.createFromPath(currentMap().file.getPath() + ".jpg"));
                    } else {
                        im.setBackground(Drawable.createFromPath(currentMap().file.getPath() + ".jpg"));
                    }*/
                } catch (Exception ex) {
                    im.setBackgroundResource(R.drawable.back);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                TextView t1 = (TextView) findViewById(R.id.text_knots);
                t1.setText("نقشه انتخاب نشده است");
            }

        });
        RefreshList();
        ReadSetting();

        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            final Handler handler = new Handler();
            final Activity this_ = this;
            iii=0;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Tools.hideKeyboard(this_);
                                } finally {
                                }
                            }
                        });
                        if (iii < 5)
                            handler.postDelayed(this, 800);
                        iii++;
                    }
                }
            }, 0);
        } catch (Exception ex) {
        }
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_CALL||
            event.getKeyCode() == KeyEvent.KEYCODE_FORWARD||
            event.getKeyCode() == KeyEvent.KEYCODE_HEADSETHOOK ) {
            Map m = currentMap();
            if(m!=null) {
                m.GoNext();
                UpdateView();
                if (!playing)
                    Say(true);
            }
            return true;
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK  ) {
            Map m = currentMap();
            if(m!=null) {
                m.GoNext();
                UpdateView();
                if (!playing)
                    Say(true);
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
    int iii=0;
    public void  onClickPanel_back(View v) {
        findViewById(R.id.panel_1).setAlpha(1f);
        findViewById(R.id.panel_2).setVisibility(View.VISIBLE);
    }
    public void onClickBtn_image(View v) {
        if(findViewById(R.id.panel_2).getVisibility()== View.VISIBLE) {
            findViewById(R.id.panel_1).setAlpha(0.2f);
            findViewById(R.id.panel_2).setVisibility(View.INVISIBLE);
        }else {
            findViewById(R.id.panel_1).setAlpha(1f);
            findViewById(R.id.panel_2).setVisibility(View.VISIBLE);
        }
        /*
        Dialog builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });

        ImageView imageView = new ImageView(this);
        RelativeLayout im = (RelativeLayout)findViewById(R.id.back_panel);
        imageView.setImageDrawable(im.getBackground());
        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        builder.show();*/
    }
    public void onClickBtn_reset(View v) {
        final Map m = currentMap();
        if (m == null) return;

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        m.GoFirst();
                        UpdateView();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("ابتدا نقشه").setPositiveButton("بله", dialogClickListener)
                .setNegativeButton("نه", dialogClickListener).show();
        //DrawMap();
    }
    public void DrawMap() {
        Map m = currentMap();
        if(m==null)return;
        for (int i = 0; i < m.rows.size(); i++)
            m.rows.get(i).ReadIf();
        int N = 1;
        for (int i = 0; i < m.rows.get(0).colors.size(); i++)
            for (int j = 0; j < m.rows.get(0).colors.get(i).knots.size(); j++)
                N = Math.max(N, m.rows.get(0).colors.get(i).knots.get(j));
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Bitmap bitmap = Bitmap.createBitmap(N, m.rows.size(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        {
            for (int i = 0; i < m.rows.size(); i++)
                for (int j = 0; j < m.rows.get(i).colors.size(); j++) {
                    int c = m.rows.get(i).colors.get(j).getColor();
                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    paint.setColor(c);
                    for (int k = 0; k < m.rows.get(i).colors.get(j).knots.size(); k++) {
                        canvas.drawPoint(m.rows.get(i).colors.get(j).knots.get(k), i, paint);
                    }
                }
        }
        imageView.setImageBitmap(bitmap);
    }
    public void onClickBtn_row_next(View v){
        Map m = currentMap();
        if(m==null)return;
        m.current_row_index++;
        m.current_knot_index=0;
        m.current_color_index=0;
        UpdateView();
    }
    public void onClickBtn_row_next_10(View v){
        Map m = currentMap();
        if(m==null)return;
        m.current_row_index+=10;
        m.current_knot_index=0;
        m.current_color_index=0;
        UpdateView();
    }
    public void onClickBtn_row_prev(View v){
        Map m = currentMap();
        if(m==null)return;
        m.current_row_index--;
        m.current_knot_index=0;
        m.current_color_index=0;
        UpdateView();
    }
    public void onClickBtn_row_prev_10(View v){
        Map m = currentMap();
        if(m==null)return;
        m.current_row_index-=10;
        m.current_knot_index=0;
        m.current_color_index=0;
        UpdateView();
    }
    public void onClickBtn_color_next(View v){
        Map m = currentMap();
        if(m==null)return;
        m.current_color_index++;
        m.current_knot_index=0;
        UpdateView();
    }
    public void onClickBtn_color_next_10(View v){
        Map m = currentMap();
        if(m==null)return;
        m.current_color_index+=10;
        m.current_knot_index=0;
        UpdateView();
    }
    public void onClickBtn_color_prev(View v){
        Map m = currentMap();
        if(m==null)return;
        m.current_color_index--;
        m.current_knot_index=0;
        UpdateView();
    }
    public void onClickBtn_color_prev_10(View v){
        Map m = currentMap();
        if(m==null)return;
        m.current_color_index-=10;
        m.current_knot_index=0;
        UpdateView();
    }
    public void onClickBtn_next(View v){
        Map m = currentMap();
        if(m==null)return;
        m.GoNext();
        UpdateView();
    }
    public void onClickBtn_prev(View v){
        Map m = currentMap();
        if(m==null)return;
        m.GoPrev();
        UpdateView();
    }

    public void onClickBtn_play(View v) {
        Map m = currentMap();
        if(m==null)return;
        findViewById(R.id.btn_start).setVisibility(View.GONE);
        findViewById(R.id.btn_stop).setVisibility(View.VISIBLE);
        double gpm = Double.parseDouble(((EditText) findViewById(R.id.edit_speed)).getText().toString());
        dt = (int) (1000.0 / (gpm / 60.0) - 1000.0);

        playing = true;
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (playing) {
                    currentMap().GoNext();
                    final Semaphore mutex = new Semaphore(0);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                UpdateView();
                            } finally {
                                mutex.release();
                            }
                        }
                    });
                    try {
                        mutex.acquire(); // wait for runOnUiThread
                    } catch (Exception e) {
                    }
                    int dt_ = dt;
                    if (currentMap().current_knot_index == 0)
                        dt_ *= 1.9;
                    if (dt_ < 1000)
                        dt_ = 1100;
                    handler.postDelayed(this, dt_);
                }
            }
        }, 0);

        UpdateView();
        WriteSetting();
    }
    Handler handler;
    boolean playing = false;
    public void onClickBtn_stop(View v) {
        Map m = currentMap();
        if (m == null) return;
        findViewById(R.id.btn_stop).setVisibility(View.GONE);
        findViewById(R.id.btn_start).setVisibility(View.VISIBLE);
        Tools.Silent();
        Tools.Silent();
        Tools.Silent();
        playing = false;
        UpdateView();
        if (handler != null)
            try {
                handler.removeCallbacksAndMessages(null); 
            } catch (Exception ex) {
            }
    }
    public void onClickBtn_delete(View v){
        Map m = currentMap();
        if(m==null)return;
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        currentMap().file.delete();
                        RefreshList();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("نقشه حذف شود؟").setPositiveButton("بله", dialogClickListener)
                .setNegativeButton("نه", dialogClickListener).show();
    }
    public void onClickBtn_row_ok(View v) {
        Map m = currentMap();
        if(m==null)return;
        String str = ((EditText) findViewById(R.id.edit_row)).getText().toString();
        m.current_row_index = Integer.parseInt(str) - 1;
        UpdateView();
    }
    public void onClickBtn_refresh(View v)
    {
        RefreshList();
    }
    public void onClickBtn_Add(View v) {
        try {
            final Activity ac = this;
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            AlertDialog.Builder builder = new AlertDialog.Builder(ac);
                            builder.setTitle("کد نقشه را وارد کنید:"+ "  (مثال: 12502)");
                            final EditText input = new EditText(ac);
                            input.setInputType(InputType.TYPE_CLASS_TEXT );
                            builder.setView(input);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new Thread(new Runnable() {
                                        {
                                            Toast toast = Toast.makeText(getApplicationContext(), "دانلود شروع شد", Toast.LENGTH_LONG);
                                            toast.show();
                                        }

                                        public void run() {
                                            try {
                                                final boolean res = Map.Download(input.getText().toString(), false, PlanDir());
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (res) {
                                                            Toast toast = Toast.makeText(getApplicationContext(), "نقشه دانلود شد و به لیست اضافه شد", Toast.LENGTH_SHORT);
                                                            toast.show();
                                                        } else {
                                                            Toast toast = Toast.makeText(getApplicationContext(), "خطا در دریافت نقشه", Toast.LENGTH_SHORT);
                                                            toast.show();
                                                        }
                                                        RefreshList();
                                                    }
                                                });

                                            } catch (Exception ex) {
                                            }
                                        }
                                    }).start();
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            builder.show();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            Intent intent = new Intent()
                                    .setType("*/*")
                                    .setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "فایل نقشه یا فشرده را انتخاب کنید"), 123);

                            break;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("انتخاب روش").setPositiveButton("از اینترنت", dialogClickListener)
                    .setNegativeButton("از کارت حافظه", dialogClickListener).show();
        } catch (Exception ex) {
        }
    }
    int dt = 1000;
    public void onClickBtn_speed_up(View v) {
        try {
            EditText et = (EditText) findViewById(R.id.edit_speed);
            double gpm = Double.parseDouble(et.getText().toString()) + 1;
            et.setText((int) gpm + "");
            dt = (int) (1000.0 / (gpm / 60.0) - 1000.0);
        } catch (Exception ex) {
        }
    }
    public void onClickBtn_speed_down(View v) {
        try {
            EditText et = (EditText) findViewById(R.id.edit_speed);
            double gpm = Double.parseDouble(et.getText().toString()) - 1;
            et.setText((int) gpm + "");
            dt = (int) (1000.0 / (gpm / 60.0) - 1000.0);
        } catch (Exception ex) {
        }
    }
    public void onClickBtn_sound_on(View v) {
        sound = false;
        findViewById(R.id.btn_sound_off).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_sound_on).setVisibility(View.GONE);
    }
    public void onClickBtn_sound_off(View v) {
        sound = true;
        findViewById(R.id.btn_sound_off).setVisibility(View.GONE);
        findViewById(R.id.btn_sound_on).setVisibility(View.VISIBLE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK) {
            Uri selectedFile = data.getData(); //The uri with the location of the file
            String file_path = Tools.getPath(this, selectedFile);
            try {
                String dir = PlanDir();
                String file = Tools.getFileName(file_path);
                Tools.fileCopy(file_path, dir + file);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            RefreshList();
        }
    }

    void UpdateView() {
        try {
            Map m = currentMap();
            if(m==null)return;
            m.ValidateIndex();
            //RadioButton rb_text = (RadioButton) findViewById(R.id.radioBtn_text);
            {
                ((EditText) findViewById(R.id.edit_row)).setText(Tools.En2Fa(m.current_row().number + ""));
                ((EditText) findViewById(R.id.edit_color)).setText(Tools.En2Fa(m.current_color().code + ""));
                ((TextView) findViewById(R.id.text_color_num)).setText("شماره گره های رنگ شماره  " + Tools.En2Fa(m.current_color().code + ""));
            }
            {
                findViewById(R.id.btn_next).setEnabled(!m.isLast());
                findViewById(R.id.btn_prev).setEnabled(!m.isFirst());
                findViewById(R.id.btn_row_next).setEnabled(!m.isLastRow());
                findViewById(R.id.btn_row_prev).setEnabled(!m.isFirstRow());
                findViewById(R.id.btn_row_next_10).setEnabled(!m.isLastRow());
                findViewById(R.id.btn_row_prev_10).setEnabled(!m.isFirstRow());
                findViewById(R.id.btn_color_next).setEnabled(!m.isLastColor());
                findViewById(R.id.btn_color_prev).setEnabled(!m.isFirstColor());
                findViewById(R.id.btn_color_next_10).setEnabled(!m.isLastColor());
                findViewById(R.id.btn_color_prev_10).setEnabled(!m.isFirstColor());
            }

            ((TextView) findViewById(R.id.text_knots)).setText(Html.fromHtml(Tools.En2Fa(m.current_color().ToString(m.current_knot_index))));

            ((TextView) findViewById(R.id.text_row)).setText(Tools.En2Fa(m.current_row().number+""));
            ((TextView) findViewById(R.id.textV_color)).setText(Tools.En2Fa(m.current_color().code +""));
            ((TextView) findViewById(R.id.text_knot_)).setText(Tools.En2Fa(m.current_knot() +""));
            Say(false);
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    Thread th;
    boolean sound=true;
    void Say(boolean say_if_not_playing) {
        final Context context = this;
        if ((say_if_not_playing || playing) && sound)
            try {
                Tools.Silent();
                Tools.Silent();
                Tools.Silent();
                try {
                    if (th != null)
                        th.interrupt();
                } catch (Exception ex) {
                }
                th = new Thread(new Runnable() {
                    public void run() {
                        try {
                            Map m = currentMap();
                            if (m.current_knot_index == 0) {
                                Tools.SpeekRang(context);
                                Tools.Speek(m.current_color().code + "", context);
                            }
                            //if (m.current_knot_index == 0)
                            //    Tools.SpeekGereh(context);
                            Tools.Speek(m.current_knot() + "", context);
                        } catch (Exception ex) {
                        }
                    }
                });
                th.start();
            } catch (Exception ex) {
            }
    }
    public String RootDir() {
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MRB";
        //String dir = this.getFilesDir().getAbsolutePath()+"/Plan";
        File wallpaperDirectory = new File(dir);
        wallpaperDirectory.mkdirs();
        return dir + "/";
    }
    public String PlanDir() {
        String dir = RootDir() + "Plan";
        //String dir = this.getFilesDir().getAbsolutePath()+"/Plan";
        File wallpaperDirectory = new File(dir);
        wallpaperDirectory.mkdirs();
        return dir + "/";
    }

    List<Map> Maps = new ArrayList<Map>();
    Map currentMap() {
        if (Maps == null || Maps.size() == 0) return null;
        Spinner sp_ = (Spinner) findViewById(R.id.spinner_map);
        int i = sp_.getSelectedItemPosition();
        if (i < 0) return null;
        return Maps.get(i);
    }
    public void RefreshList() {
        Maps = new ArrayList<Map>();
        File dir = new File(PlanDir());
        File[] F = dir.listFiles();
        if (F != null)
            for (File f : F)
                try {
                    if (f.getPath().toLowerCase().endsWith(".zip")) {
                        Tools.unpackZip(dir.getPath()+"/", f.getPath());
                        f.delete();
                    }
                } catch (Exception ex) {
                }
        F = dir.listFiles();
        Spinner sp = (Spinner) findViewById(R.id.spinner_map);
        // you need to have a list of data that you want the spinner to display
        List<String> spinnerArray = new ArrayList<String>();
        if (F != null)
            for (File f : F) {
                if (!f.getPath().toLowerCase().endsWith(".set"))
                    if (!f.getPath().endsWith(".jpg"))
                        try {
                            Map m = new Map();
                            m.Read(f);
                            spinnerArray.add(m.Name);
                            Maps.add(m);
                        } catch (Exception ex1) {
                            try {
                                f.delete();
                            } catch (Exception ex2) {
                            }
                        }
            }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    void ReadSetting() {
        try {
            byte[] B = new byte[3];
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(RootDir() + "main.set"));
            buf.read(B, 0, B.length);
            buf.close();
            ((Spinner) findViewById(R.id.spinner_map)).setSelection(B[0]);
            ((EditText) findViewById(R.id.edit_speed)).setText(B[1] + "");
            sound = B[2] == 1;
            if (sound) {
                findViewById(R.id.btn_sound_off).setVisibility(View.GONE);
                findViewById(R.id.btn_sound_on).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.btn_sound_off).setVisibility(View.VISIBLE);
                findViewById(R.id.btn_sound_on).setVisibility(View.GONE);
            }
        } catch (Exception ex) {
        }
    }
    void WriteSetting(){
        try {
            byte[] B = new byte[3];
            BufferedOutputStream buf = new BufferedOutputStream(new FileOutputStream(RootDir() + "main.set"));
            B[0] = (byte)(((Spinner)findViewById(R.id.spinner_map)).getSelectedItemPosition());
            B[1] = (byte)Integer.parseInt(((EditText) findViewById(R.id.edit_speed)).getText().toString());
            B[2] = (byte)(sound?1:0);
            buf.write(B, 0, B.length);
            buf.close();
        } catch (Exception ex) {
        }
    }

}
