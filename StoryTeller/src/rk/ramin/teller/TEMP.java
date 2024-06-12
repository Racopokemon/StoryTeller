package rk.ramin.teller;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioEqualizer;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.EqualizerBand;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class TEMP extends Application {
	private static final int BANDS = 128;
	private MediaPlayer mp;
	public void go() {
		Media m;
		String s = "file:///";
		try {
			s = new File("C:\\Users\\Der Guru\\Desktop\\Story Teller\\rally_replay.mp3").toURI().toURL().toString();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(s);
		m = new Media(s);
		mp = new MediaPlayer(m);
		mp.setAutoPlay(true);
		mp.setCycleCount(MediaPlayer.INDEFINITE);
		AudioEqualizer eq = mp.getAudioEqualizer();
		updateEq(1);
		eq.setEnabled(true);
		/*
		mp.setAudioSpectrumInterval(0.02);
		mp.setAudioSpectrumThreshold(-60);
		mp.setAudioSpectrumNumBands(BANDS);
		mp.setAudioSpectrumListener(new AudioSpectrumListener() {
			@Override
			public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] whoneedsthephases) {
				float min = Float.MAX_VALUE, max = Float.MIN_VALUE;
				for (int i = 0; i < BANDS; i++) {
					float v = magnitudes[i];
					if (v < min) {
						min = v;
					}
					if (v > max) {
						max = v;
					}
					rects[i].setWidth((v+100)*2);
				}
				//System.out.format("Min: %f, Max: %f\n", min, max);
			}
		});
		*/
	}
	private static final int TIME = 100;
	private void updateEq(double factor) {
		mp.getAudioEqualizer().getBands().clear();
		mp.getAudioEqualizer().getBands().addAll(
				//new EqualizerBand(50, 120, EqualizerBand.MIN_GAIN), 
				new EqualizerBand(1700, 300, 0),
				new EqualizerBand(15000, 8000, EqualizerBand.MIN_GAIN*factor),
				new EqualizerBand(20000, 10000, EqualizerBand.MIN_GAIN*factor));
	}
	
	private Rectangle[] rects;
	@Override
	public void start(Stage stage) throws Exception {
		VBox h = new VBox(1);
		rects = new Rectangle[BANDS];
		for (int i = 0; i < BANDS; i++) {
			rects[i] = new Rectangle(3,3, Color.DARKSLATEBLUE);
			h.getChildren().add(0, rects[i]);
		}
		Button b = new Button("Fade diggha");
		h.getChildren().add(b);
		b.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				if (state != -1) {
					if (state == 0) {
						state = -1;
						new Thread(new Runnable() {
							@Override
							public void run() {
								for (int i = 0; i < 20; i++) {
									try {
										Thread.sleep(TIME);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									updateEq(1.0-(i/20.0));
								}
								state = 1;
							}
						}).start();
					} else {
						state = -1;
						new Thread(new Runnable() {
							@Override
							public void run() {
								for (int i = 0; i < 20; i++) {
									try {
										Thread.sleep(TIME);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									updateEq((i/20.0));
								}
								state = 0;
							}
						}).start();
					}
				}
			}
		});
		h.setAlignment(Pos.CENTER_LEFT);
		h.setMaxWidth(200);
		StackPane p = new StackPane(h);
		p.setBackground(new Background(new BackgroundFill(Color.ANTIQUEWHITE, null, null)));
		Scene s = new Scene(p);
		stage.setScene(s);
		stage.setWidth(800);
		stage.setHeight(600);
		stage.show();
		go();
	}
	
	private int state = 0;
}
