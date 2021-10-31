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
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/** <em>Delta</em>Timer is a class which provides a {@link #getΔTimer() timer
 * for each thread that requests one}, and the returned timer provides the
 * current {@link #getΔTime() delta time}.
 * 
 * @since 1.0
 * @author Brian_Entei &lt;br45entei&#064;gmail.com&gt; */
public class ΔTimer {
	
	private static final double zero = 0x0.0p0;
	private static final double dividend = 1000000000.0D;
	
	private static final void updateFrameTime(ΔTimer δTimer) {
		δTimer.lastΔTime = δTimer.ΔTime;
		δTimer.lastFrameTime = δTimer.nanoTime;
		δTimer.nanoTime = System.nanoTime();
		
		double ΔTime = ((δTimer.nanoTime - δTimer.lastFrameTime) + zero) / dividend;
		δTimer.ΔTime = ΔTime != ΔTime || Double.isInfinite(ΔTime) ? zero : Math.abs(ΔTime);
	}
	
	private static final ConcurrentHashMap<WeakReference<Thread>, ΔTimer> instances = new ConcurrentHashMap<>();
	
	/** Obtains and returns a {@link ΔTimer} instance for the
	 * {@link Thread#currentThread() current thread}.
	 * 
	 * @return The ΔTimer instance for the current Thread */
	public static final ΔTimer getΔTimer() {
		Thread key = Thread.currentThread();
		for(Entry<WeakReference<Thread>, ΔTimer> entry : instances.entrySet()) {
			WeakReference<Thread> ref = entry.getKey();
			Thread thread = ref.get();
			if(thread == null) {
				instances.remove(ref);
				continue;
			}
			if(thread == key) {
				return entry.getValue();
			}
		}
		ΔTimer δTimer = new ΔTimer();
		instances.put(new WeakReference<>(key), δTimer);
		return δTimer;
	}
	
	private volatile long nanoTime = System.nanoTime();
	private volatile long lastFrameTime = this.nanoTime;
	private volatile double ΔTime = (((this.nanoTime = System.nanoTime()) - this.lastFrameTime) + zero) / dividend;
	private volatile double lastΔTime = this.ΔTime;
	
	private ΔTimer() {
		this.nanoTime = System.nanoTime();
		updateFrameTime(this);
	}
	
	/** Returns the Δ (delta) time, or the difference between the current
	 * {@link System#nanoTime() nanoTime()} and the last, divided by
	 * <tt>1000000000.0</tt>.
	 * 
	 * @param update Whether or not the Δ time should be updated
	 * @return The Δ time, or the difference between the current
	 *         <tt>nanoTime()</tt> and the last, divided by
	 *         <tt>1000000000.0</tt>.
	 * @see #getΔTime()
	 * @see #getLastΔTime() */
	public final double getΔTime(boolean update) {
		if(update) {
			updateFrameTime(this);
		}
		return this.ΔTime;
	}
	
	/** Updates and returns the Δ (delta) time, or the difference between the
	 * current {@link System#nanoTime() nanoTime()} and the last, divided by
	 * <tt>1000000000.0</tt>.
	 * 
	 * @return The Δ time, or the difference between the current
	 *         <tt>nanoTime()</tt> and the last, divided by
	 *         <tt>1000000000.0</tt>.
	 * @see #getΔTime(boolean)
	 * @see #getLastΔTime() */
	public final double getΔTime() {
		return this.getΔTime(true);
	}
	
	/** Returns the Δ (delta) time of the last frame from the frame before it.
	 * 
	 * @return The Δ time of the last frame from the frame before it.
	 * @see #getΔTime(boolean) */
	public final double getLastΔTime() {
		return this.lastΔTime;
	}
	
}
