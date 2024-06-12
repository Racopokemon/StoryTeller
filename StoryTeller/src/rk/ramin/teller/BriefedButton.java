package rk.ramin.teller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;

/**
 * A Button in one of four different kinds (add, remove, var, or test), that is briefed, so it knows already what to do if its clicked:
 * The buttonClicked method is called at an area you give in the constructor, optionally transferring a ButtonProvider during this call.
 * How the order of the buttons works: 
 * The userData of this node saves an Integer-value representing the position in the buttonProvider (left, mid, right, and the order in this category),
 * this value is automatically set during construction depending on the BriefedButtonType (add or removal buttons are always at the most right position
 * etc). This approach works fine as long as there are not multiple buttons of the same type in the same category, as their order might change in this
 * case. However this would also bring problems to the user as well, as he is confronted with two buttons in the same category looking exactly the same,
 * making it difficult for him to understand which button has which job. ...
 * In the current implementation this problem appears only once in the placeholder link in the AnswersBox, and for such cases it is possible to
 * manually influence the ordering by simply giving another value as UserData. (Here we just set a value higher than all values normal
 * briefedButtons can get to make sure the button is always shown at the most right position). 
 * 
 * Default positions (userData values) of all types: 
 * 1 remove
 * 3 var
 * 5 test
 * 7 + - (not create able from here, but you can ask your buttonProvider to add a combined button)
 * 9 add
 * 
 * ! the DOTS StyledButtonTypes are not used in briefedButtons. 
 * 
 * positions 0,2,4,6,8,10 are free, if you might need to put any button anywhere between it manually. 
 */
public class BriefedButton extends StyledButton {
	
	private static EventHandler<ActionEvent> listener;
	
	static {
		listener = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				((BriefedButton)event.getSource()).clicked();
			}
		};
	}
	
	private ButtonProvider owner;
	private Area receptor;
	
	/**
	 * Builds a new BriefedButton, that automatically notifies its receptor when its clicked.
	 * @param type Required. Our type.
	 * @param owner Not required, as you might need to create buttons even if there is no ButtonProvider that this button belongs to.
	 * So passing a null in here will have no negative effect. The only use of this reference is to be passed to the receptor on click,
	 * what can be useful e.g. when you have a AreaMulti and pressed a + or - button on one special ButtonProvider. 
	 * @param receptor
	 */
	public BriefedButton(StyledButtonType type, ButtonProvider owner, Area receptor) {
		super(type);
		this.owner = owner;
		this.receptor = receptor;
		
		switch (type) {
		case ADD:
		case ADD_SMALL: 
			setUserData(9);
			break;
		case REMOVE: 
		case REMOVE_SMALL:
			setUserData(9);
			break;
		case VAR: 
			setUserData(3);
			break;
		case TEST: 
			setUserData(5);
			break;
		}
		
		setOnAction(listener);
	}
	
	private void clicked() {
		receptor.onButtonClicked(type, owner);
	}
}
