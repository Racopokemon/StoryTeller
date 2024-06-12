package rk.ramin.teller;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ElementCustomLink extends VBox implements ElementChooserItem {
	
	private ElementTextField text;
	
	public ElementCustomLink(ConstructionData data) {
		getChildren().add(text = new ElementTextField(data));
		text.setPromptText("Type a page number");
		new Compiler(this, text, data.getEditor().getVarManager().getVarNameObservableList());
	}
	
	@Override
	public Object save() {
		return text.save();
	}

	@Override
	public void load(Object o) {
		text.load(o);
	}

	@Override
	public void brief(ButtonProvider owner, Area receptor) {}
	
	@Override
	public void requestFocus() {
		text.requestFocus();
	}

}
