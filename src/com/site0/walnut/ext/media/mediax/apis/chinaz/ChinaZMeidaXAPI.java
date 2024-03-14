package com.site0.walnut.ext.media.mediax.apis.chinaz;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nutz.http.Header;
import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import com.site0.walnut.ext.media.mediax.MxAPIKey;
import com.site0.walnut.ext.media.mediax.apis.NoTicketMediaXAPI;
import com.site0.walnut.ext.media.mediax.bean.MxAccount;
import com.site0.walnut.ext.media.mediax.bean.MxCrawl;
import com.site0.walnut.ext.media.mediax.bean.MxPost;
import com.site0.walnut.ext.media.mediax.bean.MxReCrawl;
import com.site0.walnut.ext.media.mediax.bean.MxRePost;
import com.site0.walnut.ext.media.mediax.util.Mxs;

@MxAPIKey("icp.chinaz.com")
public class ChinaZMeidaXAPI extends NoTicketMediaXAPI {

    public ChinaZMeidaXAPI(MxAccount account) {
        super(account);
    }

    @Override
    public MxRePost post(MxPost obj) {
        return null;
    }

    @Override
    public List<MxReCrawl> crawl(MxCrawl cr) {
        // 准备请求头
        Header header = Mxs.genHeader(null);

        // 准备输出结果
        List<MxReCrawl> list = new LinkedList<>();

        // 爬取 HTML
        __do_crawl(cr.uri.toString(), cr, list, header);

        // 返回结果
        return list;
    }

    private void __do_crawl(String url, MxCrawl cr, List<MxReCrawl> list, Header header) {
        Response resp = Http.get(url, header, 10000, 10000);
        String html = resp.getContent();

        // 更新一下头，以便标识 Refer
        header.set("Referer", url);

        // 解析 HTML
        Document doc = Jsoup.parse(html);

        // 填充一下
        Date myLastDate = __fill_list_by_doc(cr, doc, list);

        // 看看有没有必要翻页，继续查
        if (cr.limit > 0 && list.size() < cr.limit) {
            // 嗯，没限制日期，或者估计下一篇可能还有新的，就查一下
            if (null == cr.lastDate
                || null == myLastDate
                || myLastDate.getTime() >= cr.lastDate.getTime()) {
                // 首先从网页里找到下一页
                Elements eleNext = doc.select("#pagelist a[title=\"下一页\"]");
                if (eleNext.size() > 0) {
                    String nextUrl = Mxs.normalizePath(cr.uri, eleNext.attr("href"));
                    // 确保没有重爬
                    if (!nextUrl.equals(url)) {
                        __do_crawl(nextUrl, cr, list, header);
                    }
                }
            }
        }
    }

    private Date __fill_list_by_doc(MxCrawl cr, Document doc, List<MxReCrawl> list) {
        // 寻找主要表格
        Element eleTable = doc.getElementsByClass("Tool-batchTable").first();
        if (eleTable == null)
            return new Date();
        // 得到表头
        Element eleTHead = eleTable.getElementsByTag("thead").first();
        Elements eleThs = eleTHead.getElementsByTag("th");
        String[] keys = new String[eleThs.size()];
        int i = 0;
        for (Element eleTh : eleThs) {
            keys[i++] = Strings.trim(eleTh.text());
        }

        // 得到表体，循环
        Element eleTBody = eleTable.getElementsByTag("tbody").first();
        Elements eleRows = eleTBody.children();
        Date myLastDate = null;
        for (Element eleRow : eleRows) {
            Elements eleTds = eleRow.children();
            i = 0;
            MxReCrawl rec = new MxReCrawl();
            rec.index(list.size());
            for (Element eleTd : eleTds) {
                if (i > keys.length)
                    break;
                String key = keys[i++];
                Object val = null;
                // 特殊字段
                if ("网站首页网址".equals(key)) {
                    Elements spans = eleTd.select("span");
                    // 就是一个值
                    if (spans.size() == 1) {
                        val = spans.get(0).text();
                    }
                    // 多个的话是一个数组
                    else if (spans.size() > 1) {
                        String[] ary = new String[spans.size()];
                        for (int x = 0; x < ary.length; x++) {
                            ary[x] = spans.get(x).text();
                        }
                        val = ary;
                    }
                }
                // 其他的就取一下值咯
                else {
                    val = Strings.trim(eleTd.text());
                }
                rec.put(key, val);
            }
            // 看看日期过滤条件
            if (null != cr.lastDate) {
                String my_ds = rec.getString("审核时间");
                // 木有条件，无视
                if (null == my_ds)
                    continue;
                // 比较一下，如果发现已经到了指定日期之前的日期，直接终止
                myLastDate = Times.D(my_ds);
                if (myLastDate.getTime() < cr.lastDate.getTime()) {
                    break;
                }
            }

            // 加入列表
            list.add(rec);

            // 看看够没够
            if (cr.limit > 0 && list.size() >= cr.limit)
                break;
        }

        return myLastDate;
    }

}
