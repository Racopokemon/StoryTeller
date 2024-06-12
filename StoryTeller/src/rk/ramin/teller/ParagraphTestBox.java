package rk.ramin.teller;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class ParagraphTestBox extends Box {

	private AreaTest areaTest;
	private RomanBox romans;
	private boolean test = false;
	AreaMultiConstant focus;
	
	public ParagraphTestBox(ConstructionData data) {
		super(true, true, Color.web("CAC8F7"));
		AreaTest a = new AreaTest(this, data);
		AreaMultiOptional b = new AreaVars(data, this);
		AreaMultiConstant c = focus = new AreaMultiConstant(true, true, Color.web("DCE3F7"), ElementTextArea.class, data, this);
		relaxedPlaceForSomeFancyGuestsToChill = c;
		addArea(a);
		addArea(b);
		addArea(c);
		a.hide();
		b.clearAndHide();
		
		romans = data.getRomanBox();
		areaTest = a;
	}
	
	public void showArea(Area a) {
		super.showArea(a);
		if (a == areaTest) {
			test = true;
			romans.onTestAddedToParagraph();
		}
	}
	public void hideArea(Area a) {
		super.hideArea(a);
		if (a == areaTest) {
			test = false;
			romans.onTestRemovedFromParagraph();
		}
	}
	
	public boolean hasTest() {
		return test;
	}
	
	public void requestFocus() {
		focus.requestFocus();
	}

}
