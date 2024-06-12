package rk.ramin.teller;

import java.util.ArrayList;
import java.util.List;

/**
 * The element chooser for all possible events (was originally only setting vars, now includes also events like playing sounds, exiting the game, etc)
 */
public class ElementChooserEvent extends ElementChooser {

	public ElementChooserEvent(ConstructionData data) {
		super(data);
	}

	private static ElementChooserType[] elementList = {ElementChooserType.SET_VAR, ElementChooserType.SET_ENVIRONMENT, ElementChooserType.SET_COLOR, ElementChooserType.SET_SOUND};
	
	@Override
	public ElementChooserType[] getItemList() {
		return elementList;
	}

}
