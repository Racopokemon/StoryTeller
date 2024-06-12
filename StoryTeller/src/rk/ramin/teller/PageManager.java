package rk.ramin.teller;

import java.awt.Robot;
import java.util.ArrayList;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class PageManager extends ScrollPane {
	
	private EditorWindow parent;
	private PageHolder selected;
	private ElementLink remitter;
	private Pane content;
	
	private ArrayList<PageHolder> pages = new ArrayList<PageHolder>(); 
	private ArrayList<Arrow> arrows = new ArrayList<Arrow>();
	private Arrow highlightedArrow = null;
	
	private Line linker;
	private Robot robot;
	private boolean selectPageNameAfterLinking;
	
	private Text name;
	
	private double dragX, dragY;
	
	public PageManager(EditorWindow parent) {
		try {
			robot = new Robot();
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.parent = parent;
		content = new Pane();
		this.setContent(content);
		this.setFitToHeight(true);
		this.setFitToWidth(true);
		//content.setMinSize(1000, 1000);
		content.setOnMousePressed(new ClickHandler());
		content.setOnMouseExited(new ExitHandler());
		content.setOnMouseMoved(new MoveHandler());
		content.setOnMouseDragged(new DragHandler());
		this.setOnKeyPressed(new KeyHandler());
		linker = new Line();
		linker.setStroke(Color.DODGERBLUE);
		linker.setStrokeWidth(2);
		name = new Text();
		name.setVisible(false);
		content.getChildren().add(name);
	}
	
	private void showName(PageHolder p) {
		if (p.getPage().getName().equals("")) {
			hideName();
		} else {
			name.setText(p.getPage().getName());
			content.getChildren().remove(name);
			content.getChildren().add(name); //Has to be top most
			Bounds b = name.getBoundsInLocal();
			name.relocate(p.getMidX()-b.getWidth()*0.5, p.getMidY()-PageHolder.RADIUS*2.5);
			name.setVisible(true);
		}
	}
	private void hideName() {
		name.setVisible(false);
	}
	
	public void pageClicked(PageHolder p, MouseEvent e) {
		if (remitter == null) {
			Point2D pt = content.sceneToLocal(e.getSceneX(), e.getSceneY());
			dragX = p.getMidX()-pt.getX();
			dragY = p.getMidY()-pt.getY();
			if (e.getButton() == MouseButton.SECONDARY && (e.isControlDown() || e.isShiftDown())) {
				removePage(p);
			} else {
				if (p == selected) {
					return;
				}
			}
			selectPage(p);
			updateArrows();
		} else {
			if (e.getButton() == MouseButton.PRIMARY) {
				if (selected == p) {
					remitter.setLink(null);
					System.out.println("Attempted to link to itself. Removed the link.");
				} else {
					remitter.setLink(p.getPage());
					save(); 
				}
			} else {
				remitter.linkingEndedWithoutChange();
				save(); //Saving changes instantly to update the arrows
			}
			exitLinking();
		}
	}
	
	private void selectPage(PageHolder p) {
		if (selected != null) {
			save();
			selected.setSelected(false);
		}
		parent.loadPage(p.getPage());
		selected = p;
		selected.setSelected(true);
	}
	
	public void paneClicked(MouseEvent event) {
		if (remitter == null) {
			if (event.getClickCount()==2) {
				Point2D p = content.sceneToLocal(event.getSceneX(), event.getSceneY());
				PageHolder h = new PageHolder(this, p.getX(), p.getY());
				addPage(h);
				selectPage(h);
			} else {
				unload();
			}
			updateArrows();
		} else {
			if (event.getButton() == MouseButton.PRIMARY) {
				Point2D p = this.sceneToLocal(event.getSceneX(), event.getSceneY());
				PageHolder ph = new PageHolder(this, p.getX(), p.getY());
				addPage(ph);
				remitter.setLink(ph.getPage());
				if (event.isControlDown() || event.isShiftDown()) {
					save();
				} else {
					selectPageNameAfterLinking = true;
					selectPage(ph); //Saving already happens with this call
				}
				//remitter.setLink(null);
				//save();
			} else {
				remitter.linkingEndedWithoutChange();
				save();
			}
			exitLinking();
		}
	}
	
	public void addPage(PageHolder p) {
		pages.add(p);
		parent.addPage(p.getPage());
		content.getChildren().add(p);
		updateArrows();
	}
	
	public void removePage(PageHolder h) {
		if (remitter != null) {
			throw new RuntimeException("Attempt to remove a pageHolder during linking process");
		}
		if (selected == h) {
			unload();
		}
		pages.remove(h);
		content.getChildren().remove(h);
		parent.deletePage(h.getPage());
		updateArrows();
	}
	
	public void beginLinking(ElementLink rem) {
		save();
		updateArrows();
		remitter = rem;
		selectPageNameAfterLinking = false;
		content.getChildren().add(linker);
		linker.setStartX(selected.getMidX());
		linker.setStartY(selected.getMidY());
		linker.setEndX(selected.getMidX());
		linker.setEndY(selected.getMidY());
		Point2D p = content.localToScreen(selected.getMidX(), selected.getMidY());
		robot.mouseMove((int)p.getX(), (int)p.getY());
		this.requestFocus();
		//Push mouse to selected page
			//If right mouse or any key is pressed or the mouse leaves the manager, the linking process is ended. 
			//If the manager itself gets clicked, null will be sent to the remitter and the linking process will be ended. 
			//If a page gets clicked, this page will be sent to the remitter. If the clicked page is already selected, null will be returned. 
	}
	
	private void moveMouseToRemitter() {
		Bounds b;
		Point2D p;
		if (selectPageNameAfterLinking) {
			TextField nm = parent.getPageNameTextField();
			b = nm.getBoundsInLocal();
			p = nm.localToScreen(
				0.5*(b.getMaxX()+b.getMinX()),
				0.5*(b.getMaxY()+b.getMinY()));
			//nm.requestFocus();
			//nm.positionCaret(0); Not working, dont know why :(
		} else {
			b = remitter.getBoundsInLocal();
			p = remitter.localToScreen(
					0.5*(b.getMaxX()+b.getMinX()),
					0.5*(b.getMaxY()+b.getMinY()));
		}
		robot.mouseMove((int)p.getX(), (int)p.getY());
	}
	
	private void save() {
		if (selected != null) {
			parent.saveInPage(selected.getPage());
		}
	}
	public void saveAppearanceOnly() {
		parent.saveInPageAppearanceOnly(selected.getPage());
		if (selected.getPage().getMarker() != 0) parent.getVarManager().updatePagesTextOnly();
		//Maybe were using something that a pageManager should know / care about, but anyway:
		//To avoid the buttons being updated for every page name, that has nothing to do with a init or current page,
		//we can simply check its marker - only if this is set (different to 0), it gets rendered as a init or current page
		//and even needs to receive an update for the buttons. 
	}
	
	public void redrawArrows() {
		save();
		updateArrows();
	}
	
	public void unload() {
		if (selected == null) {
			return;
		}
		save();
		parent.unloadPage();
		selected.setSelected(false);
		selected = null;
	}
	
	private void exitLinking() {
		moveMouseToRemitter();
		remitter = null;
		content.getChildren().remove(linker);
		updateArrows();
	}
		
	private class ClickHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) {
			for (PageHolder ph : pages) {
				double x = event.getSceneX(), y = event.getSceneY();
				Point2D pt = ph.sceneToLocal(x,y); 
				if (ph.contains(pt)) {
					pageClicked(ph, event);
					return;
				}
			}
			paneClicked(event);
		}
	}
	private class MoveHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) {
			if (remitter != null) {
				Point2D p = linker.sceneToLocal(event.getSceneX(),event.getSceneY());
				linker.setEndX(p.getX());
				linker.setEndY(p.getY());
			}
			for (PageHolder ph : pages) {
				double x = event.getSceneX(), y = event.getSceneY();
				Point2D pt = ph.sceneToLocal(x,y); 
				if (ph.contains(pt)) {
					showName(ph);
					return;
				}
			}
			hideName();
		}
	}
	private class ExitHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) {
			if (remitter != null) {
				remitter.linkingEndedWithoutChange();
				save();
				exitLinking();
			}
		}
	}
	private class KeyHandler implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent event) {
			if (remitter == null) {
				if ((selected != null) && (event.getCode() == KeyCode.DELETE)) {
					removePage(selected);
				} else if (event.getCode() == KeyCode.F5) {
					parent.getVarManager().initiateTest(3);
				} else if (event.getCode() == KeyCode.F11) {
					parent.getVarManager().initiateTest(2);
				}
			} else if (event.getCode() != KeyCode.CONTROL && event.getCode() != KeyCode.SHIFT) {
				if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
					remitter.setLink(null);
					//save(); 
				} else {
					remitter.linkingEndedWithoutChange();
					save();
				}
				exitLinking();
			}
		}
	}
	private class DragHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) {
			if (selected != null) {
				Point2D p = content.sceneToLocal(event.getSceneX()+dragX,event.getSceneY()+dragY);
				selected.setPosAndApply(p.getX(), p.getY());
				hideName();
				updateArrows();
			}
		}
	}
	
	private void updateArrows() {
		getChildren().removeAll(arrows);
		arrows.clear();
		for (PageHolder from : pages) {
			int customLinksAtCustomAnswer = 0, customLinksAtAnswer = 0;
			boolean sel = from == selected;
			byte type = sel ? Arrow.SELECTED : Arrow.NORMAL;
			for (PageLink to : from.getPage().getAllLinks()) {
				if (to.isCustomLink()) {
					if (to.isCustomAnswer()) {
						customLinksAtCustomAnswer++;
					} else {
						customLinksAtAnswer++;
					}
				} else {
					Arrow w = Arrow.getArrow(from.getMidX(), from.getMidY(), to.getLink().getWrapper().getMidX(), to.getLink().getWrapper().getMidY(),
							type , sel ? to.getLink() : null, to.isCustomAnswer()); 
					if (w != null) {
						arrows.add(w);
					}
				}
			}
			if (customLinksAtCustomAnswer + customLinksAtAnswer > 0) {
				arrows.add(new Arrow(from.getMidX(), from.getMidY(), type, customLinksAtAnswer, customLinksAtCustomAnswer));
			}
		}
		getChildren().addAll(arrows);
		highlightedArrow = null;
	}
	
	public void highlightArrow(Page id) {
		if (id == null) {
			//un-highlight
			if (highlightedArrow != null) {
				highlightedArrow.setType(Arrow.SELECTED);
				highlightedArrow = null;
			}
		} else {
			//highlight new one
			if (highlightedArrow != null) {
				highlightedArrow.setType(Arrow.SELECTED);
				highlightedArrow = null;
			}
			for (Arrow a : arrows) {
				if (a.hasThisId(id)) {
					a.setType(Arrow.HOVER);
					highlightedArrow = a;
					return;
				}
			}
			System.out.println("How could this happen? A linkBar requested to highlight an arrow with this page as id "+id+"but there is no such an arrow :o");
		}
	}
	
	/**
	 * Returns an arrayList containing all pages.
	 * Be warned: All this pages still contain references to their wrappers - 
	 * to avoid memory leaks you have to clear these references before loading another chapter. 
	 */
	public ArrayList<Page> getAllPages() {
		ArrayList<Page> ret = new ArrayList<Page>();
		for (PageHolder p : pages) {
			ret.add(p.getPage());
		}
		return ret;
	}

	/**
	 * Loads a new chapter, or just cleans things up if you give null
	 */
	public void loadChapter(Chapter c) {
		selected = null;
		parent.unloadPage();
		remitter = null;
		pages.clear();
		content.getChildren().clear();
		if (c != null) {
			for (Page p : c.getPages()) {
				PageHolder ph = new PageHolder(this, p);
				pages.add(ph);
				content.getChildren().add(ph);
			}
		}
		updateArrows();
	}
	
	/**
	 * Yees you can ask for the selected page - but causing updates for it is still MY job!
	 */
	public Page getSelectedPage() {
		if (selected == null) {
			return null;
		} else {
			return selected.getPage();
		}
	}
}
