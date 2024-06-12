package rk.ramin.teller;

public class ConstructionData {
	
	private EditorWindow editor;
	private RomanBox romans;
	private AnswersBox answers;
	
	public ConstructionData(EditorWindow editor) {
		this.editor = editor;
	}
	
	public EditorWindow getEditor() {
		return editor;
	}
	
	public void setRomanBox(RomanBox r) {
		romans = r;
	}
	public RomanBox getRomanBox() {
		return romans;
	}

	public void setAnswers(AnswersBox answers) {
		this.answers = answers;
	}
	public AnswersBox getAnswers() {
		return answers;
	}
}
