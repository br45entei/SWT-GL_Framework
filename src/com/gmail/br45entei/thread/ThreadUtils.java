/*******************************************************************************
 * 
 * Copyright © 2022 Brian_Entei (br45entei@gmail.com)
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

import com.gmail.br45entei.util.CodeUtil;

import java.lang.reflect.Field;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** @author Brian_Entei &ltbr45entei&#064;gmail.com&gt; */
public class ThreadUtils {
	
	private static volatile ThreadGroup defaultThreadGroup = getDefaultThreadGroup();
	
	/** @param args Program command line arguments */
	public static final void main(String[] args) {
		Thread[] threads = getThreadsIn(Thread.currentThread());//.getThreadGroup());
		System.out.println("# of threads: " + threads.length);
		for(Thread thread : threads) {
			System.out.println(thread.getName());
		}
	}
	
	/** @param thread The thread whose group will be used
	 * @return The Threads in the given thread's group */
	public static final Thread[] getThreadsIn(Thread thread) {
		SecurityManager s = System.getSecurityManager();
		ThreadGroup group = (s != null && s.getThreadGroup() != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
		return getThreadsIn(thread == null || thread.getThreadGroup() == null ? group : thread.getThreadGroup());
	}
	
	/** @param group The thread group that will be used
	 * @return The Threads in the given group */
	public static final Thread[] getThreadsIn(ThreadGroup group) {
		try {
			Field threadsField = ThreadGroup.class.getDeclaredField("threads");
			threadsField.setAccessible(true);//Required!
			return CodeUtil.clean((Thread[]) threadsField.get(group));
		} catch(Throwable ignored) {
		}
		return new Thread[0];
	}
	
	/** @return The default JVM thread group */
	public static final ThreadGroup getDefaultThreadGroup() {
		if(defaultThreadGroup == null) {
			SecurityManager s = System.getSecurityManager();
			ThreadGroup parent = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			defaultThreadGroup = new ThreadGroup(parent, "CustomThreadGroup");//-" + poolNumber.get());
		}
		return defaultThreadGroup;
	}
	
	/** @author Brian_Entei &ltbr45entei&#064;gmail.com&gt; */
	public static final class CustomThreadFactory implements ThreadFactory {
		
		/** 8MB */
		public static final long eightMB = 1L << 23;
		/** 64MB */
		public static final long sixtyFourMB = 1L << 26L;
		/** 128MB */
		public static final long oneHundredTwentyEightMB = 1L << 27L;
		/** (64MB) */
		public static final long defaultStackSize = sixtyFourMB;
		
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final int _poolNumber;
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private volatile String poolPrefix;
		private volatile String threadPrefix;
		
		private final long stackSize;
		private final boolean daemon;
		
		/** Default constructor */
		public CustomThreadFactory() {
			this("pool-", "-thread-");
		}
		
		/** @param poolPrefix The thread pool's prefix that will be used for the
		 *            names of newly created threads
		 * @param threadPrefix The thread pool's middle prefix that will be used
		 *            for the names of newly created threads */
		public CustomThreadFactory(String poolPrefix, String threadPrefix) {
			this(poolPrefix, threadPrefix, null);
		}
		
		/** @param group The parent thread group to use when creating the child
		 *            thread group */
		public CustomThreadFactory(ThreadGroup group) {
			this("pool-", "-thread-", group, defaultStackSize);
		}
		
		/** @param daemon Whether or not the child thread group and all
		 *            resulting threads will be marked as daemon */
		public CustomThreadFactory(boolean daemon) {
			this("pool-", "-thread-", daemon);
		}
		
		/** @param group The parent thread group to use when creating the child
		 *            thread group
		 * @param daemon Whether or not the child thread group and all resulting
		 *            threads will be marked as daemon */
		public CustomThreadFactory(ThreadGroup group, boolean daemon) {
			this("pool-", "-thread-", group, daemon, defaultStackSize);
		}
		
		/** @param poolPrefix The thread pool's prefix that will be used for the
		 *            names of newly created threads
		 * @param threadPrefix The thread pool's middle prefix that will be used
		 *            for the names of newly created threads
		 * @param daemon Whether or not the child thread group and all resulting
		 *            threads will be marked as daemon */
		public CustomThreadFactory(String poolPrefix, String threadPrefix, boolean daemon) {
			this(poolPrefix, threadPrefix, daemon, defaultStackSize);
		}
		
		/** @param poolPrefix The thread pool's prefix that will be used for the
		 *            names of newly created threads
		 * @param threadPrefix The thread pool's middle prefix that will be used
		 *            for the names of newly created threads
		 * @param group The parent thread group to use when creating the child
		 *            thread group */
		public CustomThreadFactory(String poolPrefix, String threadPrefix, ThreadGroup group) {
			this(poolPrefix, threadPrefix, group, defaultStackSize);
		}
		
		/** @param poolPrefix The thread pool's prefix that will be used for the
		 *            names of newly created threads
		 * @param threadPrefix The thread pool's middle prefix that will be used
		 *            for the names of newly created threads
		 * @param group The parent thread group to use when creating the child
		 *            thread group
		 * @param daemon Whether or not the child thread group and all resulting
		 *            threads will be marked as daemon */
		public CustomThreadFactory(String poolPrefix, String threadPrefix, ThreadGroup group, boolean daemon) {
			this(poolPrefix, threadPrefix, group, daemon, defaultStackSize);
		}
		
		/** @param poolPrefix The thread pool's prefix that will be used for the
		 *            names of newly created threads
		 * @param threadPrefix The thread pool's middle prefix that will be used
		 *            for the names of newly created threads
		 * @param group The thread group to use, or null to have one created
		 * @param stackSize The desired stack size for newly created threads, or
		 *            zero to indicate that this parameter is to be ignored. */
		public CustomThreadFactory(String poolPrefix, String threadPrefix, ThreadGroup group, long stackSize) {
			poolPrefix.toString();
			threadPrefix.toString();
			if(group == null) {
				SecurityManager s = System.getSecurityManager();
				ThreadGroup parent = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
				group = new ThreadGroup(parent, "CustomThreadGroup-" + poolNumber.get());
			}
			this.group = group;
			this.daemon = this.group.isDaemon();
			this._poolNumber = poolNumber.getAndIncrement();
			this.poolPrefix = poolPrefix;
			this.threadPrefix = threadPrefix;
			this.stackSize = stackSize < eightMB ? (stackSize == 0L ? 0L : eightMB) : (stackSize > oneHundredTwentyEightMB ? oneHundredTwentyEightMB : stackSize);
		}
		
		/** @param poolPrefix The thread pool's prefix that will be used for the
		 *            names of newly created threads
		 * @param threadPrefix The thread pool's middle prefix that will be used
		 *            for the names of newly created threads
		 * @param daemon Whether or not the child thread group and all resulting
		 *            threads will be marked as daemon
		 * @param stackSize The desired stack size for newly created threads, or
		 *            zero to indicate that this parameter is to be ignored. */
		public CustomThreadFactory(String poolPrefix, String threadPrefix, boolean daemon, long stackSize) {
			this(poolPrefix, threadPrefix, "CustomThreadGroup-" + (poolNumber.get() + 1), daemon, stackSize);
		}
		
		private static final ThreadGroup initGroup(String threadGroupName, boolean daemon) {
			ThreadGroup group = new ThreadGroup(threadGroupName);
			group.setDaemon(daemon);
			return group;
		}
		
		/** @param poolPrefix The thread pool's prefix that will be used for the
		 *            names of newly created threads
		 * @param threadPrefix The thread pool's middle prefix that will be used
		 *            for the names of newly created threads
		 * @param threadGroupName The name to use for the child thread group
		 *            that will be created
		 * @param daemon Whether or not the child thread group and all resulting
		 *            threads will be marked as daemon
		 * @param stackSize The desired stack size for newly created threads, or
		 *            zero to indicate that this parameter is to be ignored. */
		public CustomThreadFactory(String poolPrefix, String threadPrefix, String threadGroupName, boolean daemon, long stackSize) {
			this(poolPrefix, threadPrefix, initGroup(threadGroupName, daemon), daemon, stackSize);
		}
		
		/** @param poolPrefix The thread pool's prefix that will be used for the
		 *            names of newly created threads
		 * @param threadPrefix The thread pool's middle prefix that will be used
		 *            for the names of newly created threads
		 * @param group The thread group to use, or null to have one created
		 * @param daemon Whether or not the child thread group and all resulting
		 *            threads will be marked as daemon
		 * @param stackSize The desired stack size for newly created threads, or
		 *            zero to indicate that this parameter is to be ignored. */
		public CustomThreadFactory(String poolPrefix, String threadPrefix, ThreadGroup group, boolean daemon, long stackSize) {
			this.daemon = daemon;
			poolPrefix.toString();
			threadPrefix.toString();
			if(group == null) {
				new NullPointerException().printStackTrace();
				SecurityManager s = System.getSecurityManager();
				ThreadGroup parent = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
				group = new ThreadGroup(parent, "CustomThreadGroup-" + poolNumber.get());
				group.setDaemon(daemon);
			}
			this.group = group;
			this._poolNumber = poolNumber.getAndIncrement();
			this.poolPrefix = poolPrefix;
			this.threadPrefix = threadPrefix;
			this.stackSize = stackSize < eightMB ? (stackSize == 0L ? 0L : eightMB) : (stackSize > oneHundredTwentyEightMB ? oneHundredTwentyEightMB : stackSize);
		}
		
		/** @return This thread factory's name */
		public final String getName() {
			return "CustomThreadFactory-" + this._poolNumber;
		}
		
		/** @return This thread factory's full thread prefix */
		public final String getThreadPrefix() {
			return this.poolPrefix + this._poolNumber + this.threadPrefix;
		}
		
		/** @return This thread factory's thread group */
		public final ThreadGroup getGroup() {
			return this.group;
		}
		
		/** @return Whether or not this thread factory creates daemon threads */
		public final boolean isDaemon() {
			return this.daemon;
		}
		
		/** @param poolPrefix The thread pool's prefix that will be used for the
		 *            names of newly created threads
		 * @param threadPrefix The thread pool's middle prefix that will be used
		 *            for the names of newly created threads
		 * @return A reference to this thread factory */
		public final CustomThreadFactory setThreadPrefix(String poolPrefix, String threadPrefix) {
			this.poolPrefix = poolPrefix == null ? "" : poolPrefix;
			this.threadPrefix = threadPrefix == null ? "" : threadPrefix;
			return this;
		}
		
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(this.group, r, this.getThreadPrefix() + this.threadNumber.getAndIncrement(), this.stackSize);
			if(t.isDaemon() != this.daemon) t.setDaemon(this.daemon);//if(t.isDaemon()) t.setDaemon(false);
			if(t.getPriority() != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
		
		/** @return The threads in this thread factory's thread group
		 * @see ThreadUtils#getThreadsIn(ThreadGroup) */
		public final Thread[] getThreads() {
			return getThreadsIn(this.group);
		}
		
	}
	
	/** @author Brian_Entei &ltbr45entei&#064;gmail.com&gt; */
	public static final class ExecutorGroup {
		
		protected final ThreadGroup threadGroup;
		private final CustomThreadFactory threadFactory;
		private final LinkedBlockingQueue<Runnable> threadWorkQueue = new LinkedBlockingQueue<>();
		private final boolean daemon;
		
		/** Default constructor */
		public ExecutorGroup() {
			this(new CustomThreadFactory());
		}
		
		/** @param factory The {@link CustomThreadFactory custom} thread factory
		 *            to use */
		public ExecutorGroup(CustomThreadFactory factory) {
			this.threadFactory = factory;
			this.threadGroup = this.threadFactory.getGroup();
			this.daemon = this.threadFactory.isDaemon();
		}
		
		/** @param poolPrefix The thread pool's prefix that will be used for the
		 *            names of newly created threads
		 * @param threadPrefix The thread pool's middle prefix that will be used
		 *            for the names of newly created threads */
		public ExecutorGroup(String poolPrefix, String threadPrefix) {
			this(poolPrefix, threadPrefix, null);
		}
		
		/** @param poolPrefix The thread pool's prefix that will be used for the
		 *            names of newly created threads
		 * @param threadPrefix The thread pool's middle prefix that will be used
		 *            for the names of newly created threads
		 * @param group The thread group that will be used with the custom
		 *            thread factory, or null to have it create one */
		public ExecutorGroup(String poolPrefix, String threadPrefix, ThreadGroup group) {
			this.threadFactory = new CustomThreadFactory(poolPrefix, threadPrefix, group);
			this.threadGroup = this.threadFactory.getGroup();
			this.daemon = this.threadFactory.isDaemon();
		}
		
		/** @param factory The {@link CustomThreadFactory custom} thread factory
		 *            to use
		 * @param poolPrefix The thread pool's middle prefix that will be used
		 *            for the names of newly created threads
		 * @param threadPrefix The thread pool's middle prefix that will be used
		 *            for the names of newly created threads */
		public ExecutorGroup(CustomThreadFactory factory, String poolPrefix, String threadPrefix) {
			this.threadFactory = factory;
			this.threadFactory.setThreadPrefix(poolPrefix, threadPrefix);
			this.threadGroup = this.threadFactory.getGroup();
			this.daemon = this.threadFactory.isDaemon();
		}
		
		/** @param daemon Whether or not the custom thread factory will create
		 *            daemon threads */
		public ExecutorGroup(boolean daemon) {
			this("workers", daemon);
		}
		
		/** @param threadGroupName The name to use for the custom thread
		 *            factory's child thread group that will be created
		 * @param daemon Whether or not the custom thread factory will create
		 *            daemon threads */
		public ExecutorGroup(String threadGroupName, boolean daemon) {
			this.threadGroup = new ThreadGroup(threadGroupName);
			this.threadGroup.setDaemon(daemon);
			this.daemon = daemon;
			this.threadFactory = new CustomThreadFactory(ExecutorGroup.this.threadGroup, this.daemon);
		}
		
		/** @param group The {@link CustomThreadFactory custom} thread factory
		 *            to use
		 * @param daemon Whether or not the custom thread factory will create
		 *            daemon threads */
		public ExecutorGroup(ThreadGroup group, boolean daemon) {
			this.threadGroup = group;
			this.threadGroup.setDaemon(daemon);
			this.daemon = daemon;
			this.threadFactory = new CustomThreadFactory(ExecutorGroup.this.threadGroup, this.daemon);
		}
		
		/** @return The custom thread factory's full thread prefix */
		public final String getThreadPrefix() {
			return this.threadFactory.getThreadPrefix();
		}
		
		/** @param poolPrefix The thread pool's prefix that will be used for the
		 *            names of newly created threads
		 * @param threadPrefix The thread pool's middle prefix that will be used
		 *            for the names of newly created threads
		 * @return A reference to this executor group */
		public final ExecutorGroup setThreadPrefix(String poolPrefix, String threadPrefix) {
			this.threadFactory.setThreadPrefix(poolPrefix, threadPrefix);
			return this;
		}
		
		/** @return This executor group's linked blocking work queue */
		public final BlockingQueue<Runnable> getWorkQueue() {
			return this.threadWorkQueue;
		}
		
		/** @param nThreads The number of threads to use for both the core pool
		 *            size and the maximum pool size
		 * @return The resulting thread pool executor service */
		public final ThreadPoolExecutor getExecutor(int nThreads) {
			return this.getExecutor(nThreads, nThreads);
		}
		
		/** @param corePoolSize The numver of threads to use for the core pool
		 *            size
		 * @param maximumPoolSize the number of threads to keep in the pool,
		 *            even if they are idle
		 * @return The resulting thread pool executor service */
		public final ThreadPoolExecutor getExecutor(int corePoolSize, int maximumPoolSize) {
			return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 0L, TimeUnit.MILLISECONDS, this.threadWorkQueue, this.threadFactory);
		}
		
	}
	
}
