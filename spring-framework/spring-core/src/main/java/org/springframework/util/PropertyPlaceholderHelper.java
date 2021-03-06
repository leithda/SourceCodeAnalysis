/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.lang.Nullable;

/**
 * Utility class for working with Strings that have placeholder values in them. A placeholder takes the form
 * {@code ${name}}. Using {@code PropertyPlaceholderHelper} these placeholders can be substituted for
 * user-supplied values. <p> Values for substitution can be supplied using a {@link Properties} instance or
 * using a {@link PlaceholderResolver}.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 3.0
 */
public class PropertyPlaceholderHelper {

	private static final Log logger = LogFactory.getLog(PropertyPlaceholderHelper.class);

	/**
	 * 通用的前缀后缀
	 */
	private static final Map<String, String> wellKnownSimplePrefixes = new HashMap<>(4);

	static {
		wellKnownSimplePrefixes.put("}", "{");
		wellKnownSimplePrefixes.put("]", "[");
		wellKnownSimplePrefixes.put(")", "(");
	}


	/**
	 * 前缀
	 */
	private final String placeholderPrefix;

	/**
	 * 后缀
	 */
	private final String placeholderSuffix;

	/**
	 *
	 */
	private final String simplePrefix;

	/**
	 * 分隔符
	 */
	@Nullable
	private final String valueSeparator;

	/**
	 * 是否忽略无法解析的占位符
	 */
	private final boolean ignoreUnresolvablePlaceholders;


	/**
	 * 使用前缀和后缀创建 {@code PropertyPlaceholderHelper} 实例，默认忽略无法解析的占位符
	 * @param placeholderPrefix 占位符前缀
	 * @param placeholderSuffix 占位符后缀
	 */
	public PropertyPlaceholderHelper(String placeholderPrefix, String placeholderSuffix) {
		this(placeholderPrefix, placeholderSuffix, null, true);
	}

	/**
	 * 使用前缀和后缀创建 {@code PropertyPlaceholderHelper} 实例
	 * @param placeholderPrefix 占位符前缀
	 * @param placeholderSuffix 占位符后缀
	 * @param valueSeparator 如果有默认值，占位符和默认值中间的分隔符
	 * @param ignoreUnresolvablePlaceholders 为 ({@code true}) 将忽略不可解析的占位符，为 ({@code false}) 将抛出异常
	 */
	public PropertyPlaceholderHelper(String placeholderPrefix, String placeholderSuffix,
			@Nullable String valueSeparator, boolean ignoreUnresolvablePlaceholders) {

		Assert.notNull(placeholderPrefix, "'placeholderPrefix' must not be null");
		Assert.notNull(placeholderSuffix, "'placeholderSuffix' must not be null");
		this.placeholderPrefix = placeholderPrefix;
		this.placeholderSuffix = placeholderSuffix;
		String simplePrefixForSuffix = wellKnownSimplePrefixes.get(this.placeholderSuffix);
		if (simplePrefixForSuffix != null && this.placeholderPrefix.endsWith(simplePrefixForSuffix)) {
			this.simplePrefix = simplePrefixForSuffix;
		}
		else {
			this.simplePrefix = this.placeholderPrefix;
		}
		this.valueSeparator = valueSeparator;
		this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
	}


	/**
	 * 替换所有的 {@code ${name}} 格式的占位符为提供  {@link Properties} 中的相应属性值
	 * @param value 给定文本
	 * @param properties 替换使用的 {@code Properties}
	 * @return 替换占位符后的值
	 */
	public String replacePlaceholders(String value, final Properties properties) {
		Assert.notNull(properties, "'properties' must not be null");
		return replacePlaceholders(value, properties::getProperty);
	}

	/**
	 * 使用提供的 {@link PlaceholderResolver} 替换所有格式为 {@code ${name}} 的占位符
	 * @param value 包含占位符的值
	 * @param placeholderResolver 占位符解析器 {@code PlaceholderResolver}
	 * @return 替换占位符后的值
	 */
	public String replacePlaceholders(String value, PlaceholderResolver placeholderResolver) {
		Assert.notNull(value, "'value' must not be null");
		return parseStringValue(value, placeholderResolver, new HashSet<>());
	}

	protected String parseStringValue(
			String value, PlaceholderResolver placeholderResolver, Set<String> visitedPlaceholders) {

		StringBuilder result = new StringBuilder(value);

		// <1>
		int startIndex = value.indexOf(this.placeholderPrefix);
		while (startIndex != -1) {
			// <2>
			int endIndex = findPlaceholderEndIndex(result, startIndex);
			if (endIndex != -1) {
				String placeholder = result.substring(startIndex + this.placeholderPrefix.length(), endIndex);
				String originalPlaceholder = placeholder;
				// <3> 属性嵌套检查，即 key => %${key}%，会导致无限递归
				if (!visitedPlaceholders.add(originalPlaceholder)) {
					throw new IllegalArgumentException(
							"Circular placeholder reference '" + originalPlaceholder + "' in property definitions");
				}
				// 递归
				placeholder = parseStringValue(placeholder, placeholderResolver, visitedPlaceholders);
				// 占位符全部解析完成
				String propVal = placeholderResolver.resolvePlaceholder(placeholder);

				// <4> 解析后的值为空，解析分隔符
				if (propVal == null && this.valueSeparator != null) {
					int separatorIndex = placeholder.indexOf(this.valueSeparator);
					if (separatorIndex != -1) {
						String actualPlaceholder = placeholder.substring(0, separatorIndex);
						String defaultValue = placeholder.substring(separatorIndex + this.valueSeparator.length());
						// 递归
						propVal = placeholderResolver.resolvePlaceholder(actualPlaceholder);
						if (propVal == null) {
							propVal = defaultValue;
						}
					}
				}
				// <5> 解析默认值后，进行判断
				if (propVal != null) {
					// 递归
					propVal = parseStringValue(propVal, placeholderResolver, visitedPlaceholders);
					result.replace(startIndex, endIndex + this.placeholderSuffix.length(), propVal);
					if (logger.isTraceEnabled()) {
						logger.trace("Resolved placeholder '" + placeholder + "'");
					}
					startIndex = result.indexOf(this.placeholderPrefix, startIndex + propVal.length());
				}
				else if (this.ignoreUnresolvablePlaceholders) {
					// Proceed with unprocessed value.
					startIndex = result.indexOf(this.placeholderPrefix, endIndex + this.placeholderSuffix.length());
				}
				else {
					throw new IllegalArgumentException("Could not resolve placeholder '" +
							placeholder + "'" + " in value \"" + value + "\"");
				}
				visitedPlaceholders.remove(originalPlaceholder);
			}
			else {
				startIndex = -1;
			}
		}

		return result.toString();
	}

	/**
	 * 找到占位符的结尾索引
	 * @param buf 给定内容
	 * @param startIndex 占位符起始位置索引
	 * @return 索引
	 */
	private int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
		// 设置索引位置到（起始位置+占位符长度）
		int index = startIndex + this.placeholderPrefix.length();

		int withinNestedPlaceholder = 0;	// 嵌套占位符次数

		// 遍历内容
		while (index < buf.length()) {


			if (StringUtils.substringMatch(buf, index, this.placeholderSuffix)) {
				if (withinNestedPlaceholder > 0) {
					withinNestedPlaceholder--;
					index = index + this.placeholderSuffix.length();
				}
				else {
					return index;
				}
			}
			else if (StringUtils.substringMatch(buf, index, this.simplePrefix)) {
				withinNestedPlaceholder++;
				index = index + this.simplePrefix.length();
			}
			else {
				index++;
			}
		}
		return -1;
	}


	/**
	 * Strategy interface used to resolve replacement values for placeholders contained in Strings.
	 * 策略模式接口
	 */
	@FunctionalInterface
	public interface PlaceholderResolver {

		/**
		 * Resolve the supplied placeholder name to the replacement value.
		 * @param placeholderName the name of the placeholder to resolve
		 * @return the replacement value, or {@code null} if no replacement is to be made
		 */
		@Nullable
		String resolvePlaceholder(String placeholderName);
	}

}
