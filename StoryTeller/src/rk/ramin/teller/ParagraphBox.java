package rk.ramin.teller;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;

/**
 * Just a bad duplicate to the paragraphTestBox without test and the consequences these have.
 * (Anyhow this makes still more sense to me than making a box containing only textAreas and vars, and another box that only addes a test to it ...
 * Anyhow the test belongs to a paragraph as well and at the same layer as its romans itself and its vars. 
 */
public class ParagraphBox extends Box {

	public ParagraphBox(ConstructionData data) {
		super(true, true, Color.web("CED2EA"));
		Label txt = new Label("If there is no other text to show: ");
		txt.setMaxWidth(1000000000);
		AreaSingleConstant a = new AreaSingleConstant(txt);
		AreaMultiOptional b = new AreaVars(data, this);
		AreaMultiConstant c = new AreaMultiConstant(true, true, Color.web("DCE3F7"), ElementTextArea.class, data, this);
		relaxedPlaceForSomeFancyGuestsToChill = c;
		addArea(a);
		addArea(b);
		addArea(c);
		b.clearAndHide();
	}
	
}
