package rk.ramin.teller;

/**
 * This node is intended to be added to a ButtonProvider and report "click" events to its area as well as its BriefedButtons do. 
 * For this purpose, the ButtonProvider calls the brief method on every BriefedNode added to it and informs the node about its owner
 * and about the receptor. With this information the node can do the onButtonClicked-calls itself on the node. 
 * 
 * Note: Because of unlucky design it may happen that brief is called several times. In this case: The last call is the right one, so
 * just overwrite the old vars. 
 * 
 * If this node will be placed in AreaMultis, call onButtonClicked with ADD_SMALL and REMOVE_SMALL,
 * if it will be placed in AreaSingleOptionals, call onButtonClicked with REMOVE
 */
public interface BriefedNode {
	public void brief(ButtonProvider owner, Area receptor);
}
