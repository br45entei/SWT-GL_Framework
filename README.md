# SWT-GL_Framework
A window framework combining SWT and LWJGL3 using the awesome <a href="https://github.com/br45entei/lwjgl3-swt">LWJGLX/lwjgl3-swt</a> project.<br>
This project is primarily intended to make it easier to create new 3D desktop games by allowing developers to focus on creating the game itself, rather than worrying about implementing background things such as the main application window, input support, etc.

### How Do I Use It?
You start out by defining your own class which implements either `Game` or `Renderer`, and then you simply do the following:

```Java
import com.gmail.br45entei.game.Game;
import com.gmail.br45entei.game.graphics.Renderer;
import com.gmail.br45entei.game.ui.Window;

public class MyGame implements Game {

	public static void main(String[] args) {
		Game game = new MyGame(...);
		
		Window window = new Window("Window Title", 800, 600, 60.0D); // new Window(title, width, height, framerate);
		window.setRenderer(game);
		window.open();
	}
	
	@Override
	public String getName() {
		return "My Game"
	}
	
	@Override
	public boolean isInitialized() {
	//...
	
}
```

You may also specify OpenGL context creation attributes by passing in a `GLData` object onto the end of the Window constructor.

```Java
import org.lwjgl.opengl.swt.GLData;
	...
	public static void main(String[] args) {
		...
		GLData data = new GLData();
		data.doubleBuffer = true;
		data.swapInterval = Integer.valueOf(1);
		data.majorVersion = 3;
		data.minorVersion = 3;
		data.forwardCompatible = true;
		
		Window window = new Window(title, width, height, framerate, data);
		...
```
