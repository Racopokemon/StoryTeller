package rk.ramin.teller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

//http://stackoverflow.com/questions/15159988/javafx-2-2-textfield-maxlength

public class LimitedTextField extends TextField {
	
	private int limit;
	
	public LimitedTextField(int howManyChars) {
		super();
		limit = howManyChars;
		textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (newValue.length()>limit) {
					LimitedTextField.this.setText(oldValue); //Simply denies every input if its length is too long
				}
			}
		});
	}
	public LimitedTextField(String text, int howManyChars) {
		super(text);
		limit = howManyChars;
	}
	private void addListener() {
		
	}
	
}
