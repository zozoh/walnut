package org.nutz.walnut.jetty.log;

import java.util.Date;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Index;
import org.nutz.dao.entity.annotation.Name;
import org.nutz.dao.entity.annotation.Table;
import org.nutz.dao.entity.annotation.TableIndexes;

@Table("t_aclog")
@TableIndexes(@Index(fields="createTime", name="t_alog_ct_index", unique=false))
public class AccessLog {

    /**
     * 日志编号
     */
    @Name
    private String id;
    /**
     * 请求时间
     */
    @Column("ct")
    private Date createTime;
    /**
     * 耗时
     */
    @Column("du")
    private long duration;
    /**
     * 请求者的ip
     */
    @Column("ip")
    private String remoteIp;
    /**
     * 请求的方法
     */
    @Column("meth")
    private String method;
    /**
     * 请求的URI
     */
    @ColDefine(width=2048)
    @Column("uri")
    private String uri;
    /**
     * 请求的查询字符串
     */
    @Column("qs")
    @ColDefine(width=2048)
    private String queryString;
    /**
     * UserAgent
     */
    @Column("ua")
    private String userAgent;
    /**
     * 请求的HostName
     */
    @Column("host")
    private String host;
    /**
     * 请求的session id
     */
    @Column("seid")
    private String sessionId;
    /**
     * 请求的跟踪id
     */
    @Column("tid")
    private String traceId;
    
    /**
     * 跟踪的用户识别号
     */
    @Column("u_id")
    private String userId;
    /**
     * 请求的全部headers
     */
    //@Column("headers")
    private String headers;
    /**
     * 响应代码
     */
    @Column("rc")
    private int respCode;
    /**
     * 引用页
     */
    @Column("rf")
    private String referer;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public Date getCreateTime() {
        return createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    public long getDuration() {
        return duration;
    }
    public void setDuration(long duration) {
        this.duration = duration;
    }
    public String getRemoteIp() {
        return remoteIp;
    }
    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }
    public String getMethod() {
        return method;
    }
    public void setMethod(String method) {
        this.method = method;
    }
    public String getUri() {
        return uri;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }
    public String getQueryString() {
        return queryString;
    }
    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }
    public String getUserAgent() {
        return userAgent;
    }
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    public String getTraceId() {
        return traceId;
    }
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
    public String getHeaders() {
        return headers;
    }
    public void setHeaders(String headers) {
        this.headers = headers;
    }
    public int getRespCode() {
        return respCode;
    }
    public void setRespCode(int respCode) {
        this.respCode = respCode;
    }
    public String getReferer() {
        return referer;
    }
    public void setReferer(String referer) {
        this.referer = referer;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
