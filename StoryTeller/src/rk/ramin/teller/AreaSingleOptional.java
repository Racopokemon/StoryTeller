package rk.ramin.teller;

import javafx.scene.Node;

/**
 * An area containing a single node, having one own button: a - button to remove it.
 * If the node gets removed through this, a + button is added to our boxes place for external buttons. 
 * 
 * Starts visible, call hide() to change this (after its sure you provided a place for external buttons)
 */
public class AreaSingleOptional extends ButtonProvider implements Area, Saveable {
	
	private Box owner;
	private Node addButton;
	private StyledButtonType addButtonType;
	private boolean hidden;
	private ConstructionData data;
	
	/**
	 * Important: The node must have a constructor that only requires an instance of ConstructionData!
	 * (If the node is hidden and then saved, the node will be resetted after the file is loaded later.
	 * For this a new node is being constructed using this constructor)
	 */
	public AreaSingleOptional(Node n, Box owner, StyledButtonType buttonStyle, ConstructionData data) {
		super(n, null); //Its "this" pls pls pls
		receptor = this;
		this.data = data;
		this.owner = owner;
		if (n instanceof BriefedNode) { //Rebriefing -.-
			((BriefedNode)n).brief(this, this);
		}
		addButton = new BriefedButton(buttonStyle, this, this);
		addButtonType = buttonStyle;
		addInternalButton(StyledButtonType.REMOVE);
		hidden = false;
	}
	/**
	 * Special constructor for the case that you not only want to select the appearance of the add button (that is shown, if this area itself is hidden
	 * at the moment), but also at which custom position it is shown. 
	 */
	public AreaSingleOptional(Node n, Box owner, StyledButtonType buttonStyle, int buttonPos, ConstructionData data) {
		this(n, owner, buttonStyle, data);
		addButton.setUserData(buttonPos);
	}
	
	public void hide() {
		impl_traverse(com.sun.javafx.scene.traversal.Direction.PREVIOUS); //If its not working anymore - simply delete it. 
		owner.hideArea(this);
		owner.getPlaceForExternalButtons(this).addExternalButton(addButton);
		hidden = true;
	}
	
	public void show() {
		owner.showArea(this);
		owner.getPlaceForExternalButtons(this).removeExternalButton(addButton);
		hidden = false;
	}
	
	@Override
	public void onButtonClicked(StyledButtonType type, ButtonProvider owner) {
		if (type == addButtonType) {
			show();
		} else if (type == StyledButtonType.REMOVE || type == StyledButtonType.REMOVE_SMALL) { //Not sure about the second one. Can there happen anything wrong with? 
			hide();
		}
	}
	
	@Override
	public Object save() {
		if (hidden) {
			return null;
		} else {
			Node n = getNode();
			if (n instanceof Saveable) {
				return ((Saveable)n).save();
			} else {
				return new Object(); //Basically the most useless instance of a class ever existed, but it is a placeholder in this case. 
			}
		}
	}

	@Override
	public void load(Object o) {
		Node n = getNode();
		if (o == null) {
			if (!hidden) {
				hide();
			}
			if (n != null) {
				resetNode(data);
			}
		} else {
			if (hidden) {
				show();
			}
			if (n instanceof Saveable) {
				((Saveable)n).load(o);
			}
		}
	}
}
