package rk.ramin.teller;

import javafx.scene.paint.Color;

public class SingleLinkBox extends Box {

	public SingleLinkBox(ConstructionData data) {
		super(true, true, Color.web("F76975"));
		AreaVars a = new AreaVars(data, this);
		AreaSingleConstant b = new AreaSingleConstant(new ElementChooserLink(data));
		relaxedPlaceForSomeFancyGuestsToChill = b;
		addArea(a);
		addArea(b);
		a.clearAndHide();
	}

}
