/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core.env;

import static java.lang.String.format;
import static org.springframework.util.SystemPropertyUtils.PLACEHOLDER_PREFIX;
import static org.springframework.util.SystemPropertyUtils.PLACEHOLDER_SUFFIX;
import static org.springframework.util.SystemPropertyUtils.VALUE_SEPARATOR;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;

/**
 * Abstract base class for resolving properties against any underlying source.
 *
 * @author Chris Beams
 * @since 3.1
 */
public abstract class AbstractPropertyResolver implements ConfigurablePropertyResolver {

	protected final Log logger = LogFactory.getLog(getClass());

	protected ConfigurableConversionService conversionService = new DefaultConversionService();

	private PropertyPlaceholderHelper nonStrictHelper;
	private PropertyPlaceholderHelper strictHelper;
	private boolean ignoreUnresolvableNestedPlaceholders = false;

	private String placeholderPrefix = PLACEHOLDER_PREFIX;
	private String placeholderSuffix = PLACEHOLDER_SUFFIX;
	private String valueSeparator = VALUE_SEPARATOR;

	private final Set<String> requiredProperties = new LinkedHashSet<String>();

	@Override
	public ConfigurableConversionService getConversionService() {
		return this.conversionService;
	}

	@Override
	public void setConversionService(ConfigurableConversionService conversionService) {
		this.conversionService = conversionService;
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		String value = getProperty(key);
		return value == null ? defaultValue : value;
	}

	@Override
	public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
		T value = getProperty(key, targetType);
		return value == null ? defaultValue : value;
	}

	@Override
	public void setRequiredProperties(String... requiredProperties) {
		for (String key : requiredProperties) {
			this.requiredProperties.add(key);
		}
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
	public String getRequiredProperty(String key) throws IllegalStateException {
		String value = getProperty(key);
		if (value == null) {
			throw new IllegalStateException(format("required key [%s] not found", key));
		}
		return value;
	}

	@Override
	public <T> T getRequiredProperty(String key, Class<T> valueType) throws IllegalStateException {
		T value = getProperty(key, valueType);
		if (value == null) {
			throw new IllegalStateException(format("required key [%s] not found", key));
		}
		return value;
	}

	/**
	 * {@inheritDoc} The default is "${".
	 * @see org.springframework.util.SystemPropertyUtils#PLACEHOLDER_PREFIX
	 */
	@Override
	public void setPlaceholderPrefix(String placeholderPrefix) {
		this.placeholderPrefix = placeholderPrefix;
	}

	/**
	 * {@inheritDoc} The default is "}".
	 * @see org.springframework.util.SystemPropertyUtils#PLACEHOLDER_SUFFIX
	 */
	@Override
	public void setPlaceholderSuffix(String placeholderSuffix) {
		this.placeholderSuffix = placeholderSuffix;
	}

	/**
	 * {@inheritDoc} The default is ":".
	 * @see org.springframework.util.SystemPropertyUtils#VALUE_SEPARATOR
	 */
	@Override
	public void setValueSeparator(String valueSeparator) {
		this.valueSeparator = valueSeparator;
	}

	@Override
	public String resolvePlaceholders(String text) {
		if (nonStrictHelper == null) {
			nonStrictHelper = createPlaceholderHelper(true);
		}
		return doResolvePlaceholders(text, nonStrictHelper);
	}

	@Override
	public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
		if (strictHelper == null) {
			strictHelper = createPlaceholderHelper(false);
		}
		return doResolvePlaceholders(text, strictHelper);
	}

	/**
	 * {@inheritDoc}
	 * <p>The default value for this implementation is {@code false}.
	 * @since 3.2
	 */
	@Override
	public void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders) {
		this.ignoreUnresolvableNestedPlaceholders = ignoreUnresolvableNestedPlaceholders;
	}

	/**
	 * Resolve placeholders within the given string, deferring to the value of
	 * {@link #setIgnoreUnresolvableNestedPlaceholders(boolean)} to determine whether any
	 * unresolvable placeholders should raise an exception or be ignored.
	 * @since 3.2
	 * @see #setIgnoreUnresolvableNestedPlaceholders(boolean)
	 */
	protected String resolveNestedPlaceholders(String value) {
		return this.ignoreUnresolvableNestedPlaceholders ?
				this.resolvePlaceholders(value) :
				this.resolveRequiredPlaceholders(value);
	}

	private PropertyPlaceholderHelper createPlaceholderHelper(boolean ignoreUnresolvablePlaceholders) {
		return new PropertyPlaceholderHelper(this.placeholderPrefix, this.placeholderSuffix,
				this.valueSeparator, ignoreUnresolvablePlaceholders);
	}

	private String doResolvePlaceholders(String text, PropertyPlaceholderHelper helper) {
		return helper.replacePlaceholders(text, new PlaceholderResolver() {
			@Override
			public String resolvePlaceholder(String placeholderName) {
				return getProperty(placeholderName);
			}
		});
	}

}
