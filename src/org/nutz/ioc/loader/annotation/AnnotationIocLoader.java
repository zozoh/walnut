package org.nutz.ioc.loader.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.nutz.ioc.*;
import org.nutz.ioc.annotation.InjectName;
import org.nutz.ioc.meta.IocEventSet;
import org.nutz.ioc.meta.IocField;
import org.nutz.ioc.meta.IocObject;
import org.nutz.ioc.meta.IocValue;
import org.nutz.json.Json;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.util.MethodParamNamesScaner;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.resource.Scans;

/**
 * 基于注解的Ioc配置
 * 
 * @author wendal(wendal1985@gmail.com)
 * 
 */
public class AnnotationIocLoader implements IocLoader {

    private static final Log log = Logs.get();

    private HashMap<String, IocObject> map = new HashMap<String, IocObject>();

    protected String[] packages;

    public AnnotationIocLoader() {
        packages = new String[0];
    }

    public AnnotationIocLoader(String... packages) {
        for (String pkg : packages) {
            log.infof(" > scan '%s'", pkg);
            for (Class<?> classZ : Scans.me().scanPackage(pkg)) {
                addClass(classZ);
            }
        }
        if (map.isEmpty()) {
            log.warnf("NONE @IocBean found!! Check your ioc configure!! packages=%s",
                      Arrays.toString(packages));
        }
        this.packages = packages;
    }

    public void addClass(Class<?> classZ) {
        if (classZ.isInterface()
            || classZ.isMemberClass()
            || classZ.isEnum()
            || classZ.isAnnotation()
            || classZ.isAnonymousClass()) {
            return;
        }
        int modify = classZ.getModifiers();
        if (Modifier.isAbstract(modify) || (!Modifier.isPublic(modify))) {
            return;
        }
        IocBean iocBean = classZ.getAnnotation(IocBean.class);
        if (iocBean != null) {
            // 采用 @IocBean->name
            String beanName = iocBean.name();
            if (Strings.isBlank(beanName)) {
                // 否则采用 @InjectName
                InjectName innm = classZ.getAnnotation(InjectName.class);
                if (null != innm && !Strings.isBlank(innm.value())) {
                    beanName = innm.value();
                }
                // 大哥（姐），您都不设啊!? 那就用 simpleName 吧
                else {
                    beanName = Strings.lowerFirst(classZ.getSimpleName());
                }
            }

            // 重名了, 需要用户用@IocBean(name="xxxx") 区分一下
            if (map.containsKey(beanName)) {
                throw new IocException(beanName,
                                       "Duplicate beanName=%s, by %s !!  Have been define by %s !!",
                                       beanName,
                                       classZ.getName(),
                                       map.get(beanName).getType().getName());
            }

            IocObject iocObject = new IocObject();
            iocObject.setType(classZ);
            map.put(beanName, iocObject);

            log.infof("   > add '%-40s' - %s", beanName, classZ.getName());

            iocObject.setSingleton(iocBean.singleton());
            if (!Strings.isBlank(iocBean.scope())) {
                iocObject.setScope(iocBean.scope());
            }

            // 看看构造函数都需要什么函数
            String[] args = iocBean.args();
            // if (null == args || args.length == 0)
            // args = iocBean.param();
            if (null != args && args.length > 0) {
                for (String value : args) {
                    iocObject.addArg(Iocs.convert(value, true));
                }
            }

            // 设置Events
            IocEventSet eventSet = new IocEventSet();
            iocObject.setEvents(eventSet);
            if (!Strings.isBlank(iocBean.create())) {
                eventSet.setCreate(iocBean.create().trim().intern());
            }
            if (!Strings.isBlank(iocBean.depose())) {
                eventSet.setDepose(iocBean.depose().trim().intern());
            }
            if (!Strings.isBlank(iocBean.fetch())) {
                eventSet.setFetch(iocBean.fetch().trim().intern());
            }

            // 处理字段(以@Inject方式,位于字段)
            List<String> fieldList = new ArrayList<String>();
            Mirror<?> mirror = Mirror.me(classZ);
            Field[] fields = mirror.getFields(Inject.class);
            for (Field field : fields) {
                Inject inject = field.getAnnotation(Inject.class);
                // 无需检查,因为字段名是唯一的
                // if(fieldList.contains(field.getName()))
                // throw duplicateField(classZ,field.getName());
                IocField iocField = new IocField();
                iocField.setName(field.getName());
                IocValue iocValue;
                if (Strings.isBlank(inject.value())) {
                    if ("ioc".equals(field.getName())) {
                        iocValue = new IocValue();
                        iocValue.setType(IocValue.TYPE_REFER);
                        iocValue.setValue("$ioc");
                    } else {
                        iocValue = new IocValue();
                        iocValue.setType(IocValue.TYPE_REFER_TYPE);
                        iocValue.setValue(field);
                    }
                } else {
                    iocValue = Iocs.convert(inject.value(), true);
                }
                iocField.setValue(iocValue);
                iocField.setOptional(inject.optional());
                iocObject.addField(iocField);
                fieldList.add(iocField.getName());
            }
            // 处理字段(以@Value方式,位于字段)
            Field[] valueFields = mirror.getFields(Value.class);
            for (Field valueField : valueFields) {
                Value value = valueField.getAnnotation(Value.class);
                String configName;
                if (Strings.isBlank(value.name())) {
                    configName = Strings.lowerFirst(valueField.getName());
                } else {
                    configName = value.name();
                }
                String defaultValue = value.defaultValue();
                IocValue iocValue;
                if (Strings.isBlank(defaultValue)) {
                    iocValue = Iocs.convert(String.format("java:$conf.get('%s')", configName),
                                            true);
                } else {
                    iocValue = Iocs.convert(String.format("java:$conf.get('%s', '%s')",
                                                          configName,
                                                          defaultValue),
                                            true);
                }
                IocField iocField = new IocField();
                iocField.setName(valueField.getName());
                iocField.setValue(iocValue);
                iocField.setOptional(false);
                iocObject.addField(iocField);
                fieldList.add(iocField.getName());
            }
            // 处理字段(以@Inject方式,位于set方法)
            Method[] methods;
            try {
                methods = classZ.getMethods();
            }
            catch (Exception e) {
                // 如果获取失败,就忽略之
                log.infof("Fail to call getMethods() in Class=%s, miss class or Security Limit, ignore it",
                          classZ,
                          e);
                methods = new Method[0];
            }
            catch (NoClassDefFoundError e) {
                log.infof("Fail to call getMethods() in Class=%s, miss class or Security Limit, ignore it",
                          classZ,
                          e);
                methods = new Method[0];
            }
            for (Method method : methods) {
                Inject inject = method.getAnnotation(Inject.class);
                if (inject == null) {
                    continue;
                }
                // 过滤特殊方法
                int m = method.getModifiers();
                if (Modifier.isAbstract(m) || (!Modifier.isPublic(m)) || Modifier.isStatic(m)) {
                    continue;
                }
                String methodName = method.getName();
                if (methodName.startsWith("set")
                    && methodName.length() > 3
                    && method.getParameterTypes().length == 1) {
                    IocField iocField = new IocField();
                    iocField.setName(Strings.lowerFirst(methodName.substring(3)));
                    if (fieldList.contains(iocField.getName())) {
                        throw duplicateField(beanName, classZ, iocField.getName());
                    }
                    IocValue iocValue;
                    if (Strings.isBlank(inject.value())) {
                        iocValue = new IocValue();
                        iocValue.setType(IocValue.TYPE_REFER_TYPE);
                        iocValue.setValue(Strings.lowerFirst(methodName.substring(3))
                                          + "#"
                                          + method.getParameterTypes()[0].getName());
                    } else {
                        iocValue = Iocs.convert(inject.value(), true);
                    }
                    iocField.setValue(iocValue);
                    iocField.setOptional(inject.optional()); // 对setter方法上的@Inject注解，也支持optional设置
                    iocObject.addField(iocField);
                    fieldList.add(iocField.getName());
                }
            }
            // 处理字段(以@IocBean.field方式)
            String[] flds = iocBean.fields();
            if (flds != null && flds.length > 0) {
                for (String fieldInfo : flds) {
                    if (fieldList.contains(fieldInfo)) {
                        throw duplicateField(beanName, classZ, fieldInfo);
                    }
                    IocField iocField = new IocField();
                    if (fieldInfo.contains(":")) { // dao:jndi:dataSource/jdbc形式
                        String[] datas = fieldInfo.split(":", 2);
                        // 完整形式, 与@Inject完全一致了
                        iocField.setName(datas[0]);
                        if (datas[1].endsWith("!optional")) { // fields中支持optional设置
                            iocField.setOptional(true);
                            datas[1] = datas[1].substring(0, datas[1].indexOf("!optional"));
                        }
                        iocField.setValue(Iocs.convert(datas[1], true));
                        iocObject.addField(iocField);
                    } else {
                        if (fieldInfo.endsWith("!optional")) {
                            iocField.setOptional(true);
                            fieldInfo = fieldInfo.substring(0, fieldInfo.indexOf("!optional"));
                        }
                        // 基本形式, 引用与自身同名的bean
                        iocField.setName(fieldInfo);
                        IocValue iocValue = new IocValue();
                        iocValue.setType(IocValue.TYPE_REFER);
                        iocValue.setValue(fieldInfo);
                        iocField.setValue(iocValue);
                        iocObject.addField(iocField);
                    }
                    fieldList.add(iocField.getName());
                }
            }

            // 处理工厂方法
            if (!Strings.isBlank(iocBean.factory())) {
                iocObject.setFactory(iocBean.factory());
            }

            // 看看有没有方法标注了@IocBean
            for (Method method : methods) {
                IocBean ib = method.getAnnotation(IocBean.class);
                if (ib == null) {
                    continue;
                }
                handleIocBeanMethod(method, ib, beanName);
            }
        } else {
            // 不再检查其他类.
        }
    }

    protected void handleIocBeanMethod(Method method, IocBean ib, String facotryBeanName) {
        String beanName = ib.name();
        if (Strings.isBlank(beanName)) {
            String methodName = method.getName();
            if (methodName.startsWith("get")) {
                methodName = methodName.substring(3);
            } else if (methodName.startsWith("build")) {
                methodName = methodName.substring(5);
            }
            beanName = Strings.lowerFirst(methodName);
        }
        if (log.isDebugEnabled()) {
            log.debugf("Found @IocBean method : %s define as name=%s",
                       Wlang.simpleMethodDesc(method),
                       beanName);
        }
        IocObject iobj = new IocObject();
        iobj.setType(method.getReturnType());
        iobj.setFactory("$" + facotryBeanName + "#" + method.getName());

        List<String> paramNames = MethodParamNamesScaner.getParamNames(method);
        Class<?>[] paramTypes = method.getParameterTypes();
        Annotation[][] anns = method.getParameterAnnotations();
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            String paramName = (paramNames != null
                                && (paramNames.size() >= (i - 1))) ? paramNames.get(i) : "arg" + i;
            IocValue ival = new IocValue();
            Inject inject = null;
            if (anns[i] != null && anns[i].length > 0) {
                for (Annotation anno : anns[i]) {
                    if (anno instanceof Inject) {
                        inject = (Inject) anno;
                        break;
                    }
                }
            }
            if (inject == null || Strings.isBlank(inject.value())) {
                ival.setType(IocValue.TYPE_REFER_TYPE);
                ival.setValue(paramName + "#" + paramType.getName());
            } else {
                ival = Iocs.convert(inject.value(), true);
            }
            iobj.addArg(ival);
        }
        // 设置Events
        IocEventSet eventSet = new IocEventSet();
        iobj.setEvents(eventSet);
        if (!Strings.isBlank(ib.create())) {
            eventSet.setCreate(ib.create().trim().intern());
        }
        if (!Strings.isBlank(ib.depose())) {
            eventSet.setDepose(ib.depose().trim().intern());
        }
        if (!Strings.isBlank(ib.fetch())) {
            eventSet.setFetch(ib.fetch().trim().intern());
        }
        iobj.setSingleton(ib.singleton());
        map.put(beanName, iobj);
    }

    @Override
    public String[] getName() {
        return map.keySet().toArray(new String[map.size()]);
    }

    @Override
    public boolean has(String name) {
        return map.containsKey(name);
    }

    @Override
    public IocObject load(IocLoading loading, String name) throws ObjectLoadException {
        if (has(name)) {
            return map.get(name);
        }
        throw new ObjectLoadException("Object '"
                                      + name
                                      + "' without define! Pls check your ioc configure");
    }

    private static final IocException duplicateField(String beanName,
                                                     Class<?> classZ,
                                                     String name) {
        return new IocException(beanName,
                                "Duplicate filed defined! Class=%s,FileName=%s",
                                classZ,
                                name);
    }

    @Override
    public String toString() {
        return "/*AnnotationIocLoader*/\n" + Json.toJson(map);
    }

    public String[] getPackages() {
        return packages;
    }
}
