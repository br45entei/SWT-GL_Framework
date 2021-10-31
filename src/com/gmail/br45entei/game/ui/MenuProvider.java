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
package com.gmail.br45entei.game.ui;

import com.gmail.br45entei.thread.ThreadType;
import com.gmail.br45entei.thread.UsedBy;

import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.lwjgl.opengl.swt.GLCanvas;

/** MenuProvider is an interface which provides developers a way to add context menus to their game implementations.<br>
 * 'MenuBar' menus appear in the top menu bar of the main application {@link Window}, and 'PopupMenu' menus appear in the right-click
 * context menu of the main Window's {@link GLCanvas}.
 * 
 * @since 1.0
 * @author Brian_Entei */
public interface MenuProvider {
	
	/** Called to retrieve the name of the {@link MenuItem} within the primary MenuBar that this provider will populate.
	 *
	 * @return The name of the primary {@link MenuItem} that this provider
	 *         will populate
	 * @see ThreadType#UNSPECIFIED */
	@UsedBy(ThreadType.UNSPECIFIED)
	public String getMenuName();
	
	/** Called when a new {@link Menu MenuBar} is being created for the main {@link Window}.<br>
	 * This allows you to use the provided menu to add your own {@link org.eclipse.swt.widgets.MenuItem menu items} which can perform
	 * various tasks when clicked.
	 *
	 * @param menu The {@link Menu MenuBar} that you can populate with your
	 *            own {@link org.eclipse.swt.widgets.MenuItem menu items}
	 * @see ThreadType#UI */
	@UsedBy(ThreadType.UI)
	public void onMenuBarCreation(Menu menu);
	
	/** Called when the main {@link Window}'s existing {@link Menu MenuBar} is about to be disposed.<br>
	 * This gives you the opportunity to free up any system resources and perform any necessary tasks before the menu is destroyed.
	 *
	 * @param menu The {@link Menu MenuBar} that is about to be disposed
	 * @see ThreadType#UI */
	@UsedBy(ThreadType.UI)
	public void onMenuBarDeletion(Menu menu);
	
	/** Called when the main {@link Window} thread needs to know whether or not this {@link MenuProvider} provides a popup (right click)
	 * menu.
	 * 
	 * @return Whether or not this MenuProvider provides a popup menu. */
	public boolean providesPopupMenu();
	
	/** Called when a new {@link Menu PopupMenu} (right-click menu) is being created for the main @link Window}'s {@link GLCanvas}.<br>
	 * This allows you to use the provided menu to add your own {@link org.eclipse.swt.widgets.MenuItem menu items} which can perform
	 * various tasks when clicked.
	 *
	 * @param menu The {@link Menu PopupMenu} that you can populate with your
	 *            own {@link org.eclipse.swt.widgets.MenuItem menu items}
	 * @see ThreadType#UI */
	@UsedBy(ThreadType.UI)
	public void onPopupMenuCreation(Menu menu);
	
	/** Called when the main {@link Window}'s {@link GLCanvas}' existing {@link Menu PopupMenu} (right-click menu) is about to be
	 * disposed.<br>
	 * This gives you the opportunity to free up any system resources and perform any necessary tasks before the menu is destroyed.
	 *
	 * @param menu The {@link Menu PopupMenu} that is about to be disposed
	 * @see ThreadType#UI */
	@UsedBy(ThreadType.UI)
	public void onPopupMenuDeletion(Menu menu);
	
	/** Called by the {@link Window}'s display thread once per 'tick' to allow {@link MenuProvider}s to update their menu items when
	 * necessary.
	 *
	 * @see ThreadType#UI */
	@UsedBy(ThreadType.UI)
	public void updateMenuItems();
	
	/** Gives this provider a chance to handle any exceptions that it might throw.<br>
	 * If the exception is not handled, this provider is removed from the listeners queue to prevent future unhandled exceptions.
	 *
	 * @param ex The exception that this provider threw
	 * @param method This provider's method that threw the error
	 * @param params The method parameters (if any) that were passed in
	 * @return Whether or not this provider has handled the exception
	 * @see ThreadType#UI */
	@UsedBy(ThreadType.UI)
	public boolean handleException(Throwable ex, String method, Object... params);
	
}
