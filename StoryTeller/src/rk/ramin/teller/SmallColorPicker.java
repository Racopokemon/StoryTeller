package rk.ramin.teller;

import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;

/**
 * Very small extension to the ColorPicker, that only shows a small color button. 
 * @author Der Guru
 *
 */
public class SmallColorPicker extends ColorPicker {
	public SmallColorPicker() {
		super();
		init();
	}
	
	public SmallColorPicker(Color c) {
		super(c);
		init();
	}
	
	private void init() {
		getStyleClass().add("button");
		setStyle("-fx-color-label-visible: false;");
		setMinWidth(31); //I try to avoid those absolute values, but here it would not render nice without it.
	}
}
