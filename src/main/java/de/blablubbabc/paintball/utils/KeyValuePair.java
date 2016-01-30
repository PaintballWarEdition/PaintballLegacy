/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.utils;

import java.util.Map.Entry;

public class KeyValuePair implements Entry<String, String> {
	private final String key;
	private String value;
	
	public KeyValuePair(String key, String value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public final String getKey() {
		return key;
	}

	@Override
	public final String getValue() {
		return value;
	}

	@Override
	public final String setValue(String value) {
		this.value = value;
		return value;
	}
	
	@Override
	public final boolean equals(Object o) {
		if (!(o instanceof Entry)) return false;
		@SuppressWarnings("rawtypes")
		Entry e = (Entry) o;
		Object k1 = getKey();
		Object k2 = e.getKey();
		if (k1 == k2 || (k1 != null && k1.equals(k2))) {
			Object v1 = getValue();
			Object v2 = e.getValue();
			if (v1 == v2 || (v1 != null && v1.equals(v2))) return true;
		}
		return false;
	}
	
	@Override
	public final int hashCode() {
		return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
	}
	
	@Override
	public final String toString() {
		return getKey() + "=" + getValue();
	}
}
