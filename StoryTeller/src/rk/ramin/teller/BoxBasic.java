package rk.ramin.teller;

import java.awt.TextField;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * Nothing more than a class that makes some features of VBoxes easier accessable and run automatically: 
 * A container that can have (optionaly, but this is the new feature we add) a special color and some insets to its surrounding nodes,
 * including a black outline. This appearance is called "rim". The rim, if activated, is only shown if the BasicBox contains more than one 
 * element. In addition you can decide whether or not this node has some spacing between its elements. 
 */
public class BoxBasic extends StackPane implements ListChangeListener<Node>{
	
	private static Insets insets = new Insets(EditorWindow.PAGE_DATA_VSPACING, EditorWindow.PAGE_DATA_HSPACING, EditorWindow.PAGE_DATA_VSPACING, EditorWindow.PAGE_DATA_HSPACING);//new Insets(EditorWindow.SPACING);
	
	private Background backWithout, backWith;
	
	protected VBox box;
	private boolean useRim;
	private boolean rimHidden;
	
	public BoxBasic(boolean spacing, boolean useRim, Paint color) {
		this.useRim = useRim;
		box = new VBox(spacing ? EditorWindow.PAGE_DATA_VSPACING : 0);//EditorWindow.SPACING : 0);
		getChildren().add(box);
		if (useRim) {
			backWithout = new Background(new BackgroundFill(color, null, null));
			//backWith = new Background(new BackgroundFill(OUTLINE_COLOR, null, null), new BackgroundFill(color, null, outlineInsets));
			backWith = new Background(new BackgroundFill(color, null, null));
			box.getChildren().addListener(this);
			rimHidden = true;
			hideRim();
		}
	}
	
	private void showRim() {
		StackPane.setMargin(box, insets);
		setBackground(backWith);
	}
	
	private void hideRim() {
		StackPane.setMargin(box, null);
		setBackground(backWithout);
	}

	public void onChanged(javafx.collections.ListChangeListener.Change<? extends Node> c) {
		if (box.getChildren().size() <= 1 != rimHidden) {
			rimHidden = !rimHidden;
			if (rimHidden) {
				hideRim();
			} else {
				showRim();
			}
		}
	}
}
