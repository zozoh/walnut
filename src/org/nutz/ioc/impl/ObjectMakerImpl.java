package org.nutz.ioc.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.nutz.ioc.IocEventTrigger;
import org.nutz.ioc.IocException;
import org.nutz.ioc.IocMaking;
import org.nutz.ioc.ObjectMaker;
import org.nutz.ioc.ObjectProxy;
import org.nutz.ioc.ValueProxy;
import org.nutz.ioc.meta.IocEventSet;
import org.nutz.ioc.meta.IocField;
import org.nutz.ioc.meta.IocObject;
import org.nutz.ioc.weaver.DefaultWeaver;
import org.nutz.ioc.weaver.FieldInjector;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.born.Borning;
import org.nutz.lang.born.MethodBorning;
import org.nutz.lang.born.MethodCastingBorning;
import org.nutz.lang.reflect.FastClassFactory;
import org.nutz.lang.reflect.FastMethod;

/**
 * 在这里，需要考虑 AOP
 * 
 * @author zozoh(zozohtnt@gmail.com)
 * @author wendal(wendal1985@gmail.com)
 */
public class ObjectMakerImpl implements ObjectMaker {

    public ObjectProxy make(final IocMaking ing, IocObject iobj) {

        // 获取配置的对象事件集合
        IocEventSet iocEventSet = iobj.getEvents();

        // 建立对象代理，并保存在上下文环境中 只有对象为 singleton
        // 并且有一个非 null 的名称的时候才会保存
        // 就是说，所有内部对象，将会随这其所附属的对象来保存，而自己不会单独保存
        ObjectProxy op = new ObjectProxy();
        if (iobj.isSingleton() && null != ing.getObjectName())
            ing.getContext().save(iobj.getScope(), ing.getObjectName(), op);


        try {
            // 准备对象的编织方式
            DefaultWeaver dw = new DefaultWeaver();
            dw.setListeners(ing.getListeners());
            op.setWeaver(dw);

            // 构造函数参数
            ValueProxy[] vps = new ValueProxy[Wlang.eleSize(iobj.getArgs())];
            for (int i = 0; i < vps.length; i++)
                vps[i] = ing.makeValue(iobj.getArgs()[i]);
            dw.setArgs(vps);

            // 先获取一遍，根据这个数组来获得构造函数
            Object[] args = new Object[vps.length];
            boolean hasNullArg = false;
            for (int i = 0; i < args.length; i++) {
                args[i] = vps[i].get(ing);
                if (args[i] == null) {
                    hasNullArg = true;
                }
            }
            // 获取 Mirror， AOP 将在这个方法中进行
            Mirror<?> mirror = null;

            // 缓存构造函数
            if (iobj.getFactory() != null) {
                // factory这属性, 格式应该是 类名#方法名 或者 $iocbean#方法名
                final String[] ss = iobj.getFactory().split("#", 2);
                if (ss[0].startsWith("$")) {
                    dw.setBorning(new Borning<Object>() {
                        public Object born(Object... args) {
                            Object factoryBean = ing.getIoc().get(null, ss[0].substring(1));
                            return Mirror.me(factoryBean).invoke(factoryBean, ss[1], args);
                        }
                    });
                } else {
                    Mirror<?> mi = Mirror.me(Wlang.loadClass(ss[0]));
                    Method m;
                    if (hasNullArg) {
                        m = (Method) Wlang.first(mi.findMethods(ss[1],args.length));
                        if (m == null)
                            throw new IocException(ing.getObjectName(), "Factory method not found --> ", iobj.getFactory());
                        dw.setBorning(new MethodCastingBorning<Object>(m));
                    } else {
                        m = mi.findMethod(ss[1], args);
                        dw.setBorning(new MethodBorning<Object>(m));
                    }
                    if (iobj.getType() == null)
                        iobj.setType(m.getReturnType());
                }
                if (iobj.getType() != null)
                    mirror = ing.getMirrors().getMirror(iobj.getType(), ing.getObjectName());
            } else {
                mirror = ing.getMirrors().getMirror(iobj.getType(), ing.getObjectName());
                dw.setBorning((Borning<?>) mirror.getBorning(args));
            }
            

            // 为对象代理设置触发事件
            if (null != iobj.getEvents()) {
                op.setFetch(createTrigger(mirror, iocEventSet.getFetch()));
                op.setDepose(createTrigger(mirror, iocEventSet.getDepose()));
                dw.setCreate(createTrigger(mirror, iocEventSet.getCreate()));
            }

            // 如果这个对象是容器中的单例，那么就可以生成实例了
            // 这一步非常重要，它解除了字段互相引用的问题
            Object obj = null;
            if (iobj.isSingleton()) {
                obj = dw.born(ing);
                op.setObj(obj);
            }

            // 获得每个字段的注入方式
            List<IocField> _fields = new ArrayList<IocField>(iobj.getFields().values());
            FieldInjector[] fields = new FieldInjector[_fields.size()];
            for (int i = 0; i < fields.length; i++) {
                IocField ifld = _fields.get(i);
                try {
                    ValueProxy vp = ing.makeValue(ifld.getValue());
                    fields[i] = FieldInjector.create(mirror, ifld.getName(), vp, ifld.isOptional());
                }
                catch (Exception e) {
                	throw Wlang.wrapThrow(e, "Fail to eval Injector for field: '%s'", ifld.getName());
                }
            }
            dw.setFields(fields);

            // 如果是单例对象，前面已经生成实例了，在这里需要填充一下它的字段
            if (null != obj)
                dw.fill(ing, obj);

            // 对象创建完毕，如果有 create 事件，调用它
            Object tmp = dw.onCreate(obj);
            if (tmp != null)
                op.setObj(tmp);
        }
        catch (IocException e) {
            ing.getContext().remove(iobj.getScope(), ing.getObjectName());
            ((IocException)e).addBeanNames(ing.getObjectName());
            throw e;
        }
        // 当异常发生，从 context 里移除 ObjectProxy
        catch (Throwable e) {
            ing.getContext().remove(iobj.getScope(), ing.getObjectName());
            throw new IocException(ing.getObjectName(), e, "throw Exception when creating");
        }

        // 返回
        return op;
    }

    @SuppressWarnings({"unchecked"})
    private static IocEventTrigger<Object> createTrigger(Mirror<?> mirror, final String str) {
        if (Strings.isBlank(str))
            return null;
        if (str.contains(".")) {
            try {
                return (IocEventTrigger<Object>) Mirror.me(Wlang.loadClass(str))
                                                       .born();
            }
            catch (Exception e) {
                throw Wlang.wrapThrow(e);
            }
        }
        return new IocEventTrigger<Object>() {
        	protected FastMethod fm;
			public void trigger(Object obj) {
				try {
					if (fm == null) {
						Method method = Mirror.me(obj).findMethod(str);
						fm = FastClassFactory.get(method);
					}
					fm.invoke(obj);
				} catch (Exception e) {
					throw Wlang.wrapThrow(e);
				}
			}
        };
    }

}
