package rk.ramin.teller;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 * A node that holds a special other node (that can't be changed) and optionally gives the option to add different buttons to its right side.
 * Those buttons can be changed all over the time, furthermore there are three "categories" to add those buttons to, 
 * depending on which use the buttons have and from where they come from. Buttons from different categories will be shown grouped, 
 * only buttons of the left category will be shown directly connected to the node. 
 * Basically you don't need to care about anything of this, as it manages itself quite well most of the time.
 * There is a difference between internal buttons (that belong directly to the ButtonProvider and can be added with a specified method) and
 * external buttons, that come to this Provider from somewhere else, belonging to another (or no) ButtonProvider, informing another Area. 
 * Those buttons are only shown in this ButtonProvider, because we have the space for it and the pane looks most compact with it. 
 * You have the possibility to clear all internal buttons, without having a reference to them. (If you want to delete other buttons, 
 * you need a reference)
 */
public class ButtonProvider extends HBox {

	private static Comparator<Node> comp = new Comparator<Node>() {
		@Override
		public int compare(Node n, Node m) {
			return (Integer)n.getUserData()-(Integer)m.getUserData();
		}
	};
	
	private static Insets insets = new Insets(0,0,0,EditorWindow.SPACING);
	
	private Node node;
	protected Area receptor;
	private ArrayList<Node> left, mid, right;
	
	/**
	 * Node is the node, that this ButtonProvider wraps around. 
	 * Area is the area containing us and that will be informed of all clicks to internal buttons / other briefed things 
	 * pretending to be buttons as the briefedNodes. 
	 * If the node is a briefedNode, its brief-method gets called during this constructor. 
	 * Also accepts null as node. Then we only provide optional buttons. 
	 */
	public ButtonProvider(Node n, Area receptor) {
		node = n;
		this.receptor = receptor;
		if (n != null) {
			addAndInitNode(n);
		}
		this.setAlignment(Pos.TOP_RIGHT);
		left = new ArrayList<>();
		if (!(n instanceof Box)) {
			mid = new ArrayList<>();
			right = new ArrayList<>();
		}
		//.. layout stuff to come
	}
	
	private void addAndInitNode(Node n) {
		getChildren().add(0, n);
		HBox.setHgrow(n, Priority.ALWAYS);
		if (n instanceof BriefedNode) {
			((BriefedNode)n).brief(this, receptor);
		}
	}
	
	/**
	 * Adds a BriefedButton of the type you specify to the left category. 
	 * As the button is internal, its owner is this ButtonProvider and the area that should be informed in case the button is clicked,
	 * is the area this ButtonProvider belongs to. 
	 */
	public void addInternalButton(StyledButtonType type) {
		addInternalButton(new BriefedButton(type, this, receptor));
	}

	/**
	 * Shortcut for adding the combined +- buttons as internal buttons, automatically setting them up to know their owner and receiver,
	 * also setting their userData (where the position for the button order is saved) to the default / recommended value of 7. 
	 */
	public void addInternalCombinedButton() {
		VBox combined = new VBox(new BriefedButton(StyledButtonType.ADD_SMALL, this, receptor), 
				new BriefedButton(StyledButtonType.REMOVE_SMALL, this, receptor));
		combined.setUserData(7);
		addInternalButton(combined);
	}

	private void addInternalButton(Node n) {
		//No check for the right userData needed, all methods calling this dont't give anyone the chance to change the correct starting values. 
		addButton(n);
	}

	/**
	 * Adds a button (or any other kind of node, like a VBox containing two buttons, thats the intended use) as an external button.
	 * The buttons userData will be adapted if needed (+ 11, if it was internal before) to fit its role as external button (that is
	 * shown in the mid or right category).
	 * In case our node is a box, we add all our buttons anywhere there, to keep the layout compact (don't care about that, thats 
	 * just our job and were is doing it fine, just relax and give us your buttons.)
	 */
	public void addExternalButton(Node n) {
		adaptUserDataToMid(n);
		addButton(n);
	}
	
	public void addExternalButtons(ArrayList<Node> l) {
		for (Node n : l) {
			adaptUserDataToMid(n);
		}
		addButtons(l);
	}

	/**
	 * Adds a button to our node (if its a box) or to the ButtonProvider itself, calling for redraw afterwards.
	 */
	private void addButton(Node n) {
		if (mid == null) {
			if ((Integer)n.getUserData() < 11) {
				left.add(n);
			}
			adaptUserDataToRight(n);
			((Box)node).getPlaceForExternalButtons(this).addExternalButton(n);
		} else {
			insertButton(n);
			redrawButtons();
		}
	}
	private void addButtons(ArrayList<Node> l) {
		if (mid == null) {
			for (Node n : l) {
				adaptUserDataToRight(n);
			}
			((Box)node).getPlaceForExternalButtons(this).addExternalButtons(l);
		} else {
			for (Node n : l) {
				insertButton(n);
			}
			redrawButtons();
		}
	}
	private void adaptUserDataToMid(Node n) {
		if ((Integer)n.getUserData() < 11) {
			n.setUserData((Integer)n.getUserData()%11 + 11);
		}
	}

	private void adaptUserDataToRight(Node n) {
		if ((Integer)n.getUserData() < 22) {
			n.setUserData((Integer)n.getUserData()%11 + 22);
		}
	}

	/**
	 * The base functionality. No checks whether we better should add the button button to our node,
	 * no (!) redrawing. (So take care to call a redrawButtons() afterwards).
	 * Just adds the button to its right category. 
	 */
	private void insertButton(Node n) {
		if ((Integer)n.getUserData() < 11) {
			left.add(n);
		} else if ((Integer)n.getUserData() < 22) {
			mid.add(n);
		} else {
			right.add(n);
		}
	}
	/**
	 * Could theoretically also remove other buttons, if the node is a box.
	 * So just call this with buttons you added before, and everything will be fine. 
	 * Works with internal buttons as well as with external ones. 
	 */
	public void removeButton(Node n) {
		if (mid == null) {
			((Box)node).getPlaceForExternalButtons(this).removeExternalButton(n);
			left.remove(n);
		} else {
			left.remove(n);
			mid.remove(n);
			right.remove(n);
			redrawButtons();
		}
	}
	/**
	 * Removes multiple buttons at once, being a bit more efficient because the buttons are not redrawn after every single removal. 
	 */
	public void removeButtons(ArrayList<Node> l) {
		if (mid == null) {
			((Box)node).getPlaceForExternalButtons(this).removeExternalButtons(l);
			left.removeAll(l);
		} else {
			left.removeAll(l);
			mid.removeAll(l);
			right.removeAll(l);
			redrawButtons();
		}
	}
	
	private void redrawButtons() {
		List<Node> l = getChildren();
		l.clear();
		left.sort(comp);
		mid.sort(comp);
		right.sort(comp);
		if (node != null) {
			l.add(node);
		}
		for (Node n : left) {
			l.add(n);
			HBox.setMargin(n, null);
		}
		int gap = 1;
		for (Node n : mid) {
			l.add(n);
			HBox.setMargin(n, gap-- > 0 ? insets : null);
		}
		gap = 1;
		for (Node n : right) {
			l.add(n);
			HBox.setMargin(n, gap-- > 0 ? insets : null);
		}
	}
	
	/**
	 * Removes all buttons that have been added as internal buttons. 
	 * External buttons remain unchanged. 
	 */
	public void clearInternalButtons() {
		if (mid == null) {
			((Box)node).getPlaceForExternalButtons(this).removeExternalButtons(left);
		}
		left.clear();
		if (mid != null) {
			redrawButtons();
		}
		
	}
	
	public void requestFocus() {
		if (node == null) {
			super.requestFocus();
		} else {
			node.requestFocus();
		}
	}
	
	/**
	 * We only need this in very special, advanced cases (e.g. in the RomanBox), the whole basic concept does not need this method.
	 * Returns the node we are holding, or null if we do not hold anything. 
	 * The reference to the current node *might* change during the runtime (even if it is kind of immutable in that way, that you cannot 
	 * explicitly change the node), so keep this in mind as you code. (The instances change after a reset-call, that internally just creates
	 * a new instance of the object, that is in a resetted state after construction. Might seem a bit dumb, but this is part of the concept).   
	 */
	public Node getNode() {
		return node;
	}
	
	/**
	 * Brings the node we are holding to a initial state (in fact, the node simply gets recreated).
	 * This is a kind of tricky action, requiring the node being 
	 * - not null
	 * - having a constructor that only needs a ConstructionData instance (that you have to give us as well to allow us to call the constructor)
	 * If you are calling this, you should be knowing quite well what youre doing with this, so make sure those two conditions are fulfilled.  
	 */
	public void resetNode(ConstructionData cd) {
		try {
			Node n = node.getClass().getConstructor(ConstructionData.class).newInstance(cd);
			getChildren().remove(node);
			addAndInitNode(n);
			node = n;
		} catch (SecurityException | NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		} 
	}
}
