package com.j256.simplecsv.processor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Information about a particular field that can be used by converters and other internal code.
 * 
 * @author graywatson
 */
public class FieldInfo<T> {

	private final String name;
	private final Class<T> type;
	private final Field field;
	private final Method getMethod;
	private final Method setMethod;

	public FieldInfo(String name, Class<T> type, Field field, Method getMethod, Method setMethod) {
		this.name = name;
		this.type = type;
		this.field = field;
		this.getMethod = getMethod;
		this.setMethod = setMethod;
	}

	public static <T> FieldInfo<T> fromfield(Field field) {
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) field.getType();
		return new FieldInfo<T>(field.getName(), clazz, field, null, null);
	}

	public String getName() {
		return name;
	}

	public Class<T> getType() {
		return type;
	}

	public T getValue(Object obj) throws IllegalAccessException, InvocationTargetException {
		if (field == null) {
			@SuppressWarnings("unchecked")
			T cast = (T) getMethod.invoke(obj);
			return cast;
		} else {
			@SuppressWarnings("unchecked")
			T cast = (T) field.get(obj);
			return cast;
		}
	}

	public void setValue(Object obj, T value) throws IllegalAccessException, InvocationTargetException {
		if (field == null) {
			setMethod.invoke(obj, value);
		} else {
			field.set(obj, value);
		}
	}

	@Override
	public String toString() {
		return "FieldInfo [name=" + name + ", type=" + type + "]";
	}
}
