package rk.ramin.teller;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class PageHolder extends Group {
	
	public static final double RADIUS = 12;
	
	private Page page;
	//private PageManager parent;
	
	private Ellipse el;
	private Text shortcut;
	
	public PageHolder(PageManager pm, Page p) {
		page = p;
		init(pm);
	}
	
	/**
	 * Automatically builds a page. MidX and midY contain the mouse position during the double click.
	 */
	public PageHolder(PageManager pm, double midX, double midY) {
		page = new Page();
		page.setPosition(midX, midY);
		init(pm);
	}
	
	private void init(PageManager pm) {
		//parent = pm;
		this.setMouseTransparent(true);
		el = new Ellipse(RADIUS, RADIUS);
		el.setStroke(Color.BLACK);
		el.setStrokeWidth(1);
		shortcut = new Text();
		shortcut.setTextOrigin(VPos.CENTER);
		shortcut.setTextAlignment(TextAlignment.CENTER);
		getChildren().addAll(el,shortcut);
		page.setWrapper(this);
		updateAppearance();
		applyPosition();
	}
	
	/**
	 * The center of this page will be placed at the given coordinates - relative to the "content" pane in the pageManager.
	 */
	public void setPosAndApply(double x, double y) {
		page.setPosition(x, y);
		applyPosition();
	}
	
	private void applyPosition() {
		el.relocate(page.getX()-RADIUS,page.getY()-RADIUS);
		applyShortcutPosition();
	}
	private void applyShortcutPosition() {
		Bounds b = shortcut.getLayoutBounds();
		shortcut.relocate(page.getX()-b.getWidth()/2., page.getY()-b.getHeight()/2.);
	}
	
	public Page getPage() {
		return page;
	}
	
	public void setSelected(boolean s) {
		if (s) {
			el.setStrokeWidth(2.5);
		} else {
			el.setStrokeWidth(1);
		}
	}
	
	/*
	private class ClickHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent me) {
			System.out.println("page got clicked");
			//Right click? Left click? Shift+Right (for removal?)
			//Whats about drag/drop?
			if (me.getButton() == MouseButton.SECONDARY && (me.isShiftDown() || me.isAltDown() || me.isControlDown())) {
				parent.removePage(PageHolder.this);
			} else {
				parent.pageClicked(PageHolder.this);
			}
		}
	}*/
	
	/**
	 * The x coordinate of the center of this pageHolder, relative to the "content" pane in the pageManager
	 */
	public double getMidX() {
		return page.getX();
	}
	/**
	 * The y coordinate of the center of this pageHolder, relative to the "content" pane in the pageManager
	 */
	public double getMidY() {
		return page.getY();
	}
	
	public void updateAppearance() {
		el.setFill(page.getColor());
		el.getStrokeDashArray().clear();
		byte m = page.getMarker();
		if (m == 1 || m == 3) el.getStrokeDashArray().addAll(5., 5.);
		if (m > 1) el.getStrokeDashArray().addAll(1., 5.);
		shortcut.setText(page.getShortcut());
		shortcut.setFill(page.isDark() ? Color.WHITE : Color.BLACK);
		applyShortcutPosition();
	}
}
