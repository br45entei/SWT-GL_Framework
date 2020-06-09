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
package com.gmail.br45entei.game;

import com.gmail.br45entei.game.GLThread.Renderer;
import com.gmail.br45entei.game.Window.MenuProvider;
import com.gmail.br45entei.game.input.InputCallback;

/** Game is an interface which ties {@link Renderer}, {@link InputCallback}
 * together as one, making it easier for developers to create their own 3D
 * desktop applications.<br>
 * <br>
 * If you would like to add context menus to your game, have it implement
 * {@link MenuProvider}.<br>
 * <br>
 * <b>Note:</b>&nbsp;The methods that this interface provides are called by
 * different threads, depending on what super interface defines them.<br>
 * While this will have been taken into account behind the scenes, you should
 * still take care to ensure that code from one thread doesn't attempt to call
 * code intended for another thread (e.g. OpenGL code being called by the
 * Window's display thread or vice versa), or exceptions may be thrown.
 *
 * @author Brian_Entei
 * @since 1.0 */
public interface Game extends Renderer, InputCallback {
	
}
