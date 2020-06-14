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
package com.gmail.br45entei.util;

import java.awt.MouseInfo;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.SWTEventListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TypedListener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.wb.swt.SWTResourceManager;

import etinyplugins.commons.swt.UndoRedoImpl;

/** @author Brian_Entei */
public class SWTUtil {
	
	public static final Image[] getTitleImages() {
		return new Image[] {SWTResourceManager.getImage(SWTUtil.class, "/assets/textures/swt/title/Entei-16x16.png"), SWTResourceManager.getImage(SWTUtil.class, "/assets/textures/swt/title/Entei-32x32.png"), SWTResourceManager.getImage(SWTUtil.class, "/assets/textures/swt/title/Entei-48x48.png"), SWTResourceManager.getImage(SWTUtil.class, "/assets/textures/swt/title/Entei-64x64.png"), SWTResourceManager.getImage(SWTUtil.class, "/assets/textures/swt/title/Entei-128x128.png")};
	}
	
	public static final void centerShell2OnShell1(Shell shell1, Shell shell2) {
		Point size1 = shell1.getSize();
		Point loc1 = shell1.getLocation();
		Point size2 = shell2.getSize();
		shell2.setLocation(loc1.x + (size1.x / 2) - (size2.x / 2), loc1.y + (size1.y / 2) - (size2.y / 2));
	}
	
	public static final boolean setEnabled(Control control, boolean enabled) {
		if(control.isEnabled() != enabled) {
			control.setEnabled(enabled);
			return control.isEnabled() == enabled;
		}
		return false;
	}
	
	public static final boolean setEnabled(MenuItem control, boolean enabled) {
		if(control.isEnabled() != enabled) {
			control.setEnabled(enabled);
			return control.isEnabled() == enabled;
		}
		return false;
	}
	
	public static final boolean setVisible(Control control, boolean visible) {
		if(control.isVisible() != visible) {
			control.setVisible(visible);
			return control.isVisible() == visible;
		}
		return false;
	}
	
	public static final boolean setSelection(Button button, boolean selected) {
		if(button.getSelection() != selected) {
			button.setSelection(selected);
			return button.getSelection() == selected;
		}
		return false;
	}
	
	public static final boolean setSelection(MenuItem menuItem, boolean selected) {
		if(menuItem.getSelection() != selected) {
			menuItem.setSelection(selected);
			return menuItem.getSelection() == selected;
		}
		return false;
	}
	
	public static final boolean setText(Label label, String string) {
		if(!label.getText().equals(string)) {
			label.setText(string);
			return label.getText().equals(string);
		}
		return false;
	}
	
	public static final boolean setText(MenuItem menuItem, String string) {
		if(!menuItem.getText().equals(string)) {
			menuItem.setText(string);
			return menuItem.getText().equals(string);
		}
		return false;
	}
	
	public static final boolean setText(StyledText stxt, String text) {
		if(!stxt.getText().equals(text)) {
			stxt.setText(text);
			return stxt.getText().equals(text);
		}
		return false;
	}
	
	public static final boolean setText(Text text, String string) {
		if(!text.getText().equals(string)) {
			text.setText(string);
			return text.getText().equals(string);
		}
		return false;
	}
	
	public static final boolean setText(CCombo combo, String string) {
		if(!combo.getText().equals(string)) {
			combo.setText(string);
			return combo.getText().equals(string);
		}
		return false;
	}
	
	public static final void setTextFor(StyledText styledText, String text) {
		/*if(styledText.getText().equals(text)) {
			return;
		}*/
		final int numOfVisibleLines = Math.floorDiv(styledText.getSize().y, styledText.getLineHeight());
		final int originalIndex = styledText.getTopIndex();
		int index = originalIndex;
		final int lineCount = styledText.getLineCount();
		/*if(HTTPClientRequest.debug) {
			this.inputField.setText("index: \"" + index + "\"; line count: \"" + lineCount + "\"; visible lines: \"" + numOfVisibleLines + "\";");
		}*/
		//Main.runClock();
		if(lineCount - index == numOfVisibleLines) {
			index = -1;
		}
		final Point selection = styledText.getSelection();
		final int caretOffset = styledText.getCaretOffset();
		//==
		//styledText.setText(text);
		styledText.getContent().setText(text);
		//==
		try {
			if(caretOffset == selection.x) {//Right to left text selection
				styledText.setCaretOffset(caretOffset);
				styledText.setSelection(selection.y, selection.x);
			} else {//Left to right text selection
				styledText.setSelection(selection);
				styledText.setCaretOffset(caretOffset);
			}
		} catch(IllegalArgumentException ignored) {
		}
		final int newLineCount = styledText.getLineCount();
		if(index == -1) {
			index = newLineCount - 1;
		} else {
			if(newLineCount >= lineCount) {
				index = newLineCount - (lineCount - index);
			} else {
				index = newLineCount - (newLineCount - index);
			}
		}
		styledText.setTopIndex(index);//originalIndex);//this.isScrollLocked ? originalIndex : index);
	}
	
	public static final boolean setToolTipText(Control control, String string) {
		if(!control.getToolTipText().equals(string)) {
			control.setToolTipText(string);
			return control.getToolTipText().equals(string);
		}
		return false;
	}
	
	public static final boolean setLocation(Control control, Point location) {
		if(!control.getLocation().equals(location)) {
			control.setLocation(location);
			return control.getLocation().equals(location);
		}
		return false;
	}
	
	public static final boolean setLocation(Control control, int x, int y) {
		return setLocation(control, new Point(x, y));
	}
	
	public static final boolean setSize(Control control, Point size) {
		if(!control.getSize().equals(size)) {
			control.setSize(size);
			return control.getSize().equals(size);
		}
		return false;
	}
	
	public static final boolean setSize(Control control, int width, int height) {
		return setSize(control, new Point(width, height));
	}
	
	public static final boolean setBounds(Control control, Rectangle rect) {
		if(!control.getBounds().equals(rect)) {
			control.setBounds(rect);
			return control.getBounds().equals(rect);
		}
		return false;
	}
	
	public static final boolean setBounds(Control control, int x, int y, int width, int height) {
		return setBounds(control, new Rectangle(x, y, width, height));
	}
	
	public static final boolean setImage(Button button, Image image) {
		if(button.getImage() != image) {
			button.setImage(image);
			return button.getImage() == image;
		}
		return false;
	}
	
	public static final boolean select(CCombo combo, int index) {
		if(combo.getSelectionIndex() != index) {
			combo.select(index);
			return combo.getSelectionIndex() == index;
		}
		return false;
	}
	
	/** @param combo The CCombo whose list items will be set
	 * @param items The list of strings to set
	 * @return True if the CCombo's list was altered as a result */
	public static final boolean setItems(CCombo combo, String[] items) {
		if(!Arrays.equals(combo.getItems(), items)) {
			combo.setItems(items);
			return true;
		}
		return false;
	}
	
	/** @param shell The shell whose border width will be returned
	 * @return The actual width of the shell's border (sometimes
	 *         {@link Shell#getBorderWidth() shell.getBorderWidth()} returns
	 *         inaccurate
	 *         results; YMMV) */
	public static final int getActualBorderWidth(Shell shell) {
		Point shellSize = shell.getSize();
		Rectangle clientArea = shell.getClientArea();
		return Long.valueOf(Math.round((shellSize.x - clientArea.width) / 2.0)).intValue();
	}
	
	/** @param shell The shell whose title-bar height will be returned
	 * @return The height of the title-bar for the given shell */
	public static final int getTitleBarHeight(Shell shell) {
		Point shellSize = shell.getSize();
		Rectangle clientArea = shell.getClientArea();
		return(shellSize.y - clientArea.height - Long.valueOf(Math.round((shellSize.x - clientArea.width) / 2.0)).intValue());
	}
	
	/** @param shell The shell whose Menu-bar height will be returned
	 * @return The height of the menu-bar for the given shell. */
	public static final int getMenuBarHeight(Shell shell) {
		Point shellSize = shell.getSize();
		Rectangle clientArea = shell.getClientArea();
		return shellSize.y - clientArea.height;
	}
	
	public static final List<SWTEventListener> removeListenersFrom(Control control, SWTEventListener... listeners) {
		List<SWTEventListener> remainingListeners = new ArrayList<>(listeners.length);
		List<SWTEventListener> removedListeners = new ArrayList<>(listeners.length);
		for(SWTEventListener listener : listeners) {
			remainingListeners.add(listener);
		}
		for(int eventType = SWT.None; eventType <= SWT.ZoomChanged; eventType++) {
			for(Listener listener : control.getListeners(eventType)) {
				if(removedListeners.size() == listeners.length) {
					break;
				}
				if(listener instanceof TypedListener) {
					TypedListener typedListener = (TypedListener) listener;
					SWTEventListener check = typedListener.getEventListener();
					if(remainingListeners.contains(check)) {
						removedListeners.add(check);
						control.removeListener(eventType, listener);
					}
				}
			}
			if(removedListeners.size() == listeners.length) {
				break;
			}
		}
		remainingListeners.removeAll(removedListeners);
		return remainingListeners;
	}
	
	public static final SWTEventListener[] enableMouseDragging(Composite composite) {
		final java.awt.Point[] cursorOffset = {new java.awt.Point()};
		final boolean[] mouseDown = {false};
		final SWTEventListener[] listeners = {null, null};
		composite.addMouseListener((MouseListener) (listeners[0] = new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				cursorOffset[0] = MouseInfo.getPointerInfo().getLocation();
				Point shellLoc = composite.getShell().getLocation();
				cursorOffset[0].x -= shellLoc.x;
				cursorOffset[0].y -= shellLoc.y;
				mouseDown[0] = true;
			}
			
			@Override
			public void mouseUp(MouseEvent e) {
				mouseDown[0] = false;
			}
		}));
		composite.addMouseMoveListener((MouseMoveListener) (listeners[1] = new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				if(mouseDown[0]) {
					java.awt.Point mLoc = MouseInfo.getPointerInfo().getLocation();
					composite.getShell().setLocation(mLoc.x - cursorOffset[0].x, mLoc.y - cursorOffset[0].y);
				}
			}
		}));
		composite.setCursor(composite.getDisplay().getSystemCursor(SWT.CURSOR_SIZEALL));
		Shell shell = composite.getShell();
		if(shell != composite) {
			shell.setCursor(composite.getCursor());
		}
		return listeners;
	}
	
	/** Opens a FileDialog with the given text and settings, and returns a file
	 * that the user has selected.
	 * 
	 * @param shell The parent Shell for the FileDialog
	 * @param title The title of the FileDialog
	 * @param openOrSave Whether or not the selected file will be opened or
	 *            saved to
	 * @param overwrite If true, the FileDialog will prompt for confirmation on
	 *            saving to an existing file
	 * @param folder The parent folder to start browsing in
	 * @param fileName The default name of the file to select, may be
	 *            <tt><b>null</b></tt>
	 * @param filterExtensions The filter extensions and names (e.g.
	 *            <code>{{"*.txt", "*.*"}, {"Text Files (*.txt)", "All Files (*.*)"}}</code>)
	 * @return The file that the user has selected, or <tt><b>null</b></tt> if
	 *         the user clicked cancel or closed the dialog */
	public static final File getUserSelectedFile(Shell shell, String title, Boolean openOrSave, boolean overwrite, File folder, String fileName, String[][] filterExtensions) {
		FileDialog dialog = new FileDialog(shell, openOrSave == null ? SWT.NONE : (openOrSave.booleanValue() ? SWT.OPEN : SWT.SAVE));
		dialog.setText(title);
		dialog.setFileName(fileName == null ? "" : fileName);
		dialog.setFilterPath(folder == null ? null : folder.getAbsolutePath());
		if(openOrSave != null && !openOrSave.booleanValue()) {
			dialog.setOverwrite(overwrite);
		}
		dialog.setFilterExtensions(filterExtensions[0]);
		dialog.setFilterNames(filterExtensions[1]);
		
		String path = dialog.open();
		if(path != null) {
			File check = new File(path);
			if(openOrSave == null) {
				return check;
			}
			if(openOrSave.booleanValue() && !check.isFile()) {
				MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.RETRY | SWT.CANCEL);
				box.setText("Unable To Open Selected File");
				box.setMessage("Failed to open the file %s, is it a directory or system file?\nClick 'Retry' to select another file.");
				
				switch(box.open()) {
				case SWT.RETRY:
					return getUserSelectedFile(shell, title, openOrSave, overwrite, folder, fileName, filterExtensions);
				case SWT.CANCEL:
				default:
					return null;
				}
			}
			return check;
		}
		return null;
	}
	
	/** Opens a DirectoryDialog with the given text and settings, and returns a
	 * folder that the user has selected.
	 * 
	 * @param shell The parent Shell for the DirectoryDialog
	 * @param title The title of the DirectoryDialog
	 * @param message The message of the DirectoryDialog
	 * @param folder The parent folder to start browsing in
	 * @return The folder that the user has selected, or <tt><b>null</b></tt> if
	 *         the user clicked cancel or closed the dialog */
	public static final File getUserSelectedFolder(Shell shell, String title, String message, File folder) {
		DirectoryDialog dialog = new DirectoryDialog(shell, SWT.NONE);
		dialog.setText(title);
		dialog.setMessage(message);
		dialog.setFilterPath(folder == null ? null : folder.getAbsolutePath());
		
		String path = dialog.open();
		if(path != null) {
			File check = new File(path);
			if(!check.isDirectory()) {// || check.list() == null) {
				MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.RETRY | SWT.CANCEL);
				box.setText("Unable To Use Selected Folder");
				box.setMessage("Failed to browse the folder %s, is it a file or system folder?\nClick 'Retry' to select another folder.");
				
				switch(box.open()) {
				case SWT.RETRY:
					return getUserSelectedFolder(shell, title, message, folder);
				case SWT.CANCEL:
				default:
					return null;
				}
			}
			return check;
		}
		return null;
	}
	
	/** @param args Program command line arguments
	 * @wbp.parser.entryPoint */
	public static final void main(String[] args) {
		System.out.println(getTextFromUser("InputBox Dialog Title", "Hello, world!\nWould you like to enter some text today?\nEnter it below and click 'Submit', or click 'Cancel' to return nothing!"));
	}
	
	/** Prompts the user for some text with the given title and message.
	 * 
	 * @param title The title of the input box dialog
	 * @param message The message of the input box dialog
	 * @return The user's entered text, or <tt>null</tt> if the user cancelled
	 *         the action or closed the dialog */
	public static final String getTextFromUser(String title, String message) {
		Display display = Display.getDefault();
		Shell shell = new Shell(display, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
		shell.setText(title == null ? "InputBox Dialog" : title);
		shell.setSize(450, 320);
		
		final String[] input = {null};
		
		Text txtMessage = new Text(shell, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		txtMessage.setBounds(10, 10, 424, 75);
		txtMessage.setText(message);
		
		Text txtInput = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		txtInput.setBounds(10, 91, 424, 159);
		
		Button btnSubmit = new Button(shell, SWT.NONE);
		btnSubmit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				input[0] = txtInput.getText();
				shell.close();
			}
		});
		btnSubmit.setBounds(10, 256, 209, 25);
		btnSubmit.setText("Submit");
		
		Button btnCancel = new Button(shell, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				input[0] = null;
				shell.close();
			}
		});
		btnCancel.setBounds(225, 256, 209, 25);
		btnCancel.setText("Cancel");
		
		shell.open();
		shell.layout();
		
		while(!shell.isDisposed()) {
			//shell.update();
			if(!display.readAndDispatch()) {
				try {
					Thread.sleep(10L);
				} catch(InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}
		}
		shell.dispose();
		return input[0];
	}
	
	/** Adds an <tt>Undo/Redo/Cut/Copy/Paste/Delete/Select All</tt> right-click
	 * context
	 * menu (Pop-up menu) to the given {@link StyledText}.<br>
	 * If the given {@link UndoRedoImpl} is <tt><b>null</b></tt> (or the styled
	 * text is read only), the Undo and Redo {@link MenuItem}s will not be
	 * present.
	 * 
	 * @param stxt The StyledText that will get a new right-click context menu
	 * @param undoRedoImpl The {@link UndoRedoImpl} to use for the Undo and Redo
	 *            {@link MenuItem}s
	 * @return The newly created {@link Menu} */
	public static final Menu addTextEditorPopupMenu(final StyledText stxt, final UndoRedoImpl undoRedoImpl) {
		Menu menu = new Menu(stxt);
		final Point[] selectionRange = new Point[] {null};
		final String[] clipboardContents = {null};
		
		final MenuItem mntmUndo = new MenuItem(menu, SWT.NONE);
		mntmUndo.setImage(SWTResourceManager.getImage(SWTUtil.class, "/assets/textures/swt/icons/application/arrow_undo.png"));
		mntmUndo.setText("Undo\tCtrl+Z");
		mntmUndo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean readOnly = (stxt.getStyle() & SWT.READ_ONLY) != 0;
				boolean editable = stxt.getEditable() && !readOnly;
				
				if(editable) {
					undoRedoImpl.undo();
				}
			}
		});
		
		final MenuItem mntmRedo = new MenuItem(menu, SWT.NONE);
		mntmRedo.setImage(SWTResourceManager.getImage(SWTUtil.class, "/assets/textures/swt/icons/application/arrow_redo.png"));
		mntmRedo.setText("Redo\tCtrl+Y");
		mntmRedo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean readOnly = (stxt.getStyle() & SWT.READ_ONLY) != 0;
				boolean editable = stxt.getEditable() && !readOnly;
				
				if(editable) {
					undoRedoImpl.redo();
				}
			}
		});
		
		final MenuItem separator_1 = new MenuItem(menu, SWT.SEPARATOR);
		
		final MenuItem mntmCut = new MenuItem(menu, SWT.NONE);
		mntmCut.setImage(SWTResourceManager.getImage(SWTUtil.class, "/assets/textures/swt/icons/application/cut.png"));
		mntmCut.setText("Cut\tCtrl+X");
		mntmCut.setAccelerator(SWT.CTRL | 'X');
		mntmCut.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stxt.invokeAction(ST.CUT);//Totally didn't know this was a thing! Thanks internet! https://stackoverflow.com/a/7193423
			}
		});
		
		final MenuItem mntmCopy = new MenuItem(menu, SWT.NONE);
		mntmCopy.setImage(SWTResourceManager.getImage(SWTUtil.class, "/assets/textures/swt/icons/application/page_copy.png"));
		mntmCopy.setText("Copy\tCtrl+C");
		mntmCopy.setAccelerator(SWT.CTRL | 'X');
		mntmCopy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stxt.invokeAction(ST.COPY);
			}
		});
		
		final MenuItem mntmPaste = new MenuItem(menu, SWT.NONE);
		mntmPaste.setImage(SWTResourceManager.getImage(SWTUtil.class, "/assets/textures/swt/icons/application/paste_plain.png"));
		mntmPaste.setText("Paste\tCtrl+V");
		mntmPaste.setAccelerator(SWT.CTRL | 'X');
		mntmPaste.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stxt.invokeAction(ST.PASTE);
			}
		});
		
		final MenuItem mntmDelete = new MenuItem(menu, SWT.NONE);
		mntmDelete.setImage(SWTResourceManager.getImage(SWTUtil.class, "/assets/textures/swt/icons/application/cross.png"));
		mntmDelete.setText("Delete\tDel");
		mntmDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stxt.invokeAction(ST.DELETE_NEXT);
			}
		});
		
		new MenuItem(menu, SWT.SEPARATOR);
		
		final MenuItem mntmSelectAll = new MenuItem(menu, SWT.NONE);
		mntmSelectAll.setImage(SWTResourceManager.getImage(SWTUtil.class, "/assets/textures/swt/icons/application/page_white_stack.png"));
		mntmSelectAll.setText("Select All\tCtrl+A");
		mntmSelectAll.setAccelerator(SWT.CTRL | 'A');
		mntmSelectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stxt.invokeAction(ST.SELECT_ALL);
			}
		});
		stxt.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if((e.stateMask & SWT.CTRL) != 0 && (e.stateMask & SWT.SHIFT) == 0 && e.keyCode == 'a') {
					stxt.invokeAction(ST.SELECT_ALL);
				}
			}
		});
		
		menu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) {
				boolean readOnly = (stxt.getStyle() & SWT.READ_ONLY) != 0;
				boolean editable = stxt.getEditable() && !readOnly;
				String selection = stxt.getSelectionText();
				selection = selection.isEmpty() ? null : selection;
				selectionRange[0] = selection == null ? null : stxt.getSelection();
				Clipboard clipboard = new Clipboard(stxt.getDisplay());
				Object data = clipboard.getContents(TextTransfer.getInstance());
				clipboardContents[0] = data instanceof String ? (String) data : null;
				clipboard.dispose();
				
				if(readOnly || undoRedoImpl == null) {
					mntmUndo.dispose();
					mntmRedo.dispose();
					separator_1.dispose();
				} else if(!mntmUndo.isDisposed() && !mntmRedo.isDisposed()) {
					mntmUndo.setEnabled(editable && undoRedoImpl.canUndo());
					mntmRedo.setEnabled(editable && undoRedoImpl.canRedo());
				}
				mntmCut.setEnabled(editable && selectionRange[0] != null);
				mntmCopy.setEnabled(selectionRange[0] != null);
				mntmPaste.setEnabled(editable && clipboardContents[0] != null);
				mntmDelete.setEnabled(editable && selectionRange[0] != null);
				mntmSelectAll.setEnabled(true);
			}
		});
		
		stxt.setMenu(menu);
		return menu;
	}
	
	//===================================================================================================================
	
	private static final Class<?> getClass(String name) {
		try {
			return Class.forName(name);
		} catch(LinkageError | ClassNotFoundException ex) {
			return null;
		}
	}
	
	private static final Field getField(Class<?> clazz, String name) {
		for(Field field : clazz.getDeclaredFields()) {
			if(field.getName().equals(name)) {
				return field;
			}
		}
		return null;
	}
	
	private static final Object getValue(Field field, Object obj) {
		boolean accessible = field.isAccessible();
		try {
			field.setAccessible(true);
			return field.get(obj);
		} catch(SecurityException | IllegalArgumentException | IllegalAccessException ex) {
			return null;
		} finally {
			try {
				field.setAccessible(accessible);
			} catch(SecurityException ignored) {
			}
		}
	}
	
	public static final long /*int*/ getHandle(Shell shell) {
		Field handle = getField(Shell.class, "shellHandle");//Linux
		if(handle == null) {
			handle = getField(Control.class, "handle");//Win32
		}
		//handle = handle == null ? getField(Widget.class, "handle") : handle;//Win32
		Object value;
		if(handle == null) {//OSX
			handle = getField(Shell.class, "window");
			if(handle == null) {
				return 0;//Unknown OS or SWT distribution
			}
			Class<?> NSWindow = getClass("org.eclipse.swt.internal.cocoa.NSWindow");
			Class<?> id = getClass("org.eclipse.swt.internal.cocoa.id");
			if(NSWindow == null || id == null) {
				return 0;//Unknown OS or SWT distribution
			}
			Object nsWindow = getValue(handle, shell);
			if(nsWindow == null) {
				return 0;//Unknown SWT distribution/state
			}
			Field _id = getField(id, "id");
			if(_id == null) {
				return 0;
			}
			value = getValue(_id, nsWindow);
		} else {//Windows or Linux
			value = getValue(handle, shell);
		}
		if(value == null) {
			return 0;
		}
		if(value instanceof Long) {
			return ((Long) value).longValue();
		}
		if(value instanceof Integer) {
			return ((Integer) value).longValue();
		}
		return 0;
	}
	
	public static final boolean getAlwaysOnTop(Shell shell) {
		return (shell.getStyle() & SWT.ON_TOP) != 0;
	}
	
	public static final boolean setAlwaysOnTop(Shell shell, boolean onTop) {
		Point location = shell.getLocation();
		Point dimension = shell.getSize();
		switch(Platform.get()) {
		case WINDOWS:
			org.eclipse.swt.internal.win32.OS.SetWindowPos(SWTUtil.getHandle(shell), onTop ? org.eclipse.swt.internal.win32.OS.HWND_TOPMOST : org.eclipse.swt.internal.win32.OS.HWND_NOTOPMOST, location.x, location.y, dimension.x, dimension.y, 0);
			Field style = getField(Widget.class, "style");
			@SuppressWarnings("deprecation")
			boolean wasAccessible = style.isAccessible();
			try {
				style.setAccessible(true);
				int originalStyle = shell.getStyle();
				style.set(shell, Integer.valueOf(onTop ? originalStyle | SWT.ON_TOP : originalStyle & ~SWT.ON_TOP));
			} catch(IllegalArgumentException | IllegalAccessException e) {
				System.err.print("setAlwaysOnTop() threw an error: ");
				e.printStackTrace(System.err);
				System.err.flush();
			} finally {
				style.setAccessible(wasAccessible);
			}
			return true;
		case LINUX:
			// TODO Figure out how to do this for Linux
			return false;
		case MACOSX:
			// TODO Figure out how to do this for MacOS
			return false;
		case UNKNOWN:
		default:
			return false;
		}
		
	}
	
}
