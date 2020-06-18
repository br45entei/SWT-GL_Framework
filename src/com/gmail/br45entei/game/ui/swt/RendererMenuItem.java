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
package com.gmail.br45entei.game.ui.swt;

import com.gmail.br45entei.game.graphics.Renderer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;

/** RendererMenuItem is a simple wrapper-class which extends {@link MenuItem},
 * allowing for the storage and retrieval of a {@link Renderer} object.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>CHECK, CASCADE, PUSH, RADIO, SEPARATOR</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Arm, Help, Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles CHECK, CASCADE, PUSH, RADIO and SEPARATOR
 * may be specified.
 * </p>
 *
 * @author Brian_Entei */
public class RendererMenuItem extends MenuItem {
	
	private final Renderer renderer;
	
	/** Constructs a new instance of this class given its parent
	 * (which must be a <code>Menu</code>) and a style value
	 * describing its behavior and appearance. The item is added
	 * to the end of the items maintained by its parent.
	 * <p>
	 * The style value is either one of the style constants defined in
	 * class <code>SWT</code> which is applicable to instances of this
	 * class, or must be built by <em>bitwise OR</em>'ing together
	 * (that is, using the <code>int</code> "|" operator) two or more
	 * of those <code>SWT</code> style constants. The class description
	 * lists the style constants that are applicable to the class.
	 * Style bits are also inherited from superclasses.
	 * </p>
	 *
	 * @param parent a menu control which will be the parent of the new instance
	 *            (cannot be null)
	 * @param renderer The {@link Renderer} that this MenuItem will store
	 * @param style the style of control to construct
	 * 			
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the parent</li>
	 *                <li>ERROR_INVALID_SUBCLASS - if this class is not an
	 *                allowed subclass</li>
	 *                </ul>
	 *
	 * @see SWT#CHECK
	 * @see SWT#CASCADE
	 * @see SWT#PUSH
	 * @see SWT#RADIO
	 * @see SWT#SEPARATOR
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle */
	public RendererMenuItem(Menu parent, Renderer renderer, int style) {
		super(parent, style);
		this.renderer = renderer;
	}
	
	/** Constructs a new instance of this class given its parent
	 * (which must be a <code>Menu</code>), a style value
	 * describing its behavior and appearance, and the index
	 * at which to place it in the items maintained by its parent.
	 * <p>
	 * The style value is either one of the style constants defined in
	 * class <code>SWT</code> which is applicable to instances of this
	 * class, or must be built by <em>bitwise OR</em>'ing together
	 * (that is, using the <code>int</code> "|" operator) two or more
	 * of those <code>SWT</code> style constants. The class description
	 * lists the style constants that are applicable to the class.
	 * Style bits are also inherited from superclasses.
	 * </p>
	 *
	 * @param parent a menu control which will be the parent of the new instance
	 *            (cannot be null)
	 * @param renderer The {@link Renderer} that this MenuItem will store
	 * @param style the style of control to construct
	 * @param index the zero-relative index to store the receiver in its parent
	 * 			
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *                <li>ERROR_INVALID_RANGE - if the index is not between 0
	 *                and the number of elements in the parent (inclusive)</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the parent</li>
	 *                <li>ERROR_INVALID_SUBCLASS - if this class is not an
	 *                allowed subclass</li>
	 *                </ul>
	 *
	 * @see SWT#CHECK
	 * @see SWT#CASCADE
	 * @see SWT#PUSH
	 * @see SWT#RADIO
	 * @see SWT#SEPARATOR
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle */
	public RendererMenuItem(Menu parent, Renderer renderer, int style, int index) {
		super(parent, style, index);
		this.renderer = renderer;
	}
	
	@Override
	protected void checkSubclass() {
	}
	
	/** @return The {@link Renderer} that this {@link MenuItem} has stored */
	public Renderer getRenderer() {
		return this.renderer;
	}
	
}
