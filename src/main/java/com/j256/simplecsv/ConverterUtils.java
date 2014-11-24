package com.j256.simplecsv;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.j256.simplecsv.converter.BigDecimalConverter;
import com.j256.simplecsv.converter.BigIntegerConverter;
import com.j256.simplecsv.converter.BooleanConverter;
import com.j256.simplecsv.converter.ByteConverter;
import com.j256.simplecsv.converter.CharacterConverter;
import com.j256.simplecsv.converter.Converter;
import com.j256.simplecsv.converter.DateConverter;
import com.j256.simplecsv.converter.DoubleConverter;
import com.j256.simplecsv.converter.EnumConverter;
import com.j256.simplecsv.converter.FloatConverter;
import com.j256.simplecsv.converter.IntegerConverter;
import com.j256.simplecsv.converter.LongConverter;
import com.j256.simplecsv.converter.ShortConverter;
import com.j256.simplecsv.converter.StringConverter;
import com.j256.simplecsv.converter.UuidConverter;

/**
 * Manages the collection of converter objects so we can reuse them as necessary.
 * 
 * @author graywatson
 */
public class ConverterUtils {

	/**
	 * Add internal converters to the map.
	 */
	public static void addInternalConverters(Map<Class<?>, Converter<?, ?>> converterMap) {
		converterMap.put(BigDecimal.class, BigDecimalConverter.getSingleton());
		converterMap.put(BigInteger.class, BigIntegerConverter.getSingleton());
		converterMap.put(Boolean.class, BooleanConverter.getSingleton());
		converterMap.put(boolean.class, BooleanConverter.getSingleton());
		converterMap.put(Byte.class, ByteConverter.getSingleton());
		converterMap.put(byte.class, ByteConverter.getSingleton());
		converterMap.put(Character.class, CharacterConverter.getSingleton());
		converterMap.put(char.class, CharacterConverter.getSingleton());
		converterMap.put(Date.class, DateConverter.getSingleton());
		converterMap.put(Double.class, DoubleConverter.getSingleton());
		converterMap.put(double.class, DoubleConverter.getSingleton());
		converterMap.put(Enum.class, EnumConverter.getSingleton());
		converterMap.put(Float.class, FloatConverter.getSingleton());
		converterMap.put(float.class, FloatConverter.getSingleton());
		converterMap.put(Integer.class, IntegerConverter.getSingleton());
		converterMap.put(int.class, IntegerConverter.getSingleton());
		converterMap.put(Long.class, LongConverter.getSingleton());
		converterMap.put(long.class, LongConverter.getSingleton());
		converterMap.put(Short.class, ShortConverter.getSingleton());
		converterMap.put(short.class, ShortConverter.getSingleton());
		converterMap.put(String.class, StringConverter.getSingleton());
		converterMap.put(UUID.class, UuidConverter.getSingleton());
	}

	/**
	 * Construct a converter instance.
	 */
	public static Converter<?, ?> constructConverter(Class<? extends Converter<?, ?>> clazz) {
		Constructor<? extends Converter<?, ?>> constructor;
		try {
			constructor = clazz.getConstructor();
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not find public no-arg constructor for CSV converter class: "
					+ clazz, e);
		}
		try {
			return constructor.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not construct new CSV converter: " + clazz, e);
		}
	}
}
