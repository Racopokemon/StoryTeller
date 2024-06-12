package rk.ramin.teller;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class AreaVars extends AreaMultiOptional {

	public AreaVars(ConstructionData data, Box box) {
		super(false, true, Color.web("F2CE74"), VarBox.class, data, box, StyledButtonType.VAR);
		// TODO Auto-generated constructor stub
	}
	
	public AreaVars(ConstructionData data, Box box, int buttonPos) {
		super(false, true, Color.web("F2CE74"), VarBox.class, data, box, StyledButtonType.VAR, buttonPos);
		// TODO Auto-generated constructor stub
	}
	
}
