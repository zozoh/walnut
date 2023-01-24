package org.nutz.walnut.ext.net.xapi.bean;

public enum XApiBodyType {

    /**
     * <ul>
     * <li><code>method</code> 必须为 <code>POST</code>
     * <li><code>params</code> 作为 QueryString
     * <li><code>body</code> 编码为一个 JSON 字符串,
     * </ul>
     */
    json,

    /**
     * <ul>
     * <li><code>method</code> 必须为 <code>POST</code>
     * <li><code>params</code> 编码为一个 Query Form
     * <li><code>body</code> 无视
     * </ul>
     */
    form,

    /**
     * <ul>
     * <li><code>method</code> 必须为 <code>POST</code>
     * <li><code>params</code> 作为 QueryString
     * <li><code>body</code> 解析为 <code>HttpFormPart</code> 的列表
     * </ul>
     * 
     * @see org.nutz.walnut.ext.net.http.bean.HttpFormPart
     */
    multipart,

    /**
     * <ul>
     * <li><code>method</code> 必须为 <code>POST</code>
     * <li><code>params</code> 作为 QueryString
     * <li><code>body</code> 如果为 CheapDocument，则渲染为字符串
     * </ul>
     */
    xml,

    /**
     * <ul>
     * <li><code>method</code> 必须为 <code>POST</code>
     * <li><code>params</code> 作为 QueryString
     * <li><code>body</code> 作为纯字符串
     * </ul>
     */
    text,

    /**
     * <ul>
     * <li><code>method</code> 必须为 <code>POST</code>
     * <li><code>params</code> 作为 QueryString
     * <li><code>body</code> 作为字节输入流
     * </ul>
     */
    bin

}
