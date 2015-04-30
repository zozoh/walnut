package org.nutz.walnut.impl.io.local;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
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

    private Map<String, MemNodeItem> byMount;

    public MemNodeMap() {
        head = new MemNodeItem();
        tail = head;
        byId = new HashMap<String, MemNodeItem>();
        byPath = new HashMap<String, MemNodeItem>();
        byMount = new HashMap<String, MemNodeItem>();
    }

    public void clear() {
        byId.clear();
        byPath.clear();
        byMount.clear();
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
        if (!Strings.isBlank(tail.mount)) {
            byMount.put(tail.mount, tail);
        }
    }

    public void writeAndClose(Writer w) {
        try {
            byId.clear();
            byPath.clear();
            byMount.clear();

            BufferedWriter bw = Streams.buffw(w);
            MemNodeItem mni = head.next;
            while (null != mni) {
                byId.put(mni.id, mni);
                byPath.put(mni.path, mni);

                if (!Strings.isBlank(mni.mount))
                    byMount.put(mni.mount, mni);

                StringBuilder sb = new StringBuilder();
                sb.append(mni).append('\n');
                bw.write(sb.toString());
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

    public boolean hasMount(String mnt) {
        return byMount.containsKey(mnt);
    }

    public MemNodeItem getById(String id) {
        return byId.get(id);
    }

    public MemNodeItem getByPath(String path) {
        return byPath.get(path);
    }

    public MemNodeItem getByMount(String mnt) {
        return byMount.get(mnt);
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

    public Collection<MemNodeItem> mounts() {
        return byMount.values();
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
