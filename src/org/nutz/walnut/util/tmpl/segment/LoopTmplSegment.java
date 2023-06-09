package org.nutz.walnut.util.tmpl.segment;

import org.nutz.lang.util.NutBean;

public class LoopTmplSegment extends TmplBlockSegment {

    
    private String varName;
    
    private String indexName;
    
    private String looperName;
    
    

    @Override
    public void renderTo(NutBean context, boolean showKey, StringBuilder sb) {
        
        
        super.renderTo(context, showKey, sb);
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getLooperName() {
        return looperName;
    }

    public void setLooperName(String looperName) {
        this.looperName = looperName;
    }
    
    
}
