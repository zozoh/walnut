package org.nutz.walnut.ext.titanium.sidebar;

import java.util.List;

public class TiSidebarInput {

    private List<TiSidebarInputItem> sidebar;

    public boolean hasSidebar() {
        return null != sidebar && sidebar.size() > 0;
    }

    public List<TiSidebarInputItem> getSidebar() {
        return sidebar;
    }

    public void setSidebar(List<TiSidebarInputItem> sidebar) {
        this.sidebar = sidebar;
    }

}
