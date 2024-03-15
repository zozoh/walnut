package org.nutz.dao.impl.entity.field;

import java.lang.reflect.Type;

import org.nutz.dao.entity.Entity;
import org.nutz.dao.entity.EntityField;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Mirror;
import org.nutz.lang.eject.Ejecting;
import org.nutz.lang.inject.Injecting;

public abstract class AbstractEntityField implements EntityField {

    protected Entity<?> entity;

    protected String name;

    protected Type type;

    protected Class<?> typeClass;

    protected Mirror<?> mirror;

    protected Injecting injecting;

    protected Ejecting ejecting;

    public AbstractEntityField(Entity<?> entity) {
        this.entity = entity;
    }

    public Entity<?> getEntity() {
        return entity;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public Class<?> getTypeClass() {
        return typeClass;
    }

    public Mirror<?> getMirror() {
        return mirror;
    }

    public void setValue(Object obj, Object value) {
        injecting.inject(obj, value);
    }

    public Object getValue(Object obj) {
        return ejecting.eject(obj);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setInjecting(Injecting injecting) {
        this.injecting = injecting;
    }

    public void setEjecting(Ejecting ejecting) {
        this.ejecting = ejecting;
    }

    public void setType(Type type) {
        this.type = type;
        this.typeClass = Wlang.getTypeClass(type);
        this.mirror = Mirror.me(typeClass);
    }

    public void setEntity(Entity<?> entity) {
        this.entity = entity;
    }

    public void setTypeClass(Class<?> typeClass) {
        this.typeClass = typeClass;
    }

    public void setMirror(Mirror<?> mirror) {
        this.mirror = mirror;
    }

    public String toString() {
        return String.format("'%s'(%s)", this.name, this.entity.getType().getName());
    }

}
