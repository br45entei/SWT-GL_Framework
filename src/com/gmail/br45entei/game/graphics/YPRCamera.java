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

import java.io.ByteArrayOutputStream;

import org.lwjgl.opengl.GL11;

/** A simple 3D camera implementation using Euler angles (yaw, pitch, and roll).
 *
 * @since 1.0
 * @author Brian_Entei */
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
			yaw += dxy.x * this.mouseSensitivity * (this.fovy / this.targetFovy) * (upsideDown && this.invertYawWhenUpsideDown ? -1.0f : 1.0f);
			pitch += dxy.y * this.mouseSensitivity * (this.fovy / this.targetFovy) * ((rolledOver && this.invertPitchWhenUpsideDown) ? -1.0f : 1.0f);
			
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
				x = y = z = yaw = pitch = roll = 0;
				this.resetCameraFields();
				if(Keyboard.isKeyDown(Keys.VK_RSHIFT)) {
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
		this.tX = Math.abs(this.tX);
		this.tY = Math.abs(this.tY);
		this.tZ = Math.abs(this.tZ);
		
		//Increase the velocity by the current acceleration, capped to the terminal velocity:
		this.vX = Math.min(this.tX, this.vX + (this.aX * deltaTime));
		this.vY = Math.min(this.tY, this.vY + (this.aY * deltaTime));
		this.vZ = Math.min(this.tZ, this.vZ + (this.aZ * deltaTime));
		
		//Add the velocity to this camera's position:
		this.x += Math.min(this.tX, this.vX * deltaTime);
		this.y += Math.min(this.tY, this.vY * deltaTime);
		this.z += Math.min(this.tZ, this.vZ * deltaTime);
		
		return this;
	}
	
	//===================================================================================================================================================================
	
	private volatile long lastSecond = 0L;
	
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
	
	public final YPRCamera setTargetFovy(double fovy) {
		this.targetFovy = fovy != fovy || Double.isInfinite(fovy) ? this.targetFovy : fovy;
		return this;
	}
	
	/** @param fovy The new field of view setting for this camera
	 * @return This YPRCamera */
	public final YPRCamera setFovy(double fovy) {
		this.fovy = fovy != fovy || Double.isInfinite(fovy) ? this.fovy : fovy;
		this.zoomFovy = (20.0 / 70.0) * this.fovy;
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
	
	public static final byte[] saveCameraToBytes(YPRCamera camera) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(171);
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
		
		return baos.toByteArray();// 169 bytes (0 - 168)
	}
	
	public byte[] saveToBytes() {
		return YPRCamera.saveCameraToBytes(this);
	}
	
	protected static final double safeLoadDouble(byte[] data, int offset, double def) {
		def = def != def || Double.isInfinite(def) ? 0x0.0p0 : def;
		
		double value = Double.longBitsToDouble(BitUtil.bytesToLong(data, offset));
		return value != value || Double.isInfinite(value) ? def : value;
	}
	
	public static final boolean loadFromBytes(YPRCamera camera, byte[] data, int offset) {
		if(data == null || (data.length - offset) < 169) {
			return false;
		}
		{
			byte[] tmp = data;
			data = new byte[169];
			System.arraycopy(tmp, offset, data, 0, data.length);
		}
		offset = 0;
		
		camera.setFovy(safeLoadDouble(data, Long.BYTES * (offset++), camera.fovy));// 0 - 7
		camera.zNear = safeLoadDouble(data, Long.BYTES * (offset++), camera.zNear);// 8 - 15
		camera.zFar = safeLoadDouble(data, Long.BYTES * (offset++), camera.zFar);// 16 - 23
		
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
		
		camera.setMouseSensitivity(safeLoadDouble(data, Long.BYTES * (offset++), camera.getMouseSensitivity()));// 152 - 159
		camera.setMovementSpeed(safeLoadDouble(data, Long.BYTES * (offset++), camera.getMovementSpeed()));// 160 - 167
		
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
		
		return true;
	}
	
	//===================================================================================================================================================================
	
}
