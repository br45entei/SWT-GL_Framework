package uk.co.electronstudio.sdl2gdx;


import static org.libsdl.SDL.SDL_CONTROLLER_AXIS_MAX;
import static org.libsdl.SDL.SDL_CONTROLLER_BUTTON_DPAD_DOWN;
import static org.libsdl.SDL.SDL_CONTROLLER_BUTTON_DPAD_LEFT;
import static org.libsdl.SDL.SDL_CONTROLLER_BUTTON_DPAD_RIGHT;
import static org.libsdl.SDL.SDL_CONTROLLER_BUTTON_DPAD_UP;
import static org.libsdl.SDL.SDL_CONTROLLER_BUTTON_MAX;
import static org.libsdl.SDL.SDL_CONTROLLER_TYPE_NINTENDO_SWITCH_PRO;
import static org.libsdl.SDL.SDL_CONTROLLER_TYPE_PS3;
import static org.libsdl.SDL.SDL_CONTROLLER_TYPE_PS4;
import static org.libsdl.SDL.SDL_CONTROLLER_TYPE_VIRTUAL;
import static org.libsdl.SDL.SDL_CONTROLLER_TYPE_XBOX360;
import static org.libsdl.SDL.SDL_CONTROLLER_TYPE_XBOXONE;
import static org.libsdl.SDL.SDL_HAT_DOWN;
import static org.libsdl.SDL.SDL_HAT_LEFT;
import static org.libsdl.SDL.SDL_HAT_LEFTDOWN;
import static org.libsdl.SDL.SDL_HAT_LEFTUP;
import static org.libsdl.SDL.SDL_HAT_RIGHT;
import static org.libsdl.SDL.SDL_HAT_RIGHTDOWN;
import static org.libsdl.SDL.SDL_HAT_RIGHTUP;
import static org.libsdl.SDL.SDL_HAT_UP;

import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.UUID;

import org.libsdl.SDL;
import org.libsdl.SDL_Error;
import org.libsdl.SDL_GameController;
import org.libsdl.SDL_Joystick;

// TODO implement native SDL events.  Tried but they don't seem to work reliably on MacOS!

public class SDL2Controller implements RumbleController {
    final SDL2ControllerManager manager;
    final Array<ControllerListener> listeners = new Array<ControllerListener>();
    final int device_index;
    public final SDL_Joystick joystick;
    final SDL_GameController controller;
    final float[] axisState;
    final boolean[] buttonState;
    final PovDirection[] hatState;
    final static Vector3 zero = new Vector3(0, 0, 0);

    volatile String lastKnownJoystickName;

    public SDL2Controller(SDL2ControllerManager manager, int device_index) throws SDL_Error {
        this.manager = manager;
        this.device_index = device_index;

        this.joystick = SDL_Joystick.JoystickOpen(device_index);

        this.hatState = new PovDirection[this.joystick.numHats()];

        if (SDL.SDL_IsGameController(device_index)) {
            this.controller = SDL_GameController.GameControllerOpen(device_index);
            this.buttonState = new boolean[SDL_CONTROLLER_BUTTON_MAX];
            this.axisState = new float[SDL_CONTROLLER_AXIS_MAX];
        } else {
            this.controller = null;
            this.buttonState = new boolean[this.joystick.numButtons()];
            this.axisState = new float[this.joystick.numAxes()];
        }
        System.out.println("joystick " + this.joystick + " controller " + this.controller);
        if (this.joystick == null && this.controller == null) throw new SDL_Error();

        this.toString();

    }

    public boolean isConnected() {
        return this.joystick.getAttached();
    }
//	public SDL2Controller(SDL2ControllerManager manager, SDL_Joystick joystick) {
//		this(manager, joystick, null);
//	}
//
//	public SDL2Controller(SDL2ControllerManager manager, SDL_Joystick joystick, SDL_GameController controller) {
//		this.manager = manager;
//		this.joystick = joystick;
//		this.controller = controller;
////		this.axisState = new float[GLFW.glfwGetJoystickAxes(index).limit()];
////		this.buttonState = new boolean[GLFW.glfwGetJoystickButtons(index).limit()];
////		this.hatState = new byte[GLFW.glfwGetJoystickHats(index).limit()];
////		this.name = GLFW.glfwGetJoystickName(index);
//	}

    void pollState() throws SDL_Error {
//		if(!GLFW.glfwJoystickPresent(index)) {
//			manager.disconnected(this);
//			return;
//		}
//
//		FloatBuffer axes = GLFW.glfwGetJoystickAxes(index);
//		if(axes == null) {
//			manager.disconnected(this);
//			return;
//		}
//		ByteBuffer buttons = GLFW.glfwGetJoystickButtons(index);
//		if(buttons == null) {
//			manager.disconnected(this);
//			return;
//		}
//		ByteBuffer hats = GLFW.glfwGetJoystickHats(index);
//		if(hats == null) {
//			manager.disconnected(this);
//			return;
//		}
//
//		for(int i = 0; i < axes.limit(); i++) {
//			if(axisState[i] != axes.get(i)) {
//				for(ControllerListener listener: listeners) {
//					listener.axisMoved(this, i, axes.get(i));
//				}
//				manager.axisChanged(this, i, axes.get(i));
//			}
//			axisState[i] = axes.get(i);
//		}


        for (int i = 0; i < this.axisState.length; i++) {
            if (this.axisState[i] != getAxis(i)) {
                for (ControllerListener listener : this.listeners) {
                    listener.axisMoved(this, i, getAxis(i));
                }
                this.manager.axisChanged(this, i, getAxis(i));
            }
            this.axisState[i] = getAxis(i);
        }


        for (int i = 0; i < this.buttonState.length; i++) {
            if (this.buttonState[i] != getButton(i)) {
                for (ControllerListener listener : this.listeners) {
                    if (getButton((i))) {
                        listener.buttonDown(this, i);
                    } else {
                        listener.buttonUp(this, i);
                    }
                }
                this.manager.buttonChanged(this, i, getButton(i));
            }
            this.buttonState[i] = getButton(i);
        }

        for (int i = 0; i < this.hatState.length; i++) {
            if (this.hatState[i] != getPov(i)) {
                this.hatState[i] = getPov(i);
                for (ControllerListener listener : this.listeners) {
                    listener.povMoved(this, i, getPov(i));
                }
                this.manager.hatChanged(this, i, getPov(i));
            }
        }

    }

    @Override
    public void addListener(ControllerListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(ControllerListener listener) {
        this.listeners.removeValue(listener, true);
    }

    @Override
    public boolean getButton(int buttonCode) {
        if (this.controller != null) {
            return this.controller.getButton(buttonCode);
        } else {
            return this.joystick.getButton(buttonCode);
        }
    }

    @Override
    public float getAxis(int axisCode) {
        if (this.controller != null) {
            return this.controller.getAxis(axisCode);
        } else {
            return this.joystick.getAxis(axisCode);
        }
    }

    @Override
    public PovDirection getPov(int povCode) {
        if (this.controller != null) {
            if (this.buttonState[SDL_CONTROLLER_BUTTON_DPAD_UP] && this.buttonState[SDL_CONTROLLER_BUTTON_DPAD_RIGHT])
                return PovDirection.northEast;
            else if (this.buttonState[SDL_CONTROLLER_BUTTON_DPAD_UP] && this.buttonState[SDL_CONTROLLER_BUTTON_DPAD_LEFT])
                return PovDirection.northWest;
            else if (this.buttonState[SDL_CONTROLLER_BUTTON_DPAD_DOWN] && this.buttonState[SDL_CONTROLLER_BUTTON_DPAD_RIGHT])
                return PovDirection.southEast;
            else if (this.buttonState[SDL_CONTROLLER_BUTTON_DPAD_DOWN] && this.buttonState[SDL_CONTROLLER_BUTTON_DPAD_LEFT])
                return PovDirection.southWest;
            else if (this.buttonState[SDL_CONTROLLER_BUTTON_DPAD_UP]) return PovDirection.north;
            else if (this.buttonState[SDL_CONTROLLER_BUTTON_DPAD_RIGHT]) return PovDirection.east;
            else if (this.buttonState[SDL_CONTROLLER_BUTTON_DPAD_DOWN]) return PovDirection.south;
            else if (this.buttonState[SDL_CONTROLLER_BUTTON_DPAD_LEFT]) return PovDirection.west;
            else return PovDirection.center;
        } else if (this.joystick != null) {
            switch (this.joystick.getHat(povCode)) {
                case SDL_HAT_UP:
                    return PovDirection.north;
                case SDL_HAT_DOWN:
                    return PovDirection.south;
                case SDL_HAT_RIGHT:
                    return PovDirection.east;
                case SDL_HAT_LEFT:
                    return PovDirection.west;
                case SDL_HAT_RIGHTUP:
                    return PovDirection.northEast;
                case SDL_HAT_RIGHTDOWN:
                    return PovDirection.southEast;
                case SDL_HAT_LEFTUP:
                    return PovDirection.northWest;
                case SDL_HAT_LEFTDOWN:
                    return PovDirection.southWest;
                default:
                    return PovDirection.center;
            }
        } else return PovDirection.center;
    }

    @Override
    public boolean getSliderX(int sliderCode) {
        return false;
    }

    @Override
    public boolean getSliderY(int sliderCode) {
        return false;
    }

    @Override
    public Vector3 getAccelerometer(int accelerometerCode) {
        return zero;
    }

    @Override
    public void setAccelerometerSensitivity(float sensitivity) {
    }

    @Override
    public String getName() {
        if(this.device_index >= 0) {
            String name = SDL_Joystick.joystickNameForIndex(this.device_index);
            if(name != null) {
                this.lastKnownJoystickName = name;
                return "[".concat(Integer.toString(this.device_index)).concat("] ").concat(name);
            }
            return "[".concat(Integer.toString(this.device_index)).concat("] ").concat(this.getLastKnownJoystickName());//.concat(" <Disconnected>");
        }
        if (this.controller != null) {
            return "SDL GameController " + this.controller.name();
        }
        return "SDL Joystick " + this.joystick.name();
    }

    public String getLastKnownJoystickName() {
        if(this.device_index >= 0) {
            String name = SDL_Joystick.joystickNameForIndex(this.device_index);
            if(name != null) {
                this.lastKnownJoystickName = name;
            }
        }
        return this.lastKnownJoystickName == null ? "" : this.lastKnownJoystickName;
    }

    public String getGUID() {
        String guid = this.joystick.GUID();
        try {
            return UUID.fromString(guid).toString();
        } catch(IllegalArgumentException | NullPointerException ex) {
            if(!guid.contains("-")) {
                if(guid.length() == 32) {
                    return guid.substring(0, 8).concat("-").concat(guid.substring(8, 12)).concat("-").concat(guid.substring(12, 16)).concat("-").concat(guid.substring(16, 20)).concat("-").concat(guid.substring(20));
                }
            }
            return "00000000-0000-0000-0000-000000000000";
        }
    }

    public int getID() {
        return this.joystick.instanceID().id;
    }

    @Override
    public String toString() {
        return getName() + " instance:" + this.joystick.instanceID() + " " + " guid: " + this.joystick.GUID() + " v " + this.joystick.productVersion(this.device_index);
    }

    public void close() {
        this.joystick.close();
        if (this.controller != null) this.controller.close();
    }

    /**
     * Vibrate the controller using the new rumble API
     * This will return false if the controller doesn't support vibration or if SDL was unable to start
     * vibration (maybe the controller doesn't support left/right vibration, maybe it was unplugged in the
     * middle of trying, etc...)
     *
     * @param leftMagnitude  The speed for the left motor to vibrate (this should be between 0 and 1)
     * @param rightMagnitude The speed for the right motor to vibrate (this should be between 0 and 1)
     * @return Whether or not the controller was able to be vibrated (i.e. if rumble is supported)
     */
    @Override
	public boolean rumble(float leftMagnitude, float rightMagnitude, int duration_ms) {
        return this.joystick.rumble(leftMagnitude, rightMagnitude, duration_ms);
    }

    public enum PowerLevel {
        UNKNOWN, EMPTY, LOW, MEDIUM, FULL, WIRED, MAX
    }

    public PowerLevel getPowerLevel() {
        switch (this.joystick.currentPowerLevel()) {
            case SDL.SDL_JOYSTICK_POWER_EMPTY:
                return PowerLevel.EMPTY;
            case SDL.SDL_JOYSTICK_POWER_LOW:
                return PowerLevel.LOW;
            case SDL.SDL_JOYSTICK_POWER_MEDIUM:
                return PowerLevel.MEDIUM;
            case SDL.SDL_JOYSTICK_POWER_FULL:
                return PowerLevel.FULL;
            case SDL.SDL_JOYSTICK_POWER_WIRED:
                return PowerLevel.WIRED;
            case SDL.SDL_JOYSTICK_POWER_MAX:
                return PowerLevel.MAX;
        }
        return PowerLevel.UNKNOWN;
    }

    public ControllerType getType() {
        if (this.controller != null) {
            switch (this.controller.getType()) {
                case SDL_CONTROLLER_TYPE_XBOX360:
                    return ControllerType.XBOX360;
                case SDL_CONTROLLER_TYPE_XBOXONE:
                    return ControllerType.XBOXONE;
                case SDL_CONTROLLER_TYPE_PS3:
                    return ControllerType.PS3;
                case SDL_CONTROLLER_TYPE_PS4:
                    return ControllerType.PS4;
                case SDL_CONTROLLER_TYPE_NINTENDO_SWITCH_PRO:
                    return ControllerType.NINTENDO_SWITCH_PRO;
                case SDL_CONTROLLER_TYPE_VIRTUAL:
                    return ControllerType.VIRTUAL;
            }
        }
        return ControllerType.UNKNOWN;
    }

    public int getPlayerIndex() {
        if (this.controller != null) {
            return this.controller.getPlayerIndex();
        }
        return -1;
    }


    public enum ControllerType {
        UNKNOWN, XBOX360, XBOXONE, PS3, PS4, NINTENDO_SWITCH_PRO, VIRTUAL
    }

}
