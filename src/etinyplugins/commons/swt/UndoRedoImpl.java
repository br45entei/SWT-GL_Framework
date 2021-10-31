package etinyplugins.commons.swt;

import java.util.HashMap;
import java.util.Stack;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;

/** Adds the Undo-Redo functionality (working Ctrl+Z and Ctrl+Y) to an instance
 * of {@link StyledText}.
 * 
 * @author <a href= "https://sourceforge.net/p/etinyplugins/blog/2013/02/add-undoredo-support-to-your-swt-styledtext-s/">Petr Bodnar</a>
 * @author Brian_Entei &lt;br45entei&#064;gmail.com&gt; (Added {@link #setStack(String)}, {@link #canUndo()}, {@link #canRedo()} to allow
 *         for multiple separate histories with a single styled text)
 * @see {@linkplain <a href= "http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/SWTUndoRedo.htm">Inspiration for this code, though not
 *      really functioning - it mainly shows which listeners to use...</a>}
 * @see {@linkplain <a href="http://stackoverflow.com/questions/7179464/swt-how-to-recreate-a-default-context-menu-for-text-fields">SWT's
 *      StyledText doesn't support Undo-Redo out-of-the-box</a>} */
@SuppressWarnings("javadoc")
public class UndoRedoImpl implements KeyListener, ExtendedModifyListener {
	
	/** Encapsulation of the Undo and Redo stack(s). */
	private static class UndoRedoStack<T> {
		
		private Stack<T> undo;
		private Stack<T> redo;
		
		public UndoRedoStack() {
			this.undo = new Stack<>();
			this.redo = new Stack<>();
		}
		
		public void pushUndo(T delta) {
			this.undo.add(delta);
		}
		
		public void pushRedo(T delta) {
			this.redo.add(delta);
		}
		
		public T popUndo() {
			T res = this.undo.pop();
			return res;
		}
		
		public T popRedo() {
			T res = this.redo.pop();
			return res;
		}
		
		public void clearRedo() {
			this.redo.clear();
		}
		
		public boolean hasUndo() {
			return !this.undo.isEmpty();
		}
		
		public boolean hasRedo() {
			return !this.redo.isEmpty();
		}
		
	}
	
	private StyledText editor;
	private final HashMap<String, UndoRedoStack<ExtendedModifyEvent>> stack = new HashMap<>();
	//private boolean isUndo;
	//private boolean isRedo;
	private final HashMap<String, boolean[]> stackFlags = new HashMap<>();
	private volatile String selectedStack;
	
	/** Creates a new instance of this class. Automatically starts listening to
	 * corresponding key and modify events coming from the given
	 * <var>editor</var>.
	 * 
	 * @param editor
	 *            the text field to which the Undo-Redo functionality should be
	 *            added */
	public UndoRedoImpl(StyledText editor) {
		editor.addExtendedModifyListener(this);
		editor.addKeyListener(this);
		this.editor = editor;
		this.setStack("<default>");
	}
	
	/** @param stackName The unique name of the stack to set
	 * @author Brian_Entei
	 * @return This {@link UndoRedoImpl} */
	public UndoRedoImpl setStack(String stackName) {
		if(stackName == null || stackName.isEmpty()) {
			stackName = "<default>";
		}
		this.selectedStack = stackName;
		if(this.stack.get(this.selectedStack) == null) {
			this.stack.put(this.selectedStack, new UndoRedoStack<>());
		}
		if(this.stackFlags.get(this.selectedStack) == null) {
			this.stackFlags.put(this.selectedStack, new boolean[2]);
		}
		return this;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		// Listen to CTRL+Z for Undo, to CTRL+Y or CTRL+SHIFT+Z for Redo
		boolean isCtrl = (e.stateMask & SWT.CTRL) > 0;
		boolean isAlt = (e.stateMask & SWT.ALT) > 0;
		if(isCtrl && !isAlt) {
			boolean isShift = (e.stateMask & SWT.SHIFT) > 0;
			if(!isShift && e.keyCode == 'z') {
				this.undo();
			} else if(!isShift && e.keyCode == 'y' || isShift && e.keyCode == 'z') {
				this.redo();
			}
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		// ignore
	}
	
	/** Creates a corresponding Undo or Redo step from the given event and
	 * pushes
	 * it to the stack. The Redo stack is, logically, emptied if the event comes
	 * from a normal user action.
	 * 
	 * @param event
	 * @see org.eclipse.swt.custom.ExtendedModifyListener#modifyText(org.eclipse.
	 *      swt.custom.ExtendedModifyEvent) */
	@Override
	public void modifyText(ExtendedModifyEvent event) {
		UndoRedoStack<ExtendedModifyEvent> stack = this.stack.get(this.selectedStack);
		boolean[] stackFlags = this.stackFlags.get(this.selectedStack);
		
		if(stackFlags[0]) {//isUndo
			stack.pushRedo(event);
		} else { // is Redo or a normal user action
			stack.pushUndo(event);
			if(!stackFlags[1]) {//!isRedo
				stack.clearRedo();
				// TODO Switch to treat consecutive characters as one event?
			}
		}
	}
	
	/** Returns true if there are any undo action available.
	 * 
	 * @author Brian_Entei
	 * @return Whether or not the current stack has any undo actions
	 *         available */
	public boolean canUndo() {
		UndoRedoStack<ExtendedModifyEvent> stack = this.stack.get(this.selectedStack);
		return stack.hasUndo();
	}
	
	/** Returns true if there are any redo action available.
	 * 
	 * @author Brian_Entei
	 * @return Whether or not the current stack has any redo actions
	 *         available */
	public boolean canRedo() {
		UndoRedoStack<ExtendedModifyEvent> stack = this.stack.get(this.selectedStack);
		return stack.hasRedo();
	}
	
	/** Performs the Undo action. A new corresponding Redo step is automatically
	 * pushed to the stack.
	 * 
	 * @return This {@link UndoRedoImpl} */
	public UndoRedoImpl undo() {
		UndoRedoStack<ExtendedModifyEvent> stack = this.stack.get(this.selectedStack);
		boolean[] stackFlags = this.stackFlags.get(this.selectedStack);
		
		if(stack.hasUndo()) {
			stackFlags[0] = true;//this.isUndo = true;
			this.revertEvent(stack.popUndo());
			stackFlags[0] = false;//this.isUndo = false;
		}
		return this;
	}
	
	/** Performs the Redo action. A new corresponding Undo step is automatically
	 * pushed to the stack.
	 * 
	 * @return This {@link UndoRedoImpl} */
	public UndoRedoImpl redo() {
		UndoRedoStack<ExtendedModifyEvent> stack = this.stack.get(this.selectedStack);
		boolean[] stackFlags = this.stackFlags.get(this.selectedStack);
		
		if(stack.hasRedo()) {
			stackFlags[1] = true;//this.isRedo = true;
			this.revertEvent(stack.popRedo());
			stackFlags[1] = false;//this.isRedo = false;
		}
		return this;
	}
	
	/** Reverts the given modify event, in the way as the Eclipse text editor
	 * does it.
	 * 
	 * @param event */
	private void revertEvent(ExtendedModifyEvent event) {
		this.editor.replaceTextRange(event.start, event.length, event.replacedText);
		// (causes the modifyText() listener method to be called)
		this.editor.setSelectionRange(event.start, event.replacedText.length());
	}
	
}
