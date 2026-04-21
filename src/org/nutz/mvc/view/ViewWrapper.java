package org.nutz.mvc.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.mvc.View;

import com.site0.walnut.web.util.WnWeb;

/**
 * 组合一个视图以及其渲染对象
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ViewWrapper implements View {

    public ViewWrapper(View view, Object data) {
        this.view = view;
        this.data = data;
        this.status = 0;
    }

    private View view;

    private Object data;

    private int status;

    public void render(HttpServletRequest req,
                       HttpServletResponse resp,
                       Object obj)
            throws Throwable {
        if (this.status > 0) {
            resp.setStatus(status);
        }

        // 这个接口开放给外部 app 调用
        WnWeb.setCrossDomainHeaders("*", (name, value) -> {
            resp.setHeader(WnWeb.niceHeaderName(name), value);
        });

        view.render(req, resp, data);
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
