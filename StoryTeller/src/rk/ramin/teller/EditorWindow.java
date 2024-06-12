package rk.ramin.teller;

import java.awt.Robot;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.imageio.ImageIO;

import com.sun.xml.internal.fastinfoset.sax.Properties;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Callback;
import javafx.stage.Stage;

public class EditorWindow extends Application {

	public static final double SPACING = 5;
	public static final double PAGE_DATA_HSPACING = 3;
	public static final double PAGE_DATA_VSPACING = 3;
	//public static final double PAGE_DATA_INDENTION = 5;
	private static final double BUTTON_SIZE = 26;
	
	private VBox editHolder;
	private VBox chapterHolder;
	private ComboBox<String> chapters;
	private RomanBox romans;
	private AnswersBox answers;
	private CustomAnswersBox customAnswers;
	//private CaseBar romans;
	//private AnswersBar answers;
	//private CustomAnswersBar customAnswers;
	
	private PageManager manager;
	private VarManager vars;
	private Story story;
	private Chapter currentChapter;
	private Scene scene;
	private Stage stage;
	
	private File fileLocation;
	
	private TextField pageName;
	private TextField pageShortcut;
	private ColorPicker pageColor;
	private Label pageNumber;
	
	private boolean pageLoaded = false;
	/** During maintenance no apperanceUpdates (text, shortcut and color) will require  */
	private boolean maintenance = false;
	
	private Label addPageHint;
	private int addPageHintIndex;
	
	private VBox pagePropertiesPane;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		vars = new VarManager(this);
		
		SplitPane split = new SplitPane();
		split.setOrientation(Orientation.HORIZONTAL);
		
		VBox everythingHolder = pagePropertiesPane = new VBox(SPACING);
		editHolder = new VBox(SPACING);
		StackPane stack = new StackPane(everythingHolder);
		StackPane.setMargin(everythingHolder, new Insets(SPACING));
		ScrollPane scroll1 = new ScrollPane(stack);
		
		scroll1.setFitToWidth(true);
		scroll1.setHbarPolicy(ScrollBarPolicy.NEVER);
		scroll1.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		chapters = new ComboBox<String>();
		chapterHolder = new VBox(SPACING);
		chapterHolder.getChildren().add(chapters);
		chapterHolder.getChildren().add(new Separator(Orientation.HORIZONTAL));
		
		addPageHint = new Label("Double click the middle area to add a new page. \nClick a page to edit it. \n");
		addPageHint.setWrapText(true);
		Font f = addPageHint.getFont();
		//addPageHint.setFont(Font.font(f.getFamily(), FontWeight.BOLD, f.getSize()));
		chapterHolder.getChildren().add(addPageHint);
		addPageHintIndex = chapterHolder.getChildren().size()-1;
		
		editHolder = new VBox(SPACING);
		pageShortcut = new LimitedTextField(2);
		pageShortcut.setPrefColumnCount(2);
		pageShortcut.setAlignment(Pos.CENTER);
		pageName = new TextField("screenDescr");
		pageName.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				romans.requestFocus();
			}
		});
		HBox apre = new HBox();
		apre.setAlignment(Pos.CENTER);
		pageNumber = new Label();
		pageNumber.setMinWidth(Control.USE_PREF_SIZE);
		pageNumber.getStyleClass().add("unimportantFeature");
		pageNumber.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				int oldNumber = manager.getSelectedPage().getPageNumber();
				PageNumberDialog d = new PageNumberDialog(manager.getSelectedPage().getPageNumber());
				Optional<Integer> result = d.showAndWait();
				if (result.isPresent()) {
					int newNumber = result.get();
					if (newNumber != oldNumber) {
						story.changePageNumber(oldNumber, newNumber);
						pageNumber.setText(newNumber+"");
					}
				}
			};
		});
		apre.getChildren().addAll(pageNumber, pageName, pageShortcut, pageColor = new SmallColorPicker());
		HBox.setMargin(pageNumber, new Insets(0,SPACING,0,0));
		HBox.setHgrow(pageShortcut, Priority.NEVER);
		HBox.setHgrow(pageNumber, Priority.NEVER);
		HBox.setHgrow(pageColor, Priority.NEVER);
		HBox.setHgrow(pageName, Priority.ALWAYS);
		editHolder.getChildren().add(apre);
		editHolder.getChildren().add(new Separator(Orientation.HORIZONTAL));
		
		/////////////////////////////////////////////////////////
		
		ConstructionData data = new ConstructionData(this);
		editHolder.getChildren().add(romans = new RomanBox(data));
		editHolder.getChildren().add(new Separator(Orientation.HORIZONTAL));
		editHolder.getChildren().add(answers = new AnswersBox(data));
		editHolder.getChildren().add(new Separator(Orientation.HORIZONTAL));
		editHolder.getChildren().add(customAnswers = new CustomAnswersBox(data));
		pageColor.setMaxWidth(Double.MAX_VALUE);
		ChangeListener<Object> listener = new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
				/* Of course this would also work and would be a little bit easier ...
				 * saveInPageAppearanceOnly(manager.getSelectedPage());
				 * But that would break the concept that the page manager ... manages its pages and has the control over when they will be saved etc.
				 */
				if (!maintenance) {
					manager.saveAppearanceOnly();
				}
			}
		};
		pageShortcut.textProperty().addListener(listener);
		pageName.textProperty().addListener(listener);
		pageColor.valueProperty().addListener(listener);
		/*
		//editHolder.getChildren().add(romans = new CaseBar());
		editHolder.getChildren().add(new Separator(Orientation.HORIZONTAL));
		editHolder.getChildren().add(new Label("Edit your answers:"));
		//customAnswers = new CustomAnswersBar(this);
		//editHolder.getChildren().add(answers = new AnswersBar(this, customAnswers));
		editHolder.getChildren().add(new Separator(Orientation.HORIZONTAL));
		//editHolder.getChildren().add(customAnswers);
		*/
		everythingHolder.getChildren().addAll(chapterHolder, editHolder);
		//editHolder.setVgrow(customAnswers, Priority.ALWAYS);
		
		//ScrollPane scroll2 = new ScrollPane();
		manager = new PageManager(this);
		unloadPage();
		
		split.getItems().addAll(scroll1, manager, vars.getGui());
		split.setDividerPositions(0.31,0.70);
		
		//MenuItem mt = new MenuItem("TEMP GO ON");
		MenuItem mn = new MenuItem("New");
		mn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				clearEditor();
			}
		});
		MenuItem mo = new MenuItem("Open ...");
		mo.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				File f = IOHelper.chooseFileToOpen(stage);
				if (f != null) {
					openStory(f);
				}
			}
		});
		MenuItem ms = new MenuItem("Save");
		ms.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (prepareSaving()) {
					if (fileLocation == null) {
						saveStoryAs();
					} else {
						saveStoryAt(fileLocation);
					}
				}
			}
		});
		MenuItem msa = new MenuItem("Save as ...");
		msa.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (prepareSaving()) {
					saveStoryAs();
				}
			}
		});
		
		MenuItem stStart = new MenuItem("Set selected page as entrance page");
		stStart.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Page p = manager.getSelectedPage();
				if (p == null) {
					System.out.println("There is no selected page, the entrance page was not changed.");
				} else {
					story.getSaveStats().get(0).setInitPage(p);
					for (SaveStat s : story.getSaveStats()) {
						if (s.getInitPage() == null) {
							s.setInitPage(p);
						}
						if (s.getCurrentPage() == null) {
							s.setCurrentPage(p);
						}
					}
					System.out.println("Entrance page changed to "+manager.getSelectedPage());
					vars.updatePages();
				}
			}
		});
		
		MenuItem plTest = new MenuItem("Play from start");
		plTest.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				PlayProject(story.getSaveStats().get(0));
			}
		});
		
		Menu m1 = new Menu("File");
		m1.getItems().addAll(mn,mo,ms,msa);
		Menu m2 = new Menu("Story");
		m2.getItems().addAll(stStart);
		Menu m3 = new Menu("Chapter");
		Menu m4 = new Menu("Play");
		m4.getItems().addAll(plTest);
		Menu m5 = new Menu("About");
		MenuBar mb = new MenuBar(m1, m2, m3, m4, m5);
		
		BorderPane root = new BorderPane();
		root.setTop(mb);
		root.setCenter(split);
		
		Scene sc = new Scene(root);
		sc.getStylesheets().add(getClass().getResource("css.xml").toExternalForm());
		scene = sc;
		stage = primaryStage;
		stage.setTitle("iGuru's Story Teller");
		stage.getIcons().add(new Image("/graphics/icon.png"));
		primaryStage.setScene(sc);
		primaryStage.setWidth(960);
		primaryStage.setHeight(600);
		primaryStage.show();
		clearEditor();
		
		try {
			loadStory(IOHelper.loadStory(IOHelper.DATA_PATH), IOHelper.DATA_PATH);
			fileLocation = IOHelper.DATA_PATH;
		} catch (Exception e) {
			System.out.println("Did not load default file");
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Go");
				temp = new TEMP();
				//temp.go();
			}
		}).start();
	}
	private TEMP temp;
	
	public void loadPage(Page p) {
		maintenance = true;
		if (!pageLoaded) {
			pageLoaded = true;
			chapterHolder.getChildren().remove(addPageHint);
		}
		editHolder.setDisable(false);
		pageName.setText(p.getName());
		pageShortcut.setText(p.getShortcut());
		pageNumber.setText(p.getPageNumber() + "");
		pageColor.setValue(p.getColor());
		Object[] content = p.getContent();
		romans.load(content[0]);
		answers.load(content[1]);
		customAnswers.load(content[2]);
		maintenance = false;
	}
	
	public void saveInPage(Page p) {
		if (!pageLoaded) {
			throw new RuntimeException("Thats what I call organization: You want me to save your page - but there is actually no one loaded ...");
		}
		saveInPageAppearanceOnly(p);
		Object[] content = new Object[] {
			romans.save(),
			answers.save(),
			customAnswers.save()
		};
		p.setContent(content);
	}
	
	public void saveInPageAppearanceOnly(Page p) {
		p.setName(pageName.getText());
		p.setShortcut(pageShortcut.getText());
		p.setColor(pageColor.getValue());
		p.getWrapper().updateAppearance();
	}
	
	public void unloadPage() {
		maintenance = true;
		if (pageLoaded) {
			pageLoaded = false;
			chapterHolder.getChildren().add(addPageHintIndex, addPageHint);
		}
		pageName.setText("");
		pageShortcut.setText("");
		pageColor.setValue(Color.WHITE);
		pageNumber.setText("");
		editHolder.setDisable(true);
		romans.load(PageContentHelper.oneInstanceOfADefaultPage[0]);
		answers.load(PageContentHelper.oneInstanceOfADefaultPage[1]);
		customAnswers.load(PageContentHelper.oneInstanceOfADefaultPage[2]);
	}
	
	public void requestLinking(ElementLink l) {
		manager.beginLinking(l);
	}
	
	public void updateArrows() {
		if (!maintenance) {
			manager.redrawArrows();
		}
	}
	
	public static Button getSquareButton(String text) {
		Button b = new Button(text);
		b.setTextOverrun(OverrunStyle.CLIP);
		b.setTextAlignment(TextAlignment.CENTER);
		b.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
		b.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
		b.setFocusTraversable(false);
		return b;
	}
	
	public static Font getOtherFontType(Font before,  FontWeight fw, FontPosture fp) {
		return Font.font(before.getFamily(), fw, fp, before.getSize());
	}
	
	public void highlightArrow(Page id) {
		manager.highlightArrow(id);
	}
	
	/**
	 * Updates the selected chapter (currentChapter) in the story, keeps it loaded
	 * All pages still have references to their wrappers.
	 */
	public void saveChapter() {
		manager.unload();
		currentChapter.setPages(manager.getAllPages());
		//Editing a chapter in the manager just means that we edit a copy. At this point we save all of this pages to the chapter again so it is now up to date.
	}
	
	/**
	 * You should do this before you load another page,
	 * but you must not call this on the page that is currently edited.
	 * 
	 * TODO: This method is never called anywhere ... ??
	 */
	public void clearWrapperReferences(Chapter c) {
		if (c == currentChapter) {
			throw new RuntimeException("Attempted to clear all wrapper references in the currently loaded page "+currentChapter);
		}
		for (Page p : c.getPages()) {
			p.setWrapper(null);
		}
	}
	
	public void clearEditor() {
		fileLocation = null;
		story = new Story();
		currentChapter = story.getChapterList().get(0);
		manager.loadChapter(null);
		loadVars();
	}
	
	public void deletePage(Page p) {
		story.deletePage(p);
		vars.updatePages();
	}
	
	public TextField getPageNameTextField() {
		return pageName;
	}
	
	public void PlayProject(SaveStat s) {
		saveChapter();
		stage.hide();
		MainWindow.initiateTest(story, s, this); //In future you might also give sets of vars and (NO launch arguments as well) these vars also save, where to continue the game.
	}
	
	public VarManager getVarManager() {
		return vars;
	}
	
	public Page getSelectedPage() {
		return manager.getSelectedPage();
	}
	
	private void loadStory(Story s, File f) {
		story = s;
		fileLocation = f;
		manager.loadChapter(story.getChapterList().get(0));
		loadVars();
		currentChapter = story.getChapterList().get(0);
		System.out.println("Successfully loaded a story. ");
	}
	
	/**
	 * Has to be called before we save the story to a file. 
	 * Prepares the story so that it is afterwards ready to be saved. 
	 * (E.g. saves the chapter to the story, updates vars, ...)
	 * It this method returns false, the story is empty and there is no sense to save the file. 
	 */
	private boolean prepareSaving() {
		saveChapter();
		//saveVars();
		boolean e = story.isEmpty();
		if (e) {
			System.out.println("Didnt save story because its empty. ");
		}
		return !e;
	}
	
	private void saveStoryAs() {
		File f = IOHelper.chooseFileToSave(stage);
		if (f == null) {
			System.out.println("Didnt save story because you didn't choose a file location. ");
			return;
		}
		fileLocation = f;
		saveStoryAt(f);
	}
	
	private void saveStoryAt(File f) {
		try {
			IOHelper.saveStory(story, f);
			fileLocation = f;
			System.out.println("File successfully saved");
		} catch (IOException e) {
			e.printStackTrace();
			IOHelper.showErrorAndWait(e, "Could not save file: ");
		}
	}
	
	private void openStory(File f) {
		try {
			loadStory(IOHelper.loadStory(f), f);
			fileLocation = f;
		} catch (Exception e) {
			e.printStackTrace();
			IOHelper.showErrorAndWait(e, "Could not load story file:");
		}
	}
	
	/*
	private void saveVars() {
		story.setVars(vars.saveVarsToArray());
	}*/
	/**
	 * Updates the varManager so it fits to the vars inside the (new) story
	 */
	private void loadVars() {
		//vars.loadVars(story.getVars());
		vars.setSaveStats(story.getSaveStats());
	}
	
	public void onPlayingEnded() {
		stage.show();
		vars.updateCurrentVarValues();
		vars.updatePages();
	}

	public void addPage(Page page) {
		story.addPage(page);
	}
	
	public class PageNumberDialog extends Dialog<Integer> {
		
		private int defVal; 
		private TextField text;
		private Label label;
		
		public PageNumberDialog(int defaultValue) {
			defVal = defaultValue;
			DialogPane p = getDialogPane();
			p.getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
			setResultConverter(new Callback<ButtonType, Integer>() {
				@Override
				public Integer call(ButtonType type) {
					if (type == ButtonType.CANCEL) {
						return defVal;
					}
					return parseString(text.getText());
				}
			});
			setTitle("Change page number");
			text = new TextField(defaultValue + "");
			BorderPane pane = new BorderPane();
			pane.setTop(new Label("Here you may change the number of this page."));
			pane.setCenter(label = new Label("\n\n\n\n"));
			label.setTextFill(Color.DARKGRAY);
			label.setFont(getOtherFontType(label.getFont(), FontWeight.NORMAL, FontPosture.ITALIC));
			label.setWrapText(true);
			BorderPane.setMargin(label, new Insets(10));
			BorderPane.setAlignment(label, Pos.CENTER_LEFT);
			pane.setBottom(text);
			p.setContent(pane);
			text.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					try {
						int i = parseString(newValue);
						updateMessage(i);
					} catch (NumberFormatException e) {
						text.setText(oldValue);
					}
				}
			});
			//updateMessage(defaultValue);
		}
		
		private void updateMessage(int number) {
			Page p = story.getPageWithNumber(number);
			String message;
			if (p == null) {
				Compiler.setTextFieldColor(text, Compiler.COLOR_OK);
				message = "This number is not assigned to any page yet.";
			} else if (p == manager.getSelectedPage()) {
				Compiler.setTextFieldColor(text, Compiler.COLOR_OK);
				message = "This is the pages current number (so you won't change anything by now).";
			} else {
				Compiler.setTextFieldColor(text, Compiler.COLOR_WARNING);
				String name = p.getName();
				if (name != null && !name.equals("")) {
					message = "This number is already assigned to a page named "+name+".";									
				} else {
					message = "This number is already assigned to a page.";
				}
				message += "\nIf you assign this number, this swaps both page numbers.";
			}
			label.setText(message);
		}
		
		private int parseString(String text) {
			if (text.equals("") || text.equals("-")) {
				return 0;
			} else {
				return Integer.parseInt(text);
			}
		}
	}
}
