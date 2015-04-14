package org.nutz.walnut.impl.local;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;

public class MemNodeMap {

    private MemNodeItem head;

    private MemNodeItem tail;

    private Map<String, MemNodeItem> byId;

    private Map<String, MemNodeItem> byPath;

    public MemNodeMap() {
        head = new MemNodeItem();
        tail = head;
        byId = new HashMap<String, MemNodeItem>();
        byPath = new HashMap<String, MemNodeItem>();
    }

    public void clear() {
        byId.clear();
        byPath.clear();
        tail = head;
        head.next = null;
    }

    public void loadAndClose(Reader r) {
        try {
            BufferedReader br = Streams.buffr(r);
            String line;
            while (null != (line = br.readLine())) {
                if (Strings.isBlank(line))
                    continue;
                if (line.startsWith("#"))
                    continue;
                add(line);
            }
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            Streams.safeClose(r);
        }
    }

    public void add(String str) {
        tail = new MemNodeItem(tail, str);
        byId.put(tail.id, tail);
        byPath.put(tail.path, tail);
    }

    public void writeAndClose(Writer w) {
        try {
            BufferedWriter bw = Streams.buffw(w);
            MemNodeItem mni = head.next;
            while (null != mni) {
                bw.write(mni.id + ":" + mni.path + "\n");
                mni = mni.next;
            }
            bw.flush();
        }
        catch (Exception e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            Streams.safeClose(w);
        }

    }

    public boolean hasId(String id) {
        return byId.containsKey(id);
    }

    public boolean hasPath(String path) {
        return byPath.containsKey(path);
    }

    public String getPath(String id) {
        MemNodeItem mni = byId.get(id);
        return null == mni ? null : mni.path;
    }

    public String getId(String path) {
        MemNodeItem mni = byPath.get(path);
        return null == mni ? null : mni.id;
    }

    public MemNodeItem removeById(String id) {
        MemNodeItem mni = byId.get(id);
        if (null != mni) {
            mni.remove();
            byId.remove(mni.id);
            byPath.remove(mni.path);
        }
        return mni;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        writeAndClose(Lang.opw(sb));
        return sb.toString();
    }

    public void fromString(CharSequence cs) {
        loadAndClose(Lang.inr(cs));
    }

}
