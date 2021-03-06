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

import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.lang.Nullable;

/**
 * Configuration interface to be implemented by most if not all {@link PropertyResolver}
 * types. Provides facilities for accessing and customizing the
 * {@link org.springframework.core.convert.ConversionService ConversionService}
 * used when converting property values from one type to another.
 *
 * @author Chris Beams
 * @since 3.1
 */
public interface ConfigurablePropertyResolver extends PropertyResolver {

	/**
	 * 返回解析属性是使用的 {@link ConfigurableConversionService}
	 * <p>返回的 {@link ConfigurableConversionService}  允许添加或删除指定的 {@code Converter} 实例:
	 * <pre class="code">
	 * ConfigurableConversionService cs = env.getConversionService();
	 * cs.addConverter(new FooConverter());
	 * </pre>
	 * @see PropertyResolver#getProperty(String, Class)
	 * @see org.springframework.core.convert.converter.ConverterRegistry#addConverter
	 */
	ConfigurableConversionService getConversionService();

	/**
	 * 全局替换 {@link ConfigurableConversionService}
	 * <p><strong>注意:</strong> 全量替换 {@code ConversionService}, 需要考虑 在  {@link #getConversionService()} 方法中 通过 {@code #addConverter} 添加或移除自定义的
	 * {@code Converter} 实例
	 * @see PropertyResolver#getProperty(String, Class)
	 * @see #getConversionService()
	 * @see org.springframework.core.convert.converter.ConverterRegistry#addConverter
	 */
	void setConversionService(ConfigurableConversionService conversionService);

	/**
	 * 设置占位符前缀
	 */
	void setPlaceholderPrefix(String placeholderPrefix);

	/**
	 * 设置占位符后缀
	 */
	void setPlaceholderSuffix(String placeholderSuffix);

	/**
	 * 默认值的分隔符，默认是 ':'
	 */
	void setValueSeparator(@Nullable String valueSeparator);

	/**
	 * 是否忽略解析不了的占位符，为否时抛出异常
	 * @since 3.2
	 */
	void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders);

	/**
	 * 指定必须设置的属性，通过 {@link #validateRequiredProperties()} 进行校验
	 */
	void setRequiredProperties(String... requiredProperties);

	/**
	 * 验证必输属性
	 * @throws MissingRequiredPropertiesException 必输属性无法解析异常
	 */
	void validateRequiredProperties() throws MissingRequiredPropertiesException;

}
