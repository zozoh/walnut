package org.nutz.lang.util;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 通过实现ParameterizedType,达到无需通过反射得到泛型Type</p> 通过嵌套Map/List,可得出无限可能
 * 
 * @author wendal(wendal1985@gmail.com)
 * 
 */
public class NutType implements ParameterizedType {

    /**
     * 生成List<AbcBean>形式的泛型信息
     * 
     * @param clazz
     *            泛型的具体类
     * @return List<clazz>形式的泛型Type
     */
    public static Type list(Type clazz) {
        return new NutType(List.class, clazz);
    }

    /**
     * 生成Abc[]形式的泛型信息
     */
    public static Type array(Class<?> clazz) {
        return new NutType(Array.newInstance(clazz, 0).getClass());
    }

    /**
     * 生成Map<key,value>形式的泛型Type
     * 
     * @param key
     *            key的泛型
     * @param value
     *            value的泛型
     * @return Map<key,value>形式的泛型Type
     */
    public static Type map(Type key, Type value) {
        return new NutType(Map.class, key, value);
    }

    /**
     * 生成Map<String,value>形式的泛型Type
     * 
     * @param value
     *            value的泛型
     * @return Map<String,value>形式的泛型Type
     */
    public static Type mapStr(Type value) {
        return new NutType(Map.class, String.class, value);
    }

    public NutType() {}

    public NutType(Type rawType, Type... actualTypeArguments) {
        this.rawType = rawType;
        this.actualTypeArguments = actualTypeArguments;
    }

    private Type[] actualTypeArguments;

    private Type rawType;

    private Type ownerType;

    public Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }

    public Type getRawType() {
        return rawType;
    }

    public Type getOwnerType() {
        return ownerType;
    }

    public void setActualTypeArguments(Type... actualTypeArguments) {
        this.actualTypeArguments = actualTypeArguments;
    }

    public void setOwnerType(Type ownerType) {
        this.ownerType = ownerType;
    }

    public void setRawType(Type rawType) {
        this.rawType = rawType;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(actualTypeArguments);
		result = prime * result + ((ownerType == null) ? 0 : ownerType.hashCode());
		result = prime * result + ((rawType == null) ? 0 : rawType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NutType other = (NutType) obj;
		if (!Arrays.equals(actualTypeArguments, other.actualTypeArguments))
			return false;
		if (ownerType == null) {
			if (other.ownerType != null)
				return false;
		} else if (!ownerType.equals(other.ownerType))
			return false;
		if (rawType == null) {
			if (other.rawType != null)
				return false;
		} else if (!rawType.equals(other.rawType))
			return false;
		return true;
	}
}
