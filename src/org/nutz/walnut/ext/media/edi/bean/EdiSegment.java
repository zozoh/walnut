package org.nutz.walnut.ext.media.edi.bean;

import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Ws;

/**
 * 一个 EDI 报文行
 * 
 * @author zozoh
 *
 */
public class EdiSegment extends EdiItem {

    private List<EdiComponent> components;

    public EdiSegment(EdiAdvice advice) {
        super(advice);
    }

    public EdiSegment(EdiAdvice advice, List<EdiComponent> components) {
        this(advice);
        this.components = components;
    }

    @Override
    public void joinString(StringBuilder sb) {
        if (null != this.components) {
            int i = 0;
            for (EdiComponent com : this.components) {
                if ((i++) > 0) {
                    sb.append(advice.component);
                }
                com.joinString(sb);
            }
        }
    }

    public NutBean getBean(String... keys) {
        NutMap bean = new NutMap();
        this.fillBean(bean, keys);
        return bean;
    }

    /**
     * 根据指定的键，按顺序填充对象。
     * <p>
     * 提供的没每个键都对应一个 <code>component</code> 如果其只有一个元素，则对应这个元素的值
     * 如果有多个元素，则回形成要给对象数组<code>Object[]</code>
     * <p>
     * 也可以提供形式如下的键:
     * 
     * <pre>
     * name: key1,key2,key3...
     * </pre>
     * 
     * 即，会根据 (key1, key2, key3...) 填充一个新对象，<br>
     * 并将这个对象填充到著对象的 <code>name</code> 字段中去。
     * <p>
     * 
     * 如果并未给前缀，而仅仅是<code>key1,key2,key3...</code> 则会将这些键直接填充到主对象。
     * 
     * @param bean
     *            要被填充的对象
     * @param keys
     *            填充的键
     */
    public void fillBean(NutBean bean, String... keys) {
        // 防空
        if (null == bean) {
            return;
        }
        int N = Math.min(keys.length, this.components.size());
        for (int i = 0; i < N; i++) {
            EdiComponent com = this.components.get(i);
            if (com.isEmpty()) {
                continue;
            }
            String str = keys[i];
            if (Ws.isBlank(str)) {
                continue;
            }
            int pos = str.indexOf(':');

            // 创建新对象
            if (pos > 0) {
                String key = str.substring(0, pos).trim();
                String[] ks = Ws.splitTrimed(str.substring(pos + 1));
                NutMap map = new NutMap();
                com.fillBean(map, ks);
                bean.put(key, map);
            }
            // 那么就是搞到主对象里咯
            else {
                // 多个键
                String[] ks = Ws.splitTrimed(str);
                if (ks.length > 1) {
                    com.fillBean(bean, ks);
                }
                // 一个键，那么就尝试归纳组件元素，是一个数组还是单个值
                else if (ks.length == 1) {
                    if (!Ws.isBlank(ks[0])) {
                        Object val = com.getElementsValue();
                        bean.put(ks[0], val);
                    }
                }
                // 不可能啊
                else {
                    throw Wlang.impossible();
                }
            }
        }

    }

    public boolean isTag(String name) {
        return is(name);
    }

    public boolean is(String... tags) {
        if (null == components) {
            return false;
        }
        if (tags.length > 0) {
            int N = tags.length;
            if (components.size() < N) {
                return false;
            }
            int i = 0;
            for (EdiComponent com : components) {
                if (i >= N) {
                    break;
                }
                String tag = tags[i++];
                if (!com.is(tag)) {
                    return false;
                }
            }
            return true;
        }
        return components.size() > 0;
    }

    public void setComponent(int index, String str) {
        EdiElement ele = new EdiElement(str);
        EdiComponent com = new EdiComponent(advice, Wlang.list(ele));
        this.setComponent(index, com);
    }

    public void setComponent(int index, Integer n) {
        EdiElement ele = new EdiElement(EdiElementType.NUMBER, n.toString());
        EdiComponent com = new EdiComponent(advice, Wlang.list(ele));
        this.setComponent(index, com);
    }

    public void setComponent(int index, EdiComponent com) {
        if (null == this.components) {
            throw Er.create("e.edi.segment.componentsWithoutInit");
        }
        if (index < 0 || index >= components.size()) {
            throw Er.create("e.edi.segment.componentOutOfBound", index);
        }
        this.components.set(index, com);
    }

    public List<EdiComponent> getComponents() {
        return components;
    }

    public void setComponents(List<EdiComponent> components) {
        this.components = components;
    }

}
