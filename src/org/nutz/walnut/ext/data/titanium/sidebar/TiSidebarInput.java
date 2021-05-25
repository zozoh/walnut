package org.nutz.walnut.ext.data.titanium.sidebar;

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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.hasSidebar()) {
            for (TiSidebarInputItem it : sidebar) {
                it.joinString(sb, 0);
                sb.append('\n');
            }
        } else {
            sb.append("~ EMPTY ~");
        }
        return sb.toString();
    }

}
