package rk.ramin.teller;

import java.util.ArrayList;

import javafx.scene.Node;

/**
 * Clearly the most boring Area of them all. No own buttons, just holding another node and providing some space for external buttons. 
 * Attention: Don't add a BriefedNode to this, triggering any of its events sent to the area will end in a nullPointerException. 
 */
public class AreaSingleConstant extends ButtonProvider implements Area, AreaButtonAccepter, Saveable {

	public AreaSingleConstant(Node n) {
		super(n, null); //Should be this, but sadly thats not allowed (okay otherwise I can understand it)
	}

	@Override
	public void removeExternalButton(Node n) {
		removeButton(n);
	}

	@Override
	public void removeExternalButtons(ArrayList<Node> l) {
		removeButtons(l);
	}

	@Override
	public void onButtonClicked(StyledButtonType type, ButtonProvider owner) {
		//Will never happen :(
	}

	@Override
	public Object save() {
		Node n = getNode();
		if (n instanceof Saveable) {
			return ((Saveable) n).save();
		} else {
			return null;
		}
	}

	@Override
	public void load(Object o) {
		Node n = getNode();
		if (n instanceof Saveable) {
			((Saveable) n).load(o);
		}
	}
}
