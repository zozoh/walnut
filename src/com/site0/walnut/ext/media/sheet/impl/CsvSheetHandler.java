package com.site0.walnut.ext.media.sheet.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.nutz.castor.Castors;
import org.nutz.lang.Encoding;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.util.Ws;

public class CsvSheetHandler extends AbstractSheetHandler {

    @Override
    public SheetResult read(InputStream ins, NutMap conf) {
        String sep = conf.getString("sep", ",");
        char[] sep_cs = sep.toCharArray();

        // 准备返回
        List<NutBean> list = new LinkedList<>();

        // 首先按行读取
        BufferedReader br = Streams.buffr(new InputStreamReader(ins, Encoding.CHARSET_UTF8));
        String line;
        try {
            // 读取首行，作为键
            line = br.readLine();
            if (null == line) {
                return new SheetResult(list);
            }
            String[] keys = Strings.split(line, false, true, sep_cs);

            // 读取后续的行
            while (null != (line = br.readLine())) {
                String[] ss = Strings.split(line, false, true, sep_cs);
                int len = Math.min(keys.length, ss.length);
                NutMap map = new NutMap();
                for (int i = 0; i < len; i++) {
                    String key = Strings.trim(keys[i]);
                    String val = Strings.trim(ss[i]);
                    map.put(key, val);
                }
                list.add(map);
            }
        }
        catch (IOException e) {
            throw Er.wrap(e);
        }
        return new SheetResult(list);
    }

    @Override
    public void write(OutputStream ops, List<NutBean> list, List<String> headKeys, NutMap conf) {
        String sep = conf.getString("sep", ",");
        boolean noheader = conf.getBoolean("noheader", false);
        String emptyCell = conf.getString("emptyCell", "--");

        // 至少得有一个对象吧
        if (null == list || list.isEmpty())
            return;

        // 第一个对象的 key 作为标题栏吧
        String[] keys = null; // 准备用第一个对象的 Key 来归纳列标题
        if (!noheader) {
            // 指定了标题栏
            if (null != headKeys && !headKeys.isEmpty()) {
                keys = headKeys.toArray(new String[headKeys.size()]);
            }
            // 用第一个对象的键作为标题栏
            else {
                NutBean first = list.get(0);
                keys = first.keySet().toArray(new String[first.size()]);
            }
        }

        Writer writer = new OutputStreamWriter(ops, Encoding.CHARSET_UTF8);

        try {
            String line;
            // 输出标题栏
            if (!noheader) {
                line = Ws.join(keys, sep) + "\n";
                writer.write(line);
            }

            // 依次输出
            int i = 1;
            int len = list.size();
            for (NutBean map : list) {
                // 日志
                this._on_process(i++, len, map);

                // 准备输出
                ArrayList<Object> cells = new ArrayList<>(map.size());
                for (int x = 0; x < keys.length; x++) {
                    String key = keys[x];
                    Object val = map.get(key);
                    // 空值
                    if (null == val) {
                        val = emptyCell;
                    }
                    // 值包含空格
                    else {
                        String str = Castors.me().castToString(val);
                        // 替换双引号
                        str = str.replace('"', '“');
                        if (str.matches("^.*[ \\t\\n\\r]+.*$")) {
                            val = "\"" + str + "\"";
                        } else {
                            val = str;
                        }
                    }
                    cells.add(val);
                }
                line = Strings.join(sep, cells) + "\n";
                // 写入
                writer.write(line);
            }
            // 结束日志
            this._on_end(len);
        }
        catch (IOException e) {
            throw Er.wrap(e);
        }
        finally {
            Streams.safeFlush(writer);
        }
    }

}
