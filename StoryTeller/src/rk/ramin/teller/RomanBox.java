package rk.ramin.teller;

import java.util.ArrayList;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class RomanBox extends Box implements AreaMultiElementListener {

	/**
	 * True, if we are showing another ParagraphBox (without test) that is shown in the case all other tests in the other paragraphs are false. 
	 */
	private boolean precaution;
	private AreaSingleConstant precautionBox;
	private ArrayList<ParagraphTestBox> allParagraphs = new ArrayList<>();
	private ConstructionData data;
	private AreaMultiConstant focus;
	
	public RomanBox(ConstructionData data) {
		super(true, false, null);
		data.setRomanBox(this);
		this.data = data;
		AreaMultiConstant a = focus = new AreaMultiConstant(true, false, null, ParagraphTestBox.class, data, this);
		AreaSingleConstant b = new AreaSingleConstant(new ParagraphBox(data));
		addArea(a);
		addArea(b);
		hideArea(b);
		precaution = false;
		
		precautionBox = b;
		a.setListener(this);
	}
	
	public void onTestAddedToParagraph() {
		showPrecautionIfNeeded();
	}
	
	public void onTestRemovedFromParagraph() {
		hidePrecautionIfNeeded();
	}
	
	private void showPrecautionIfNeeded() {
		if (precaution) {
			return;
		}
		for (ParagraphTestBox p : allParagraphs) {
			if (!p.hasTest()) {
				return;
			}
		}
		showArea(precautionBox);
		precaution = true;
	}
	
	private void hidePrecautionIfNeeded() {
		if (precaution) {
			hideArea(precautionBox);
			precaution = false;
		}
	}

	@Override
	public void onElementAdded(Node n) {
		ParagraphTestBox p = (ParagraphTestBox) n;
		allParagraphs.add(p);
		hidePrecautionIfNeeded();
	}
	@Override
	public void onElementRemoved(Node n) {
		allParagraphs.remove(n);
		showPrecautionIfNeeded();
		
	}
	
	@Override
	public Object save() {
		int l = precaution ? areas.size() : areas.size()-1;
		Object[] s = new Object[l];
		for (int i = 0; i < l; i++) {
			s[i] = areas.get(i).save();
		}
		return s;
	}
	@Override
	public void load(Object o) {
		Object[] l = (Object[]) o;
		for (int i = 0; i < l.length; i++) {
			areas.get(i).load(l[i]);
		}
		if (l.length < areas.size()) {
			precautionBox.resetNode(data);
		}
	}
	
	/**
	 * This is only called in one case, when the user has typed a name for the page and pressed enter. 
	 * Then this box is getting focussed and will select the very first element in the AreaMultiConstant of our paragraphs. 
	 */
	public void requestFocus() {
		focus.requestFocus();
	}
}
