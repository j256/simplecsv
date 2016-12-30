package com.j256.simplecsv.processor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Information about a particular Java field (or get/set method pair) that can be used by converters and other internal
 * code.
 * 
 * @author graywatson
 */
public class FieldInfo<T> {

	private final String name;
	private final Class<T> type;
	// may be null
	private final Field field;
	// may be null
	private final Method getMethod;
	// may be null
	private final Method setMethod;

	/**
	 * Create a field info from a Field.
	 */
	public static <T> FieldInfo<T> fromfield(Field field) {
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) field.getType();
		return new FieldInfo<T>(field.getName(), clazz, field, null, null);
	}

	/**
	 * Create a field info from a get/is and set method.
	 */
	public static <T> FieldInfo<T> fromMethods(String fieldName, Method getMethod, Method setMethod) {
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) getMethod.getReturnType();
		return new FieldInfo<T>(fieldName, clazz, null, getMethod, setMethod);
	}

	private FieldInfo(String name, Class<T> type, Field field, Method getMethod, Method setMethod) {
		this.name = name;
		this.type = type;
		this.field = field;
		this.getMethod = getMethod;
		this.setMethod = setMethod;
	}

	/**
	 * Get the value associated with this field from the object parameter either by getting from the field or calling
	 * the get method.
	 */
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

	/**
	 * Set the value associated with this field from the object parameter either by setting via the field or calling the
	 * set method.
	 */
	public void setValue(Object obj, T value) throws IllegalAccessException, InvocationTargetException {
		if (field == null) {
			setMethod.invoke(obj, value);
		} else {
			field.set(obj, value);
		}
	}

	public String getName() {
		return name;
	}

	/**
	 * Return the class type of the field.
	 */
	public Class<T> getType() {
		return type;
	}

	/**
	 * Return the Field information associated with our field info. If the field was configured using get/set methods,
	 * then this will return null.
	 */
	public Field getField() {
		return field;
	}

	/**
	 * Return the get/is method associated with our field info. If the field was configured using a field then this will
	 * return null.
	 */
	public Method getGetMethod() {
		return getMethod;
	}

	/**
	 * Return the set method associated with our field info. If the field was configured using a field then this will
	 * return null.
	 */
	public Method getSetMethod() {
		return setMethod;
	}

	@Override
	public String toString() {
		return "FieldInfo [name=" + name + ", type=" + type + "]";
	}
}
