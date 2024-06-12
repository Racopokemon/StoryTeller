package rk.ramin.teller;

/**
 * A box consists of one or more Areas, Areas consist of ButtonProviders.
 * This interface concludes what all Areas have in common: As they hold ButtonProviders with buttons,
 * they can receive click-events from underlying buttons, that influence them. 
 */
public interface Area extends Saveable {
	public void onButtonClicked(StyledButtonType type, ButtonProvider owner);
}
