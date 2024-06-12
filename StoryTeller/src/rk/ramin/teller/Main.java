package rk.ramin.teller;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javafx.application.Application;

public class Main {

	public static void main(String[] args) {
		if (args.length == 0) {
			Application.launch(MainWindow.class, args);
		} else {
			Application.launch(EditorWindow.class, args);
		}
	}

}
