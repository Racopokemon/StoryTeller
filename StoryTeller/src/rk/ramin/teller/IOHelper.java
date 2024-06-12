package rk.ramin.teller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

public class IOHelper {
	
	public static File DATA_PATH = new File("C:\\Users\\Der Guru\\Desktop\\data.sty"); //TODO: Relpace this with return new File(MyClass.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath())
	public static File SAVE_PATH = new File("C:\\Users\\Der Guru\\Desktop\\save.sav"); //Taken from https://stackoverflow.com/questions/320542/how-to-get-the-path-of-a-running-jar-file, thank you guys :*
	private static FileChooser dataChooser, saveChooser;
	
	static {
		dataChooser = new FileChooser();
		dataChooser.setInitialDirectory(IOHelper.DATA_PATH.getParentFile());
		dataChooser.getExtensionFilters().addAll(new ExtensionFilter("Story files","*.sty"),
				new ExtensionFilter("Old story files","*.ser"), new ExtensionFilter("All files","*.*"));
		saveChooser = new FileChooser();
		saveChooser.setInitialDirectory(IOHelper.DATA_PATH.getParentFile());
		saveChooser.getExtensionFilters().addAll(new ExtensionFilter("Save files","*.sav"),
				new ExtensionFilter("All files","*.*"));
	}
	
	public static void saveStory(Story s, File dest) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(dest)));
		zos.putNextEntry(new ZipEntry("data"));
		ObjectOutputStream oos = new ObjectOutputStream(zos);
		oos.writeObject(s);
		oos.flush();
		oos.close();
	}
	
	public static Story loadStory(File dest) throws IOException, ClassNotFoundException {
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(dest)));
		zis.getNextEntry();
		ObjectInputStream ois = new ObjectInputStream(zis);
		Story s = (Story)ois.readObject();
		ois.close();
		s.makeCompatible();
		return s;
	}
	
	public static File chooseFileToSave(Window ownerWindow) {
		dataChooser.setTitle("Where to safe the story file?");
		return dataChooser.showSaveDialog(ownerWindow);
	}
	
	public static File chooseFileToOpen(Window ownerWindow) {
		dataChooser.setTitle("Select story file");
		return dataChooser.showOpenDialog(ownerWindow);
	}
	
	public static void showErrorAndWait(Exception e, String descr) {
		Alert a = new Alert(AlertType.ERROR);
		a.setTitle("Error");
		a.setHeaderText(descr);
		a.setContentText(e.getClass().getName() + "\n" + e.getMessage());
		a.showAndWait();
	}
	
	public static File chooseSaveFileToOpen(Window ownerWindow) {
		saveChooser.setTitle("Select save file");
		return saveChooser.showOpenDialog(ownerWindow);
	}
	
	public static File chooseSaveFileToSave(Window ownerWindow) {
		saveChooser.setTitle("Where to safe the save file?");
		return saveChooser.showSaveDialog(ownerWindow);
	}
	
	public static SaveStat loadSaveStat(File f) throws IOException, ClassNotFoundException {
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(f)));
		zis.getNextEntry();
		ObjectInputStream ois = new ObjectInputStream(zis);
		SaveStat s = (SaveStat) ois.readObject();
		ois.close();
		s.makeCompatible();
		return s;
	}
	
	public static void saveSaveStat(SaveStat s, File f) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
		zos.putNextEntry(new ZipEntry("save"));
		ObjectOutputStream oos = new ObjectOutputStream(zos);
		oos.writeObject(s);
		oos.flush();
		oos.close();
	}
}
