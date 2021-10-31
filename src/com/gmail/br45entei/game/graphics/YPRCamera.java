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
package com.gmail.br45entei.game.graphics;

import com.gmail.br45entei.game.graphics.MatrixStack.MultiplicationOrder;
import com.gmail.br45entei.game.graphics.MatrixStack.RotationOrder;
import com.gmail.br45entei.game.graphics.Shader.Uniform;
import com.gmail.br45entei.game.input.Keyboard;
import com.gmail.br45entei.game.input.Keyboard.Keys;
import com.gmail.br45entei.game.input.Mouse;
import com.gmail.br45entei.game.math.MathUtil;
import com.gmail.br45entei.game.math.Matrix4f;
import com.gmail.br45entei.util.BitUtil;
import com.gmail.br45entei.util.BufferUtil;
import com.gmail.br45entei.util.CodeUtil;
import com.gmail.br45entei.util.FileUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.lwjgl.opengl.GL11;

/** A simple 3D camera implementation using Euler angles (yaw, pitch, and roll).
 *
 * @since 1.0
 * @author Brian_Entei &lt;br45entei&#064;gmail.com&gt; */
public class YPRCamera {
	
	public volatile int vpX = 0, vpY = 0, vpWidth = 800, vpHeight = 600;
	private volatile double fovy = 45.0/*70.0*/;
	public volatile double zNear = 0.01, zFar = 1000.0;
	private volatile double lastFovy = this.fovy, lastZNear = this.zNear,
			lastZFar = this.zFar;
	public volatile double x, y, z, zDist;
	public volatile double vX, vY, vZ, aX, aY, aZ;
	public volatile double tX = 75, tY = 75, tZ = 75;
	private volatile double lastX, lastY, lastZ, lastZDist;
	public volatile double yaw, pitch, roll;
	private volatile double lastYaw, lastPitch, lastRoll;
	private volatile double[] perspectiveProjectionMatrix = GLUtil.getPerspectiveMatrixd(this.fovy, (this.vpWidth + 0.0) / (this.vpHeight + 0.0), this.zNear, this.zFar);
	private volatile double[] orthographicProjectionMatrix = GLUtil.getOrthographicMatrixd(this.vpX, this.vpY, this.vpWidth, this.vpHeight, this.zNear, this.zFar);
	private final MatrixStack stack = new MatrixStack(4);
	private volatile double[] modelViewMatrix = this.stack.loadIdentity().push().translate(0, 0, -this.zDist).rotate(-this.yaw, -this.pitch, -this.roll, RotationOrder.ZXY, MultiplicationOrder.NEWxOLD).translate(-this.x, -this.y, -this.z, MultiplicationOrder.NEWxOLD).pop();
	
	private volatile boolean printCameraInfo = false;
	
	private volatile double targetFovy = this.fovy;
	private volatile double zoomFovy = (20.0 / 70.0) * this.fovy;
	private volatile boolean freeLook = false;
	private volatile boolean invertYawWhenUpsideDown = false;
	private volatile boolean invertPitchWhenUpsideDown = false;
	private volatile double mouseSensitivity = 0.15;
	private volatile boolean freeMove = false;
	private volatile boolean invertForwardMovementWhenUpsideDown = false;
	private volatile boolean invertVerticalMovementWhenUpsideDown = false;
	private volatile double movementSpeed = 1.2;
	
	/**
	 * 
	 */
	public YPRCamera() {
	}
	
	public double[] getPerspectiveProjectionMatrix() {
		return this.perspectiveProjectionMatrix;
	}
	
	public double[] getPerspectiveProjectionMatrix(double fovy, int width, int height, double zNear, double zFar) {
		return this.perspectiveProjectionMatrix = GLUtil.getPerspectiveMatrixd(this.fovy = fovy, ((this.vpWidth = width) + 0.0) / ((this.vpHeight = height) + 0.0), this.zNear = zNear, this.zFar = zFar);
	}
	
	public double[] getPerspectiveProjectionMatrix(double fovy, int width, int height) {
		return this.perspectiveProjectionMatrix = GLUtil.getPerspectiveMatrixd(this.fovy = fovy, ((this.vpWidth = width) + 0.0) / ((this.vpHeight = height) + 0.0), this.zNear, this.zFar);
	}
	
	public double[] getPerspectiveProjectionMatrix(int width, int height) {
		return this.perspectiveProjectionMatrix = GLUtil.getPerspectiveMatrixd(this.fovy, ((this.vpWidth = width) + 0.0) / ((this.vpHeight = height) + 0.0), this.zNear, this.zFar);
	}
	
	public double[] getPerspectiveProjectionMatrix(double fovy) {
		return this.perspectiveProjectionMatrix = GLUtil.getPerspectiveMatrixd(this.fovy = fovy, (this.vpWidth + 0.0) / (this.vpHeight + 0.0), this.zNear, this.zFar);
	}
	
	public float[] getPerspectiveProjectionMatrixf() {
		return GLUtil.asFloatArray(this.getPerspectiveProjectionMatrix());
	}
	
	public float[] getPerspectiveProjectionMatrixf(float fovy, int width, int height, float zNear, float zFar) {
		return GLUtil.asFloatArray(this.getPerspectiveProjectionMatrix(fovy, width, height, zNear, zFar));
	}
	
	public float[] getPerspectiveProjectionMatrixf(float fovy, int width, int height) {
		return GLUtil.asFloatArray(this.getPerspectiveProjectionMatrix(fovy, width, height, this.zNear, this.zFar));
	}
	
	public float[] getPerspectiveProjectionMatrixf(int width, int height) {
		return GLUtil.asFloatArray(this.getPerspectiveProjectionMatrix(this.fovy, width, height, this.zNear, this.zFar));
	}
	
	public float[] getPerspectiveProjectionMatrixf(float fovy) {
		return GLUtil.asFloatArray(this.getPerspectiveProjectionMatrix(fovy, this.vpWidth, this.vpHeight, this.zNear, this.zFar));
	}
	
	public double[] getOrthographicProjectionMatrix() {
		return this.orthographicProjectionMatrix;
	}
	
	public double[] getOrthographicProjectionMatrix(int x, int y, int width, int height, double zNear, double zFar) {
		return this.orthographicProjectionMatrix = GLUtil.getOrthographicMatrixd(this.vpX = x, this.vpY = y, this.vpWidth = width, this.vpHeight = height, this.zNear = zNear, this.zFar = zFar);
	}
	
	public double[] getOrthographicProjectionMatrix(int x, int y, int width, int height) {
		return this.getOrthographicProjectionMatrix(x, y, width, height, this.zNear, this.zFar);
	}
	
	public float[] getOrthographicProjectionMatrixf() {
		return GLUtil.asFloatArray(this.getOrthographicProjectionMatrix());
	}
	
	public float[] getOrthographicProjectionMatrixf(int x, int y, int width, int height, float zNear, float zFar) {
		return GLUtil.asFloatArray(this.getOrthographicProjectionMatrix(x, y, width, height, zNear, zFar));
	}
	
	public float[] getOrthographicProjectionMatrixf(int x, int y, int width, int height) {
		return GLUtil.asFloatArray(this.getOrthographicProjectionMatrix(x, y, width, height, this.zNear, this.zFar));
	}
	
	public double[] getModelViewMatrix() {
		return this.modelViewMatrix;
	}
	
	public double[] getModelViewMatrix(double x, double y, double z, double zDist, double yaw, double pitch, double roll) {
		//return this.modelViewMatrix = this.stack.loadIdentity().push().translate(0, 0, -(this.zDist = zDist)).rotate(-(this.yaw = yaw), -(this.pitch = pitch), -(this.roll = roll), RotationOrder.ZXY, MultiplicationOrder.NEWxOLD).translate(-(this.x = x), -(this.y = y), -(this.z = z), MultiplicationOrder.NEWxOLD).pop();
		return this.modelViewMatrix = GLUtil.translateMatrix4x4d(GLUtil.rotateZXYMatrix4x4d(GLUtil.translateMatrix4x4d(GLUtil.getIdentityd(), 0, 0, -(this.zDist = zDist)), -(this.yaw = yaw), -(this.pitch = pitch), -(this.roll = roll)), -(this.x = x), -(this.y = y), -(this.z = z));
	}
	
	public float[] getModelViewMatrixf() {
		return GLUtil.asFloatArray(this.getModelViewMatrix());
	}
	
	public YPRCamera input(double deltaTime) {
		double x = this.x, y = this.y, z = this.z, zDist = this.zDist;
		double yaw = this.yaw, pitch = this.pitch, roll = this.roll;
		if(Mouse.isCaptured() && Mouse.shouldIListenToClickEvents()) {
			double moveAmount = this.movementSpeed * deltaTime * (Keyboard.isKeyDown(Keys.VK_LCONTROL) ? 1.5 : 1.0);
			final boolean upsideDown = this.isUpsideDown();//this.modelView[4] < -0.0f;
			//final boolean pitchedOver = pitch > 90.0f && pitch < 270.0f;
			final boolean rolledOver = roll > 90.0f && roll < 270.0f;
			
			if(Keyboard.isKeyDown(Keys.VK_W)) {
				if(this.freeMove) {
					x -= this.modelViewMatrix[2] * moveAmount;
					y -= this.modelViewMatrix[6] * moveAmount;
					z -= this.modelViewMatrix[10] * moveAmount;
				} else {
					x += this.modelViewMatrix[8] * moveAmount * (upsideDown && this.invertForwardMovementWhenUpsideDown ? -1.0f : 1.0f);
					z -= this.modelViewMatrix[0] * moveAmount * (upsideDown && this.invertForwardMovementWhenUpsideDown ? -1.0f : 1.0f);
				}
			}
			if(Keyboard.isKeyDown(Keys.VK_S)) {
				if(this.freeMove) {
					x += this.modelViewMatrix[2] * moveAmount;
					y += this.modelViewMatrix[6] * moveAmount;
					z += this.modelViewMatrix[10] * moveAmount;
				} else {
					x -= this.modelViewMatrix[8] * moveAmount * (upsideDown && this.invertForwardMovementWhenUpsideDown ? -1.0f : 1.0f);
					z += this.modelViewMatrix[0] * moveAmount * (upsideDown && this.invertForwardMovementWhenUpsideDown ? -1.0f : 1.0f);
				}
			}
			if(Keyboard.isKeyDown(Keys.VK_A)) {
				if(this.freeMove) {
					x -= this.modelViewMatrix[0] * moveAmount;
					y -= this.modelViewMatrix[4] * moveAmount;
					z -= this.modelViewMatrix[8] * moveAmount;
				} else {
					x -= this.modelViewMatrix[0] * moveAmount;
					z -= this.modelViewMatrix[8] * moveAmount;
				}
			}
			if(Keyboard.isKeyDown(Keys.VK_D)) {
				if(this.freeMove) {
					x += this.modelViewMatrix[0] * moveAmount;
					y += this.modelViewMatrix[4] * moveAmount;
					z += this.modelViewMatrix[8] * moveAmount;
				} else {
					x += this.modelViewMatrix[0] * moveAmount;
					z += this.modelViewMatrix[8] * moveAmount;
				}
			}
			if(Keyboard.isKeyDown(Keys.VK_SPACE)) {
				if(this.freeMove) {
					x += this.modelViewMatrix[1] * moveAmount;
					y += this.modelViewMatrix[5] * moveAmount;
					z += this.modelViewMatrix[9] * moveAmount;
				} else {
					y += moveAmount * (upsideDown && this.invertVerticalMovementWhenUpsideDown ? -1.0f : 1.0f);
				}
			}
			if(Keyboard.isKeyDown(Keys.VK_LSHIFT)) {
				if(this.freeMove) {
					x -= this.modelViewMatrix[1] * moveAmount;
					y -= this.modelViewMatrix[5] * moveAmount;
					z -= this.modelViewMatrix[9] * moveAmount;
				} else {
					y -= moveAmount * (upsideDown && this.invertVerticalMovementWhenUpsideDown ? -1.0f : 1.0f);
				}
			}
			
			java.awt.Point wheelDxy = Mouse.getWheelΔXY(true);
			zDist -= wheelDxy.y / 3;
			
			java.awt.Point dxy = Mouse.getΔXY(false);
			double ΔY = dxy.x * this.mouseSensitivity * (this.fovy / this.targetFovy) * (upsideDown && this.invertYawWhenUpsideDown ? -1.0f : 1.0f);
			double ΔP = dxy.y * this.mouseSensitivity * (this.fovy / this.targetFovy) * ((rolledOver && this.invertPitchWhenUpsideDown) ? -1.0f : 1.0f);
			
			double r = Math.toRadians(roll);
			//double rO = Math.toRadians((roll + 180.0) % 360.0);
			double rc = Math.cos(r);
			double rOc = Math.copySign(1.0 - Math.abs(rc), rc);//(Math.cos(rO) + 1.0) / 2.0;
			//double rs = Math.sin(r);
			//double psign = Math.copySign(1.0, rs);
			//double ysign = Math.copySign(1.0, rc);
			//double effectiveYawIncrease = ((ΔY * rc) - (ΔP * rs)) * ysign;
			//double effectivePitchIncrease = ((ΔP * rc) - (ΔY * rs));// * psign;
			double effectiveYawIncrease = ((ΔY * rc) - (ΔP * rOc));
			double effectivePitchIncrease = ((ΔP * rc) - (ΔY * rOc));
			
			yaw += effectiveYawIncrease;
			pitch += effectivePitchIncrease;
			
			//if(this.freeLook) {
			if(Keyboard.isKeyDown(Keys.VK_OPEN_BRACKET)) {
				roll -= deltaTime * this.mouseSensitivity * (this.fovy / this.targetFovy) * 100.0f;
			}
			if(Keyboard.isKeyDown(Keys.VK_CLOSE_BRACKET)) {
				roll += deltaTime * this.mouseSensitivity * (this.fovy / this.targetFovy) * 100.0f;
			}
			//}
			
			if(Keyboard.getKeyDown(Keys.VK_Z)) {
				this.fovy = this.zoomFovy;
			}
			if(Keyboard.getKeyUp(Keys.VK_Z)) {
				this.fovy = this.targetFovy;
			}
			
			if(Mouse.isButtonDown(Mouse.BUTTON_MIDDLE)) {
				zDist = 0;
			}
			if(Keyboard.isKeyDown(Keys.VK_R)) {
				boolean resetAll = Keyboard.isKeyDown(Keys.VK_RSHIFT);
				if(resetAll) {
					this.aX = this.aY = this.aZ = this.vX = this.vY = this.vZ = 0x0.0p0;
				}
				x = y = z = yaw = pitch = roll = 0;
				this.resetCameraFields();
				if(resetAll) {
					this.mouseSensitivity = 0.15;
					this.movementSpeed = 1.2;
					this.aX = this.aY = this.aZ = this.vX = this.vY = this.vZ = 0x0.0p0;
				}
			}
		}
		
		yaw = (360.0f + yaw) % 360.0f;
		pitch = (360.0f + pitch) % 360.0f;
		roll = (360.0f + roll) % 360.0f;
		if(!this.freeLook) {
			pitch = pitch > 90.0f && pitch <= 180.0f ? 90.0f : (pitch < 270.0f && pitch > 180.0f ? 270.0f : pitch);
			if(roll > 180.0f) {
				roll -= 360.0f;
				roll *= 0.95f;
				roll += 360.0f;
			} else {
				roll *= 0.95f;
			}
			roll = (360.0f + roll) % 360.0f;
			roll = Math.abs(roll) < 0.01f || Math.abs(roll) > 359.99f ? 0.0f : roll;
		}
		
		//Using the stack from the display thread and the GLThread at the same time causes the rendered frame to sometimes flicker about, so we won't do that.
		//this.modelViewMatrix = this.stack.modelView(this.x, this.y, this.z, this.yaw, this.pitch, this.roll).peek();
		if((x != this.lastX || y != this.lastY || z != this.lastZ || zDist != this.lastZDist) || (yaw != this.lastYaw || pitch != this.lastPitch || roll != this.lastRoll)) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.zDist = zDist;
			this.yaw = yaw;
			this.pitch = pitch;
			this.roll = roll;
		}
		
		return this;
	}
	
	public static final double cleanDouble(double d) {
		return d != d || Double.isInfinite(d) ? 0x0.0p0 : d;
	}
	
	public YPRCamera update(double deltaTime) {
		double aspect = (this.vpWidth + 0.0) / (this.vpHeight + 0.0);
		this.perspectiveProjectionMatrix = GLUtil.getPerspectiveMatrixd(this.fovy, aspect, this.zNear, this.zFar);
		this.orthographicProjectionMatrix = GLUtil.getOrthographicMatrixd(this.vpX, this.vpY, this.vpWidth, this.vpHeight, this.zNear, this.zFar);
		this.modelViewMatrix = GLUtil.translateMatrix4x4d(GLUtil.rotateZXYMatrix4x4d(GLUtil.translateMatrix4x4d(GLUtil.getIdentityd(), 0, 0, -this.zDist), -this.yaw, -this.pitch, -this.roll), -this.x, -this.y, -this.z);
		//this.modelViewMatrix = this.stack.loadIdentity().translate(0, 0, -this.zDist).rotate(-this.yaw, -this.pitch, -this.roll, RotationOrder.ZXY, MultiplicationOrder.NEWxOLD).translate(-this.x, -this.y, -this.z, MultiplicationOrder.NEWxOLD).peek();
		
		boolean printNow = false;
		long now = System.currentTimeMillis();
		if(now - this.lastSecond >= 1000L) {
			printNow = true;
		}
		
		if((this.fovy != this.lastFovy || this.zNear != this.lastZNear || this.zFar != this.lastZFar) || (this.x != this.lastX || this.y != this.lastY || this.z != this.lastZ || this.zDist != this.lastZDist) || (this.yaw != this.lastYaw || this.pitch != this.lastPitch || this.roll != this.lastRoll)) {
			this.lastFovy = this.fovy;
			this.lastZNear = this.zNear;
			this.lastZFar = this.zFar;
			this.lastX = this.x;
			this.lastY = this.y;
			this.lastZ = this.z;
			this.lastZDist = this.zDist;
			this.lastYaw = this.yaw;
			this.lastPitch = this.pitch;
			this.lastRoll = this.roll;
			this.lastSecond = now;
			printNow = true;
		}
		
		if(printNow && this.printCameraInfo) {
			this.lastSecond = now;
			System.out.println("===================================");
			System.out.println(String.format("X: %s; Y: %s; Z: %s;", MathUtil.limitDecimalNoRounding(this.x, 4, true), MathUtil.limitDecimalNoRounding(this.y, 4, true), MathUtil.limitDecimalNoRounding(this.z, 4, true)));
			System.out.println("===================================");
			System.out.println(String.format("Yaw: %s; Pitch: %s; Roll: %s;", MathUtil.limitDecimalNoRounding(this.yaw, 4, true), MathUtil.limitDecimalNoRounding(this.pitch, 4, true), MathUtil.limitDecimalNoRounding(this.roll, 4, true)));
			System.out.println(GLUtil.matrix4x4ToStringd(this.modelViewMatrix, 8, true));
		}
		
		//Make sure the terminal velocity is positive
		this.tX = cleanDouble(Math.abs(this.tX));
		this.tY = cleanDouble(Math.abs(this.tY));
		this.tZ = cleanDouble(Math.abs(this.tZ));
		
		//Increase the velocity by the current acceleration, capped to the terminal velocity:
		this.vX = cleanDouble(Math.min(this.tX, this.vX + cleanDouble(this.aX * deltaTime)));
		this.vY = cleanDouble(Math.min(this.tY, this.vY + cleanDouble(this.aY * deltaTime)));
		this.vZ = cleanDouble(Math.min(this.tZ, this.vZ + cleanDouble(this.aZ * deltaTime)));
		
		//Add the velocity to this camera's position:
		this.x += cleanDouble(Math.min(this.tX, this.vX * deltaTime));
		this.y += cleanDouble(Math.min(this.tY, this.vY * deltaTime));
		this.z += cleanDouble(Math.min(this.tZ, this.vZ * deltaTime));
		
		return this;
	}
	
	//===================================================================================================================================================================
	
	private volatile long lastSecond = 0L;
	
	public double[] getForward() {
		return new double[] {-this.modelViewMatrix[2], -this.modelViewMatrix[6], -this.modelViewMatrix[10]};
	}
	
	public float[] getForwardf() {
		return GLUtil.asFloatArray(this.getForward());
	}
	
	public double[] getBackward() {
		return new double[] {this.modelViewMatrix[2], this.modelViewMatrix[6], this.modelViewMatrix[10]};
	}
	
	public float[] getBackwardf() {
		return GLUtil.asFloatArray(this.getBackward());
	}
	
	public double[] getLeft() {
		return new double[] {-this.modelViewMatrix[0], -this.modelViewMatrix[4], -this.modelViewMatrix[8]};
	}
	
	public float[] getLeftf() {
		return GLUtil.asFloatArray(this.getLeft());
	}
	
	public double[] getRight() {
		return new double[] {this.modelViewMatrix[0], this.modelViewMatrix[4], this.modelViewMatrix[8]};
	}
	
	public float[] getRightf() {
		return GLUtil.asFloatArray(this.getRight());
	}
	
	public double[] getUp() {
		return new double[] {this.modelViewMatrix[1], this.modelViewMatrix[5], this.modelViewMatrix[9]};
	}
	
	public float[] getUpf() {
		return GLUtil.asFloatArray(this.getUp());
	}
	
	public double[] getDown() {
		return new double[] {-this.modelViewMatrix[1], -this.modelViewMatrix[5], -this.modelViewMatrix[9]};
	}
	
	public float[] getDownf() {
		return GLUtil.asFloatArray(this.getDown());
	}
	
	public YPRCamera moveForward(double deltaTime) {
		double moveAmount = this.movementSpeed * deltaTime;
		if(this.freeMove) {
			this.x -= this.modelViewMatrix[2] * moveAmount;
			this.y -= this.modelViewMatrix[6] * moveAmount;
			this.z -= this.modelViewMatrix[10] * moveAmount;
		} else {
			moveAmount *= (this.isUpsideDown() && this.invertForwardMovementWhenUpsideDown ? -1.0f : 1.0f);
			this.x += this.modelViewMatrix[8] * moveAmount;
			this.z -= this.modelViewMatrix[0] * moveAmount;
		}
		return this;
	}
	
	public YPRCamera moveBackward(double deltaTime) {
		double moveAmount = this.movementSpeed * deltaTime;
		if(this.freeMove) {
			this.x += this.modelViewMatrix[2] * moveAmount;
			this.y += this.modelViewMatrix[6] * moveAmount;
			this.z += this.modelViewMatrix[10] * moveAmount;
		} else {
			moveAmount *= (this.isUpsideDown() && this.invertForwardMovementWhenUpsideDown ? -1.0f : 1.0f);
			this.x -= this.modelViewMatrix[8] * moveAmount;
			this.z += this.modelViewMatrix[0] * moveAmount;
		}
		return this;
	}
	
	public YPRCamera moveLeft(double deltaTime) {
		double moveAmount = this.movementSpeed * deltaTime;
		if(this.freeMove) {
			this.x -= this.modelViewMatrix[0] * moveAmount;
			this.y -= this.modelViewMatrix[4] * moveAmount;
			this.z -= this.modelViewMatrix[8] * moveAmount;
		} else {
			this.x -= this.modelViewMatrix[0] * moveAmount;
			this.z -= this.modelViewMatrix[8] * moveAmount;
		}
		return this;
	}
	
	public YPRCamera moveRight(double deltaTime) {
		double moveAmount = this.movementSpeed * deltaTime;
		if(this.freeMove) {
			this.x += this.modelViewMatrix[0] * moveAmount;
			this.y += this.modelViewMatrix[4] * moveAmount;
			this.z += this.modelViewMatrix[8] * moveAmount;
		} else {
			this.x += this.modelViewMatrix[0] * moveAmount;
			this.z += this.modelViewMatrix[8] * moveAmount;
		}
		return this;
	}
	
	public YPRCamera moveUp(double deltaTime) {
		double moveAmount = this.movementSpeed * deltaTime;
		if(this.freeMove) {
			this.x += this.modelViewMatrix[1] * moveAmount;
			this.y += this.modelViewMatrix[5] * moveAmount;
			this.z += this.modelViewMatrix[9] * moveAmount;
		} else {
			this.y += moveAmount * (this.isUpsideDown() && this.invertVerticalMovementWhenUpsideDown ? -1.0f : 1.0f);
		}
		return this;
	}
	
	public YPRCamera moveDown(double deltaTime) {
		double moveAmount = this.movementSpeed * deltaTime;
		if(this.freeMove) {
			this.x -= this.modelViewMatrix[1] * moveAmount;
			this.y -= this.modelViewMatrix[5] * moveAmount;
			this.z -= this.modelViewMatrix[9] * moveAmount;
		} else {
			this.y -= moveAmount * (this.isUpsideDown() && this.invertVerticalMovementWhenUpsideDown ? -1.0f : 1.0f);
		}
		return this;
	}
	
	public YPRCamera resetCameraFields() {
		this.x = this.y = this.z = this.zDist = this.yaw = this.pitch = this.roll = 0;
		this.zNear = 0.01f;
		this.zFar = 1000.0f;
		this.fovy = this.fovy == this.targetFovy || this.fovy == this.zoomFovy ? this.fovy : this.targetFovy;
		return this;
	}
	
	/** @return This camera's field of view setting */
	public final double getFovy() {
		return this.fovy;
	}
	
	/** @param fovy The new field of view setting for this camera
	 * @return This YPRCamera */
	public final YPRCamera setFovy(double fovy) {
		this.fovy = fovy != fovy || Double.isInfinite(fovy) ? this.fovy : fovy;
		this.zoomFovy = (20.0 / 70.0) * this.fovy;
		return this;
	}
	
	/** @return This camera's target field of view setting */
	public final double getTargetFovy() {
		return this.targetFovy;
	}
	
	public final YPRCamera setTargetFovy(double fovy) {
		this.targetFovy = fovy != fovy || Double.isInfinite(fovy) ? this.targetFovy : fovy;
		return this;
	}
	
	/** Returns whether or not camera info is printed every second, as well as
	 * when it changes.
	 * 
	 * @return Whether or not camera info is printed every second and as it
	 *         changes */
	public boolean isPrintCameraInfoEnabled() {
		return this.printCameraInfo;
	}
	
	/** Sets whether or not this {@link YPRCamera}'s info is printed every
	 * second, as well as when it changes.
	 * 
	 * @param printCameraInfo Whether or not camera info is printed
	 * @return This YPRCamera */
	public YPRCamera setPrintCameraInfo(boolean printCameraInfo) {
		this.printCameraInfo = printCameraInfo;
		return this;
	}
	
	public double getMouseSensitivity() {
		return this.mouseSensitivity;
	}
	
	public YPRCamera setMouseSensitivity(double mouseSensitivity) {
		mouseSensitivity = mouseSensitivity != mouseSensitivity || Double.isInfinite(mouseSensitivity) ? this.mouseSensitivity : mouseSensitivity;
		
		this.mouseSensitivity = Math.min(1.0, Math.max(-1.0, mouseSensitivity));
		return this;
	}
	
	public boolean isFreeLookEnabled() {
		return this.freeLook;
	}
	
	public YPRCamera setFreeLookEnabled(boolean freeLook) {
		this.freeLook = freeLook;
		return this;
	}
	
	public YPRCamera toggleFreeLook() {
		return this.setFreeLookEnabled(!this.isFreeLookEnabled());
	}
	
	public boolean isInvertYawWhileUpsideDownEnabled() {
		return this.invertYawWhenUpsideDown;
	}
	
	public YPRCamera setInvertYawWhileUpsideDownEnabled(boolean invertYawWhenUpsideDown) {
		this.invertYawWhenUpsideDown = invertYawWhenUpsideDown;
		return this;
	}
	
	public YPRCamera toggleInvertYawWhileUpsideDown() {
		return this.setInvertYawWhileUpsideDownEnabled(!this.isInvertYawWhileUpsideDownEnabled());
	}
	
	public boolean isInvertPitchWhileUpsideDownEnabled() {
		return this.invertPitchWhenUpsideDown;
	}
	
	public YPRCamera setInvertPitchWhileUpsideDownEnabled(boolean invertPitchWhenUpsideDown) {
		this.invertPitchWhenUpsideDown = invertPitchWhenUpsideDown;
		return this;
	}
	
	public YPRCamera toggleInvertPitchWhileUpsideDown() {
		return this.setInvertPitchWhileUpsideDownEnabled(!this.isInvertPitchWhileUpsideDownEnabled());
	}
	
	public double getMovementSpeed() {
		return this.movementSpeed;
	}
	
	public YPRCamera setMovementSpeed(double movementSpeed) {
		this.movementSpeed = movementSpeed != movementSpeed || Double.isInfinite(movementSpeed) ? this.movementSpeed : movementSpeed;
		return this;
	}
	
	public boolean isFreeMoveEnabled() {
		return this.freeMove;
	}
	
	public YPRCamera setFreeMoveEnabled(boolean freeMove) {
		this.freeMove = freeMove;
		return this;
	}
	
	public YPRCamera toggleFreeMove() {
		return this.setFreeMoveEnabled(!this.isFreeMoveEnabled());
	}
	
	public boolean isInvertForwardMovementWhileUpsideDownEnabled() {
		return this.invertForwardMovementWhenUpsideDown;
	}
	
	public YPRCamera setInvertForwardMovementWhileUpsideDownEnabled(boolean invertForwardMovementWhenUpsideDown) {
		this.invertForwardMovementWhenUpsideDown = invertForwardMovementWhenUpsideDown;
		return this;
	}
	
	public YPRCamera toggleInvertForwardMovementWhileUpsideDown() {
		return this.setInvertForwardMovementWhileUpsideDownEnabled(!this.isInvertForwardMovementWhileUpsideDownEnabled());
	}
	
	public boolean isInvertVerticalMovementWhileUpsideDownEnabled() {
		return this.invertVerticalMovementWhenUpsideDown;
	}
	
	public YPRCamera setInvertVerticalMovementWhileUpsideDownEnabled(boolean invertVerticalMovementWhenUpsideDown) {
		this.invertVerticalMovementWhenUpsideDown = invertVerticalMovementWhenUpsideDown;
		return this;
	}
	
	public YPRCamera toggleInvertVerticalMovementWhileUpsideDown() {
		return this.setInvertVerticalMovementWhileUpsideDownEnabled(!this.isInvertVerticalMovementWhileUpsideDownEnabled());
	}
	
	public boolean isUpsideDown() {
		return Float.parseFloat(MathUtil.limitDecimalNoRounding(this.modelViewMatrix[5], 4)) < -0.0f;//return this.pitch > 90.0f && this.pitch < 270.0f;
	}
	
	//===================================================================================================================================================================
	
	public float[] getPositionf() {
		return new float[] {(float) this.x, (float) this.y, (float) this.z};
	}
	
	public double[] getPositiond() {
		return new double[] {this.x, this.y, this.z};
	}
	
	public float[] getPositionOffsetf(float offset) {
		float[] posOffset = new float[3];
		float rX = Math.round((float) this.x);
		float rY = Math.round((float) this.y);
		float rZ = Math.round((float) this.z);
		if(rX > this.x) {
			posOffset[0] = (float) (rX - this.x) + offset;
		} else {
			posOffset[0] = (float) (this.x - Math.floor(this.x)) + offset;
		}
		if(rY > this.y) {
			posOffset[1] = (float) (rY - this.y) + offset;
		} else {
			posOffset[1] = (float) (this.y - Math.floor(this.y)) + offset;
		}
		if(rZ > this.z) {
			posOffset[2] = (float) (rZ - this.z) + offset;
		} else {
			posOffset[2] = (float) (this.z - Math.floor(this.z)) + offset;
		}
		
		return posOffset;
	}
	
	public float[] getPositionOffsetf() {
		return this.getPositionOffsetf(0.0f);
	}
	
	public double[] getPositionOffsetd(double offset) {
		double[] posOffset = new double[3];
		//double rX = Math.round(this.x);
		//double rY = Math.round(this.y);
		//double rZ = Math.round(this.z);
		//if(rX > this.x) {
		//	posOffset[0] = (rX - this.x) + offset;
		//} else {
		posOffset[0] = (this.x - Math.floor(this.x)) + offset;
		//}
		//if(rY > this.y) {
		//	posOffset[1] = (rY - this.y) + offset;
		//} else {
		posOffset[1] = (this.y - Math.floor(this.y)) + offset;
		//}
		//if(rZ > this.z) {
		//	posOffset[2] = (rZ - this.z) + offset;
		//} else {
		posOffset[2] = (this.z - Math.floor(this.z)) + offset;
		//}
		
		return posOffset;
	}
	
	public double[] getPositionOffsetd() {
		return this.getPositionOffsetd(0.0);
	}
	
	public static double[] getPositionOffsetd(double[] xyz, double offset) {
		double[] positionOffset = new double[xyz.length];
		for(int i = 0; i < xyz.length; i++) {
			positionOffset[i] = (xyz[i] - Math.floor(xyz[i])) + offset;
		}
		return positionOffset;
	}
	
	//===================================================================================================================================================================
	
	public YPRCamera glShaderLookThrough(boolean perspectiveOrOrthographic, PerspectiveShader shader) {
		shader.setProjection(perspectiveOrOrthographic ? this.getPerspectiveProjectionMatrixf() : this.getOrthographicProjectionMatrixf());
		shader.setModelView(/*perspectiveOrOrthographic ? */this.getModelViewMatrixf()/* : GLUtil.getIdentityf()*/);
		return this;
	}
	
	public YPRCamera glShaderLookThroughFloatArray(boolean perspectiveOrOrthographic, Shader shader, String projectionUniformName, String modelViewUniformName) {
		Uniform<float[]> projection = shader.getUniform(projectionUniformName, float[].class);
		Uniform<float[]> modelView = shader.getUniform(modelViewUniformName, float[].class);
		if(projection != null) {
			projection.glSetValue(perspectiveOrOrthographic ? this.getPerspectiveProjectionMatrixf() : this.getOrthographicProjectionMatrixf());
		}
		if(modelView != null) {
			modelView.glSetValue(this.getModelViewMatrixf());
		}
		return this;
	}
	
	public YPRCamera glShaderLookThroughMatrix4f(boolean perspectiveOrOrthographic, Shader shader, String projectionUniformName, String modelViewUniformName) {
		Uniform<Matrix4f> projection = shader.getUniform(projectionUniformName, Matrix4f.class);
		Uniform<Matrix4f> modelView = shader.getUniform(modelViewUniformName, Matrix4f.class);
		if(projection != null) {
			projection.glSetValue(new Matrix4f(perspectiveOrOrthographic ? this.getPerspectiveProjectionMatrixf() : this.getOrthographicProjectionMatrixf()));
		}
		if(modelView != null) {
			modelView.glSetValue(new Matrix4f(this.getModelViewMatrixf()));
		}
		return this;
	}
	
	public YPRCamera glShaderLookThroughLWJGLMatrix4f(boolean perspectiveOrOrthographic, Shader shader, String projectionUniformName, String modelViewUniformName) {
		Uniform<org.lwjgl.util.vector.Matrix4f> projection = shader.getUniform(projectionUniformName, org.lwjgl.util.vector.Matrix4f.class);
		Uniform<org.lwjgl.util.vector.Matrix4f> modelView = shader.getUniform(modelViewUniformName, org.lwjgl.util.vector.Matrix4f.class);
		if(projection != null) {
			org.lwjgl.util.vector.Matrix4f mat4 = new org.lwjgl.util.vector.Matrix4f();
			mat4.store(BufferUtil.wrap(perspectiveOrOrthographic ? this.getPerspectiveProjectionMatrixf() : this.getOrthographicProjectionMatrixf()));
			projection.glSetValue(mat4);
		}
		if(modelView != null) {
			org.lwjgl.util.vector.Matrix4f mat4 = new org.lwjgl.util.vector.Matrix4f();
			mat4.store(BufferUtil.wrap(this.getModelViewMatrixf()));
			modelView.glSetValue(mat4);
		}
		return this;
	}
	
	@Deprecated
	public YPRCamera glLegacyLookThrough(boolean perspectiveOrOrthographic) {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadMatrixf(perspectiveOrOrthographic ? this.getPerspectiveProjectionMatrixf() : this.getOrthographicProjectionMatrixf());
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		//if(perspectiveOrOrthographic) {
		//	GL11.glLoadMatrixf(this.getModelViewMatrixf());
		//} else {
		GL11.glLoadIdentity();
		//}
		
		return this;
	}
	
	//===================================================================================================================================================================
	
	public String saveToString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("fieldOfView=").append(Double.toString(this.getFovy())).append("\r\n");
		sb.append("targetFieldOfView=").append(Double.toString(this.getTargetFovy())).append("\r\n");
		sb.append("nearClippingPlane=").append(Double.toString(this.zNear)).append("\r\n");
		sb.append("farClippingPlane=").append(Double.toString(this.zFar)).append("\r\n");
		sb.append("\r\n");
		sb.append("x=").append(Double.toString(this.x)).append("\r\n");
		sb.append("y=").append(Double.toString(this.y)).append("\r\n");
		sb.append("z=").append(Double.toString(this.z)).append("\r\n");
		sb.append("~=").append(Double.toString(this.zDist)).append("\r\n");
		sb.append("\r\n");
		sb.append("vX=").append(Double.toString(this.vX)).append("\r\n");
		sb.append("vY=").append(Double.toString(this.vY)).append("\r\n");
		sb.append("vZ=").append(Double.toString(this.vZ)).append("\r\n");
		sb.append("aX=").append(Double.toString(this.aX)).append("\r\n");
		sb.append("aY=").append(Double.toString(this.aY)).append("\r\n");
		sb.append("aZ=").append(Double.toString(this.aZ)).append("\r\n");
		sb.append("tX=").append(Double.toString(this.tX)).append("\r\n");
		sb.append("tY=").append(Double.toString(this.tY)).append("\r\n");
		sb.append("tZ=").append(Double.toString(this.tZ)).append("\r\n");
		sb.append("\r\n");
		sb.append("yaw=").append(Double.toString(this.yaw)).append("\r\n");
		sb.append("pitch=").append(Double.toString(this.pitch)).append("\r\n");
		sb.append("roll=").append(Double.toString(this.roll)).append("\r\n");
		sb.append("\r\n");
		sb.append("mouseSensitivity=").append(Double.toString(this.getMouseSensitivity())).append("\r\n");
		sb.append("movementSpeed=").append(Double.toString(this.getMovementSpeed())).append("\r\n");
		sb.append("\r\n");
		sb.append("freeMove=").append(Boolean.toString(this.isFreeMoveEnabled())).append("\r\n");
		sb.append("freeLook=").append(Boolean.toString(this.isFreeLookEnabled())).append("\r\n");
		sb.append("invertForwardMovementWhileUpsideDown=").append(Boolean.toString(this.isInvertForwardMovementWhileUpsideDownEnabled())).append("\r\n");
		sb.append("invertVerticalMovementWhileUpsideDown=").append(Boolean.toString(this.isInvertVerticalMovementWhileUpsideDownEnabled())).append("\r\n");
		sb.append("invertYawWhileUpsideDown=").append(Boolean.toString(this.isInvertYawWhileUpsideDownEnabled())).append("\r\n");
		sb.append("invertPitchWhileUpsideDown=").append(Boolean.toString(this.isInvertPitchWhileUpsideDownEnabled())).append("\r\n");
		sb.append("\r\n");
		
		return sb.toString();
	}
	
	public boolean loadFromString(String str) {
		boolean success = true;
		String[] lines = str.split(Pattern.quote("\r\n"));
		for(String line : lines) {
			String[] split = line.split(Pattern.quote("="));
			String param = split.length >= 1 ? split[0] : "";
			String value = "";
			for(int i = 1; i < split.length; i++) {
				value = value.concat(split[i]).concat(i + 1 == split.length ? "" : "=");
			}
			
			if(param.equalsIgnoreCase("fieldOfView") || param.equalsIgnoreCase("fovy")) {
				if(CodeUtil.isDouble(value)) {
					double check = Double.parseDouble(value);
					if(check != check || Double.isInfinite(check)) {
						success = false;
					} else {
						this.setFovy(Double.parseDouble(value));
					}
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("targetFieldOfView") || param.equalsIgnoreCase("targetFovy")) {
				if(CodeUtil.isDouble(value)) {
					double check = Double.parseDouble(value);
					if(check != check || Double.isInfinite(check)) {
						success = false;
					} else {
						this.setTargetFovy(Double.parseDouble(value));
					}
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("nearClippingPlane") || param.equalsIgnoreCase("zNear")) {
				if(CodeUtil.isDouble(value)) {
					double check = Double.parseDouble(value);
					if(check != check || Double.isInfinite(check)) {
						success = false;
					} else {
						this.zNear = check;
					}
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("farClippingPlane") || param.equalsIgnoreCase("zFar")) {
				if(CodeUtil.isDouble(value)) {
					double check = Double.parseDouble(value);
					if(check != check || Double.isInfinite(check)) {
						success = false;
					} else {
						this.zFar = check;
					}
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("x")) {
				if(CodeUtil.isDouble(value)) {
					double check = Double.parseDouble(value);
					if(check != check || Double.isInfinite(check)) {
						success = false;
					} else {
						this.x = check;
					}
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("y")) {
				if(CodeUtil.isDouble(value)) {
					double check = Double.parseDouble(value);
					if(check != check || Double.isInfinite(check)) {
						success = false;
					} else {
						this.y = check;
					}
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("z")) {
				if(CodeUtil.isDouble(value)) {
					double check = Double.parseDouble(value);
					if(check != check || Double.isInfinite(check)) {
						success = false;
					} else {
						this.z = check;
					}
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("~") || param.equalsIgnoreCase("zDist")) {
				if(CodeUtil.isDouble(value)) {
					double check = Double.parseDouble(value);
					if(check != check || Double.isInfinite(check)) {
						success = false;
					} else {
						this.zDist = check;
					}
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("vX")) {
				if(CodeUtil.isDouble(value)) {
					double check = Double.parseDouble(value);
					if(check != check || Double.isInfinite(check)) {
						success = false;
					} else {
						this.vX = check;
					}
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("vY")) {
				if(CodeUtil.isDouble(value)) {
					double check = Double.parseDouble(value);
					if(check != check || Double.isInfinite(check)) {
						success = false;
					} else {
						this.vY = check;
					}
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("vZ")) {
				if(CodeUtil.isDouble(value)) {
					double check = Double.parseDouble(value);
					if(check != check || Double.isInfinite(check)) {
						success = false;
					} else {
						this.vZ = check;
					}
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("aX")) {
				if(CodeUtil.isDouble(value)) {
					double check = Double.parseDouble(value);
					if(check != check || Double.isInfinite(check)) {
						success = false;
					} else {
						this.aX = check;
					}
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("aY")) {
				if(CodeUtil.isDouble(value)) {
					double check = Double.parseDouble(value);
					if(check != check || Double.isInfinite(check)) {
						success = false;
					} else {
						this.aY = check;
					}
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("aZ")) {
				if(CodeUtil.isDouble(value)) {
					double check = Double.parseDouble(value);
					if(check != check || Double.isInfinite(check)) {
						success = false;
					} else {
						this.aZ = check;
					}
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("tX")) {
				if(CodeUtil.isDouble(value)) {
					double check = Double.parseDouble(value);
					if(check != check || Double.isInfinite(check)) {
						success = false;
					} else {
						this.tX = check;
					}
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("tY")) {
				if(CodeUtil.isDouble(value)) {
					double check = Double.parseDouble(value);
					if(check != check || Double.isInfinite(check)) {
						success = false;
					} else {
						this.tY = check;
					}
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("tZ")) {
				if(CodeUtil.isDouble(value)) {
					double check = Double.parseDouble(value);
					if(check != check || Double.isInfinite(check)) {
						success = false;
					} else {
						this.tZ = check;
					}
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("yaw")) {
				if(CodeUtil.isDouble(value)) {
					double check = Double.parseDouble(value);
					if(check != check || Double.isInfinite(check)) {
						success = false;
					} else {
						this.yaw = check;
					}
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("pitch")) {
				if(CodeUtil.isDouble(value)) {
					double check = Double.parseDouble(value);
					if(check != check || Double.isInfinite(check)) {
						success = false;
					} else {
						this.pitch = check;
					}
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("roll")) {
				if(CodeUtil.isDouble(value)) {
					double check = Double.parseDouble(value);
					if(check != check || Double.isInfinite(check)) {
						success = false;
					} else {
						this.roll = check;
					}
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("mouseSensitivity")) {
				if(CodeUtil.isDouble(value)) {
					double check = Double.parseDouble(value);
					if(check != check || Double.isInfinite(check)) {
						success = false;
					} else {
						this.mouseSensitivity = check;
					}
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("movementSpeed")) {
				if(CodeUtil.isDouble(value)) {
					double check = Double.parseDouble(value);
					if(check != check || Double.isInfinite(check)) {
						success = false;
					} else {
						this.movementSpeed = check;
					}
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("freeMove")) {
				if(CodeUtil.isBoolean(value)) {
					this.setFreeMoveEnabled(Boolean.parseBoolean(value));
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("freeLook")) {
				if(CodeUtil.isBoolean(value)) {
					this.setFreeLookEnabled(Boolean.parseBoolean(value));
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("invertForwardMovementWhileUpsideDown")) {
				if(CodeUtil.isBoolean(value)) {
					this.setInvertForwardMovementWhileUpsideDownEnabled(Boolean.parseBoolean(value));
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("invertVerticalMovementWhileUpsideDown")) {
				if(CodeUtil.isBoolean(value)) {
					this.setInvertVerticalMovementWhileUpsideDownEnabled(Boolean.parseBoolean(value));
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("invertYawWhileUpsideDown")) {
				if(CodeUtil.isBoolean(value)) {
					this.setInvertYawWhileUpsideDownEnabled(Boolean.parseBoolean(value));
				} else {
					success = false;
				}
			} else if(param.equalsIgnoreCase("invertPitchWhileUpsideDown")) {
				if(CodeUtil.isBoolean(value)) {
					this.setInvertPitchWhileUpsideDownEnabled(Boolean.parseBoolean(value));
				} else {
					success = false;
				}
			}
			
		}
		return success;
	}
	
	@Override
	public String toString() {
		return this.saveToString();
	}
	
	public boolean saveToFile(File file) {
		try(PrintWriter pr = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8), true)) {
			pr.println(this.saveToString());
			pr.flush();
			return true;
		} catch(IOException ex) {
			ex.printStackTrace(System.err);
			System.err.flush();
			return false;
		}
	}
	
	public boolean loadFromFile(File file) {
		try(InputStream in = new FileInputStream(file)) {
			StringBuilder sb = new StringBuilder();
			String line;
			while((line = FileUtil.readLine(in, true, StandardCharsets.UTF_8)) != null) {
				if(line.startsWith("#") || line.trim().isEmpty()) {
					continue;
				}
				sb.append(line).append("\r\n");
			}
			return this.loadFromString(sb.toString());
		} catch(IOException ex) {
			ex.printStackTrace(System.err);
			System.err.flush();
			return false;
		}
	}
	
	public File getSaveFile(File folder) {
		return new File(folder, "camera.properties");
	}
	
	public boolean saveToFolder(File folder) {
		return this.saveToFile(this.getSaveFile(folder));
	}
	
	public boolean loadFromFolder(File folder) {
		File file = this.getSaveFile(folder);
		if(file.isFile()) {
			return this.loadFromFile(file);
		}
		return false;
	}
	
	@Deprecated
	public static final byte[] saveCameraToBytes(YPRCamera camera) {
		/*ByteArrayOutputStream baos = new ByteArrayOutputStream(171);
		byte[] data;
		
		data = BitUtil.longToBytes(Double.doubleToLongBits(camera.getFovy()));// 8 bytes
		baos.write(data, 0, data.length);
		data = BitUtil.longToBytes(Double.doubleToLongBits(camera.zNear));// 16 bytes
		baos.write(data, 0, data.length);
		data = BitUtil.longToBytes(Double.doubleToLongBits(camera.zFar));// 24 bytes
		baos.write(data, 0, data.length);
		
		data = BitUtil.longToBytes(Double.doubleToLongBits(camera.x));// 32 bytes
		baos.write(data, 0, data.length);
		data = BitUtil.longToBytes(Double.doubleToLongBits(camera.y));// 40
		baos.write(data, 0, data.length);
		data = BitUtil.longToBytes(Double.doubleToLongBits(camera.z));// 48
		baos.write(data, 0, data.length);
		data = BitUtil.longToBytes(Double.doubleToLongBits(camera.zDist));// 56
		baos.write(data, 0, data.length);
		
		data = BitUtil.longToBytes(Double.doubleToLongBits(camera.vX));// 64
		baos.write(data, 0, data.length);
		data = BitUtil.longToBytes(Double.doubleToLongBits(camera.vY));// 72
		baos.write(data, 0, data.length);
		data = BitUtil.longToBytes(Double.doubleToLongBits(camera.vZ));// 80
		baos.write(data, 0, data.length);
		data = BitUtil.longToBytes(Double.doubleToLongBits(camera.aX));// 88
		baos.write(data, 0, data.length);
		data = BitUtil.longToBytes(Double.doubleToLongBits(camera.aY));// 96
		baos.write(data, 0, data.length);
		data = BitUtil.longToBytes(Double.doubleToLongBits(camera.aZ));// 104
		baos.write(data, 0, data.length);
		data = BitUtil.longToBytes(Double.doubleToLongBits(camera.tX));// 112
		baos.write(data, 0, data.length);
		data = BitUtil.longToBytes(Double.doubleToLongBits(camera.tY));// 120
		baos.write(data, 0, data.length);
		data = BitUtil.longToBytes(Double.doubleToLongBits(camera.tZ));// 128
		baos.write(data, 0, data.length);
		
		data = BitUtil.longToBytes(Double.doubleToLongBits(camera.yaw));// 136
		baos.write(data, 0, data.length);
		data = BitUtil.longToBytes(Double.doubleToLongBits(camera.pitch));// 144
		baos.write(data, 0, data.length);
		data = BitUtil.longToBytes(Double.doubleToLongBits(camera.roll));// 152
		baos.write(data, 0, data.length);
		
		data = BitUtil.longToBytes(Double.doubleToLongBits(camera.getMouseSensitivity()));// 160
		baos.write(data, 0, data.length);
		data = BitUtil.longToBytes(Double.doubleToLongBits(camera.getMovementSpeed()));// 168
		baos.write(data, 0, data.length);
		
		boolean b0 = camera.isFreeMoveEnabled();
		boolean b1 = camera.isInvertForwardMovementWhileUpsideDownEnabled();
		boolean b2 = camera.isInvertVerticalMovementWhileUpsideDownEnabled();
		boolean b3 = camera.isFreeLookEnabled();
		boolean b4 = camera.isInvertYawWhileUpsideDownEnabled();
		boolean b5 = camera.isInvertPitchWhileUpsideDownEnabled();
		boolean b6 = false;// <unused>
		boolean b7 = false;// <unused>
		data = new byte[] {BitUtil.bitsToByte(b0, b1, b2, b3, b4, b5, b6, b7)};// 169 bytes
		baos.write(data, 0, data.length);
		
		return baos.toByteArray();// 169 bytes (0 - 168)*/
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try(DataOutputStream out = new DataOutputStream(baos)) {
			out.writeDouble(camera.getFovy());
			out.writeDouble(camera.zNear);
			out.writeDouble(camera.zFar);
			
			out.writeDouble(camera.x);
			out.writeDouble(camera.y);
			out.writeDouble(camera.z);
			out.writeDouble(camera.zDist);
			
			out.writeDouble(camera.vX);
			out.writeDouble(camera.vY);
			out.writeDouble(camera.vZ);
			out.writeDouble(camera.aX);
			out.writeDouble(camera.aY);
			out.writeDouble(camera.aZ);
			out.writeDouble(camera.tX);
			out.writeDouble(camera.tY);
			out.writeDouble(camera.tZ);
			
			out.writeDouble(camera.yaw);
			out.writeDouble(camera.pitch);
			out.writeDouble(camera.roll);
			
			out.writeDouble(camera.getMouseSensitivity());
			out.writeDouble(camera.getMovementSpeed());
			
			boolean b0 = camera.isFreeMoveEnabled();
			boolean b1 = camera.isInvertForwardMovementWhileUpsideDownEnabled();
			boolean b2 = camera.isInvertVerticalMovementWhileUpsideDownEnabled();
			boolean b3 = camera.isFreeLookEnabled();
			boolean b4 = camera.isInvertYawWhileUpsideDownEnabled();
			boolean b5 = camera.isInvertPitchWhileUpsideDownEnabled();
			boolean b6 = false;// <unused>
			boolean b7 = false;// <unused>
			byte flags = BitUtil.bitsToByte(b0, b1, b2, b3, b4, b5, b6, b7);
			out.writeByte(flags);
			
			out.flush();
		} catch(IOException ex) {
			System.err.print("Failed to prepare YPRCamera data for saving: ");
			ex.printStackTrace(System.err);
			System.err.flush();
		}
		
		return baos.toByteArray();
	}
	
	@Deprecated
	public byte[] saveToBytes() {
		return YPRCamera.saveCameraToBytes(this);
	}
	
	/*protected static final double safeLoadDouble(byte[] data, int offset, double def) {
		def = def != def || Double.isInfinite(def) ? 0x0.0p0 : def;
		
		double value = Double.longBitsToDouble(BitUtil.bytesToLong(data, offset));
		return value != value || Double.isInfinite(value) ? def : value;
	}*/
	
	@Deprecated
	public static final boolean loadFromBytes(YPRCamera camera, final byte[] data) {//, int offset) {
		/*if(data == null || (data.length - offset) < 169) {
			return false;
		}
		{
			byte[] tmp = data;
			data = new byte[169];
			System.arraycopy(tmp, offset, data, 0, data.length);
		}
		offset = 0;
		
		camera.setFovy(safeLoadDouble(data, Long.BYTES * (offset++), camera.fovy));// 0 - 7
		camera.zNear = 0.01;//safeLoadDouble(data, Long.BYTES * (offset++), camera.zNear);// 8 - 15
		camera.zFar = 1000.0;//safeLoadDouble(data, Long.BYTES * (offset++), camera.zFar);// 16 - 23
		
		camera.x = safeLoadDouble(data, Long.BYTES * (offset++), camera.x);// 24 - 31
		camera.y = safeLoadDouble(data, Long.BYTES * (offset++), camera.y);// 32 - 39
		camera.z = safeLoadDouble(data, Long.BYTES * (offset++), camera.z);// 40 - 47
		camera.zDist = safeLoadDouble(data, Long.BYTES * (offset++), camera.zDist);// 48 - 55
		
		camera.vX = safeLoadDouble(data, Long.BYTES * (offset++), camera.vX);// 56 - 63
		camera.vY = safeLoadDouble(data, Long.BYTES * (offset++), camera.vY);// 64 - 71
		camera.vZ = safeLoadDouble(data, Long.BYTES * (offset++), camera.vZ);// 72 - 79
		camera.aX = safeLoadDouble(data, Long.BYTES * (offset++), camera.aX);// 80 - 87
		camera.aY = safeLoadDouble(data, Long.BYTES * (offset++), camera.aY);// 88 - 95
		camera.aZ = safeLoadDouble(data, Long.BYTES * (offset++), camera.aZ);// 96 - 103
		camera.tX = safeLoadDouble(data, Long.BYTES * (offset++), camera.tX);// 104 - 111
		camera.tY = safeLoadDouble(data, Long.BYTES * (offset++), camera.tY);// 112 - 119
		camera.tZ = safeLoadDouble(data, Long.BYTES * (offset++), camera.tZ);// 120 - 127
		
		camera.yaw = safeLoadDouble(data, Long.BYTES * (offset++), camera.yaw);// 128 - 135
		camera.pitch = safeLoadDouble(data, Long.BYTES * (offset++), camera.pitch);// 136 - 143
		camera.roll = safeLoadDouble(data, Long.BYTES * (offset++), camera.roll);// 144 - 151
		
		camera.setMouseSensitivity(0.15);//safeLoadDouble(data, Long.BYTES * (offset++), camera.getMouseSensitivity()));// 152 - 159
		camera.setMovementSpeed(1.2);//safeLoadDouble(data, Long.BYTES * (offset++), camera.getMovementSpeed()));// 160 - 167
		
		boolean[] bits = BitUtil.byteToBits(data[Long.BYTES * offset]);// 168 (the 169th byte)
		camera.setFreeMoveEnabled(bits[0]);
		camera.setInvertForwardMovementWhileUpsideDownEnabled(bits[1]);
		camera.setInvertVerticalMovementWhileUpsideDownEnabled(bits[2]);
		camera.setFreeLookEnabled(bits[3]);
		camera.setInvertYawWhileUpsideDownEnabled(bits[4]);
		camera.setInvertPitchWhileUpsideDownEnabled(bits[5]);
		// <bits[6] and bits[7] are unused>
		
		// by this point, the offset variable is 21, but the actual index within the data array that we just used is (8 * 21) = 168.
		// Therefore, the next index within the data array we could use if it existed would be 169
		//offset = (offset * Long.BYTES) + 1;// 169
		
		// ...
		*/
		
		try(DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
			camera.setFovy(in.readDouble());
			camera.zNear = in.readDouble();
			camera.zFar = in.readDouble();
			
			camera.x = in.readDouble();
			camera.y = in.readDouble();
			camera.z = in.readDouble();
			camera.zDist = in.readDouble();
			
			camera.vX = in.readDouble();
			camera.vY = in.readDouble();
			camera.vZ = in.readDouble();
			camera.aX = in.readDouble();
			camera.aY = in.readDouble();
			camera.aZ = in.readDouble();
			camera.tX = in.readDouble();
			camera.tY = in.readDouble();
			camera.tZ = in.readDouble();
			
			camera.yaw = in.readDouble();
			camera.pitch = in.readDouble();
			camera.roll = in.readDouble();
			
			camera.setMouseSensitivity(in.readDouble());
			camera.setMovementSpeed(in.readDouble());
			
			boolean[] bits = BitUtil.byteToBits(in.readByte());
			camera.setFreeMoveEnabled(bits[0]);
			camera.setInvertForwardMovementWhileUpsideDownEnabled(bits[1]);
			camera.setInvertVerticalMovementWhileUpsideDownEnabled(bits[2]);
			camera.setFreeLookEnabled(bits[3]);
			camera.setInvertYawWhileUpsideDownEnabled(bits[4]);
			camera.setInvertPitchWhileUpsideDownEnabled(bits[5]);
			// <bits[6] and bits[7] are unused>
		} catch(IOException ex) {
			System.err.print("Failed to interpret loaded YPRCamera data: ");
			ex.printStackTrace(System.err);
			System.err.flush();
			return false;
		}
		
		return true;
	}
	
	//===================================================================================================================================================================
	
}
