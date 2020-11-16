/*******************************************************************************
 * 
 * Copyright (C) 2020 Brian_Entei (br45entei@gmail.com)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 *******************************************************************************/
package com.gmail.br45entei.thread;

import java.lang.ref.WeakReference;
import java.security.SecureRandom;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/** A simple utility class which provides one unique {@link SecureRandom} for
 * each thread that requests one.
 *
 * @since 1.0
 * @author Brian_Entei */
public class SecureRandomProvider {
	
	private static final ConcurrentHashMap<WeakReference<Thread>, SecureRandom> threadedRandoms = new ConcurrentHashMap<>();
	private static final ConcurrentLinkedDeque<SecureRandom> recycledRandoms = new ConcurrentLinkedDeque<>();
	
	private static final void recycle(SecureRandom random) {
		if(random != null) {// This should never be null, but just in case ...
			recycledRandoms.add(random);
		}
	}
	
	private static final SecureRandom createOrRecycle() {
		SecureRandom recycled = recycledRandoms.pollFirst();
		if(recycled == null) {
			return new SecureRandom();
		}
		return recycled;
	}
	
	/** Returns a {@link SecureRandom} dedicated for the
	 * {@link Thread#currentThread() current thread}.<br>
	 * The same SecureRandom object is always returned for the same thread.
	 *
	 * @return A SecureRandom dedicated for the current thread. */
	public static final SecureRandom getSecureRandom() {
		final Thread key = Thread.currentThread();
		for(Entry<WeakReference<Thread>, SecureRandom> entry : threadedRandoms.entrySet()) {
			WeakReference<Thread> ref = entry.getKey();
			Thread check = ref.get();
			if(check == null) {
				recycle(threadedRandoms.remove(ref));
				continue;
			}
			if(check == key) {
				return entry.getValue();
			}
		}
		SecureRandom random = createOrRecycle();
		threadedRandoms.put(new WeakReference<>(key), random);
		return random;
	}
	
}
