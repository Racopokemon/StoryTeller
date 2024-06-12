package rk.ramin.teller;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class ElementSound extends HBox implements ElementChooserItem {

	/**
	 * I wrote all of this within 5 minutes without much thinking, feel free to modify everything in here. 
	 */
	
	public ElementSound(ConstructionData data) {
		getChildren().add(new Label("In later releases here you can toggle sounds, background loops, their volume, etc."));
	}
	
	@Override
	public Object save() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void load(Object o) {
		// TODO Auto-generated method stub

	}

	@Override
	public void brief(ButtonProvider owner, Area receptor) {
		// TODO Auto-generated method stub

	}

}
