package org.nutz.walnut.ext.media.edi.bean;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.util.Ws;

public class EdiComponent extends EdiItem {

    private List<EdiElement> elements;

    public EdiComponent(EdiAdvice advice) {
        super(advice);
    }

    public EdiComponent(EdiAdvice advice, List<EdiElement> elements) {
        this(advice);
        this.elements = elements;
    }

    @Override
    public void joinString(StringBuilder sb) {
        if (null != elements) {
            int i = 0;
            for (EdiElement ele : this.elements) {
                if ((i++) > 0) {
                    sb.append(advice.element);
                }
                ele.joinString(sb);
            }
        }
    }

    @Override
    public void joinTree(StringBuilder sb, int depth) {
        this.joinString(sb);
    }

    public EdiElementType getFirstElementType() {
        if (null != elements && elements.size() > 0) {
            return elements.get(0).getType();
        }
        return null;
    }

    public boolean isFirstElement(String name) {
        return is(name);
    }

    public boolean is(String... tags) {
        if (null == elements) {
            return false;
        }
        if (tags.length > 0) {
            int N = tags.length;
            if (elements.size() < N) {
                return false;
            }
            int i = 0;
            for (EdiElement ele : elements) {
                if (i >= N) {
                    break;
                }
                String tag = tags[i++];
                if (!ele.isTag(tag)) {
                    return false;
                }
            }
            return true;
        }
        return elements.size() > 0;
    }

    public boolean isEmpty() {
        if (null == this.elements || this.elements.isEmpty()) {
            return true;
        }
        for (EdiElement ele : this.elements) {
            if (!ele.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void fillBean(NutBean bean, String... keys) {
        // 防空
        if (null == bean) {
            return;
        }
        int N = Math.min(elements.size(), keys.length);
        for (int i = 0; i < N; i++) {
            String key = keys[i];
            if (Ws.isBlank(key)) {
                continue;
            }
            EdiElement ele = elements.get(i);
            if (ele.isEmpty()) {
                continue;
            }
            Object val = ele.getValue();
            bean.put(key, val);
        }
    }

    /**
     * 获取所有元素的值，如果只有一个元素有值，则直接返回这个值，<br>
     * 如果没有元素有值，则返回<code>null</code>。<br>
     * 超过一个元素有值，则返回一个列表。
     * 
     * @return 元素的值
     */
    public Object getElementsValue() {
        ArrayList<Object> list = new ArrayList<>(this.elements.size());

        for (EdiElement ele : elements) {
            if (ele.isEmpty()) {
                continue;
            }
            list.add(ele.getValue());
        }

        int N = list.size();
        if (0 == N) {
            return null;
        }
        if (1 == N) {
            return list.get(0);
        }
        return list;
    }

    public List<EdiElement> getElements() {
        return elements;
    }

    public void setElements(List<EdiElement> elements) {
        this.elements = elements;
    }

}
