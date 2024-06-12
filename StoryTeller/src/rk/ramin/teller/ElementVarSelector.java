package rk.ramin.teller;

import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

public class ElementVarSelector extends ComboBox<String> implements BriefedNode, Saveable {
	
	private BriefingKeyStrokeEventHandler handler = new BriefingKeyStrokeEventHandler(false);
	
	public ElementVarSelector(ObservableList<String> values) {
		setItems(values);
		setOnKeyPressed(handler);
		//setStyleClass ...
	}

	@Override
	public void brief(ButtonProvider owner, Area receptor) {
		handler.brief(owner, receptor);
	}

	@Override
	public Object save() {
		return getValue();
	}

	@Override
	public void load(Object o) {
		setValue((String) o);
	}
}
