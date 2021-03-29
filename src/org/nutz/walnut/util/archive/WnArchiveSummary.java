package org.nutz.walnut.util.archive;

import org.nutz.walnut.util.Ws;

public class WnArchiveSummary {

    public int items;

    public long size;

    public int dir;

    public int file;

    public void joinString(StringBuilder sb) {
        sb.append("item_count: ").append(this.items).append("\n");
        sb.append("dir_count: ").append(this.dir).append("\n");
        sb.append("file_count: ").append(this.file).append("\n");
        sb.append("size_count: ");
        sb.append(Ws.sizeText(this.size));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.joinString(sb);
        return sb.toString();
    }
}
