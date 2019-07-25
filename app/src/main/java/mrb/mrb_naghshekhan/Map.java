package mrb.mrb_naghshekhan;

import android.graphics.Color;
import android.text.format.Time;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * Created by MRB on 20/07/2019.
 */
public class Map {
    public String Name = "";
    public File file;
    private byte[] B = null;

    public void Read(String file_path) {
        File file = new File(file_path);
        Read(file);
    }

    public static boolean Download(String code, boolean turned, String dir) {
        try {
            code = Tools.Fa2En(code);
            boolean res = Tools.Download("http://daremco.com/Download.aspx",
                    "type=audio&skein=true&joola=true&serial=&code=&turned=" + turned + "&productId=" + code,
                    dir + code + ".zip", "POST");
            if (res)
                try {
                    Tools.GetBitmapFromURL("http://daremco.com/content/files/products/" + code + "/300.jpg",
                            dir + code + ".dkb.jpg");
                } catch (Exception ex) {
                }
            return res;
        } catch (Exception ex) {
            return false;
        }
    }

    public void Read(File file) {
        this.file = file;
        int N = (int) file.length();
        //N=300;
        B = new byte[N];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(B, 0, B.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int i0 = 0;
        for (int i = 0; i < N; i++)
            if ((B[i] == -1 && B[i + 1] == -1 && B[i + 2] == -1 && B[i + 3] == -1) || (i > 150))//-1
            {
                i0 = i;
                break;
            }
        Name = new String(Arrays.copyOfRange(B, 0, i0), Charset.forName("UTF-8")).replace("\0", "\r\n");
        ReadSetting();
    }

    void ReadSetting() {
        try {
            byte[] B = new byte[6];
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file.getPath() + ".set"));
            buf.read(B, 0, B.length);
            buf.close();
            current_row_index = B[0] * 256 + B[1];
            current_color_index = B[2] * 256 + B[3];
            current_knot_index = B[4] * 256 + B[5];
        } catch (Exception ex) {
        }
    }

    void WriteSetting() {
        try {
            byte[] B = new byte[6];
            BufferedOutputStream buf = new BufferedOutputStream(new FileOutputStream(file.getPath() + ".set"));
            B[0] = (byte) (current_row_index / 256);
            B[1] = (byte) (current_row_index % 256);
            B[2] = (byte) (current_color_index / 256);
            B[3] = (byte) (current_color_index % 256);
            B[4] = (byte) (current_knot_index / 256);
            B[5] = (byte) (current_knot_index % 256);
            buf.write(B, 0, B.length);
            buf.close();
        } catch (Exception ex) {
        }
    }

    private void ReadFullIf() {
        if (B == null) return;
        int N = B.length;
        int i0 = 0;
        for (int i = 0; i < N; i++)
            if (B[i] == -1 && B[i + 1] == -1 && B[i + 2] == -1 && B[i + 3] == -1)//-1
            {
                i0 = i;
                break;
            }
        Name = new String(Arrays.copyOfRange(B, 0, i0), Charset.forName("UTF-8")).replace("\0", "\r\n");

        rows = new ArrayList<Row>();
        for (int i = i0; i < N; i += 4) {
            //int j = GetInt32(B, i);
            //if (j == -1) {
            if (B[i] == -1 && B[i + 1] == -1 && B[i + 2] == -1 && B[i + 3] == -1) {
                Row row = new Row();
                row.pending_B = B;
                row.number = GetInt32(B, i + 4);
                row.pending_index = i + 8;
                rows.add(row);
            }
        }
        B = null;
    }

    public int current_row_index = 0;
    public int current_color_index = 0;
    public int current_knot_index = 0;

    public Row current_row() {
        rows.get(current_row_index).ReadIf();
        return rows.get(current_row_index);
    }

    public Color current_color() {
        return current_row().colors.get(current_color_index);
    }

    public int current_knot() {
        return current_color().knots.get(current_knot_index);
    }

    public void GoFirst() {
        current_row_index = 0;
        current_color_index = 0;
        current_knot_index = 0;
    }

    public void GoNext() {
        ReadFullIf();
        ValidateIndex();
        Row row = current_row();
        Color color = current_color();
        if (current_knot_index < color.knots.size() - 1)
            current_knot_index++;
        else {
            current_knot_index = 0;
            if (current_color_index < row.colors.size() - 1)
                current_color_index++;
            else {
                current_color_index = 0;
                if (current_row_index < rows.size() - 1)
                    current_row_index++;
            }
        }
        current_row();
    }

    public void GoPrev() {
        ReadFullIf();
        ValidateIndex();
        if (current_knot_index > 0)
            current_knot_index--;
        else {
            if (current_color_index > 0) {
                current_color_index--;
                Color color = current_color();
                current_knot_index = color.knots.size() - 1;
            } else {
                if (current_row_index > 0) {
                    current_row_index--;
                    Row row = current_row();
                    current_color_index = row.colors.size() - 1;
                    Color color = current_color();
                    current_knot_index = color.knots.size() - 1;
                }
            }
        }
    }

    public void ValidateIndex() {
        ReadFullIf();
        if (current_row_index < 0)
            current_row_index = 0;
        if (current_row_index >= rows.size())
            current_row_index = rows.size() - 1;

        if (current_color_index < 0)
            current_color_index = 0;
        if (current_color_index >= current_row().colors.size())
            current_color_index = current_row().colors.size() - 1;

        if (current_knot_index < 0)
            current_knot_index = 0;
        if (current_knot_index >= current_color().knots.size())
            current_knot_index = current_color().knots.size() - 1;

        long time = System.currentTimeMillis();
        if ((time - last_save_time) > 1000 * 1) {
            WriteSetting();
            last_save_time = time;
        }
    }

    long last_save_time = 0;

    public boolean isFirst() {
        return isFirstRow() && isFirstColor() && isFirstKnot();
    }

    public boolean isLast() {
        return isLastRow() && isLastColor() && isLastKnot();
    }

    public boolean isFirstRow() {
        return current_row_index == 0;
    }

    public boolean isFirstColor() {
        return current_color_index == 0;
    }

    public boolean isFirstKnot() {
        return current_knot_index == 0;
    }

    public boolean isLastRow() {
        return current_row_index == rows.size() - 1;
    }

    public boolean isLastColor() {
        return current_color_index == current_row().colors.size() - 1;
    }

    public boolean isLastKnot() {
        return current_knot_index == current_color().knots.size() - 1;
    }

    public static int GetInt32(byte[] B, int start) {
        byte[] b = Arrays.copyOfRange(B, start, start + 4);
        ByteBuffer wrapped = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN); // big-endian by default
        int num = wrapped.getInt();
        return num;
    }

    public String ToString() {
        ReadFullIf();
        StringBuilder str = new StringBuilder();
        str.append(Name + "\r\n");
        boolean new_rag = false;
        boolean new_color = false;
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            row.ReadIf();
            str.append("\r\n ---------- " + row.number + " ---------- \r\n");
            for (int j = 0; j < row.colors.size(); j++) {
                Color color = row.colors.get(j);
                str.append(color.code + ": ");
                int last_k = -100;
                boolean dotdot = false;
                for (int k = 0; k < color.knots.size(); k++) {
                    if (color.knots.get(k) == last_k + 1) {
                        dotdot = true;
                    } else if (dotdot) {
                        dotdot = false;
                        str.append(".." + last_k + ", " + color.knots.get(k) + ", ");
                    } else {
                        str.append(color.knots.get(k) + ", ");
                    }
                    last_k = color.knots.get(k);
                }
                str.append("\r\n");
            }
        }
        String res = str.toString().replace(", ..", "..");
        return res;
    }

    public List<Row> rows = new ArrayList<Row>();

    public class Row {
        public int number;
        public int pending_index;
        public byte[] pending_B;

        public void ReadIf() {
            if (pending_B == null) return;
            byte[] B = pending_B;
            for (int i = pending_index; i < B.length; i += 4) {
                int j = GetInt32(B, i);
                if (j == -1)
                    break;
                else if (j == -2) {
                    Color c = new Color();
                    colors.add(c);
                    i = i + 4;
                    j = GetInt32(B, i);
                    c.code = j;
                } else if (j == -3) {
                    Color col = colors.get(colors.size() - 1);
                    col.knots.add(-3);
                } else {
                    Color col = colors.get(colors.size() - 1);
                    if (col.knots.size() > 1 && col.knots.get(col.knots.size() - 1) < 0) {
                        col.knots.remove(col.knots.size() - 1);
                        int last = col.knots.get(col.knots.size() - 1);
                        for (int jj = last + 1; jj <= j; jj++)
                            col.knots.add(jj);
                    } else
                        col.knots.add(j);
                }
            }
            pending_B = null;
        }

        public List<Color> colors = new ArrayList<Color>();
    }

    public class Color {
        public int code;
        public List<Integer> knots = new ArrayList<Integer>();

        int getColor() {
            return android.graphics.Color.rgb(256 - code, 150, code);
        }

        public String ToString(int knot_index) {
            StringBuilder str = new StringBuilder();
            Color color = this;
            int last_k = -100;
            int sel_knot=knots.get(knot_index);
            boolean dotdot = false;
            int dotdot_start = -1;
            for (int k = 0; k < color.knots.size(); k++) {
                if (color.knots.get(k) == last_k + 1) {
                    if(!dotdot) {
                        dotdot = true;
                        dotdot_start = color.knots.get(k)-1;
                    }
                } else {
                    if (dotdot) {
                        if ((dotdot_start < sel_knot) && (sel_knot < last_k))
                            str.append("<font color=#cc0029>");
                        str.append(".." );
                        if ((dotdot_start < sel_knot) && (sel_knot < last_k))
                            str.append("</font>");
                        if (sel_knot == last_k)
                            str.append("<font color=#cc0029>");
                        str.append(last_k+"");
                        if (sel_knot == last_k)
                            str.append("</font>");
                        str.append(", ");
                        dotdot = false;
                        dotdot_start = 1000000;
                    }
                    if (knot_index == k)
                        str.append("<font color=#cc0029>");
                    str.append(color.knots.get(k)+"");
                    if (knot_index == k)
                        str.append("</font>");
                    str.append(", ");
                }
                last_k = color.knots.get(k);
            }
            if (dotdot)
                str.append(".." + last_k);
            str.append("\r\n");
            String res = str.toString().replace(", ..", "..").replace(", <font color=#cc0029>..", "<font color=#cc0029>..");
            return res;
        }
    }
}
