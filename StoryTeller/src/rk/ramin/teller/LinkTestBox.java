package rk.ramin.teller;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class LinkTestBox extends Box implements BriefedNode {

	private ElementTest focus;
	
	public LinkTestBox(ConstructionData data) {
		super(true, true, Color.web("FFCBC9"));
		AreaSingleConstant a = new AreaSingleConstant(focus = new ElementTest(data));
		AreaSingleConstant b = new AreaSingleConstant(new SingleLinkBox(data));
		relaxedPlaceForSomeFancyGuestsToChill = a;
		addArea(a);
		addArea(b);
	}
	public void requestFocus() {
		focus.requestFocus();
	}
	
	public void brief(ButtonProvider owner, Area receptor) {
		focus.brief(owner, receptor);
	}
}
