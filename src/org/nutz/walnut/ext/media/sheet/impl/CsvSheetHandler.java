package org.nutz.walnut.ext.media.sheet.impl;

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
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;

public class CsvSheetHandler extends AbstractSheetHandler {

    @Override
    public SheetResult read(InputStream ins, NutMap conf) {
        String sep = conf.getString("sep", ",");
        char[] sep_cs = sep.toCharArray();

        // 准备返回
        List<NutMap> list = new LinkedList<>();

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
    public void write(OutputStream ops, List<NutMap> list, NutMap conf) {
        String sep = conf.getString("sep", ",");
        boolean noheader = conf.getBoolean("noheader", false);
        String emptyCell = conf.getString("emptyCell", "--");

        // 至少得有一个对象吧
        if (null == list || list.isEmpty())
            return;

        // 第一个对象的 key 作为标题栏吧
        NutMap first = list.get(0);

        Writer writer = new OutputStreamWriter(ops, Encoding.CHARSET_UTF8);

        try {
            String line;
            // 输出标题栏
            if (!noheader) {
                line = Strings.join(sep, first.keySet()) + "\n";
                writer.write(line);
            }

            // 依次输出
            int i = 1;
            int len = list.size();
            for (NutMap map : list) {
                // 日志
                this._on_process(i++, len, map);

                // 准备输出
                ArrayList<Object> cells = new ArrayList<>(map.size());
                for (Object val : map.values()) {
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