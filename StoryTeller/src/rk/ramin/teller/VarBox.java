package rk.ramin.teller;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class VarBox extends Box implements BriefedNode {

	private ElementChooserEvent var;
	
	public VarBox(ConstructionData data) {
		super(false, true, Color.web("F4E25D"));
		AreaSingleOptional a = new AreaTest(this, data);
		AreaSingleConstant b = new AreaSingleConstant(var = new ElementChooserEvent(data));
		relaxedPlaceForSomeFancyGuestsToChill = b;
		addArea(a);
		addArea(b);
		a.hide();
	}

	@Override
	public void brief(ButtonProvider owner, Area receptor) {
		var.brief(owner, receptor);
	}
	public void requestFocus() {
		var.requestFocus();
	}
}
