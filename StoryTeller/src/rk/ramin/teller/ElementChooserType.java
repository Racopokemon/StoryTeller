package rk.ramin.teller;

/**
 * Enumeral representation of all items any elementChooser (ElementChooserItem)
 * After you have created a new elementChooserItem, you should add an enum for it here.
 * @author Der Guru
 *
 */
public enum ElementChooserType {
	SET_VAR(ElementSetVar.class, "Set a variable"), 
	SET_ENVIRONMENT(ElementSetEnvironment.class, "Style and Particles"), 
	SET_COLOR(ElementSetColor.class, "Set a color to a variable"), 
	SET_SOUND(ElementSound.class, "Play sounds"),
	NORMAL_LINK(ElementLink.class, "Normal link"),
	CUSTOM_LINK(ElementCustomLink.class, "Custom link");
	
	private Class<?> elementClass;
	private String description;
	
	private ElementChooserType(Class<?> c, String descr) {
		elementClass = c;
		description = descr;
	}
	
	public Class<?> getElementClass() {
		return elementClass;
	}
	
	public String getDescription() {
		return description;
	}
}
