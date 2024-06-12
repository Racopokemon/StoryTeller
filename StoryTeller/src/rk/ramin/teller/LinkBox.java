package rk.ramin.teller;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class LinkBox extends Box {

	public LinkBox(ConstructionData data) {
		super(true, true, Color.web("EF4640"));
		AreaMultiOptional a = new AreaVars(data, this);
		AreaMultiOptional b = new AreaMultiOptional(true, false, null, LinkTestBox.class, data, this, StyledButtonType.TEST);
		AreaSingleConstant c = new AreaSingleConstant(new SingleLinkBox(data));
		relaxedPlaceForSomeFancyGuestsToChill = c;
		addArea(a);
		addArea(b);
		addArea(c);
		a.clearAndHide();
		b.clearAndHide();
	}
}
