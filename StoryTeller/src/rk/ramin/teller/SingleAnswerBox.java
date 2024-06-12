package rk.ramin.teller;

import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class SingleAnswerBox extends Box implements BriefedNode {
	
	private ElementTextField textField;
	private AreaTest test;
	private boolean testShown = false;
	private AnswersBox answers;
	
	public SingleAnswerBox(ConstructionData data) {
		super(true, true, Color.CORNFLOWERBLUE);
		answers = data.getAnswers();
		ElementTextField answer = new ElementTextField(data);
		AreaSingleConstant a = new AreaSingleConstant(answer);
		relaxedPlaceForSomeFancyGuestsToChill = a;
		//answer.brief(null);
		AreaTest b = new AreaTest(this, data);
		AreaSingleConstant c = new AreaSingleConstant(new LinkBox(data));
		addArea(a);
		addArea(b);
		addArea(c);
		b.hide();
		textField = answer;
		test = b;
	}

	@Override
	public void brief(ButtonProvider owner, Area receptor) {
		textField.brief(owner, receptor);
	}
	
	public void requestFocus() {
		textField.requestFocus();
	}
	
	@Override
	public void hideArea(Area a) {
		super.hideArea(a);
		if (a == test) {
			testShown = false;
			answers.onTestRemovedFromAnswer();
		}
	}
	@Override
	public void showArea(Area a) {
		super.showArea(a);
		if (a == test) {
			testShown = true;
			answers.onTestAddedToAnswer();
		}
	}
	public boolean hasTest() {
		return testShown;
	}
}
