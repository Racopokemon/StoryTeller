package rk.ramin.teller;

import java.util.ArrayList;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class AnswersBox extends Box implements AreaMultiElementListener {

	private AreaSingleConstant placeholder;
	private boolean placeholderActive = false;
	private ArrayList<SingleAnswerBox> answers = new ArrayList<>();
	private ConstructionData data;
	
	public AnswersBox(ConstructionData data) {
		super(true, false, null);
		data.setAnswers(this);
		this.data = data;
		AreaMultiOptional a = new AreaVars(data, this, 4);
		AreaMultiOptional b = new AreaMultiOptional(true, false, null, SingleAnswerBox.class, data, this, StyledButtonType.ADD);
		AreaSingleConstant c = new AreaSingleConstant(new LinkBox(data));
		relaxedPlaceForSomeFancyGuestsToChill = b;
		placeholder = c;
		addArea(a);
		addArea(b);
		addArea(c);
		a.clearAndHide();
		hideArea(c);
		b.setListener(this);
	}
	
	@Override
	public AreaButtonAccepter getPlaceForExternalButtons(Object whoIsAsking) {
		return whoIsAsking == relaxedPlaceForSomeFancyGuestsToChill ? placeholder : relaxedPlaceForSomeFancyGuestsToChill;
	}
	
	/*
	public void hideArea(Area a) {
		super.hideArea(a);
		if (a == relaxedPlaceForSomeFancyGuestsToChill) {
			showPlaceholderIfNeeded();
		}
	}

	@Override
	public void showArea(Area a) {
		super.showArea(a);
		if (a == relaxedPlaceForSomeFancyGuestsToChill) {
			hidePlaceholderIfNeeded();
		}
	}*/
	
	private void showPlaceholderIfNeeded() {
		if (placeholderActive) {
			return;
		}
		for (SingleAnswerBox s : answers) {
			if (!s.hasTest()) {
				return;
			}
		}
		showArea((Area) placeholder);
		placeholderActive = true;
	}
	private void hidePlaceholderIfNeeded() {
		if (!placeholderActive) {
			return;
		}
		hideArea((Area) placeholder);
		placeholderActive = false;
		data.getEditor().updateArrows();
	}
	
	public void onTestAddedToAnswer() {
		showPlaceholderIfNeeded();
	}
	public void onTestRemovedFromAnswer() {
		hidePlaceholderIfNeeded();
	}

	@Override
	public void onElementAdded(Node n) {
		answers.add((SingleAnswerBox) n);
		/*if (answers.size() == 1) {
			return;
		}*/
		hidePlaceholderIfNeeded();
	}
	@Override
	public void onElementRemoved(Node n) {
		answers.remove(n);
		/*
		if (answers.isEmpty()) {
			return;
		}*/
		showPlaceholderIfNeeded();
		data.getEditor().updateArrows();
	}
	
	/**
	 * Fortunately almost everything is organizing itself automatically when we load new data, because all Areas do the same things and make the same
	 * calls during loading as they do if the user interacts with them. So all buttons and everything is already in right position, and because all
	 * SingleAnswerBoxes also report if a test is added or removed during a loading process (and the AreaMultiOptional reports all element changes
	 * to its listener (us) as well), the placeholder is automatically in place, iif it is needed. 
	 */
	
	@Override
	public Object save() {
		int length = placeholderActive ? areas.size() : areas.size()-1;
		Object[] s = new Object[length];
		for (int i = 0; i < length; i++) {
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
		if (l.length == areas.size()-1) { //placeholder should be hidden here
			placeholder.resetNode(data);
		}
	}
}
