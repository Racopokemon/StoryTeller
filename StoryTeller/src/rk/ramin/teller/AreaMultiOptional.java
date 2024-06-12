package rk.ramin.teller;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.paint.Paint;

/**
 * This areaMulti is optional, what means that all of its elements can be deleted. 
 * If this happens, the AreaMultiOptional adds a + button to the placeForExternalButtons given by the box.
 * If this AreaMultiOptional holds any external buttons (as it implements AreaButtonAccepter), those ones will also be added to the
 * place for external buttons. 
 * 
 * Starts with one element. Call clearAndHide() to change this after inizialization. 
 */
public class AreaMultiOptional extends AreaMulti {

	private Node addButton;
	private boolean shown;
	
	private StyledButtonType addButtonType;
	
	public AreaMultiOptional(boolean spacing, boolean useRim, Paint color,
			Class<? extends Node> elementClass, ConstructionData data, Box box, StyledButtonType buttonStyle) {
		super(spacing, useRim, true, color, elementClass, data, box);
		addButtonType = buttonStyle;
		addButton = new BriefedButton(addButtonType, null, this);
		shown = true;
	}
	
	/**
	 * Special constructor for the case that you not only want to select the appearance of the add button (that is shown, if this area itself is hidden
	 * at the moment), but also at which custom position it is shown. 
	 */
	public AreaMultiOptional(boolean spacing, boolean useRim, Paint color,
			Class<? extends Node> elementClass, ConstructionData data, Box box, StyledButtonType buttonStyle, int buttonPos) {
		this(spacing, useRim, color, elementClass, data, box, buttonStyle);
		addButton.setUserData(buttonPos);
	}

	
	/**
	 * This is intended to be called after construction, if this area is really just optional and should stay hidden at the beginning. 
	 * What it does is quite simple: Removing all its elements and calling the area to hide the area.
	 * (We sadly cannot provide a parameter in the constructor whether we should start with an element or not, because it could lead
	 * to problems asking for the placeForExternalButtons - this is just another area like this one and might have not been created yet.)
	 */
	public void clearAndHide() {
		box.getChildren().clear();
		hide();
	}
	
	@Override
	public void onButtonClicked(StyledButtonType type, ButtonProvider owner) {
		if (type == addButtonType) { 
			show();
		} else {
			super.onButtonClicked(type, owner);
		}
	}
	
	@Override
	public void remove(ButtonProvider remove) {
		super.remove(remove);
		if (box.getChildren().isEmpty()) {
			hide();
		}
	}
	
	/**
	 * Call this, when the AreaMultiOptional is currently hidden (because it / and has no elements).
	 * A first element is added, the + button removed and the Area gets shown
	 */
	private void show() {
		onButtonClicked(StyledButtonType.ADD_SMALL, null);
		externalButtons.add(addButton);
		owner.getPlaceForExternalButtons(this).removeExternalButtons(externalButtons);
		externalButtons.remove(addButton);
		owner.showArea(this);
		shown = true;
		//addExternalButtons(externalButtons, externalButtonsPos); would also add the externalButtons to the array, we only want to show them
		((ButtonProvider)(box.getChildren().get(0))).addExternalButtons(externalButtons);
		box.getChildren().get(0).requestFocus();
	}
	
	/**
	 * Call this, when the AreaMultiOptional has already become empty.
	 * The + button is added and this area hidden. 
	 */
	private void hide() {
		shown = false;
		externalButtons.add(addButton);
		owner.getPlaceForExternalButtons(this).addExternalButtons(externalButtons);
		externalButtons.remove(externalButtons.size()-1);
		owner.hideArea(this);
	}
	
	
	public void addExternalButton(Node n) {
		super.addExternalButton(n);
		if (shown) {
			((ButtonProvider)box.getChildren().get(0)).addExternalButton(n);
		} else {
			owner.getPlaceForExternalButtons(this).addExternalButton(n);
		}
	}
	public void addExternalButtons(ArrayList<Node> l) {
		super.addExternalButtons(l);
		if (shown) {
			((ButtonProvider)box.getChildren().get(0)).addExternalButtons(l);
		} else {
			owner.getPlaceForExternalButtons(this).addExternalButtons(l);
		}
	}
	public void removeExternalButton(Node n) {
		super.removeExternalButton(n);
		if (shown) {
			((ButtonProvider)box.getChildren().get(0)).removeButton(n);
		} else {
			owner.getPlaceForExternalButtons(this).removeExternalButton(n);
		}
	}
	public void removeExternalButtons(ArrayList<Node> l) {
		super.removeExternalButtons(l);
		if (shown) {
			((ButtonProvider)box.getChildren().get(0)).removeButtons(l);
		} else {
			owner.getPlaceForExternalButtons(this).removeExternalButtons(l);
		}
	}
	
	@Override
	public void load(Object o) {
		List<Node> c = box.getChildren();
		Object[] ob = (Object[])o;
		while (c.size() < ob.length) {
			if (!shown) {
				onButtonClicked(addButtonType, null);
			} else {
				onButtonClicked(StyledButtonType.ADD_SMALL, null);
			}
		}
		while (c.size() > ob.length) {
			onButtonClicked(StyledButtonType.REMOVE_SMALL, null);
		}
		if (contentImplementsElement) {
			for (int i = 0; i < ob.length; i++) {
				((Saveable)((ButtonProvider)c.get(i)).getNode()).load(ob[i]);
			}
		}
	}
}
