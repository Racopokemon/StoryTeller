package rk.ramin.teller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
public class MainWindow extends Application {
	
	public static final double BOX_WIDTH = 700;
	public static final double BOX_HEIGHT = 600;
	public static final double SCREEN_WIDTH = BOX_WIDTH+100;
	public static final double SCREEN_HEIGHT = BOX_HEIGHT;
	public static final double LINE_POS = BOX_HEIGHT*0.33;
	public static final double INDENTION = 50;
	public static final double TEXT_WIDTH = BOX_WIDTH-INDENTION*2;
	public static final double SPACING = 8;
	
	private static final double ARROW_SIZE = 5;
	private static final double NEXT_ARROW_LENGTH = 30;
	private static final double SELECTION_INDENT = 24;
	private Insets INDENT = new Insets(0, INDENTION, 0, INDENTION);
	private static final Duration STEP_TIME = Duration.seconds(0.1);
	private static final Interpolator STEP_INTERP = new ExpInterpolator(3, 1.5, false);
	private static final Interpolator CONFIRM_INTERP = new JumpInterpolator(2, 0.5);
	private static final Interpolator FADE_INTERP = new ExpInterpolator(1.2, 10, true);
	private static final Interpolator IN_INTERP = new ExpInterpolator(3,2,false);
	private static final Duration CONFIRM_TIME = Duration.seconds(0.27);
	private static final Duration FADE_TIME = Duration.seconds(0.6);
	private static final Duration FADEOUT_TIME = Duration.seconds(1.2);
	private static final Duration FADEIN_TIME = Duration.seconds(1.2);
	
	private static final Duration LOADING_ARROW_LIFETIME = Duration.seconds(0.5);
	private static final Duration LOADING_ARROW_SPAWN_OFFSET = Duration.seconds(0.1);
	private static final int LOADING_ARROW_COUNT = 12;
	
	private Color backColor = Color.BLACK;
	private Color foreColor = Color.WHITE;
	
	private Font font = Font.font(24);
	private int currentFontSize = 24;
	
	private String[] newInput;
	private byte selection;
	private boolean awaitsInput = false;
	private boolean justArrow = false;
	
	private Text title;
	private Line divider;
	private VBox answersHolder;
	private TextField customAnswer;
	private Pane customAnswerHolder;
	private Group nextArrow;
	private Pane gamePane;
	
	private ArrayList<Shape> allShapesWithForeColor;
	
	private Pane backgroundPane;
	
	private Polyline[] loadingArrows;
	private Group allLoadingArrows;
	
	private Scene scene;
	private Stage stage;
	private MainWindow instance = this;
	
	private StoryManager manager;
	private AnimationFinisher finisherInputAnswer = new AnimationFinisher(true, false, false, true);
	private AnimationFinisher finisherInputCustom = new AnimationFinisher(true, false, true, false);
	private AnimationFinisher finisherInputCustomClear = new AnimationFinisher(true, true, true, false);
	private AnimationFinisher finisherInputAnswerClear = new AnimationFinisher(true, true, false, true);
	private ConfirmContinuer confirmContinuer = new ConfirmContinuer();
	//private AnimationFinisher finisherFocus = new AnimationFinisher(true, true);
	private LoadingHandler loadingHandler = new LoadingHandler();
	
	private Timeline ani = new Timeline(60);

	private static Story storyToLoad;
	private static SaveStat storySaveStat;
	private static EditorWindow owner;
	private boolean testing;
	
	private EditorWindow parent;
	private File dataFile, saveFile;
	
	private ParticleManager particles;
	
	@Override
	public void start(Stage st) throws Exception {
		//font = Font.loadFont(Files.newInputStream(new File("C:\\Users\\Der Guru\\Desktop\\Asul-Regular.ttf").toPath()), 24);
		
		/*
		 * Ich hätte eigentlich gern, dass das Programm erst lädt, wenn das Fenster schon sichtbar ist.
		 * (Dann kann ich meine tolle Ladeanimation zeigen)
		 * Dafür muss sich hier aber noch einiges ändern.
		 * 
		 * Die Anwendung weiß, ob sie nur am Testen ist (testing == true) oder nicht.
		 * Sie soll bei Ausführung ohne Startargumente zuerst im gleichen Verzeichnis die Datei data.ser suchen. Gibt es die, 
		 * öffnet sich das Fenster und lädt die Datei im Hintergrund. Gibt es einen Fehler, wird der nun als Nachricht angezeigt. 
		 * Existiert die Datei nicht, soll ein Fenster angezeigt werden, wo man die Datei auswählen kann. Bricht man ab, quittet das
		 * Programm direkt. Wählt man eine Datei aus, so wird sie geladen zum Ladebildschirm, tritt ein Fehler auf, wird dieser wieder
		 * als Nachricht ausgegeben. 
		 * In all diesen Fällen weiß das Programm, dass test == false ist. Es wird versuchen zu speichern, und keinen Editor anzeigen. 
		 * 
		 * Mit Argumenten öffnet sich der Editor. 
		 */
		testing = storyToLoad != null;
		
		if (testing) {
			parent = owner;
			manager = new StoryManager(storyToLoad, storySaveStat); 
			storyToLoad = null;
			owner = null;
		} else {
			dataFile = IOHelper.DATA_PATH;
			if (dataFile.exists()) {
				saveFile = IOHelper.SAVE_PATH;
			} else {
				dataFile = IOHelper.chooseFileToOpen(st);
				if (dataFile != null) {
					saveFile = IOHelper.chooseSaveFileToOpen(st);
				}
			}
			if (dataFile == null) {
				//System.out.println("Canceled the game: There was no file selected");
				System.exit(0);
			}
			Runnable loadTask = new Runnable() {
				@Override
				public void run() {
					/*
					try {
						System.out.println("Remove the Thread.sleep 2000 here, i just wanted to enjoy my beautiful loading animation");
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					*/
					Story s = null;
					SaveStat ss = null;
					try {
						s = IOHelper.loadStory(dataFile);
					} catch (Exception e) {
						s = new Story(); //There is already a chapter in it
						Page p = PageContentHelper.getGeneratedPageLinkingTo("Could not load the story. \n"+e.getClass().getName() + "\n" + e.getMessage(), null);
						ss = new SaveStat("Error save stat to give you the error page");
						ss.setCurrentPage(p);
					}
					if (ss == null) {
						if (saveFile != null && saveFile.exists()) {
							try {
								ss = IOHelper.loadSaveStat(saveFile);
							} catch (Exception e) {
								e.printStackTrace();
								ss = s.getSaveStats().get(0);
								ss.reset(false);
								if (ss.getCurrentPage() == null) ss.setCurrentPage(s.getAnyPage()); 
								Page p1 = PageContentHelper.getGeneratedPageLinkingTo("You will now start a new game. \n(If you don't wish this, exit now and try to fix the file)", ss.getCurrentPage()),
										p2 = PageContentHelper.getGeneratedPageLinkingTo("Could not load the save file. \n"+e.getClass().getName() + "\n" + e.getMessage(), p1);
								ss.setCurrentPage(p2);
							}
						} else {
							ss = s.getSaveStats().get(0);
							ss.reset(false);
						}
					}
					manager = new StoryManager(s, ss);
				}
			};
			new Thread(loadTask).start();
		}
		
		st.setOnHidden(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent event) {
				onHidden();
			};
		});
	
		VBox p = new VBox(SPACING*2);
		p.setMaxSize(BOX_WIDTH, BOX_HEIGHT);
		p.setMinSize(BOX_WIDTH, BOX_HEIGHT);
		p.setAlignment(Pos.TOP_CENTER);
		//p.setBackground(new Background(new BackgroundFill(Color.gray(0.2), null, null)));
		
		title = getText("");
		title.setOpacity(0);
		//txt.setTextAlignment(TextAlignment.LEFT);
		//txt.setTextOrigin(VPos.BASELINE);
		
		StackPane sp = new StackPane(title);
		//sp.setBackground(new Background(new BackgroundFill(Color.BLUE,null,null)));
		sp.setPrefHeight(LINE_POS);
		sp.setAlignment(Pos.BOTTOM_LEFT);
		
		p.getChildren().add(sp);
		VBox.setMargin(sp, INDENT);
		
		divider = new Line(3, LINE_POS, BOX_WIDTH-3, LINE_POS);
		applyStyleOnShape(divider);
		divider.setOpacity(0);
		
		p.getChildren().add(divider);
		
		answersHolder = new VBox(SPACING);
		answersHolder.setOpacity(0);
		//answersHolder.setFocusTraversable(true);
		/*
		//answ.setBackground(new Background(new BackgroundFill(Color.GRAY,null,null)));
		answ.getChildren().add(getText("10 Gebote annehmen"));
		//answ.getChildren().add(getText("Picknicken. Lalala. Ich brauch ne neue Zeile! Gebt sie mir!! Aber schnell!"));
		answ.getChildren().add(getText("Picknicken."));
		answ.getChildren().add(getText("Herunterblicken"));
		//answ.getChildren().add(getText("Boah ist das nen Scheißspiel. Ich geh jetzt Dark Souls spielen!"));
		 */
		allShapesWithForeColor = new ArrayList<>();
		allShapesWithForeColor.add(divider);
		
		
		KeyManager km = new KeyManager();
		customAnswer = new TextField();
		customAnswer.setBackground(null);
		customAnswer.getStyleClass().clear();
		updateCustomAnswerStyle();
		customAnswer.setFocusTraversable(false); //Not accessible with any keyboard shortcuts like the arrow keys or tab
		customAnswer.setOnKeyPressed(km);
		customAnswer.setOnMousePressed(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				if (selection != answersHolder.getChildren().size()) {
					answersHolder.requestFocus();
					//And this is the final step to make the textField completely "invisible" ... (hopefully completely)
				}
			}
		});
		
		Polyline arrow = getArrow(0);
		allShapesWithForeColor.add(arrow);
		
		nextArrow = new Group();
		
		for (int i = 0; i < 3; i++) {
			double offs = i*ARROW_SIZE*2.2;
			Polyline a = getArrow(offs);
			nextArrow.getChildren().add(a);
			allShapesWithForeColor.add(a);
		}
		/*double ofs = 500;
		Polyline a = new Polyline(ofs+NEXT_ARROW_LENGTH,0,ofs+ARROW_SIZE+NEXT_ARROW_LENGTH,ARROW_SIZE,ofs+NEXT_ARROW_LENGTH,ARROW_SIZE*2);
		Line b = new Line(ofs,ARROW_SIZE,NEXT_ARROW_LENGTH+ARROW_SIZE+ofs,ARROW_SIZE);
		applyStyleOnShape(a);
		applyStyleOnShape(b);
		nextArrow.getChildren().addAll(a,b);*/
		
		StackPane fieldHolder = new StackPane(); //Tricking around a bit here to keep the rest simple.
		fieldHolder.setAlignment(Pos.CENTER_LEFT);
		fieldHolder.getChildren().add(arrow);
		arrow.setTranslateX(-ARROW_SIZE-SELECTION_INDENT); //Easiest way to move the arrow to the left without interfering with the rest
		fieldHolder.getChildren().add(customAnswer);
		customAnswerHolder = fieldHolder;
		customAnswerHolder.setOpacity(0);
		
		VBox answerAndTextField = new VBox(SPACING); 
		answerAndTextField.getChildren().add(answersHolder);
		answerAndTextField.getChildren().add(fieldHolder);
		
		p.getChildren().add(answerAndTextField);
		VBox.setMargin(answerAndTextField, INDENT);
		VBox.setVgrow(p, Priority.ALWAYS);
		
		gamePane = p;
		
		//-------------------------------- LOADING ANIMATION -------------------------------------------------------------- 
		
		Group g = new Group();
		loadingArrows = new Polyline[LOADING_ARROW_COUNT];
		for (int i = 0; i < LOADING_ARROW_COUNT; i++) {
			loadingArrows[i] = getArrow(i*ARROW_SIZE*2.5);
			loadingArrows[i].setOpacity(0.0);
			g.getChildren().add(loadingArrows[i]);
		}
		allLoadingArrows = g;
		
		//-----------
		
		backgroundPane = new StackPane(g);
		backgroundPane.setBackground(new Background(new BackgroundFill(backColor, null, null)));
		
		answersHolder.setOnKeyPressed(km);
		
		Scene sc = new Scene(backgroundPane, SCREEN_WIDTH,SCREEN_HEIGHT);
		scene = sc;
		
		stage = st;
		st.setMinWidth(SCREEN_WIDTH);
		st.setMinHeight(SCREEN_HEIGHT);
		st.setScene(sc);
		st.show();
		st.setFullScreenExitHint("");
		st.setFullScreen(!testing);
		
		ani.getKeyFrames().clear();
		ani.getKeyFrames().add(new KeyFrame(testing ? Duration.seconds(0.05) : FADE_TIME, loadingHandler));
		ani.playFromStart();
		
		//loadNextPage();
	}
	
	/**
	 * Call this before you load the next page.
	 * It updates the font [TODO] and foreColor to match the values within the saveStat.  
	 * It then updates all colors and fonts to match those new values.
	 */
	private void updateAllStyles() {
		SaveStat ss = manager.getSave();
		int newFontSize = ss.getCurrent("_textSize");
		if (newFontSize != currentFontSize) {
			currentFontSize = newFontSize;
			font = Font.font(newFontSize); //Needs a rework as soon as we are able to actually change the font size
		}
		
		Color c = SaveStat.intToColor(ss.getCurrent("_textColor"));
		foreColor = c;
		divider.setStroke(c);
		title.setFill(c);
		title.setFont(font);
		updateCustomAnswerStyle();
		for (Shape s : allShapesWithForeColor) {
			s.setStroke(c);
		}
	}
	
	/**
	 * Updates font and color within the customAnswer text field to match the current font and foreColor variable.
	 */
	private void updateCustomAnswerStyle() {
		String colorName = (int)Math.round(foreColor.getRed()*255)+","+(int)Math.round(foreColor.getGreen()*255)+","+(int)Math.round(foreColor.getBlue()*255);
		customAnswer.setStyle("-fx-text-fill: rgb(" + colorName + "); -fx-display-caret: false; -fx-highlight-fill: rgba(" + colorName + ", 0.18); -fx-highlight-text-fill: rgb(" + colorName + ");"); //no special cursor, no color
		customAnswer.setFont(font);
	}

	private void showLoadingAnimation() {
		double half = (LOADING_ARROW_COUNT-1.0)*0.5;
		if (ani.getKeyFrames().size() == 1) {
			ani.getKeyFrames().clear();
			for (int i = 0; i < LOADING_ARROW_COUNT; i++) {
				Polyline p = loadingArrows[i];
				KeyValue k = new KeyValue(p.opacityProperty(), 0.0);
				Duration a = LOADING_ARROW_SPAWN_OFFSET.multiply(i), b = a.add(LOADING_ARROW_LIFETIME), 
						c = b.add(LOADING_ARROW_LIFETIME);
				
				ani.getKeyFrames().add(new KeyFrame(a, k));
				ani.getKeyFrames().add(new KeyFrame(b, new KeyValue(p.opacityProperty(), interpolate(Math.min(Math.abs(i-half)/half, 1)))));
				ani.getKeyFrames().add(new KeyFrame(c, k));
			}
			ani.getKeyFrames().add(new KeyFrame(Duration.ZERO, new KeyValue(allLoadingArrows.translateXProperty(), ARROW_SIZE*9)));
			ani.getKeyFrames().add(new KeyFrame(LOADING_ARROW_SPAWN_OFFSET.multiply(LOADING_ARROW_COUNT).add(LOADING_ARROW_LIFETIME.multiply(2)), 
					new KeyValue(allLoadingArrows.translateXProperty(), ARROW_SIZE*-9)));
			ani.setOnFinished(loadingHandler);
		}
		
		ani.playFromStart();
	}
	
	/**
	 * Called after the loading animation has ended, we have a story and a storyManager, and its time to start the actual game!
	 */
	private void initGameLoop() {
		ObservableList<Node> l = ((Pane)scene.getRoot()).getChildren();
		loadingArrows = null;
		allLoadingArrows = null;
		l.clear();
		l.add((particles = new ParticleManager(SCREEN_WIDTH, SCREEN_HEIGHT, backgroundPane, backColor)).getContent());
		l.add(gamePane);
		newInput = manager.launch();
		particles.updateValues(manager.getSave());
		updateAllStyles();
		loadNextPage();
	}
	
	private double interpolate(double v) {
		double f = 0.1;
		v = v*f*2;
		v += 1-f;
		v = 1/v;
		v -= 1/(1+f);
		return v/(1/(1-f)-1/(1+f));
	}
	
	private void onHidden() {
		particles.destroy();
		if (testing) {
			manager.saveTesting();
			parent.onPlayingEnded();
		} else {
			if (manager != null) { //Else anything went wrong and not even the manager was initalized
				SaveStat s = manager.savePlaying();
				if (s != null) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							if (saveFile == null) {
								saveFile = IOHelper.chooseSaveFileToSave(stage);
							}
							if (saveFile != null) {
								try {
									IOHelper.saveSaveStat(s, saveFile);
								} catch (Exception e) {
									IOHelper.showErrorAndWait(e, "Error while saving save-file. ");
									
								}
							}
						}
					});
					/*if (saveFile == null) { //Weird problems AFTER the file chooser was already closed ... well ...
						saveFile = IOHelper.chooseSaveFileToSave(stage);
					}
					if (saveFile != null) {
						try {
							System.out.println("Saving save file");
							IOHelper.saveSaveStat(s, saveFile);
							System.out.println("Success saving");
						} catch (Exception e) {
							System.out.println("Error during saving");
							IOHelper.showErrorAndWait(e, "Error while saving save-file. ");
							
						}
					}*/
				}
			}
		}
		//System.out.println("End.");
	}

	/**
	 * Called after the confirmAnswer has finished playing.
	 * Everything gets reseted, the nextPage will be read out and prepared for the starting animation. 
	 */
	private void loadNextPage() {
		
		updateAllStyles();
		
		title.setOpacity(1);
		divider.setOpacity(1);
		customAnswerHolder.setOpacity(0);
		customAnswerHolder.setTranslateY(0);
		customAnswer.setText("");
		answersHolder.setOpacity(1);
		answersHolder.getChildren().clear();
		nextArrow.setOpacity(1);
		
		//Answers has to be empty at this point (and the AnswerHolder as well)
		//awaitsInput should be false here, focus on the answerHolder
		selection = 0;
		title.setText(newInput[0]);
		boolean indent = true;
		for (int i = 1; i < newInput.length; i++) {
			Text a = getText(newInput[i]);
			if (indent) {
				indent = false;
				a.setTranslateX(SELECTION_INDENT);
			}
			answersHolder.getChildren().add(a);
		}
		justArrow = answersHolder.getChildren().isEmpty();
		if (justArrow) {
			answersHolder.getChildren().add(nextArrow);
			nextArrow.setTranslateY(0);
			//Polyline a = new Polyline(0,0,ARROW_SIZE,ARROW_SIZE,0,ARROW_SIZE*2);
			//applyStyleOnShape(a);
			//answersHolder.getChildren().add(a);
			nextArrow.setTranslateX(SELECTION_INDENT);
			//AND THE LAST STEP: REMOVE THE CUSTOM ANSWER BAR!
		}
		customAnswerHolder.setOpacity(0);
		
		//---------------------------------------------------------------------------------
		//Animation starts here.
		
		ani.getKeyFrames().clear();
		
		divider.setStartX(30);
		divider.setEndX(BOX_WIDTH-30);
		divider.setOpacity(0);
		title.setOpacity(0);
		//TEMP
		answersHolder.setOpacity(0);
		
		KeyValue k1 = new KeyValue(divider.startXProperty(), 3, IN_INTERP);
		KeyValue k2 = new KeyValue(divider.endXProperty(), BOX_WIDTH-3);
		KeyValue k3 = new KeyValue(divider.opacityProperty(), 1);
		KeyFrame kf1 = new KeyFrame(FADEIN_TIME, k1, k2, k3);
		
		KeyValue k4 = new KeyValue(title.translateYProperty(), 25);
		KeyValue k5 = new KeyValue(title.opacityProperty(), 0);
		KeyFrame kf2 = new KeyFrame(Duration.seconds(0.2), k4, k5);
		
		KeyValue k6 = new KeyValue(title.translateYProperty(), 0, IN_INTERP);
		KeyValue k7 = new KeyValue(title.opacityProperty(), 1);
		KeyFrame kf3 = new KeyFrame(FADEIN_TIME, k6, k7);
		
		KeyValue k8 = new KeyValue(answersHolder.opacityProperty(), 0);
		KeyValue k9 = new KeyValue(answersHolder.translateYProperty(), -15);
		KeyFrame kf4 = new KeyFrame(Duration.seconds(0.4), k8, k9);
		
		KeyValue k10 = new KeyValue(answersHolder.opacityProperty(), 1);
		KeyValue k11 = new KeyValue(answersHolder.translateYProperty(), 0, IN_INTERP);
		KeyFrame kf5 = new KeyFrame(FADEIN_TIME, k10, k11);
		/*
		for (Node n : answersHolder.getChildren()) {
			n.setOpacity(0);
		}
		*/
		
		ani.getKeyFrames().addAll(kf1, kf2, kf3, kf4, kf5);
		ani.setOnFinished(finisherInputAnswer);
		ani.playFromStart();
		
		//And finally: awaitsInput = true;
		//After animation:
		//awaitsInput = true; //TEMP
		//customAnswer.setDisable(false);
		//divider.setStartX(3);			//TEMP
		//divider.setEndX(BOX_WIDTH-3);	//TEMP
		//answersHolder.requestFocus();
	}

	private void confirmAnimation(boolean goOn) {
		boolean isCustom = selection == answersHolder.getChildren().size();
		Node n = isCustom ? customAnswerHolder : answersHolder.getChildren().get(selection);
		
		if (isCustom) {
			customAnswer.setDisable(true);
		}
		
		ani.getKeyFrames().clear();
		
		KeyValue kv0 = new KeyValue(n.translateXProperty(), SELECTION_INDENT*1.5, CONFIRM_INTERP);
		KeyFrame kf0 = new KeyFrame(CONFIRM_TIME,kv0);
		ani.getKeyFrames().add(kf0);
		
		if (goOn) {
			KeyValue kv1a = new KeyValue(n.translateYProperty(), 0);
			KeyValue kv1b = new KeyValue(n.opacityProperty(), 1);
			KeyFrame kf1 = new KeyFrame(FADE_TIME, kv1a, kv1b);
			
			KeyValue kv2a = new KeyValue(n.translateYProperty(), -15, FADE_INTERP);
			KeyValue kv2b = new KeyValue(n.opacityProperty(), 0);
			KeyFrame kf2 = new KeyFrame(FADEOUT_TIME, kv2a, kv2b);
			
			ani.getKeyFrames().addAll(kf1, kf2);
			
		} else if (isCustom) {
			
			KeyValue kv9 = new KeyValue(customAnswer.opacityProperty(),0);
			KeyFrame kf9 = new KeyFrame(CONFIRM_TIME, kv9);
			
			ani.getKeyFrames().add(kf9);
		}
		/*
		KeyValue kv0 = new KeyValue(n.opacityProperty(), 1);
		KeyValue kv1 = new KeyValue(n.translateXProperty(), SELECTION_INDENT*1.5, Interpolator.EASE_OUT);
		KeyValue kv2 = new KeyValue(n.opacityProperty(), 0);
		KeyFrame kf1 = new KeyFrame(CONFIRM_TIME.multiply(2),kv0);
		KeyFrame kf2 = new KeyFrame(CONFIRM_TIME.multiply(3),kv1,kv2);
		ani.getKeyFrames().addAll(kf1, kf2);*/
		
		if (goOn) {
			KeyValue[] kvs;
			byte index;
			if (isCustom) {
				kvs = new KeyValue[3];
				kvs[0] = new KeyValue(answersHolder.opacityProperty(),0);
				index = 1;
			} else {
				kvs = new KeyValue[answersHolder.getChildren().size()+2];
				index = 0;
				boolean skip = true;
				for (Node t : answersHolder.getChildren()) {
					if (skip && index == selection) {
						skip = false;
					} else {
						kvs[index++] = new KeyValue(t.opacityProperty(), 0);
					}
				}
				kvs[index++] = new KeyValue(customAnswerHolder.opacityProperty(), 0);
			}
			
			kvs[index++] = new KeyValue(title.opacityProperty(), 0);
			kvs[index] = new KeyValue(divider.opacityProperty(), 0);
			
			KeyFrame kf3 = new KeyFrame(CONFIRM_TIME, kvs);
			ani.getKeyFrames().add(kf3);
			//ani.getKeyFrames().add(new KeyFrame(CONFIRM_TIME.multiply(6)));
			//If we have to wait, we may add simply a keyframe here after some seconds.
			ani.setOnFinished(confirmContinuer);
		} else {
			if (isCustom) {
				ani.setOnFinished(finisherInputCustomClear);
			} else {
				ani.setOnFinished(finisherInputAnswer);
			}
		}
		
		ani.playFromStart();
		awaitsInput = false;
		//AnswerHolders opacity and customAnswerHolders opacity need to be reseted after this
		//before the whole animation thing we need to call sendAnswer()
	}
	
	private void enter() {
		if (selection == answersHolder.getChildren().size()) { //textField is selected
			newInput = manager.type(customAnswer.getText());
		} else {
			newInput = manager.choice(selection);
		}
		particles.updateValues(manager.getSave());
		if (newInput == null) {
			confirmAnimation(false);
		} else {
			confirmAnimation(true); //Continues with a endThisPage() call
		}
	}
	
	private void selectUpper() {
		if (selection > 0) {
			awaitsInput = false;
			KeyValue kv1, kv2, kv3 = null;
			
			if (selection == answersHolder.getChildren().size()) {
				kv1 = new KeyValue(customAnswerHolder.translateXProperty(), 0, STEP_INTERP);
				kv3 = new KeyValue(customAnswerHolder.opacityProperty(), 0);
			} else {
				kv1 = new KeyValue(answersHolder.getChildren().get(selection).translateXProperty(), 0, STEP_INTERP);
			}
			selection -= 1;
			kv2 = new KeyValue(answersHolder.getChildren().get(selection).translateXProperty(), SELECTION_INDENT, STEP_INTERP);
			
			ani.getKeyFrames().clear();
			if (kv3 == null) {
				ani.getKeyFrames().add(new KeyFrame(STEP_TIME, kv1, kv2));
				ani.setOnFinished(finisherInputAnswer);
			} else {
				ani.getKeyFrames().add(new KeyFrame(STEP_TIME, kv1, kv2, kv3));
				ani.setOnFinished(finisherInputAnswerClear);
			}
			ani.playFromStart();
		}
		//and finally
		//answersHolder.requestFocus();
		//customAnswer.setText("");
	}
	
	private void selectLower() {
		if (justArrow) {
			return;
		}
		if (selection < answersHolder.getChildren().size()) {
			awaitsInput = false;
			KeyValue kv1, kv2, kv3 = null;
			
			kv1 = new KeyValue(answersHolder.getChildren().get(selection).translateXProperty(), 0, STEP_INTERP);
			
			selection += 1;
			if (selection == answersHolder.getChildren().size()) {
				kv2 = new KeyValue(customAnswerHolder.translateXProperty(), SELECTION_INDENT, STEP_INTERP);
				kv3 = new KeyValue(customAnswerHolder.opacityProperty(), 1);
			} else {
				kv2 = new KeyValue(answersHolder.getChildren().get(selection).translateXProperty(), SELECTION_INDENT, STEP_INTERP);
			}

			ani.getKeyFrames().clear();
			if (kv3 == null) {
				ani.getKeyFrames().add(new KeyFrame(STEP_TIME, kv1, kv2));
				ani.setOnFinished(finisherInputAnswer);
			} else {
				ani.getKeyFrames().add(new KeyFrame(STEP_TIME, kv1, kv2, kv3));
				ani.setOnFinished(finisherInputCustom);
			}
			ani.playFromStart();
		}
	}
	
	private Text getText(String content) {
		Text t = new Text(content);
		t.setWrappingWidth(TEXT_WIDTH);
		t.setLineSpacing(-5);
		t.setFill(foreColor);
		t.setFont(font);
		return t;
	}
	
	private void applyStyleOnShape(Shape s) {
		s.setStroke(foreColor);
		s.setStrokeLineCap(StrokeLineCap.ROUND);
		s.setStrokeLineJoin(StrokeLineJoin.ROUND);
		s.setStrokeWidth(3);
	}
	
	private Polyline getArrow(double offs) {
		Polyline p = new Polyline(offs,0,ARROW_SIZE+offs,ARROW_SIZE,offs,ARROW_SIZE*2);
		applyStyleOnShape(p);
		return p;
	}
	
	private class KeyManager implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent ke) {
			if (awaitsInput) {
				if (ke.getCode() == KeyCode.ENTER) {
					enter();
				} else if (ke.getCode() == KeyCode.UP) {
					selectUpper();
				} else if (ke.getCode() == KeyCode.DOWN) {
					selectLower();
				} else if (ke.getCode() == KeyCode.ESCAPE) {
					if (stage.isFullScreen()) {
						stage.setFullScreen(false);
					} else {
						//Platform.exit();
						stage.close();
					}
				} else if (ke.getCode() == KeyCode.F5 || ke.getCode() == KeyCode.F11) {
					stage.setFullScreen(!stage.isFullScreen());
				}
			}
		}
	}
	
	private class AnimationFinisher implements EventHandler<ActionEvent> {
		boolean cca, ai, cf, af;
		public AnimationFinisher(boolean awaitsInput, boolean clearCustomAnswer, boolean requestCustomFocus, boolean requestAnswerFocus) {
			cca = clearCustomAnswer;
			ai = awaitsInput;
			cf = requestCustomFocus;
			af = requestAnswerFocus;
		}
		@Override
		public void handle(ActionEvent ae) {
			if (cca) {
				customAnswer.setText("");
				customAnswer.setOpacity(1);
				customAnswer.setDisable(false);
			}
			if (ai) {
				awaitsInput = true;
			}
			if (cf) {
				customAnswer.requestFocus();
			}
			if (af) {
				answersHolder.requestFocus();
			}
		}
	}
	
	private class ConfirmContinuer implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent ae) {
			loadNextPage();
		}
	}
	
	public static void initiateTest(Story s, SaveStat ss, EditorWindow e) {
		owner = e;
		storyToLoad = s;
		storySaveStat = ss;
		try {
			new MainWindow().start(new Stage());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	private class LoadingHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent ae) {
			if (!stage.isShowing()) {
				System.out.println("stage not showing, cancelled.");
				return;
			}
			if (manager == null) {
				showLoadingAnimation();
			} else {
				initGameLoop();
			}
		}
	}
}