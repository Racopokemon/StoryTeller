package rk.ramin.teller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class ElementLink extends Button implements ElementChooserItem {
	
	private Page link, oldLink;
	private static EditorWindow editor;
	private Font basicFont;
	private Paint basicColor;
	
	private static MouseEnterHandler mEnter;
	private static MouseLeaveHandler mLeave;
	
	public static final Color noLinkColor = Color.gray(0.45);
	
	public ElementLink(ConstructionData data) {
		if (editor == null) {
			//First instance of link bar - here we add all static references
			editor = data.getEditor();
			mEnter = new MouseEnterHandler();
			mLeave = new MouseLeaveHandler();
		}
		
		basicFont = getFont();
		basicColor = getTextFill(); 
		setFocusTraversable(false);
		setMaxWidth(100000); //Might be enough ...
		setStyle("-fx-alignment: center-left");
		setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent ae) {
				oldLink = link;
				link = null;
				setFont(EditorWindow.getOtherFontType(basicFont, FontWeight.NORMAL, FontPosture.ITALIC));
				setText("select a page");
				setTextFill(Color.WHITE);
				editor.requestLinking(ElementLink.this);
			}
		});
		setOnMouseEntered(mEnter);
		setOnMouseExited(mLeave);
		getStyleClass().add("link");
		updateLinkText();
		//Shows its destination on hover?
	}
	
	public Page getLink() {
		return link;
	}
	public void setLink(Page p) {
		link = p;
		updateLinkText();
	}
	
	public void linkingEndedWithoutChange() {
		//Called instead of setLink, if the linked page didn't change
		link = oldLink;
		updateLinkText();
	}
	
	private void updateLinkText() {
		if (link == null) {
			setTextFill(noLinkColor);
			setText("Click to link");
			setFont(EditorWindow.getOtherFontType(basicFont, FontWeight.NORMAL, FontPosture.ITALIC));
		} else {
			setTextFill(basicColor);
			if (link.getName().equals("")) {
				setText("Linked.");
			} else {
				setText("-> "+link.getName());
			}
			setFont(EditorWindow.getOtherFontType(basicFont, FontWeight.NORMAL, FontPosture.REGULAR));
		}
	}
	
	private class MouseEnterHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent me) {
			Page id = ((ElementLink) me.getTarget()).getLink();
			if (id != null) {
				editor.highlightArrow(id);
			}
		}
	}
	private class MouseLeaveHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent me) {
			editor.highlightArrow(null);
		}
	}
	@Override
	public Object save() {
		return link;
	}

	@Override
	public void load(Object o) {
		link = (Page) o;
		updateLinkText();
	}

	@Override
	public void brief(ButtonProvider owner, Area receptor) {}
}
