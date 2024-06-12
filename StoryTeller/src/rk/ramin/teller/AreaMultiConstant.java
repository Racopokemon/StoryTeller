package rk.ramin.teller;

import java.util.ArrayList;

import javafx.scene.Node;
import javafx.scene.paint.Paint;

public class AreaMultiConstant extends AreaMulti {
	public AreaMultiConstant(boolean spacing, boolean useRim, Paint color,
			Class<? extends Node> elementClass, ConstructionData data, Box box) {
		super(spacing, useRim, true, color, elementClass, data, box);
		swapButtonsOfFirstElement();
	}
	
	private void swapButtonsOfFirstElement() {
		ButtonProvider firstElement = ((ButtonProvider)box.getChildren().get(0));
		firstElement.clearInternalButtons();
		if (box.getChildren().size() == 1) {
			firstElement.addInternalButton(StyledButtonType.ADD);
		} else {
			firstElement.addInternalCombinedButton();
		}
	}
	
	@Override
	public void onButtonClicked(StyledButtonType type, ButtonProvider owner) {
		if (box.getChildren().size() == 1) {
			if (type == StyledButtonType.REMOVE_SMALL) {
				return;
			} else {
				if (type == StyledButtonType.ADD || type == StyledButtonType.ADD_SMALL) {
					super.onButtonClicked(StyledButtonType.ADD_SMALL, owner);
					swapButtonsOfFirstElement();
				}
			}
		} else {
			super.onButtonClicked(type, owner);
			if (box.getChildren().size() == 1) {
				swapButtonsOfFirstElement();
			}
		}
	}
	
	public void addExternalButton(Node n) {
		super.addExternalButton(n);
		((ButtonProvider)box.getChildren().get(0)).addExternalButton(n);
	}
	public void addExternalButtons(ArrayList<Node> l) {
		super.addExternalButtons(l);
		((ButtonProvider)box.getChildren().get(0)).addExternalButtons(l);
	}
	public void removeExternalButton(Node n) {
		super.removeExternalButton(n);
		((ButtonProvider)box.getChildren().get(0)).removeButton(n);
	}
	public void removeExternalButtons(ArrayList<Node> l) {
		super.removeExternalButtons(l);
		((ButtonProvider)box.getChildren().get(0)).removeButtons(l);
	}
}
