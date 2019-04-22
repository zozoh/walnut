package org.nutz.walnut.ext.esi;

// https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-types.html
public class EsiMappingField {

    public String type;
    public String[] fields;
    public String format;
    public String locale;
    public double boost = 1.0;
    public String analyzer;
    public String search_analyzer;
}
