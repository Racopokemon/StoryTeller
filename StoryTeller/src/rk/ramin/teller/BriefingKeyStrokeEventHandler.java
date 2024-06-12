package rk.ramin.teller;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * This small listener class listens to the following key events, can be briefed and performs the usual calls 
 * (add on enter / shift+enter, you might choose this in the constructor, remove on backspace or delete)
 * @author Der Guru
 *
 */
public class BriefingKeyStrokeEventHandler implements EventHandler<KeyEvent>, BriefedNode {
	
	private boolean needsShift;
	
	private ButtonProvider owner;
	private Area receptor;
	
	public BriefingKeyStrokeEventHandler(boolean reactToShiftEnterInstead) {
		needsShift = reactToShiftEnterInstead;
	}
	@Override
	public void handle(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER && (!needsShift || (event.isShiftDown() || event.isControlDown()))) {
			receptor.onButtonClicked(StyledButtonType.ADD_SMALL, owner);
		} else if (event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE) {
			receptor.onButtonClicked(StyledButtonType.REMOVE_SMALL, owner);
		}
	}
	@Override
	public void brief(ButtonProvider owner, Area receptor) {
		this.owner = owner;
		this.receptor = receptor;
	}
}
