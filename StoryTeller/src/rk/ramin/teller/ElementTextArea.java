package rk.ramin.teller;

import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class ElementTextArea extends TextArea implements BriefedNode, Saveable {
	
	private static EventHandler<KeyEvent> listener = new EventHandler<KeyEvent>() {
		@Override
		public void handle(KeyEvent event) {
			ElementTextArea source = (ElementTextArea) event.getSource();
			if (event.getCode() == KeyCode.ENTER && (event.isShiftDown() || event.isControlDown())) {
				source.receptor.onButtonClicked(StyledButtonType.ADD_SMALL, source.owner);
			} else if (event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE) {
				if (source.getText().equals("")) {
					source.receptor.onButtonClicked(StyledButtonType.REMOVE_SMALL, source.owner);
				}
			}
		}
	};
	
	private Area receptor;
	private ButtonProvider owner;
	
	public ElementTextArea(ConstructionData data) {
		setFont(EditorWindow.getOtherFontType(getFont(), FontWeight.BOLD, FontPosture.REGULAR));
		setPrefRowCount(3);
		setWrapText(true);
		setOnKeyPressed(listener);
		getStyleClass().add("element-text-area");
	}

	@Override
	public void brief(ButtonProvider owner, Area receptor) {
		this.owner = owner;
		this.receptor = receptor;
	}
	
	public void requestFocus() {
		super.requestFocus();
		selectEnd();
	}

	@Override
	public Object save() {
		return getText();
	}

	@Override
	public void load(Object o) {
		setText((String)o);
	}
}
