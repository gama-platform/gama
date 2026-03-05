import re

file_path = "gama.ui.display.opengl4/src/gama/ui/display/opengl4/view/NEWTLayeredDisplayMultiListener.java"

with open(file_path, "r") as f:
    content = f.read()

# We can refactor the switch case into a map lookup or a method to simplify `keyPressed` and `keyReleased`
extract_map_method = """
	private int mapKeyCodeToEventAction(int keyCode) {
		return switch (keyCode) {
			case KeyEvent.VK_UP -> IEventLayerListener.ARROW_UP;
			case KeyEvent.VK_DOWN -> IEventLayerListener.ARROW_DOWN;
			case KeyEvent.VK_LEFT -> IEventLayerListener.ARROW_LEFT;
			case KeyEvent.VK_RIGHT -> IEventLayerListener.ARROW_RIGHT;
			case KeyEvent.VK_PAGE_UP -> IEventLayerListener.KEY_PAGE_UP;
			case KeyEvent.VK_PAGE_DOWN -> IEventLayerListener.KEY_PAGE_DOWN;
			case KeyEvent.VK_ESCAPE -> IEventLayerListener.KEY_ESC;
			case KeyEvent.VK_ENTER -> IEventLayerListener.KEY_RETURN;
			case KeyEvent.VK_TAB -> IEventLayerListener.KEY_TAB;
			case KeyEvent.VK_SHIFT -> IEventLayerListener.KEY_SHIFT;
			case KeyEvent.VK_ALT -> IEventLayerListener.KEY_ALT;
			case KeyEvent.VK_CONTROL -> IEventLayerListener.KEY_CTRL;
			case KeyEvent.VK_META -> IEventLayerListener.KEY_CMD;
			default -> 0;
		};
	}
"""

content = re.sub(
    r"delegate\.specialKeyPressed\(switch \(e\.getKeyCode\(\)\) \{.*?default -> 0;\s*\}\);",
    "delegate.specialKeyPressed(mapKeyCodeToEventAction(e.getKeyCode()));",
    content,
    flags=re.DOTALL
)

content = re.sub(
    r"delegate\.specialKeyReleased\(switch \(e\.getKeyCode\(\)\) \{.*?default -> 0;\s*\}\);",
    "delegate.specialKeyReleased(mapKeyCodeToEventAction(e.getKeyCode()));",
    content,
    flags=re.DOTALL
)

# Insert the method near the top of the class
insert_pattern = re.compile(r'(public class NEWTLayeredDisplayMultiListener implements MouseListener, KeyListener \{)')
content = insert_pattern.sub(r'\1' + "\n" + extract_map_method, content)

with open(file_path, "w") as f:
    f.write(content)
