package com.site0.walnut.ext.util.react.bean;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

public class ReactAction {

    public ReactType type;

    public String path;

    public NutMap query;

    public NutMap params;

    public String targetId;

    public NutMap meta;

    public NutMap sort;

    public String input;

    public int skip;

    public int limit;

    public String toString() {
        return String.format("%s:%s:%s:%s", type, path, targetId, input);
    }

    public void explain(NutBean vars) {
        this.path = (String) Wn.explainObj(vars, this.path);
        this.query = (NutMap) Wn.explainObj(vars, this.query);
        this.params = (NutMap) Wn.explainObj(vars, this.params);
        this.targetId = (String) Wn.explainObj(vars, this.targetId);
        this.meta = (NutMap) Wn.explainObj(vars, this.meta);
        Wn.explainMetaMacro(this.meta);
        this.sort = (NutMap) Wn.explainObj(vars, this.sort);
        this.input = (String) Wn.explainObj(vars, this.input);
    }

    public boolean hasType() {
        return null != this.type;
    }

    public boolean hasPath() {
        return !Ws.isBlank(path);
    }

    public boolean hasQuery() {
        return null != query && !query.isEmpty();
    }

    public NutMap getQuery() {
        return query;
    }

    public void setQuery(NutMap query) {
        this.query = query;
    }

    public boolean hasParams() {
        return null != params && !params.isEmpty();
    }

    public boolean hasTargetId() {
        return !Ws.isBlank(targetId);
    }

    public boolean hasMeta() {
        return null != meta && !meta.isEmpty();
    }

    public boolean hasSort() {
        return null != sort && !sort.isEmpty();
    }

    public boolean hasInput() {
        return !Ws.isBlank(input);
    }

}
