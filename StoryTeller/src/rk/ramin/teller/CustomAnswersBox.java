package rk.ramin.teller;

import javafx.scene.Node;
import javafx.scene.paint.Paint;

public class CustomAnswersBox extends Box implements AreaMultiElementListener {
	
	private AreaMultiOptional optional;
	private EditorWindow editor;
	
	public CustomAnswersBox(ConstructionData data) {
		super(false, false, null);
		AreaMultiOptional a = new AreaMultiOptional(false, false, null, SingleCustomAnswerBox.class, data, this, StyledButtonType.ADD);
		AreaSingleConstant b = new AreaSingleConstant(null);
		relaxedPlaceForSomeFancyGuestsToChill = b;
		addArea(a);
		addArea(b);
		a.clearAndHide();
		optional = a;
		a.setListener(this);
		editor = data.getEditor();
	}

	public void showArea(Area a) {
		super.showArea(a);
		if (a == optional) {
			hideArea((Area) relaxedPlaceForSomeFancyGuestsToChill);
		}
	}
	
	public void hideArea(Area a) {
		super.hideArea(a);
		if (a == optional) {
			showArea((Area) relaxedPlaceForSomeFancyGuestsToChill);
		}
	}

	@Override
	public void onElementAdded(Node n) {}

	@Override
	public void onElementRemoved(Node n) {
		editor.updateArrows();
	}

}
