package com.site0.walnut.ext.sys.dsync;

import java.util.List;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.sys.dsync.bean.WnDataSyncConfig;
import com.site0.walnut.ext.sys.dsync.bean.WnDataSyncTree;
import com.site0.walnut.impl.box.JvmFilterContext;

public class DSyncContext extends JvmFilterContext {

    public WnDataSyncService api;

    public WnDataSyncConfig config;

    public List<WnDataSyncTree> trees;

    public WnObj oArchive;

    public boolean hasTrees() {
        return null != trees && !trees.isEmpty();
    }

    public boolean hasAchive() {
        return null != oArchive;
    }

}
