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

package org.springframework.core.env;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.SystemPropertyUtils;

/**
 * Abstract base class for resolving properties against any underlying source.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 */
public abstract class AbstractPropertyResolver implements ConfigurablePropertyResolver {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * 可配置转换服务
	 */
	@Nullable
	private volatile ConfigurableConversionService conversionService;

	/**
	 * 非严格模式占位符解析助手
	 */
	@Nullable
	private PropertyPlaceholderHelper nonStrictHelper;

	/**
	 * 严格模式占位符解析助手
	 */
	@Nullable
	private PropertyPlaceholderHelper strictHelper;

	private boolean ignoreUnresolvableNestedPlaceholders = false;

	private String placeholderPrefix = SystemPropertyUtils.PLACEHOLDER_PREFIX;

	private String placeholderSuffix = SystemPropertyUtils.PLACEHOLDER_SUFFIX;

	@Nullable
	private String valueSeparator = SystemPropertyUtils.VALUE_SEPARATOR;

	/**
	 * 必输属性集合
	 */
	private final Set<String> requiredProperties = new LinkedHashSet<>();


	@Override
	public ConfigurableConversionService getConversionService() {
		// Need to provide an independent DefaultConversionService, not the
		// shared DefaultConversionService used by PropertySourcesPropertyResolver.
		ConfigurableConversionService cs = this.conversionService;
		if (cs == null) {
			synchronized (this) {
				cs = this.conversionService;
				if (cs == null) {
					cs = new DefaultConversionService();
					this.conversionService = cs;
				}
			}
		}
		return cs;
	}

	@Override
	public void setConversionService(ConfigurableConversionService conversionService) {
		Assert.notNull(conversionService, "ConversionService must not be null");
		this.conversionService = conversionService;
	}


	@Override
	public void setPlaceholderPrefix(String placeholderPrefix) {
		Assert.notNull(placeholderPrefix, "'placeholderPrefix' must not be null");
		this.placeholderPrefix = placeholderPrefix;
	}


	@Override
	public void setPlaceholderSuffix(String placeholderSuffix) {
		Assert.notNull(placeholderSuffix, "'placeholderSuffix' must not be null");
		this.placeholderSuffix = placeholderSuffix;
	}


	@Override
	public void setValueSeparator(@Nullable String valueSeparator) {
		this.valueSeparator = valueSeparator;
	}


	@Override
	public void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders) {
		this.ignoreUnresolvableNestedPlaceholders = ignoreUnresolvableNestedPlaceholders;
	}


	@Override
	public void setRequiredProperties(String... requiredProperties) {
//		for (String key : requiredProperties) {
//			this.requiredProperties.add(key);
//		}
		// 优化如下：
		this.requiredProperties.addAll(Arrays.asList(requiredProperties));
	}

	@Override
	public void validateRequiredProperties() {
		MissingRequiredPropertiesException ex = new MissingRequiredPropertiesException();
		for (String key : this.requiredProperties) {
			if (this.getProperty(key) == null) {
				ex.addMissingRequiredProperty(key);
			}
		}
		if (!ex.getMissingRequiredProperties().isEmpty()) {
			throw ex;
		}
	}

	@Override
	public boolean containsProperty(String key) {
		return (getProperty(key) != null);
	}

	@Override
	@Nullable
	public String getProperty(String key) {
		return getProperty(key, String.class);
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		String value = getProperty(key);
		return (value != null ? value : defaultValue);
	}

	@Override
	public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
		T value = getProperty(key, targetType);
		return (value != null ? value : defaultValue);
	}

	@Override
	public String getRequiredProperty(String key) throws IllegalStateException {
		String value = getProperty(key);
		if (value == null) {
			throw new IllegalStateException("Required key '" + key + "' not found");
		}
		return value;
	}

	@Override
	public <T> T getRequiredProperty(String key, Class<T> valueType) throws IllegalStateException {
		T value = getProperty(key, valueType);
		if (value == null) {
			throw new IllegalStateException("Required key '" + key + "' not found");
		}
		return value;
	}

	@Override
	public String resolvePlaceholders(String text) {
		if (this.nonStrictHelper == null) {
			this.nonStrictHelper = createPlaceholderHelper(true);
		}
		return doResolvePlaceholders(text, this.nonStrictHelper);
	}

	@Override
	public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
		if (this.strictHelper == null) {
			this.strictHelper = createPlaceholderHelper(false);
		}
		return doResolvePlaceholders(text, this.strictHelper);
	}

	/**
	 * 解析占位符
	 * @since 3.2
	 * @see #setIgnoreUnresolvableNestedPlaceholders
	 */
	protected String resolveNestedPlaceholders(String value) {
		return (this.ignoreUnresolvableNestedPlaceholders ?
				resolvePlaceholders(value) : resolveRequiredPlaceholders(value));
	}

	/**
	 * 创建占位符解析器
	 * @param ignoreUnresolvablePlaceholders 是否忽略无法解析的占位符
	 * @return 占位符解析助手
	 */
	private PropertyPlaceholderHelper createPlaceholderHelper(boolean ignoreUnresolvablePlaceholders) {
		return new PropertyPlaceholderHelper(this.placeholderPrefix, this.placeholderSuffix,
				this.valueSeparator, ignoreUnresolvablePlaceholders);
	}

	/**
	 * 解析占位符
	 * @param text 给定文本
	 * @param helper 占位符解析助手
	 * @return 解析后文本
	 */
	private String doResolvePlaceholders(String text, PropertyPlaceholderHelper helper) {
		return helper.replacePlaceholders(text, this::getPropertyAsRawString);
	}

	/**
	 * 将给定值转换为指定类型
	 * @param value 原始的属性值
	 * @param targetType 属性值的指定类型
	 * @return 转换后的值，如果没有转换，则返回原始值
	 * @since 4.3.5
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	protected <T> T convertValueIfNecessary(Object value, @Nullable Class<T> targetType) {
		if (targetType == null) {
			return (T) value;
		}
		ConversionService conversionServiceToUse = this.conversionService;
		if (conversionServiceToUse == null) {
			// 如果不需要进行转换，直接强制转换，避免初始化 DefaultConversionService.
			if (ClassUtils.isAssignableValue(targetType, value)) {
				return (T) value;
			}
			conversionServiceToUse = DefaultConversionService.getSharedInstance();
		}
		return conversionServiceToUse.convert(value, targetType);
	}


	/**
	 * 获取指定的属性值(不处理值中的占位符)作为原始字符串
	 * @param key 属性名称
	 * @return 属性值，找不到时为 {@code null}
	 */
	@Nullable
	protected abstract String getPropertyAsRawString(String key);

}
