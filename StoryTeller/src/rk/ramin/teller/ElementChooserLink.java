
package rk.ramin.teller;

public class ElementChooserLink extends ElementChooser {
	private static EditorWindow editor;
	public ElementChooserLink(ConstructionData data) {
		super(data);
		if (editor == null) {
			editor = data.getEditor();
		}
	}

	private static ElementChooserType[] elementList = {ElementChooserType.NORMAL_LINK, ElementChooserType.CUSTOM_LINK};
	
	@Override
	public ElementChooserType[] getItemList() {
		return elementList;
	}
	
	protected void onElementChanged() {
		editor.updateArrows();
	}
}
