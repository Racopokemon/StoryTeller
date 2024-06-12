package rk.ramin.teller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ElementSetVar extends VBox implements ElementChooserItem {
	
	private ElementTextField text; 
	private ElementVarSelector var;
	//Replacing the normal text field with this elementTextField was the very first thing I did after returning to this project after some months,
	//not remembering anything I did here before, and maybe it is bullshit. But otherwise, just briefing the textField and letting do itself all 
	//of the work with receiving the key events and calling for deletion etc seems to be quite clever, doesnt it? In addition, it also is already 
	//styled in the right way and everything ...
	
	public ElementSetVar(ConstructionData data) {
		
		var = new ElementVarSelector(data.getEditor().getVarManager().getVarNameObservableList());
		HBox h = new HBox(var, text = new ElementTextField(data));
		var.setPrefWidth(200);
		HBox.setHgrow(var, Priority.NEVER);
		HBox.setHgrow(text, Priority.ALWAYS);
		getChildren().add(h);
		
		new Compiler(this, text, data.getEditor().getVarManager().getVarNameObservableList());
	}
	
	@Override
	public void brief(ButtonProvider owner, Area receptor) {
		text.brief(owner, receptor);
		var.brief(owner, receptor);
	}

	@Override
	public Object save() {
		return new Object[] {var.save(), text.getText()};
	}

	@Override
	public void load(Object o) {
		text.setText((String)(((Object[])o)[1]));
		var.load((((Object[])o)[0]));
	}
	
	public void requestFocus() {
		text.requestFocus();
	}
}
