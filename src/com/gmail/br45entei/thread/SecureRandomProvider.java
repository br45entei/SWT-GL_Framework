/*******************************************************************************
 * 
 * Copyright © 2021 Brian_Entei (br45entei@gmail.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
 * @author Brian_Entei &lt;br45entei&#064;gmail.com&gt; */
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
