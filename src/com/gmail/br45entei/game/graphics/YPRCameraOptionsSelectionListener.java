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
package com.gmail.br45entei.game.graphics;

import com.gmail.br45entei.game.input.Mouse;
import com.gmail.br45entei.game.ui.Window;
import com.gmail.br45entei.util.SWTUtil;

import java.beans.Beans;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

/** @since 1.0
 * @author Brian_Entei &lt;br45entei&#064;gmail.com&gt; */
public class YPRCameraOptionsSelectionListener extends SelectionAdapter {
	
	protected final Renderer renderer;
	protected final YPRCamera camera;
	protected final MenuItem mntmCameraOptions;
	
	/** @param renderer The renderer that is creating this dialog window
	 * @param camera The {@link YPRCamera} whose settings will be edited
	 * @param mntmCameraOptions The MenuItem that will be used to open this
	 *            dialog window */
	public YPRCameraOptionsSelectionListener(Renderer renderer, YPRCamera camera, MenuItem mntmCameraOptions) {
		this.renderer = renderer;
		this.camera = camera;
		this.mntmCameraOptions = mntmCameraOptions;
	}
	
	/** Updates the location of the shell relative to the parent shell,
	 * as well as text elements within the shell, etc.
	 * 
	 * @param parent The parent shell
	 * @param shell The shell */
	protected static void updateSnapToPositioning(Shell parent, Shell shell) {
		Rectangle parentBounds = parent.getBounds();
		Rectangle shellBounds = shell.getBounds();
		
		Rectangle parentClientArea = parent.getClientArea();
		int doubleBorderWidth = parentBounds.width - parentClientArea.width;
		int borderSeparationWidth = doubleBorderWidth - 2;
		Point targetLocation = new Point((parentBounds.x + parentBounds.width) - borderSeparationWidth, parentBounds.y);
		int xDiff = Math.abs(targetLocation.x - shellBounds.x);
		int yDiff = Math.abs(targetLocation.y - shellBounds.y);
		boolean shellNearTargetLocation = xDiff <= doubleBorderWidth && yDiff <= doubleBorderWidth;
		
		if(Mouse.isCaptured() || shellNearTargetLocation) {
			boolean overlappingHorizontally = shellBounds.x + shellBounds.width > parentBounds.x && shellBounds.x < parentBounds.x + parentBounds.width;
			boolean overlappingVertically = shellBounds.y + shellBounds.height > parentBounds.y && shellBounds.y < parentBounds.y + parentBounds.height;
			
			if((overlappingHorizontally && overlappingVertically) || shellNearTargetLocation) {
				SWTUtil.setLocation(shell, targetLocation);
			}
		}
	}
	
	/** @wbp.parser.entryPoint */
	@Override
	public void widgetSelected(SelectionEvent e) {
		this.mntmCameraOptions.setSelection(true);
		this.mntmCameraOptions.setEnabled(false);
		
		Window window = Window.getWindow();
		final Shell parent = window.getShell();
		final Shell shell = new Shell(parent, SWT.DIALOG_TRIM);
		final ControlListener parentMoveListener = new ControlListener() {
			volatile int lastParentX = parent.getLocation().x;
			volatile int lastParentY = parent.getLocation().y;
			volatile int lastParentWidth = parent.getSize().x;
			//volatile int lastParentHeight = parent.getSize().y;
			
			@Override
			public void controlResized(ControlEvent e) {
				this.controlMoved(e);
			}
			
			@Override
			public void controlMoved(ControlEvent e) {
				Rectangle parentBounds = parent.getBounds();
				Point shellLocation = shell.getLocation();
				
				Rectangle parentClientArea = parent.getClientArea();
				int doubleBorderWidth = parentBounds.width - parentClientArea.width;
				int borderSeparationWidth = doubleBorderWidth - 2;
				Point previousTargetLocation = new Point((this.lastParentX + this.lastParentWidth) - borderSeparationWidth, this.lastParentY);
				Point targetLocation = new Point((parentBounds.x + parentBounds.width) - borderSeparationWidth, parentBounds.y);
				int xDiff = Math.abs(previousTargetLocation.x - shellLocation.x);
				int yDiff = Math.abs(previousTargetLocation.y - shellLocation.y);
				int detectionRadius = doubleBorderWidth + (doubleBorderWidth / 2);
				boolean shellWasNearTargetLocation = xDiff <= detectionRadius && yDiff <= detectionRadius;
				
				if(shellWasNearTargetLocation) {
					SWTUtil.setLocation(shell, targetLocation);
				}
				this.lastParentX = parentBounds.x;
				this.lastParentY = parentBounds.y;
				this.lastParentWidth = parentBounds.width;
				//this.lastParentHeight = parentBounds.height;
			}
		};
		parent.addControlListener(parentMoveListener);
		
		try {
			shell.setText(this.renderer.getName().concat(" 3D Camera Options"));
			Image[] images = parent.getImages();
			if(images.length > 0) {
				shell.setImages(images);
			} else {
				shell.setImage(parent.getImage());
			}
			shell.setSize(323, 243);
			
			Button btnFreeLook = new Button(shell, SWT.CHECK);
			btnFreeLook.setToolTipText("When checked, the camera has free, unrestricted rotation.\r\nThis means you can flip the camera upside down by moving the mouse up or down far enough!");
			btnFreeLook.setSelection(this.camera.isFreeLookEnabled());
			btnFreeLook.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					YPRCameraOptionsSelectionListener.this.camera.setFreeLookEnabled(btnFreeLook.getSelection());
				}
			});
			btnFreeLook.setText("Free Look");
			btnFreeLook.setBounds(10, 13, 77, 15);
			
			Label lblMouseSensitivity = new Label(shell, SWT.NONE);
			lblMouseSensitivity.setBounds(93, 13, 138, 15);
			lblMouseSensitivity.setText("Look Sensitivity:");
			
			Spinner spnrMouseSensitivity = new Spinner(shell, SWT.BORDER);
			spnrMouseSensitivity.setToolTipText("The amount that the yaw and pitch are incremented for each movement of the mouse");
			spnrMouseSensitivity.setDigits(4);
			spnrMouseSensitivity.setMaximum(999999);
			spnrMouseSensitivity.setMinimum(-999999);
			spnrMouseSensitivity.setPageIncrement(1000);
			spnrMouseSensitivity.setIncrement(100);
			spnrMouseSensitivity.setSelection((int) Math.round(this.camera.getMouseSensitivity() * 10000.0));
			spnrMouseSensitivity.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					YPRCameraOptionsSelectionListener.this.camera.setMouseSensitivity(spnrMouseSensitivity.getSelection() / 10000.0);
				}
			});
			spnrMouseSensitivity.setBounds(237, 10, 70, 22);
			
			Button btnInvertYawWhen = new Button(shell, SWT.CHECK);
			btnInvertYawWhen.setBounds(10, 40, 194, 16);
			btnInvertYawWhen.setText("Invert Yaw When Upside-Down");
			btnInvertYawWhen.setSelection(this.camera.isInvertYawWhileUpsideDownEnabled());
			btnInvertYawWhen.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					YPRCameraOptionsSelectionListener.this.camera.setInvertYawWhileUpsideDownEnabled(btnInvertYawWhen.getSelection());
				}
			});
			
			Button btnInvertPitchWhen = new Button(shell, SWT.CHECK);
			btnInvertPitchWhen.setBounds(10, 67, 194, 16);
			btnInvertPitchWhen.setText("Invert Pitch When Upside-Down");
			btnInvertPitchWhen.setSelection(this.camera.isInvertPitchWhileUpsideDownEnabled());
			btnInvertPitchWhen.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					YPRCameraOptionsSelectionListener.this.camera.setInvertPitchWhileUpsideDownEnabled(btnInvertPitchWhen.getSelection());
				}
			});
			
			Button btnFreeMove = new Button(shell, SWT.CHECK);
			btnFreeMove.setToolTipText("When checked, the camera will move in the direction it is facing, including up/down.\r\nWhen unchecked, the camera behaves as most first-person games do; it only moves in the direction it is facing along the X and Z axes, ignoring the Y axis completely (except when using space/shift to move up and down; then the camera only moves straight up/down)");
			btnFreeMove.setSelection(this.camera.isFreeMoveEnabled());
			btnFreeMove.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					YPRCameraOptionsSelectionListener.this.camera.setFreeMoveEnabled(btnFreeMove.getSelection());
				}
			});
			btnFreeMove.setBounds(10, 94, 77, 16);
			btnFreeMove.setText("Free Move");
			
			Label lblMovementSpeed = new Label(shell, SWT.NONE);
			lblMovementSpeed.setBounds(93, 94, 138, 15);
			lblMovementSpeed.setText("Movement Speed (m/s):");
			
			Spinner spnrMovementSpeed = new Spinner(shell, SWT.BORDER);
			spnrMovementSpeed.setToolTipText("The distance the camera travels, in meters per second");
			spnrMovementSpeed.setDigits(4);
			spnrMovementSpeed.setMaximum(999999);
			spnrMovementSpeed.setMinimum(-999999);
			spnrMovementSpeed.setPageIncrement(10000);
			spnrMovementSpeed.setIncrement(1000);
			spnrMovementSpeed.setSelection((int) Math.round(this.camera.getMovementSpeed() * 10000.0));
			spnrMovementSpeed.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					YPRCameraOptionsSelectionListener.this.camera.setMovementSpeed(spnrMovementSpeed.getSelection() / 10000.0);
				}
			});
			spnrMovementSpeed.setBounds(237, 91, 70, 22);
			
			Button btnInvertForwardMovement = new Button(shell, SWT.CHECK);
			btnInvertForwardMovement.setBounds(10, 121, 277, 16);
			btnInvertForwardMovement.setText("Invert Forward Movement When Upside-Down");
			btnInvertForwardMovement.setSelection(this.camera.isInvertForwardMovementWhileUpsideDownEnabled());
			btnInvertForwardMovement.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					YPRCameraOptionsSelectionListener.this.camera.setInvertForwardMovementWhileUpsideDownEnabled(btnInvertForwardMovement.getSelection());
				}
			});
			
			Button btnInvertVerticalMovement = new Button(shell, SWT.CHECK);
			btnInvertVerticalMovement.setBounds(10, 148, 277, 16);
			btnInvertVerticalMovement.setText("Invert Vertical Movement When Upside-Down");
			btnInvertVerticalMovement.setSelection(this.camera.isInvertVerticalMovementWhileUpsideDownEnabled());
			btnInvertVerticalMovement.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					YPRCameraOptionsSelectionListener.this.camera.setInvertVerticalMovementWhileUpsideDownEnabled(btnInvertVerticalMovement.getSelection());
				}
			});
			
			Button btnDone = new Button(shell, SWT.NONE);
			btnDone.setBounds(10, 179, 297, 25);
			btnDone.setText("Done");
			btnDone.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					shell.close();
				}
			});
			
			if(!Beans.isDesignTime()) {
				Point size = shell.getSize();
				shell.setSize(size.x + 10, size.y + 10);
			}
			
			shell.open();
			shell.layout();
			
			while(window.getActiveRenderer() == this.renderer && window.swtLoop() && !shell.isDisposed()) {
				if(window.isFullscreen() || !window.isVisible()) {
					if(shell.isVisible()) {
						shell.setVisible(false);
					}
				} else {
					if(!shell.isVisible()) {
						shell.setVisible(true);
						/*boolean parentHadFocus = parent.isFocusControl() || Mouse.getCursorCanvas().isFocusControl();
						shell.open();
						shell.layout();
						if(parentHadFocus) {
							parent.forceFocus();
							Mouse.getCursorCanvas().forceFocus();
						}*/
					}
				}
				
				if(!this.mntmCameraOptions.isDisposed()) {
					SWTUtil.setEnabled(this.mntmCameraOptions, false);
					SWTUtil.setSelection(this.mntmCameraOptions, true);
				} else {
					break;
				}
				SWTUtil.setEnabled(btnInvertForwardMovement, this.camera.isFreeLookEnabled());
				SWTUtil.setEnabled(btnInvertVerticalMovement, this.camera.isFreeLookEnabled());
				if(parent.isVisible() && shell.isVisible()) {
					updateSnapToPositioning(parent, shell);
				}
			}
		} finally {
			shell.dispose();
			if(!this.mntmCameraOptions.isDisposed()) {
				this.mntmCameraOptions.setSelection(false);
				this.mntmCameraOptions.setEnabled(true);
			}
			parent.removeControlListener(parentMoveListener);
		}
	}
	//
}
