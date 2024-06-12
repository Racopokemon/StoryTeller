package rk.ramin.teller;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class ElementTextField extends TextField implements BriefedNode, Saveable {

	private ButtonProvider owner;
	private Area receptor;
	
	private static EventHandler<KeyEvent> keyListener = new EventHandler<KeyEvent>() {
		public void handle(KeyEvent event) {
			ElementTextField etf = (ElementTextField)event.getSource();
			if (etf.receptor == null) {
				return;
			}
			if (event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE) {
				if (etf.getText().equals("")) {
					etf.receptor.onButtonClicked(StyledButtonType.REMOVE_SMALL, etf.owner);
				}
			} else if (event.getCode() == KeyCode.ENTER) {
				etf.receptor.onButtonClicked(StyledButtonType.ADD_SMALL, etf.owner);
			}
		}
	};
	
	public ElementTextField(ConstructionData data) {
		setFont(EditorWindow.getOtherFontType(getFont(), FontWeight.BOLD, FontPosture.REGULAR));
		//setOnAction(actionListener);
		setOnKeyPressed(keyListener);
		getStyleClass().add("element-text-field");
	}
	
	@Override
	public void brief(ButtonProvider owner, Area receptor) {
		this.owner = owner;
		this.receptor = receptor;
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
