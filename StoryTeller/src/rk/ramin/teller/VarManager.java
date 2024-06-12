package rk.ramin.teller;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.ListenerNotFoundException;
import javax.management.RuntimeErrorException;

import com.sun.org.apache.xpath.internal.operations.Div;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.DepthTest;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import jdk.nashorn.internal.ir.CatchNode;
import rk.ramin.teller.VarManager.Var;

/**
 * Should have done this (making an own class for it) already with a PagePropertiesManager instead of writing everything into the EditorWindow
 */
public class VarManager {
	
	private EditorWindow parent;
	
	private ObservableList<String> varNames = FXCollections.observableArrayList();
	private ObservableList<Var> vars = FXCollections.observableArrayList();
	private TableView<Var> view;
	private Button varAdd;
	private Button removeSaveStatButton;
	private TextField name; 
	private TextField saveStatName;
	private TabPane tabs;
	
	private TextField filterText;
	private ToggleButton filterCheck;
	private Label placeholderLabel;
	
	private boolean listenToSaveStatName = true;
	
	private ArrayList<SaveStat> saves = new ArrayList<>();
	
	private SaveStat editedSaveStat;
	private Page initPage, currentPage;
	private Button initButton, currentButton;
	
	private Button resetToInitButton;
	
	/*
	https://docs.oracle.com/javase/tutorial/essential/regex/index.html
	That really helped me, let me recap:
	\A This has to be the beginning of the String
	\[a-zA-Z@_\$] A single character as small or big letter,@,_,$ (escaped), but e.g. no number, whitespace, etc
	\[a-zA-Z@_\$0-9]*\Z followed by as much characters of the type a-zA-Z@_\$0-9 
	     (same but with digits) as you want (*), and the condition that nothing will follow this before the string ends (\Z)
	So there can be only one match or no one, and if it spans over the whole input. (We can also call matches())
	If we insert an empty string, the first character does not exist and there is also no match.
	Pheeew ...
	*/
	private Pattern varFormatChecker = Pattern.compile("\\A[a-zA-Z@_\\$][a-zA-Z@_\\$0-9]*\\Z"); 
	private Font basicFont, italicFont;
	
	public VarManager(EditorWindow e) {
		parent = e;
	}
	
	public Node getGui() {
		new SortedList<>(varNames);
		view = new TableView<Var>(vars);
		placeholderLabel = new Label();
		placeholderLabel.setWrapText(true);
		placeholderLabel.setTextAlignment(TextAlignment.CENTER);
		view.setPlaceholder(placeholderLabel);
		view.setEditable(true);
		TableColumn<Var, String> colName = new TableColumn<Var, String>("Name");
		TableColumn<Var, String> colInit = new TableColumn<Var, String>("Initial value");
		TableColumn<Var, String> colCurrent = new TableColumn<Var, String>("Current value");
		Callback<CellDataFeatures<Var,String>, ObservableValue<String>> c = new Callback<TableColumn.CellDataFeatures<Var,String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(CellDataFeatures<Var, String> wrap) {
				if (wrap.getTableColumn() == colInit) {
					return wrap.getValue().initProperty;
				} else if (wrap.getTableColumn() == colName) {
					return wrap.getValue().nameProperty;
				} else {
					return wrap.getValue().currentProperty;
				}
			}
		};
		colName.setCellValueFactory(c);
		colInit.setCellValueFactory(c);
		colCurrent.setCellValueFactory(c);
		
		// Again copy and pasted from the oracle tutorials. Nice work!
        Callback<TableColumn<Var, String>, TableCell<Var, String>> cellFactory =
                new Callback<TableColumn<Var, String>, TableCell<Var, String>>() {
                    public TableCell<Var, String> call(TableColumn<Var, String> p) {
                       return new EditCell();
                    }
                };
		
		colInit.setCellFactory(cellFactory);
		colCurrent.setCellFactory(cellFactory);
		EventHandler<CellEditEvent<Var, String>> commitHandler = new EventHandler<TableColumn.CellEditEvent<Var,String>>() {
			@Override
			public void handle(CellEditEvent<Var, String> event) {
				int i = Integer.parseInt(event.getNewValue());
				Var v = event.getRowValue();
				SaveStat s = saves.get(getSelectedTabIndex());
				String name = v.getName();
				if (event.getTableColumn() == colInit) {
					editedSaveStat.changeVarInitValue(name, i);
					v.initProperty.set(""); //Its all a bit cheated and botched here, as the construction with clicking on another tab during an edit causes some problems.
					//Anyhow the cells save their value for themselves as well, and in some cases they dont update right if the focus change and the change of values comes almost at the same time.
					//It turns out, that, if the value saved in the cell before was the same at the old and the new tab, the property in the var does not fire an value changed event, but the 
					//cell still shows the typed value. To ensure that this cell gets updated in any case, we force an cange event by setting the "" first. 
					v.initProperty.set(String.valueOf(s.getInit(name)));
				} else {
					editedSaveStat.changeVarCurrentValue(name, i);
					v.currentProperty.set("");
					v.currentProperty.set(String.valueOf(s.getCurrent(name)));
				}
			}
		};
		colInit.setOnEditCommit(commitHandler);
		colCurrent.setOnEditCommit(commitHandler);
		
		//colInit.impl_setReorderable(false);   //Oh COME ON - you did so much work to make all of this unnecessary shit happen automatically - 
		//colCurrent.impl_setReorderable(false);  //but then you don't implement a simple option to disable those great great features???
		view.getColumns().addAll(colName, colInit, colCurrent);
		colName.setSortType(SortType.ASCENDING);
		view.getSortOrder().add(colName);
		view.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
					removeSelected();
				}
			};
		});
		view.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
					removeSelected();
				}
			}
		});
		varAdd = new Button("+");
		HBox.setHgrow(varAdd, Priority.NEVER);
		varAdd.setDisable(true);
		EventHandler<ActionEvent> add = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (!varAdd.isDisabled()) {
					addVar();
				}
			}
		};
		varAdd.setOnAction(add);

		name = new TextField();
		name.setPromptText("Enter a var name");
		name.textProperty().addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable o) {
				updateAddButtonDisable();
			}
		});
		name.setOnAction(add);
		
		HBox varControl = new HBox();
		varControl.getChildren().add(name);
		varControl.getChildren().add(varAdd);
		varControl.setHgrow(name, Priority.ALWAYS);
		VBox vars = new VBox(EditorWindow.SPACING);
		
		GridPane saveStatPane = new GridPane(),
				miscPane = new GridPane();
		Button addSaveStatButton = new Button("Clone"), removeSaveStatButton = new Button("Delete"),
				resetLeftButton = new Button("To init"), resetRightButton = new Button("To current"),
				restartButton = new Button("Restart"), continueButton = new Button("Continue"),
				fromSelectedButton = new Button("From selected"), fromStartButton = new Button("From start");
		EventHandler<ActionEvent> resetter = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				SaveStat s = saves.get(getSelectedTabIndex());
				s.reset(e.getSource() == resetRightButton);
				updateVarValues();
				updatePages();
			}
		};
		resetLeftButton.setOnAction(resetter);
		resetRightButton.setOnAction(resetter);
		resetToInitButton = resetLeftButton;
		
		EventHandler<ActionEvent> playHandler = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Object o = event.getSource();
				if (o == restartButton) {
					initiateTest(0);
				} else if (o == continueButton) {
					initiateTest(1);
				} else if (o == fromSelectedButton) {
					initiateTest(2);
				} else {
					initiateTest(3);
				}
			}
		};
		restartButton.setOnAction(playHandler);
		continueButton.setOnAction(playHandler);
		fromSelectedButton.setOnAction(playHandler);
		fromStartButton.setOnAction(playHandler);
		
		this.removeSaveStatButton = removeSaveStatButton;
		addSaveStatButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				cloneSaveStat();
			}
		});
		removeSaveStatButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				removeSaveStat();
			}
		});
		addSaveStatButton.setMaxWidth(10000000);
		removeSaveStatButton.setMaxWidth(10000000);
		resetLeftButton.setMaxWidth(10000000);
		resetRightButton.setMaxWidth(10000000);
		restartButton.setMaxWidth(10000000);
		continueButton.setMaxWidth(10000000);
		fromSelectedButton.setMaxWidth(10000000);
		fromStartButton.setMaxWidth(10000000);
		saveStatName = new TextField();
		saveStatPane.add(addSaveStatButton, 0, 0);
		saveStatPane.add(removeSaveStatButton, 1, 0);
		GridPane.setHgrow(addSaveStatButton, Priority.ALWAYS);
		GridPane.setHgrow(removeSaveStatButton, Priority.ALWAYS);
		miscPane.add(resetLeftButton, 0, 0);
		miscPane.add(resetRightButton, 1, 0);
		miscPane.add(continueButton, 0, 1);
		miscPane.add(restartButton, 1, 1);
		miscPane.add(fromSelectedButton, 0, 2);
		miscPane.add(fromStartButton, 1, 2);
		GridPane.setHgrow(resetLeftButton, Priority.ALWAYS);
		GridPane.setHgrow(resetRightButton, Priority.ALWAYS);
		GridPane.setHgrow(continueButton, Priority.ALWAYS);
		GridPane.setHgrow(restartButton, Priority.ALWAYS);
		GridPane.setHgrow(fromSelectedButton, Priority.ALWAYS);
		GridPane.setHgrow(fromStartButton, Priority.ALWAYS);
		Tab t = new Tab("Launch");
		//TabPane tabs = new TabPane(t, new Tab("user1"), new Tab("user2"), new Tab("Chapter 2"), new Tab("TEMP"));
		tabs = new TabPane(t);
		tabs.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> value, Number oldV, Number newV) {
				boolean enable = newV.intValue() == 0;
				VarManager.this.removeSaveStatButton.setDisable(enable);
				VarManager.this.saveStatName.setDisable(enable);
				if (newV.intValue() != -1) {
					VarManager.this.listenToSaveStatName = false;
					VarManager.this.saveStatName.setText(VarManager.this.saves.get(newV.intValue()).getName());
					VarManager.this.listenToSaveStatName = true;
					VarManager.this.updateVarValues();
					updatePages();
				}
			}		
		});
		saveStatName.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (listenToSaveStatName) {
					//Add a unique test (ignoreCase)
					int index = VarManager.this.getSelectedTabIndex();
					VarManager.this.saves.get(index).setName(newValue);
					VarManager.this.tabs.getTabs().get(index).setText(newValue);
				}
			}
		});
		tabs.setMinHeight(30);
		tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
		
		GridPane pages = new GridPane();
		initButton = new Button();
		currentButton = new Button();
		pages.add(initButton, 0, 0);
		pages.add(currentButton, 1, 0);
		initButton.setMaxWidth(10000000);
		currentButton.setMaxWidth(10000000);
		pages.setHgrow(initButton, Priority.ALWAYS);
		pages.setHgrow(currentButton, Priority.ALWAYS);
		basicFont = currentButton.getFont();
		italicFont = EditorWindow.getOtherFontType(basicFont, FontWeight.NORMAL, FontPosture.ITALIC);
		EventHandler<ActionEvent> pageSetter = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Page s = parent.getSelectedPage();
				if (s == null) {
					return;
				}
				if (event.getSource() == initButton) {
					saves.get(getSelectedTabIndex()).setInitPage(s);
				} else {
					saves.get(getSelectedTabIndex()).setCurrentPage(s);
				}
				updatePages();
			}
		};
		initButton.setOnAction(pageSetter);
		currentButton.setOnAction(pageSetter);
		
		ChangeListener<Object> filterListener = new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
				updatePlaceholderText();
				reloadViewAndVars();
			}
		};
		filterText = new TextField("_");
		filterCheck = new ToggleButton("Except");
		filterCheck.setSelected(true);
		updatePlaceholderText();
		filterText.textProperty().addListener(filterListener);
		filterCheck.selectedProperty().addListener(filterListener);
		HBox.setHgrow(filterText, Priority.ALWAYS);
		HBox.setHgrow(filterCheck, Priority.NEVER);
		filterText.setPromptText("Filter by prefix");
		HBox filterBox = new HBox(filterText, filterCheck);
		filterBox.setAlignment(Pos.CENTER);
		VBox viewBox = new VBox(view, filterBox);
		VBox.setVgrow(tabs, Priority.NEVER);
		VBox.setVgrow(view, Priority.ALWAYS);
		VBox.setVgrow(viewBox, Priority.ALWAYS);
		
		vars.getChildren().addAll(saveStatPane, tabs, saveStatName, miscPane, varControl, viewBox, new Label("Click to use the selected page instead:"), pages);
		StackPane varMargin = new StackPane(vars);
		StackPane.setMargin(vars, new Insets(EditorWindow.SPACING));
		return varMargin;
	}
	
	public void setSaveStats(ArrayList<SaveStat> l) {
		view.getSelectionModel().clearSelection();
		vars.clear();
		tabs.getTabs().clear();
		saves = l;
		
		for (SaveStat s : l) {
			Tab t = new Tab(s.getName());
			tabs.getTabs().add(t);
		}
		tabs.getSelectionModel().select(0);
		
		reloadViewAndVars();
		
		updatePages();
	}

	private void reloadViewAndVars() {
		vars.clear(); 
		varNames.clear();
		
		SaveStat s = saves.get(getSelectedTabIndex());
		
		Map<String, Integer> i = s.getInit(), c = s.getCurrent();
		for (String k : i.keySet()) {
			if (matchesFilter(k)) {
				vars.add(new Var(k, i.get(k).toString(), c.get(k).toString()));
				varNames.add(k);
			}
		}
		
		view.sort();
	}
	
	/**
	 * An always up to date list containing all var names currently standing in the var list.
	 * Also stays up to date (and the same reference) if you load a new file, so once youve once requested for this reference,
	 * you don't need to worry about anything more during this runtime.  
	 */
	public ObservableList<String> getVarNameObservableList() {
		return varNames;
	}
	
	public int getSaveStatIndex() {
		return getSelectedTabIndex();
	}
	
	/**
	 * 0: restart current saveStat
	 * 1: continue current saveStat
	 * 2: start from selected page (if there is no one, does nothing.)
	 * 3: play from the very beginning (resetted launch saveStat)
	 */
	public void initiateTest(int type) {
		if (type == 2) {
			Page p = parent.getSelectedPage();
			if (p == null) {
				return;
			}
			saves.get(getSelectedTabIndex()).setCurrentPage(p);
		} else if (type == 3) {
			tabs.getSelectionModel().select(0);
			type = 0;
		}
		SaveStat s = saves.get(getSelectedTabIndex());
		if (type == 0) {
			saves.get(getSelectedTabIndex()).reset(false);
		}
		if (s.getCurrentPage() == null) {
			Page p = saves.get(0).getInitPage();
			if (p != null) {
				s.setCurrentPage(p);
			}
		}
		parent.PlayProject(saves.get(getSelectedTabIndex()));
	}
	
	private void cloneSaveStat() {
		SaveStat s = saves.get(getSelectedTabIndex());
		saves.add(s.clone());
		tabs.getTabs().add(new Tab(s.getName()));
		tabs.getSelectionModel().select(saves.size()-1);
		saveStatName.requestFocus();
		saveStatName.selectAll();
	}
	
	private void removeSaveStat() {
		int i = getSelectedTabIndex();
		if (i == 0) {
			throw new RuntimeException("How got this call through? You must not remove the launch save stat.");
		} else {
			saves.remove(i);
			tabs.getTabs().remove(i);
		}
	}
	
	private int getSelectedTabIndex() {
		return tabs.getSelectionModel().getSelectedIndex();
	}

	private void updateAddButtonDisable() {varAdd.setDisable(!validVarName(name.getText().trim()));}
	
	private void addVar() {
		String value = name.getText().trim();
		//if (validVarName(value)) { If the button is not disabled, the varName is already checked. 
		
		for (SaveStat s : saves) {
			s.addVar(value);
		}
		
		if (matchesFilter(value)) {
			vars.add(new Var(value));
			varNames.add(value);
			view.sort();
		}
		
		name.setText("");
		name.requestFocus();
		name.selectAll();
		//}
	}
	
	/*
	public Variable[] saveVarsToArray() {
		Variable[] ret = new Variable[vars.size()];
		int c = 0;
		for (Var v : vars) {
			ret[c++] = v.toVariable();
		}
		return ret;
	}
	*/
	
	private void removeSelected() {
		int index = view.getSelectionModel().getSelectedIndex();
		String name;
		if (index != -1 && SaveStat.mayDelete(name = vars.get(index).getName())) {
			vars.remove(index); 
			varNames.remove(name);
			for (SaveStat s : saves) {
				s.removeVar(name);
			}
			updateAddButtonDisable(); 
		}
	}
	
	private void updateVarValues() {
		SaveStat s = saves.get(getSelectedTabIndex());
		Map<String, Integer> i = s.getInit(), c = s.getCurrent();
		for (Var v : vars) {
			String name = v.getName();
			v.updateValues(i.get(name).toString(), c.get(name).toString());
		}
	}

	private boolean validVarName(String v) {
		return v.length() < 64 && getVar(v) == null && varFormatChecker.matcher(v).matches();
	}
	
	/**
	 * If there is no var with matching name, this will return null
	 */
	private Var getVar(String n) {
		for (Var v : vars) {
			if (v.equalName(n)) {
				return v;
			}
		}
		return null;
	}
	
	public void updatePages() {
		SaveStat s = saves.get(getSelectedTabIndex());
		Page i = s.getInitPage(), c = s.getCurrentPage();
		if (initPage != null) {
			initPage.setMarker((byte)0);
			updatePageApperanceIfItsVisible(initPage);
		}
		if (currentPage != null) {
			currentPage.setMarker((byte)0);
			updatePageApperanceIfItsVisible(currentPage);
		}
		if (i == c) {
			if (i != null) {
				i.setMarker((byte)3);
				updatePageApperanceIfItsVisible(i);
			}
		} else {
			if (i != null) {
				i.setMarker((byte)1);
				updatePageApperanceIfItsVisible(i);
			}
			if (c != null) {
				c.setMarker((byte)2);
				updatePageApperanceIfItsVisible(c);
			}
		}
		initPage = i;
		currentPage = c;
		updatePagesTextOnly();
	}
	/**
	 * Only refreshes the text written on the buttons, making them fit to the initPage and currentPage.
	 * You max call this after changing the pages themselves, after all vars are set and all pages are marked, to make also the
	 * buttons fit, or if only a pages name is changed, which could possibly the init or current one, to make it 
	 * fit in this case.  
	 */
	public void updatePagesTextOnly() {
		if (initPage == null) {
			initButton.setText("Init page empty");
			formatButton(initButton, false);
		} else if (initPage.getName() == null || initPage.getName().equals("")) {
			initButton.setText("Init page set");
			formatButton(initButton, true);
		} else {
			initButton.setText("Init page: " + initPage.getName());
			formatButton(initButton, true);
		}
		if (currentPage == null) {
			currentButton.setText("Current page empty");
			formatButton(currentButton, false);
		} else if (currentPage.getName() == null || currentPage.getName().equals("")) {
			currentButton.setText("Current page set");
			formatButton(currentButton, true);
		} else {
			currentButton.setText("Current page: " + currentPage.getName());
			formatButton(currentButton, true);
		}
	}
	private void formatButton(Button b, boolean linked) {
		if (linked) {
			b.setFont(basicFont);
			b.setTextFill(Color.BLACK);
		} else {
			b.setTextFill(ElementLink.noLinkColor);
			b.setFont(italicFont);
		}
	}
	private void updatePageApperanceIfItsVisible(Page p) {
		PageHolder h = p.getWrapper();
		if (h != null) h.updateAppearance();
	}
	
	private void updatePlaceholderText() {
		if (filterCheck.isSelected()) {
			placeholderLabel.setText("There are no variables that start with '"+ filterText.getText() +"' \nYou can change the filter in the text field below, \nor add a new variable using the text field above.");
		} else {
			placeholderLabel.setText("There are no variables not starting with '"+ filterText.getText() +"' \nYou can change the filter in the text field below, \nor add a new variable using the text field above.");
		}
	}
	
	public class Var {
		private SimpleStringProperty nameProperty = new SimpleStringProperty("Hey I'm nameless - how did you manage that?");
		private SimpleStringProperty initProperty = new SimpleStringProperty("-1");
		private SimpleStringProperty currentProperty = new SimpleStringProperty("-1");
		
		public Var(String name, String init, String current) {
			nameProperty.set(name);
			initProperty.set(init);
			currentProperty.set(current);
		}
		
		public Var(String name) {
			this(name, "0", "0");
		}
		
		/**
		 * Returns NaN if there is no number in the input. 
		 */
		/*
		public boolean isValueValid() {
			try {
				Double.valueOf(valueProperty.getValue());
				return true;
			} catch (Exception e) {
				return false;
			}
		}*/
		
		public void updateValues(String init, String current) {
			initProperty.setValue(init);
			currentProperty.setValue(current);
		}
		
		public String getName() {
			return nameProperty.getValue();
		}
		public SimpleStringProperty nameProperty() {
			return nameProperty;
		}
		
		public boolean equalName(String n) {
			return nameProperty.getValue().equalsIgnoreCase(n);
		}
	}
	
	/**
	 * Many thanks to james-d, this is exactly what i was looking for, and it works amazingly well. 
	 * 
	 * @author https://gist.github.com/james-d/be5bbd6255a4640a5357
	 *
	 */
	public class EditCell extends TableCell<Var, String> {

	    // Text field for editing
	    // TODO: allow this to be a plugable control.
	    private final TextField textField = new TextField();
	    
	    public EditCell() {
	        itemProperty().addListener((obx, oldItem, newItem) -> {
	            if (newItem == null) {
	                setText(null);
	            } else {
	                setText(newItem);
	            }
	        });
	        setGraphic(textField);
	        setContentDisplay(ContentDisplay.TEXT_ONLY);

	        textField.setOnAction(evt -> {
	            finish(textField.getText());
	        });
	        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
	            if (!isNowFocused) {
	                finish(textField.getText());
	            }
	        });
	        textField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
	            if (event.getCode() == KeyCode.ESCAPE) {
	                textField.setText(getItem());
	                cancelEdit();
	                event.consume();
	            } else if (event.getCode() == KeyCode.RIGHT) {
	                getTableView().getSelectionModel().selectRightCell();
	                event.consume();
	            } else if (event.getCode() == KeyCode.LEFT) {
	                getTableView().getSelectionModel().selectLeftCell();
	                event.consume();
	            } else if (event.getCode() == KeyCode.UP) {
	                getTableView().getSelectionModel().selectAboveCell();
	                event.consume();
	            } else if (event.getCode() == KeyCode.DOWN) {
	                getTableView().getSelectionModel().selectBelowCell();
	                event.consume();
	            }
	        });
	        textField.textProperty().addListener(new ChangeListener<String>() {
            	@Override
            	public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            		textField.setStyle(number(newValue) ? "-fx-text-fill: black" : "-fx-text-fill: red");
            	}
            });
	    }
	    
        private void finish(String s) {
        	if (number(s)) {
        		commitEdit(s);
        	} else {
        		cancelEdit();
        	}
        }
	    
	    // set the text of the text field and display the graphic
	    @Override
	    public void startEdit() {
	    	editedSaveStat = saves.get(getSelectedTabIndex());
	        super.startEdit();
	        textField.setText(getItem());
	        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
	        textField.requestFocus();   
	    }

	    // revert to text display
	    @Override
	    public void cancelEdit() {
	        super.cancelEdit();
	        setContentDisplay(ContentDisplay.TEXT_ONLY);
	    }

	    // commits the edit. Update property if possible and revert to text display
	    @Override
	    public void commitEdit(String item) {
	        
	        // This block is necessary to support commit on losing focus, because the baked-in mechanism
	        // sets our editing state to false before we can intercept the loss of focus.
	        // The default commitEdit(...) method simply bails if we are not editing...
	        if (! isEditing() && ! item.equals(getItem())) {
	            TableView<Var> table = getTableView();
	            if (table != null) {
	                TableColumn<Var, String> column = getTableColumn();
	                CellEditEvent<Var, String> event = new CellEditEvent<>(table, 
	                        new TablePosition<Var, String>(table, getIndex(), column), 
	                        TableColumn.editCommitEvent(), item);
	                Event.fireEvent(column, event);
	            }
	        }

	        super.commitEdit(item);
	        
	        setContentDisplay(ContentDisplay.TEXT_ONLY);
	    }

	}
    private static boolean number(String s) {
    	try {
			Integer.parseInt(s);
		} catch (Exception e) {
			return false;
		}
    	return true;
    }
    
    public void updateCurrentVarValues() {
    	updateVarValues();
    }
    
    private boolean matchesFilter(String varName) {
    	String shorter = filterText.getText();
    	/*
    	 * This would be the right place for stuff like this, it is hidden at every condition.
    	 * (But take care about the case-insensivity)
    	if (varName.startsWith("0")) {
    		return false;
    	}
    	*/
    	if (shorter.equals("")) {
    		return true;
    	} else {
    		String longer = varName;
    		if (longer.length() < shorter.length()) {
    			String swap = shorter;
    			shorter = longer;
    			longer = swap;
    		}
    		longer = longer.substring(0, shorter.length());
    		boolean basicMatch = longer.equalsIgnoreCase(shorter);
    		if (filterCheck.isSelected()) {
    			return !basicMatch;
    		} else {
    			return basicMatch;
    		}
    	}
    }
}
