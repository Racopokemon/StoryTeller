package rk.ramin.teller;

import java.util.ArrayList;

import javafx.scene.Node;

/**
 * Different Areas implement this, mostly, but not only ones that will be all the time available in its box.
 * However, they guarantee, that every external button you confide to it, is shown anywhere all the time. 
 * (Usually inside of the area itself, but in the case the Area is getting (temporally) deleted, some other area
 * will get the job) Most AreaButtonAccepter implementing Areas are visible all the time, but there are exceptions (AreaMultiOptional).
 * 
 * (All buttons being always visible is only guaranteed, if the boxes don't change the visibility of any area themselves. 
 * If they do so to add further functionality and more advanced features, they (and their developer) have to keep sure that they don't
 * manually hide any constant areas with some external buttons)
 */
public interface AreaButtonAccepter extends Area {
	public void addExternalButton(Node n);
	public void addExternalButtons(ArrayList<Node> l);
	public void removeExternalButton(Node n);
	public void removeExternalButtons(ArrayList<Node> l);
}
