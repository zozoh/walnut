package com.site0.walnut.ext.data.esi.hdl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.index.query.WrapperQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.nutz.json.Json;
import org.nutz.lang.ContinueLoop;
import org.nutz.lang.ExitLoop;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.LoopException;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.esi.EsiConf;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.WnPager;
import com.site0.walnut.util.each.WnEachIteratee;

@JvmHdlParamArgs(value = "cnqihbslVNHQ", regex = "^(pager|content|obj|match_all|debug|rawresp)$")
public class esi_query extends esi_xxx {

    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        EsiConf conf = conf(sys, hc);
        if (conf == null) {
            sys.err.print("e.cmd.esi.query.miss_esi_conf");
            return;
        }

        // 准备参数
        String match = hc.params.get("match");
        String _sort = hc.params.get("sort");
        WnPager pager = new WnPager(hc.params);
        String w = hc.params.get("w");

        // 单一对象查询,设置一下分页
        if (hc.params.is("obj")) {
            pager.limit = 1;
            pager.skip = 0;
        }

        // 构建查询对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if (!Strings.isBlank(w)) {
            // 底层DSL数据直接上
            searchSourceBuilder.query(new WrapperQueryBuilder(w));
        } else {
            // 类obj -match语法上
            searchSourceBuilder.query(toQuery(conf, match, hc.params.is("match_all")));
        }

        // 设置好分页和排序
        setupQuery(searchSourceBuilder, _sort, pager);

        // 开始请求, 同步模式
        SearchRequest searchRequest = new SearchRequest(sys.getMyName() + "_" + conf.getName());
        searchRequest.source(searchSourceBuilder);
        SearchResponse resp = esi(hc.ioc).getClient().search(searchRequest).actionGet();

        // 看看要输出什么
        if (hc.params.is("rawresp")) {
            // 输出原始resp
            XContentBuilder builder = XContentFactory.jsonBuilder(sys.out.getOutputStream());
            resp.toXContent(builder, ToXContent.EMPTY_PARAMS);
            builder.flush();
            builder.close();
        } else {
            // 输出walnut对象列表
            pager.setSumCount(resp.getHits().totalHits);
            List<WnObj> list = new ArrayList<>();
            for (SearchHit hit : resp.getHits().getHits()) {
                WnObj wobj = sys.io.get(hit.getId());
                if (wobj != null) {
                    wobj.put("esi_hit_score", hit.getScore()); // 把分数填上
                    list.add(wobj);
                }
            }
            // 使用cmd_esi的beforeOutput进行输出, 兼容thing query
            hc.pager = pager;
            hc.output = list;
        }
    }

    public static QueryBuilder toQuery(EsiConf conf, String match, boolean match_all) {
        if (!Strings.isBlank(match)) {
            if (match.startsWith("[")) {
                List<NutMap> list = Json.fromJsonAsList(NutMap.class, match);
                BoolQueryBuilder bool = new BoolQueryBuilder();
                for (NutMap m2 : list) {
                    QueryBuilder b2 = toQueryBuilder(conf, m2);
                    if (match_all) {
                        bool.must(b2);
                    } else {
                        bool.should(b2);
                    }
                }
                return bool;
            } else {
                NutMap map = Wlang.map(match);
                return toQueryBuilder(conf, map);
            }
        }
        return QueryBuilders.matchAllQuery();
    }

    public static QueryBuilder toQueryBuilder(EsiConf conf, Map<String, Object> match) {
        BoolQueryBuilder bool = new BoolQueryBuilder();
        for (Map.Entry<String, Object> en : match.entrySet()) {
            bool.must(toQueryBuilder(conf, en.getKey(), en.getValue()));
        }
        return bool;
    }

    @SuppressWarnings("unchecked")
    public static QueryBuilder toQueryBuilder(EsiConf conf, String key, Object value) {
        if (value == null)
            return new MatchQueryBuilder(key, null); // NULL?
        // 是不是日期呢?
        if (conf.getMapping().containsKey(key)) {
            if ("date".equals(conf.getMapping().get(key).type)) {
                if (Wlang.eleSize(value) > 1) {
                    List<Object> list = (List<Object>) value;
                    return new RangeQueryBuilder(key).from(list.get(0)).to(list.get(1));
                } else {
                    return new MatchQueryBuilder(key, value);
                }
            }
        }
        if (value instanceof CharSequence) {
            String _value = String.valueOf(value);
            if (_value.contains("*")) {
                return new WildcardQueryBuilder(key, _value);
            }
            return new MatchQueryBuilder(key, _value);
        } else if (value instanceof Number) {
            return new MatchQueryBuilder(key, value);
        } else if (value instanceof Boolean) {
            return new MatchQueryBuilder(key, value);
        } else if (value.getClass().isArray() || value instanceof List) {
            int len = Wlang.eleSize(value);
            if (len == 0) {
                return new MatchQueryBuilder(key, null); // NULL?
            }
            if (len == 1) {
                return toQueryBuilder(conf, key, Wlang.firstInAny(value));
            }
            BoolQueryBuilder bool = new BoolQueryBuilder();
            Wlang.each(value, new WnEachIteratee<Object>() {
                public void invoke(int index, Object ele, Object src)
                        throws ExitLoop, ContinueLoop, LoopException {
                    bool.should(toQueryBuilder(conf, key, ele));
                }
            });
            return bool;
        }
        throw Wlang.noImplement();
    }

    public static void setupQuery(SearchSourceBuilder builder, String sort, WnPager pager) {
        // 排序
        if (!Strings.isBlank(sort)) {
            NutMap _s = Wlang.map(sort);
            for (Map.Entry<String, Object> en : _s.entrySet()) {
                if ("1".equals(String.valueOf(en.getValue()))) {
                    builder.sort(en.getKey(), SortOrder.ASC);
                } else {
                    builder.sort(en.getKey(), SortOrder.DESC);
                }
            }
        }
        // 分页
        if (pager == null) {
            pager = new WnPager(10, 0);
        }
        if (pager.skip > 0) {
            builder.from(pager.skip);
        }
        if (pager.limit < 1 || pager.limit > 100) {
            pager.limit = 10;
        }
        builder.size(pager.limit);
    }
}
