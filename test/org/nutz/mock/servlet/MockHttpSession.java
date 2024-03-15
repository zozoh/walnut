package org.nutz.mock.servlet;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import com.site0.walnut.util.Wlang;

@SuppressWarnings("deprecation")
public class MockHttpSession implements HttpSession {
    
    protected ServletContext servletContext;

    public MockHttpSession(MockServletContext servletContext) {
        this.servletContext = servletContext;
    }

    protected Map<String, Object> attributeMap = new HashMap<String, Object>();

    public void removeAttribute(String key) {
        attributeMap.remove(key);
    }

    public void setAttribute(String key, Object value) {
        attributeMap.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributeMap.get(key);
    }

    public long getCreationTime() {
        throw Wlang.noImplement();
    }

    public String getId() {
        throw Wlang.noImplement();
    }

    public long getLastAccessedTime() {
        throw Wlang.noImplement();
    }

    public int getMaxInactiveInterval() {
        throw Wlang.noImplement();
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public Object getValue(String arg0) {
        throw Wlang.noImplement();
    }

    public String[] getValueNames() {
        throw Wlang.noImplement();
    }

    public void invalidate() {
        throw Wlang.noImplement();
    }

    public boolean isNew() {
        throw Wlang.noImplement();
    }

    public void putValue(String arg0, Object arg1) {
        throw Wlang.noImplement();
    }

    public void removeValue(String arg0) {
        throw Wlang.noImplement();
    }

    public void setMaxInactiveInterval(int arg0) {
        throw Wlang.noImplement();
    }

    public Enumeration<String> getAttributeNames() {
        return new Vector<String>(attributeMap.keySet()).elements();
    }

    /**
     * @deprecated
     */
    public HttpSessionContext getSessionContext() {
        return null;
    }

}
