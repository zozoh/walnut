package org.nutz.walnut.ext.old.bulk.impl;

import java.util.List;

import org.nutz.walnut.ext.old.bulk.api.BulkHistory;
import org.nutz.walnut.ext.old.bulk.api.BulkHistoryItem;
import org.nutz.walnut.ext.old.bulk.api.BulkIgnores;
import org.nutz.walnut.ext.old.bulk.api.BulkIo;
import org.nutz.walnut.ext.old.bulk.api.BulkPatch;
import org.nutz.walnut.ext.old.bulk.api.BulkTree;

public class WnBulkIoImpl implements BulkIo {

    @Override
    public BulkIgnores getIgnores() {
        return null;
    }

    @Override
    public void writeIgnores(BulkIgnores ignores) {}

    @Override
    public void writeTree(BulkTree tree) {}

    @Override
    public BulkTree readTree(String treeId) {
        return null;
    }

    @Override
    public void writePatch(BulkPatch patch) {}

    @Override
    public BulkPatch readPatch(String patchId) {
        return null;
    }

    @Override
    public void appendHistory(BulkHistoryItem hisItem) {}

    @Override
    public BulkHistory readHistory() {
        return null;
    }

    @Override
    public List<BulkHistoryItem> findHistoryItems(long beginAms, long endAms) {
        return null;
    }

}
