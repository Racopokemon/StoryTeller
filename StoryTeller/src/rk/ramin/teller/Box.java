package rk.ramin.teller;

import java.util.ArrayList;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * Basically those colored rectangles containing other elements and rectangles. 
 * A box consists of one or more Areas, Areas consist of ButtonProviders.
 * The order, type and count of Areas can not be changed, but all elements can be hidden if they are not needed / 
 * they hide themselves if they are empty or not in use. 
 * However a box has no idea about which of its areas are shown or hidden at the moment, its the areas job to know that. 
 * Probably most important job of the box: Providing the placeForExternalButtons, a AreaButtonAccepter to that other areas and even
 * ButtonProviders holding this Box can add buttons if they don't can / want to show them themselves. 
 */
public class Box extends BoxBasic implements Saveable {
	
	protected AreaButtonAccepter relaxedPlaceForSomeFancyGuestsToChill;
	protected ArrayList<Area> areas = new ArrayList<>();
	
	/**
	 * Constructor. Here you set up the areas (you need to add them in the right order),
	 * that you should not change afterwards, and define one of them being the placeForExternalButtons. 
	 */
	public Box(boolean spacing, boolean useRim, Paint color) {
		super(spacing, useRim, color);
	}

	protected void addArea(Area a) {
		areas.add(a);
		box.getChildren().add((Node) a);
	}
	
	/**
	 * Returns you an area that accepts external buttons. 
	 * By calling this, you should give a reference to the area / buttonProvider called this method.
	 * In the very most cases every object will receive the same reference to the same object, but for some more advanced situations
	 * this provides the option to have different AreaButtonAccepters for different nodes. 
	 * However it is guaranteed that calling this method with the same Object for whoIsAsking will also always return the same reference. 
	 */
	public AreaButtonAccepter getPlaceForExternalButtons(Object whoIsAsking) {
		return relaxedPlaceForSomeFancyGuestsToChill;
	}
	
	public void hideArea(Area a) {
		box.getChildren().remove(a);
	}
	
	/**
	 * Make sure the area exists in this box and is actually hidden, otherwise you pay for it getting some bad exceptions in your face. 
	 */
	public void showArea(Area a) {
		ObservableList<Node> c = box.getChildren();
		if (c.isEmpty()) {
			c.add((Node) a);
			return;
		}
		int j = 0, g = areas.indexOf(a);
		Node n = c.get(0);
		for (int i = 0; i < g; i++) {
			if (n == areas.get(i)) {
				if (++j >= c.size()) {
					break;
				}
				n = c.get(j);
			}
		}
		c.add(j,(Node) a);
	}

	@Override
	public Object save() {
		Object[] s = new Object[areas.size()];
		for (int i = 0; i < areas.size(); i++) {
			s[i] = areas.get(i).save();
		}
		return s;
	}

	@Override
	public void load(Object o) {
		Object[] l = (Object[]) o;
		for (int i = 0; i < areas.size(); i++) {
			areas.get(i).load(l[i]);
		}
	}
	
	//Maybe the same methods with numbers instead of the instances 
}
