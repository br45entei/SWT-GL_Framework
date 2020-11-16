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

/** Class used to allow threads to sleep the optimal amount of milliseconds per
 * frame to achieve a desired FPS / period.
 * 
 * @author Brian_Entei
 * @see FrequencyTimer#FrequencyTimer(double)
 * @see FrequencyTimer#FrequencyTimer(double, double)
 * @see FrequencyTimer#frequencySleep()
 * @see FrequencyTimer#setFrequency(double)
 * @see FrequencyTimer#setFrequency(double, double)
 * @see FrequencyTimer#getTargetFrequency()
 * @see FrequencyTimer#getWorkingFrequency()
 * @see FrequencyTimer#getTargetPeriodInMilliseconds() */
public class FrequencyTimer {
	
	/** @param args Program command line arguments */
	public static final void main(String[] args) {
		double frequency = 75.0;
		/*final double targetMPF = 1000 / frequency;
		long sleepPerFrame = Math.round(Math.floor(targetMPF)), totalMilliseconds = 0, frameCount = 0, mpf = 0;
		long lastSecond = System.currentTimeMillis();
		long startTime = System.currentTimeMillis(), elapsedTime = 0, now = startTime, sleepTime = 0, additionalSleepTotal = 0;*/
		
		FrequencyTimer timer = new FrequencyTimer(frequency);
		timer.setCallback(new TimerCallback() {
			@Override
			public void onTick() {
			}
			
			@Override
			public void onSecond() {
				System.out.println("FPS: ".concat(Long.toString(timer.frameCount)).concat("; Target FPS: ").concat(Double.toString(timer.originalFrequency)).concat("; MPFPS: ").concat(Long.toString(timer.totalMilliseconds)).concat("; Additional sleep this time: ").concat(Long.toString(timer.additionalSleepTotal)).concat("; Average MPF: ").concat(Double.toString((timer.totalMilliseconds + 0.0) / (timer.frameCount + 0.0))).concat("; Target MPF: ").concat(Double.toString(timer.actualTargetMPF)).concat(";"));
			}
		});
		//SecureRandom busyWork = new SecureRandom();
		while(true) {//for(long l = 0; ; l++) {
			
			/*if(busyWork.nextBoolean() || busyWork.nextBoolean()) {
				for(int j = 0; j <= Math.max(busyWork.nextInt(1000000), 1); j++) {
					busyWork.nextDouble();
					busyWork.nextInt(Math.max(busyWork.nextInt(), 1));
				}
				try {
					Thread.sleep(10);//Simulated "heavy" load
				} catch(InterruptedException ignored) {
					Thread.currentThread().interrupt();
				}
			}*/
			
			timer.frequencySleep();
			//if(l > frequency * 3) {
			//	timer.setFrequency(timer.lastFrameCount, timer.period);
			//}
			/*now = System.currentTimeMillis();
			elapsedTime = now - startTime;
			sleepTime = sleepPerFrame - elapsedTime;
			if(sleepTime > 0) {
				if(totalMilliseconds >= (sleepPerFrame * frequency)) {
					double framesRemaining = Math.round(Math.ceil(frequency)) - frameCount;
					if(framesRemaining > 0) {//1) {
						double sleepRemaining = 1000 - totalMilliseconds;
						long additionalSleep = Math.round(Math.ceil(sleepRemaining / framesRemaining));//At 60 FPS, this ought to be close to 10 milliseconds in the last four frames
						//if(totalMilliseconds + sleepTime + additionalSleep <= 1000) {
							sleepTime += additionalSleep;
							additionalSleepTotal += additionalSleep;
						//}
					}
				}
				try {
					Thread.sleep(sleepTime);
				} catch(InterruptedException ignored) {
					Thread.currentThread().interrupt();
				}
			}
			frameCount++;
			totalMilliseconds += (mpf = ((now = System.currentTimeMillis()) - startTime));
			startTime = now;
			if(now - lastSecond >= 1000L) {
				System.out.println("FPS: ".concat(Long.toString(frameCount)).concat("; MPFPS: ").concat(Long.toString(totalMilliseconds)).concat("; Additional sleep this time: ").concat(Long.toString(additionalSleepTotal)).concat("; Average MPF: ").concat(Double.toString((totalMilliseconds + 0.0) / (frameCount + 0.0))));
				lastSecond = (now = System.currentTimeMillis());
				frameCount = 0;
				totalMilliseconds = 0;
				additionalSleepTotal = 0;
				mpf = 0;
			}*/
		}
		
		//double mpfps = sleepPerFrame * frequency;
		//System.out.println(mpfps);
		
	}
	
	/** Interface for allowing API users to listen to time-sensitive events.
	 *
	 * @author Brian_Entei */
	public static interface TimerCallback {
		
		/** Called once per tick. */
		public void onTick();
		
		/** Called once per second. */
		public void onSecond();
		
	}
	
	protected volatile double frequency = 60.0,
			originalFrequency = this.frequency;
	private volatile double period = 1000.0;
	protected volatile double targetMPF = this.period / this.frequency,
			actualTargetMPF = this.period / this.originalFrequency;
	protected volatile long sleepPerFrame = Math.round(this.frequency > this.period ? Math.ceil(this.targetMPF) : Math.floor(this.targetMPF)),
			totalMilliseconds = 0, frameCount = 0, mpf = 0;
	private volatile long lastSecond = System.currentTimeMillis();
	protected volatile long startTime = System.currentTimeMillis(),
			elapsedTime = 0, now = this.startTime, sleepTime = 0,
			additionalSleepTotal = 0;
	private volatile long lastFrameCount = 0, lastTotalMilliseconds = 0,
			lastAdditionalSleepTotal = 0, lastMPF = 0;
	
	private volatile TimerCallback callback = null;
	
	public FrequencyTimer(double frequency, double period) {
		this.setFrequency(frequency, period);
	}
	
	public FrequencyTimer(double frequency) {
		this(frequency, 1000.0);
	}
	
	public final FrequencyTimer setFrequency(double frequency, double period) {
		this.period = period != period || Double.isInfinite(period) ? this.period : period;
		this.frequency = frequency != frequency || Double.isInfinite(frequency) ? this.frequency : frequency;
		this.originalFrequency = frequency != frequency || Double.isInfinite(frequency) ? this.frequency : frequency;
		this.actualTargetMPF = this.period / this.frequency;
		
		//if(this.frequency > this.period) {
		this.frequency += frequency < period ? ((1.0 / frequency) - 1.0) : (frequency > period ? ((this.frequency / this.period) - 1.0) : 0.0);//This actually removes that strange fps variance of +/- 1 here and there for "lower" frequencies! Holy moly :D ... at least for my CPU anyway ...
		//}
		this.targetMPF = this.period / this.frequency;
		this.sleepPerFrame = Math.round(this.frequency > this.period ? Math.ceil(this.targetMPF) : Math.floor(this.targetMPF));
		return this;
	}
	
	public final FrequencyTimer setFrequency(double frequency) {
		return this.setFrequency(frequency, this.period);
	}
	
	public final InterruptedException frequencySleep() {
		InterruptedException intEx = null;
		
		this.elapsedTime = (this.now = System.currentTimeMillis()) - (this.startTime + (System.currentTimeMillis() - this.now));
		this.sleepTime = this.sleepPerFrame - this.elapsedTime;
		if(this.sleepTime > 0) {
			long additionalSleep = 0;
			if(this.totalMilliseconds/* + (frequency / 10.0)*/ >= (this.sleepPerFrame * this.frequency)) {
				double framesRemaining = Math.round(Math.ceil(this.frequency)) - this.frameCount;
				if(framesRemaining > 0) {//1) {
					double sleepRemaining = this.period - this.totalMilliseconds;
					additionalSleep = Math.round(Math.ceil(sleepRemaining / framesRemaining));//At 60 FPS and 1000 ms, this ought to be close to 10 milliseconds in the last four frames
					if(additionalSleep > 0) {//if(totalMilliseconds + sleepTime + additionalSleep <= (this.period - (frequency / 10.0))) {
						this.sleepTime = additionalSleep;//this.sleepTime = framesRemaining == 1 ? additionalSleep : this.sleepTime + additionalSleep;//this.sleepTime += additionalSleep;
						this.additionalSleepTotal += additionalSleep;
					}//}
				}
			}
			if(this.sleepTime > 0) {
				double averageMPF = (this.totalMilliseconds + 0.0) / (this.frameCount + 0.0);
				if(averageMPF > this.targetMPF) {
					this.sleepTime -= 1;
					if(additionalSleep > 0) {
						additionalSleep -= 1;
						this.additionalSleepTotal -= 1;
					}
				}
			}
			if(((this.now = System.currentTimeMillis()) - (this.lastSecond + (System.currentTimeMillis() - this.now))) + this.sleepTime > this.period) {
				this.sleepTime = Math.round(this.period - this.totalMilliseconds);
				this.additionalSleepTotal -= additionalSleep;
				//this.additionalSleepTotal += this.sleepTime;
			}
			if(this.sleepTime > 0) {
				try {
					Thread.sleep(this.sleepTime);
				} catch(InterruptedException ex) {
					intEx = ex;
					Thread.currentThread().interrupt();
				}
			}
		}
		if(this.callback != null) {
			try {
				this.callback.onTick();
			} catch(Throwable ex) {
				ex.printStackTrace(System.err);
				System.err.flush();
				this.callback = null;
			}
		}
		this.frameCount++;
		this.totalMilliseconds += (this.mpf = ((this.now = System.currentTimeMillis()) - (this.startTime + (System.currentTimeMillis() - this.now))));
		this.startTime = this.now;
		if((this.now = System.currentTimeMillis()) - (this.lastSecond + (System.currentTimeMillis() - this.now)) >= this.period) {
			if(this.callback != null) {
				try {
					this.callback.onSecond();
				} catch(Throwable ex) {
					ex.printStackTrace(System.err);
					System.err.flush();
					this.callback = null;
				}
			}
			this.lastSecond = (this.now = System.currentTimeMillis());
			this.lastFrameCount = this.frameCount;
			this.frameCount = 0;
			this.lastTotalMilliseconds = this.totalMilliseconds;
			this.totalMilliseconds = 0;
			this.lastAdditionalSleepTotal = this.additionalSleepTotal;
			this.additionalSleepTotal = 0;
			this.lastMPF = this.mpf;
			this.mpf = 0;
		}
		
		return intEx;
	}
	
	public TimerCallback getCallback() {
		return this.callback;
	}
	
	public FrequencyTimer setCallback(TimerCallback callback) {
		this.callback = callback;
		return this;
	}
	
	public double getTargetFrequency() {
		return this.originalFrequency;
	}
	
	public double getWorkingFrequency() {
		return this.frequency;
	}
	
	public long getBaseTargetSleepPerFrame() {
		return this.sleepPerFrame;
	}
	
	public double getTargetSleepPerFrame() {
		return this.targetMPF;
	}
	
	public double getWorkingTargetSleepPerFrame() {
		return this.actualTargetMPF;
	}
	
	public long getLastMillisecondsPerFrame() {
		return this.lastMPF;
	}
	
	public long getCurrentMillisecondsPerFrame() {
		return this.mpf;
	}
	
	public long getLastMillisecondsPerFramePerPeriod() {
		return this.lastTotalMilliseconds;
	}
	
	public long getCurrentMillisecondsPerFramePerPeriod() {
		return this.totalMilliseconds;
	}
	
	public long getLastAdditionalSleepTotal() {
		return this.lastAdditionalSleepTotal;
	}
	
	public long getCurrentAdditionalSleepTotal() {
		return this.additionalSleepTotal;
	}
	
	public long getLastFrameCount() {
		return this.lastFrameCount;
	}
	
	public long getCurrentFrameCount() {
		return this.frameCount;
	}
	
	public double getTargetPeriodInMilliseconds() {
		return this.period;
	}
	
	public FrequencyTimer setTargetPeriodInMilliseconds(double period) {
		return this.setFrequency(this.originalFrequency, period);
	}
	
	public double getLastAverageMillisecondsPerFrame() {
		return (this.lastTotalMilliseconds + 0.0) / (this.lastFrameCount + 0.0);
	}
	
	public double getAverageMillisecondsPerFrame() {
		return (this.totalMilliseconds + 0.0) / (this.frameCount + 0.0);
	}
	
}
