package rk.ramin.teller;

import javafx.scene.control.ComboBox;

public class ElementCustomAnswerType extends ComboBox<String> implements Saveable {

	public static String[] types = {"Matches exactly", "No case & whitespace", "Contains"};
	
	public ElementCustomAnswerType() {
		getItems().addAll(types);
		setMaxWidth(1000000);
		setValue(types[2]);
	}
	
	@Override
	public Object save() {
		if (getValue().equals(types[1])) {
			return (byte) 1;
		}
		if (getValue().equals(types[2])) {
			return (byte) 2;
		}
		return (byte) 0;
	}

	@Override
	public void load(Object o) {
		setValue(types[(byte)o]);
	}

}
