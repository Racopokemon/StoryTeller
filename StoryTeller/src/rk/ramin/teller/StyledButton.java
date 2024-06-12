package rk.ramin.teller;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;

/**
 * A styled button is kind of every button that is used in the page contents. We have different kinds, and the button automatically 
 * styles itself in this way. 
 * @author Der Guru
 *
 */
public class StyledButton extends Button {
	private static final double BUTTON_HEIGHT = 24, BUTTON_WIDTH=15, HALF_BUTTON_HEIGHT = BUTTON_HEIGHT/2.0;
	protected StyledButtonType type;
	
	public StyledButton(StyledButtonType type) {
		this.type = type;
		
		Node n = null;
		double h;
		if (type == StyledButtonType.ADD_SMALL || type == StyledButtonType.REMOVE_SMALL) {
			h = HALF_BUTTON_HEIGHT;
		} else {
			h = BUTTON_HEIGHT;
		}
		switch (type) {
		case ADD:
		case ADD_SMALL: 
			Line l1 = new Line(-2,0,2,0);
			Line l2 = new Line(0,-2,0,2);
			n = new Group(l1, l2);
			break;
			/**
			 * I never thought that this two cheap lines of code would ever work instantly - but they do!
			 * The lines are already black, have the right thickness, and are even centered on the button. Amazing ...
			 */
		case REMOVE: 
		case REMOVE_SMALL:
			n = new Line(-2,0,2,0);
			break;
		case VAR: 
			Line l3 = new Line(-3,0,3,0);
			Line l4 = new Line(-3,0,0,3);
			Line l5 = new Line(-3,0,0,-3);
			n = new Group(l3, l4, l5);
			break;
		case TEST: 
			//n = new Text("?");
			Polyline p1 = new Polyline(-4,5,-4,2,0,-2,4,2,4,5);
			Line p2 = new Line(0,-5,0,-2);
			n = new Group(p1, p2);
			break;
		case DOTS:
			Group g = new Group();
			for (int i = 1; i >= -1; i--) {
				Circle c = new Circle(0, i*3.5, 0.9, Color.BLACK);
				g.getChildren().add(c);
			}
			n = g;
			break;
		}
		setGraphic(n);
		setMinSize(BUTTON_WIDTH, h);
		setMaxSize(BUTTON_WIDTH, h);
		setFocusTraversable(false);
		/*
		if (type == StyledButtonType.TEST || type == StyledButtonType.VAR || type == StyledButtonType.DOTS) {
			setMinSize(0, h);
			setMaxSize(0, h);
			setGraphic(null);
		}
		*/
		getStyleClass().add("styled-button");

	}
}
