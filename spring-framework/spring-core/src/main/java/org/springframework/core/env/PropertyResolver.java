/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core.env;

import org.springframework.lang.Nullable;

/**
 * Interface for resolving properties against any underlying source.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see Environment
 * @see PropertySourcesPropertyResolver
 */
public interface PropertyResolver {

	/**
	 * 返回给定key是否可以解析
	 * @param key 待解析的属性名称
	 * @return 是否可以解析
	 */
	boolean containsProperty(String key);

	/**
	 * 返回给定key所对应的value,
	 * 如果key不能解析返回 {@code null}
	 * @param key 待解析的属性名称
	 * @see #getProperty(String, String)
	 * @see #getProperty(String, Class)
	 * @see #getRequiredProperty(String)
	 */
	@Nullable
	String getProperty(String key);

	/**
	 * 返回给定key所对应的value,
	 * 如果key不能解析返回 {@code defaultValue}
	 * @param key 待解析的属性值
	 * @param defaultValue 默认值
	 * @see #getRequiredProperty(String)
	 * @see #getProperty(String, Class)
	 */
	String getProperty(String key, String defaultValue);

	/**
	 * 返回给定key所对应的value,
	 * 如果key不能解析返回 {@code null}
	 * @param key 待解析的属性名
	 * @param targetType 属性值的类型
	 * @see #getRequiredProperty(String, Class)
	 */
	@Nullable
	<T> T getProperty(String key, Class<T> targetType);

	/**
	 * 返回给定key所对应的value,
	 * 如果key不能解析返回 {@code defaultValue}
	 * @param key 待解析的属性名
	 * @param targetType 属性值的类型
	 * @param defaultValue 属性值找不到时返回的默认属性值
	 * @see #getRequiredProperty(String, Class)
	 */
	<T> T getProperty(String key, Class<T> targetType, T defaultValue);


	/**
	 * 返回给定属性名称对应的属性值
	 * @param key 属性名称(不允许为空)
	 * @return 属性值
	 * @throws IllegalStateException 当属性无法解析时抛出异常
	 * @see #getRequiredProperty(String, Class)
	 */
	String getRequiredProperty(String key) throws IllegalStateException;


	/**
	 * 返回给定属性名称对应的属性值
	 * @param key 属性名称
	 * @param targetType 属性值类型
	 * @param <T> 属性值类型
	 * @return 属性值
	 * @throws IllegalStateException 如果给定属性名称无法解析抛出异常
	 */
	<T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException;

	/**
	 * 解析给定内容中给定${...}占位符，通过 {@link #getProperty} 替换占位符的值。没有默认值的占位符无法解析时，会被忽略不改变其值
	 * @param text 给定文本
	 * @return 解析后文本 (never {@code null})
	 * @throws IllegalArgumentException 给定文本为 {@code null} 时抛出
	 * @see #resolveRequiredPlaceholders
	 * @see org.springframework.util.SystemPropertyUtils#resolvePlaceholders(String)
	 */
	String resolvePlaceholders(String text);

	/**
	 * 解析给定内容中给定${...}占位符，通过 {@link #getProperty} 替换占位符的值。没有默认值的占位符无法解析时，抛出异常
	 * @return 解析后文本 (never {@code null})
	 * @throws IllegalArgumentException 给定文本为 {@code null} 时或占位符无法解析时抛出
	 * @see org.springframework.util.SystemPropertyUtils#resolvePlaceholders(String, boolean)
	 */
	String resolveRequiredPlaceholders(String text) throws IllegalArgumentException;

}
