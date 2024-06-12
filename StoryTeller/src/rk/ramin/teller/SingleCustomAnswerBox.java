package rk.ramin.teller;

import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;

public class SingleCustomAnswerBox extends Box {

	private AreaMultiConstant focus;
	
	public SingleCustomAnswerBox(ConstructionData data) {
		super(true, true, Color.web("8ED4DE"));
		AreaMultiConstant a = focus = new AreaMultiConstant(false, false, null, ElementTextField.class, data, this);
		AreaSingleConstant b = new AreaSingleConstant(new ElementCustomAnswerType());
		AreaTest c = new AreaTest(this, data);
		AreaSingleConstant d = new AreaSingleConstant(new LinkBox(data));
		relaxedPlaceForSomeFancyGuestsToChill = a;
		addArea(a);
		addArea(b);
		addArea(c);
		addArea(d);
		c.hide();
	}
	
	public void requestFocus() {
		focus.requestFocus();
	}
}
