package org.nutz.walnut.ext.bulk.api;

import java.util.List;

public interface BulkIo {

    BulkIgnores getIgnores();

    void writeIgnores(BulkIgnores ignores);

    void writeTree(BulkTree tree);

    BulkTree readTree(String treeId);

    void writePatch(BulkPatch patch);

    BulkPatch readPatch(String patchId);

    void appendHistory(BulkHistoryItem hisItem);

    BulkHistory readHistory();

    List<BulkHistoryItem> findHistoryItems(long beginAms, long endAms);
}
