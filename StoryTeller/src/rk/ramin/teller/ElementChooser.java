package rk.ramin.teller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ScrollEvent.VerticalTextScrollUnits;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * @author Der Guru
 * This Element is a container for several other Elements that the user may choose from. It itself only adds a ... button, with that 
 * the different elements can be selected. 
 * This Element also manages the saving and restoring of the right Element.
 * 
 * Important for the concrete subclasses: The getElementList-method is used to determine which elements will be suggested and in which order.
 * The first element of the list is the default element. 
 */
public abstract class ElementChooser extends HBox implements BriefedNode, Saveable {

	private ButtonProvider owner;
	private Area receptor;
	private boolean alreadyBriefed = false;
	
	private ElementChooserType index;
	/** An ElementChooserItem also has to be a node */
	private ElementChooserItem item;
	
	private StyledButton button;
	
	private ConstructionData data;
	
	private double scrollOffset;
	
	private ContextMenu menu;
	
	public ElementChooser(ConstructionData data) {
		this.data = data;
		this.setSpacing(EditorWindow.SPACING);
		//setAlignment(Pos.CENTER);
		button = new StyledButton(StyledButtonType.DOTS);
		this.getChildren().add(button);
		index = getItemList()[0];
		button.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				ElementChooserType[] items = getItemList();
				MouseButton but = event.getButton();
				if (event.isShiftDown() || event.isControlDown()) {
					if (but == MouseButton.PRIMARY) {
						nextItem();
					} else if (but == MouseButton.SECONDARY) {
						previousItem();
					}
				} else {
					if (but == MouseButton.PRIMARY) {
						if (items.length == 2) {
							nextItem(); //If there are only two elements, we cycle through them without showing a context menu
						} else {
							showContextMenu();
						}
					} else if (but == MouseButton.SECONDARY) {
						nextItem();
					}
				}
			}
		});
		button.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent event) {
				double units = -event.getTextDeltaY();
				if (Math.abs(units) > 0.0001) {
					if ((units > 0 && scrollOffset < 0) || (units < 0 && scrollOffset > 0)) {
						scrollOffset = units;
					} else {
						scrollOffset += units;
					}
					if (scrollOffset >= 1) {
						scrollOffset = 0;
						nextItem();
					} else if (scrollOffset <= -1) {
						scrollOffset = 0;
						previousItem();
					}
				}
			}
		});
		generateItem();
	}
	
	private void showContextMenu() {
		if (menu == null) {
			ElementChooserType[] items = getItemList();
			menu = new ContextMenu();
			for (int i = 0; i < items.length; i++) {
				MenuItem mi = new MenuItem(items[i].getDescription());
				mi.setUserData(items[i]);
				mi.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent ae) {
						changeItem((ElementChooserType) ((MenuItem)ae.getSource()).getUserData()); //OO at its finest ...
					}
				});
				menu.getItems().add(mi);
			}
		}
		menu.show(button, Side.BOTTOM, 0, 0);
	}
	
	private void nextItem() {
		ElementChooserType[] list = getItemList();
		int i = getIndexInList();
		if (++i >= getItemList().length) {
			i = 0;
		}
		changeItem(list[i]);
	}
	
	private void previousItem() {
		ElementChooserType[] list = getItemList();
		int i = getIndexInList();
		if (--i <= 0) {
			i = getItemList().length - 1;
		}
		changeItem(list[i]);
	}
	
	/**
	 * Very small helper method, finds and returns the index in the getItemList, at that our index is.
	 * @return
	 */
	private int getIndexInList() {
		ElementChooserType[] list = getItemList();
		for (int i = 0; i < list.length; i++) {
			if (list[i] == index) {
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public Object save() {
		return new Object[] {index, item.save()};
	}

	@Override
	public void load(Object o) {
		Object[] arr = (Object[]) o;
		ElementChooserType newIndex = ((ElementChooserType)arr[0]);
		if (newIndex != index) {
			index = newIndex;
			generateItem();
		}
		item.load(arr[1]);
	}
	
	/**
	 * Changes the item that this elementChooser contains, to the one with the given index.
	 * If the index does not change through this, nothing happens.  
	 * @param newIndex
	 */
	public void changeItem(ElementChooserType index) {
		if (this.index != index) {
			this.index = index;
			generateItem();
			onElementChanged();
		}
	}
	
	/**
	 * Generates a new item with the current item index (also, if the current item already is from this index, so check this first!), 
	 * and replaces it with the current item in this ElementChooser.
	 * The new item is stored in the item variable and gets briefed. 
	 */
	private void generateItem() {
		this.getChildren().remove(item); //It the item is null, this does not matter
		try {
			item = (ElementChooserItem) (index.getElementClass()).getConstructor(ConstructionData.class).newInstance(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.getChildren().add((Node) item);
		HBox.setHgrow((Node) item, Priority.ALWAYS);
		briefItem();
	}

	@Override
	public void brief(ButtonProvider owner, Area receptor) {
		this.owner = owner;
		this.receptor = receptor;
		alreadyBriefed = true;
		briefItem();
	}
	
	/**
	 * (Re)Briefs the current item with the current owner- and receptor-values. 
	 */
	private void briefItem() {
		if (alreadyBriefed) {
			item.brief(owner, receptor);
		}
	}
	
	/**
	 * The concrete implementations of this class return an array of all Elements that may be used here.
	 * I honestly did not manage to make this whole thing declared the right way, BUT every class within the item list has to implement the ElementChooserItem. 
	 * (The list must have at least one entry.)
	 * @return
	 */
	public abstract ElementChooserType[] getItemList();

	public void requestFocus() {
		((Node)item).requestFocus();
	}
	
	/**
	 * Override this empty method in implementing subclasses to react to the change of our item. 
	 * This is called after the item actually got changed, not if just its data changed. 
	 */
	protected void onElementChanged() {}
}
