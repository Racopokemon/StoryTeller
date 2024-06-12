package rk.ramin.teller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

public class ElementSetColor extends HBox implements ElementChooserItem {

	private ColorPicker picker;
	private ElementVarSelector var;
	
	private BriefingKeyStrokeEventHandler colorHandler;
	
	public ElementSetColor(ConstructionData data) {
		
		var = new ElementVarSelector(data.getEditor().getVarManager().getVarNameObservableList());
		HBox h = new HBox(var, picker = new SmallColorPicker());
		
		picker.valueProperty().addListener(new ChangeListener<Color>() {
			@Override
			public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
				//Transparency is not supported, we show that by instantly resetting all opacity values. 
				if (newValue.getOpacity() < 1.0) {
					picker.setValue(SaveStat.intToColor(SaveStat.colorToInt(newValue)));
				}
			}
		});
		picker.setOnKeyPressed(colorHandler = new BriefingKeyStrokeEventHandler(true));
		HBox.setHgrow(var, Priority.ALWAYS);
		var.setPrefWidth(100000000);
		HBox.setHgrow(picker, Priority.NEVER);
		//var.setw
		getChildren().add(h);
	}
	
	@Override
	public void brief(ButtonProvider owner, Area receptor) {
		var.brief(owner, receptor);
		colorHandler.brief(owner, receptor);
	}

	@Override
	public Object save() {
		return new Object[] {var.save(), SaveStat.colorToInt(picker.getValue())};
	}

	@Override
	public void load(Object o) {
		picker.setValue(SaveStat.intToColor(((int)((Object[])o)[1])));
		var.load(((Object[])o)[0]);
	}
	
	public void requestFocus() {
		picker.requestFocus(); //Does this even have any effect?
	}
}
