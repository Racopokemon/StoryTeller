package rk.ramin.teller;

import javafx.scene.Node;

/**
 * If you register a class implementing this at an areaMulti, each time an element is added or removed these methods get called.
 * The AreaMulti directly passes the instances of the elements itself to you, not just the ButtonProvider nobody cares about. 
 */
public interface AreaMultiElementListener {
	public void onElementAdded(Node n);
	public void onElementRemoved(Node n);
}
