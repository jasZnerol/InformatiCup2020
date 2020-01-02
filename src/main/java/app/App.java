package app;

import app.gui.GuiController;
import app.http.GameServer;

//Imports for the GUI
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

	public static GuiController guiController;

	public static void main(String[] args) {
		
		// All the code from the main was moved to the start-method.
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		
		new GameServer();
		Platform.setImplicitExit(true);

		try {
			// Create the stages for the GUI.
			Stage GUI = new Stage();

			// Create the loader for the GUI.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/gui.fxml"));

			// Create and assign the controllers.
			App.guiController = new GuiController();
			loader.setController(guiController);

			// Create the scenes for the GUI.
			Parent root = (Parent) loader.load();
			Scene scene = new Scene(root);

			// Maximize the stage.
			GUI.setMaximized(true);

			// Attaches the newly created scene to the stage. Shows the stage in a resizable
			// window. Also set title to "Pandemie".
			GUI.setScene(scene);
			GUI.setTitle("Pandemie");
			GUI.show();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("An error occoured while starting the GUI.");
			App.guiController = null;
		}
	}	
}