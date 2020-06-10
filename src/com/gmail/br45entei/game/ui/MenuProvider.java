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
package com.gmail.br45entei.game.ui;

import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.lwjgl.opengl.swt.GLCanvas;

/** MenuProvider is an interface which provides developers a way to add
 * context menus to their game implementations.<br>
 * 'MenuBar' menus appear in the top menu bar of the main application
 * {@link Window}, and 'PopupMenu' menus appear in the right-click context
 * menu of the main Window's {@link GLCanvas}.
 * 
 * @author Brian_Entei
 * @since 1.0 */
public interface MenuProvider {
	
	/** Called to retrieve the name of the {@link MenuItem} within the
	 * primary MenuBar that this provider will populate.
	 * 
	 * @return The name of the primary {@link MenuItem} that this provider
	 *         will populate */
	public String getMenuName();
	
	/** Called when a new {@link Menu MenuBar} is being created for the main
	 * {@link Window}.<br>
	 * This allows you to use the provided menu to add your own
	 * {@link org.eclipse.swt.widgets.MenuItem menu items} which can perform
	 * various tasks when clicked.
	 * 
	 * @param menu The {@link Menu MenuBar} that you can populate with your
	 *            own {@link org.eclipse.swt.widgets.MenuItem menu items} */
	public void onMenuBarCreation(Menu menu);
	
	/** Called when the main {@link Window}'s existing {@link Menu MenuBar}
	 * is about to be disposed.<br>
	 * This gives you the opportunity to free up any system resources and
	 * perform any necessary tasks before the menu is destroyed.
	 * 
	 * @param menu The {@link Menu MenuBar} that is about to be disposed */
	public void onMenuBarDeletion(Menu menu);
	
	/** Called when a new {@link Menu PopupMenu} is being created for the
	 * main @link Window}'s {@link GLCanvas}.<br>
	 * This allows you to use the provided menu to add your own
	 * {@link org.eclipse.swt.widgets.MenuItem menu items} which can perform
	 * various tasks when clicked.
	 * 
	 * @param menu The {@link Menu PopupMenu} that you can populate with
	 *            your
	 *            own {@link org.eclipse.swt.widgets.MenuItem menu items} */
	public void onPopupMenuCreation(Menu menu);
	
	/** Called when the main {@link Window}'s {@link GLCanvas}' existing
	 * {@link Menu PopupMenu} (right-click menu) is about to be
	 * disposed.<br>
	 * This gives you the opportunity to free up any system resources and
	 * perform any necessary tasks before the menu is destroyed.
	 * 
	 * @param menu The {@link Menu PopupMenu} that is about to be
	 *            disposed */
	public void onPopupMenuDeletion(Menu menu);
	
	/** Gives this provider a chance to handle any exceptions that it might
	 * throw.<br>
	 * If the exception is not handled, this provider is removed from the
	 * listeners queue to prevent future unhandled exceptions.
	 * 
	 * @param ex The exception that this provider threw
	 * @param method This provider's method that threw the error
	 * @param params The method parameters (if any) that were passed in
	 * @return Whether or not this provider has handled the exception */
	public boolean handleException(Throwable ex, String method, Object... params);
	
}
