/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.utils;

public abstract class Callback<T> implements Runnable {

	protected T result = null;

	@Override
	public void run() {
		this.onComplete(this.getResult());
	}

	public Callback<T> setResult(final T result) {
		this.result = result;
		return this;
	}

	public T getResult() {
		return result;
	}

	protected abstract void onComplete(T result);
}
