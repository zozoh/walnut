package org.nutz.walnut.cheap.dom.mutation;

import java.util.List;

import org.nutz.walnut.cheap.dom.CheapElement;

/**
 * 对于 DOM 节点的操作
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface DomMutation {

    List<CheapElement> mutate(CheapElement el);

}
