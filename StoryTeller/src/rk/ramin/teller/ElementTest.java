package rk.ramin.teller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ElementTest extends VBox implements BriefedNode, Saveable {

	private ElementTextField input;
	
	public ElementTest(ConstructionData data) {
		input = new ElementTextField(data); //See ElementSetVar: Maybe its bullshit using this ElementTextField, I don't realy remember what I supposed here
		getChildren().add(input);
		new Compiler(this, input, data.getEditor().getVarManager().getVarNameObservableList());
	}

	@Override
	public void requestFocus() {
		input.requestFocus();
	}
	
	@Override
	public void brief(ButtonProvider owner, Area receptor) {
		// TODO Auto-generated method stub
		input.brief(owner, receptor);
		//If backspace or del pressed and the textField is empty, the test sends a button click to its area, with type DELETE (no _SMALL, because were just in AreaSingleOptionals)
	}
	
	@Override
	public Object save() {
		//TEMP, this might change if I actually start to write the code for this. However: NEVER just save null!
		return input.getText();
	}

	@Override
	public void load(Object o) {
		input.setText((String)o);
	}
}
