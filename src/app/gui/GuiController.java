package app.gui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.stream.Collectors;

import app.game.City;
import app.game.Game;
import app.game.Pathogen;
import app.game.actions.Action;
import app.game.actions.ActionType;
import app.http.GameExchange;
import app.http.GameServer;
import app.solver.Main;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class GuiController {

	@FXML // All Buttons in the GUI
	private Button quitB, endRoundB, putUnderQuarantineB, closeAirportB, closeConnectionB, developVaccineB,
			deployVaccineB, developMedicationB, deployMedicationB, applyHygienicMeasuresB, exertInfluenceB,
			callElectionsB, launchCampaignB, medicateBiggestCitiesB, vaccinateBiggestCitiesB, autoTurnB;

	@FXML // Input fields
	private TextField roundsT, amountT;
	@FXML
	private ChoiceBox<String> pathogenesCB, citiesToCB, showDistinctCityCB;
	@FXML
	private ComboBox<String> citiesCB;

	@FXML // CityInfo
	private Text population, economy, goverment, awareness, hygiene, events;
	@FXML // PathogenInfo
	private Text infectivity, mobility, duration, lethality, prevalance;
	@FXML // OtherInfo
	private Text currentRound, currentPoints, lastAction;

	@FXML // Draw elements
	private Canvas currentMap;

	@FXML // Checking what to draw
	private CheckBox connectionBox, populationBox, infectedBox, cityNamesBox;

	// Backend components of the GUI
	private String lastActionString;
	private GameExchange currentGameExchange;
	private Game currentGame;

	// Booleans corresponding
	private static boolean showHealthyCities, showInfectedCities;

	// Constructor
	public GuiController() {
		/*
		 * Has no real purpose. Just need to be declared for the FXML-file
		 */
	}

	public void initialize() {
		
		// If game is not set return
		if (this.currentGame == null) {
			ClassLoader classLoader = getClass().getClassLoader();

			InputStream jsonStream = classLoader.getResourceAsStream("resources/EmptyGame.json");
			BufferedReader reader = new BufferedReader(new InputStreamReader(jsonStream));
			String json = reader.lines().collect(Collectors.joining(System.lineSeparator()));

			this.currentGame = new Game(json);
		}

		// Set all checkBox booleans as selected but only of this is the first
		// initialize.
		// Also initialize the selectedCity and selectedPathogen as null

		// Set ChoiceBoxes to default
		this.connectionBox.setSelected(true);
		this.populationBox.setSelected(true);
		this.infectedBox.setSelected(true);
		this.cityNamesBox.setSelected(true);

		// Set default values
		showHealthyCities = false;
		showInfectedCities = false;

		// Setting up a prompt text in the textField. Therefore when selected it will
		// disappear.
		this.roundsT.setPromptText("enter duration...");
		this.amountT.setPromptText("enter the amount ...");

		// Set the Button Tooltips.
		this.quitB.setTooltip(new Tooltip("Close the server and this window."));
		this.endRoundB.setTooltip(new Tooltip("End the current round.\namount : number of rounds that just get ended"));
		this.putUnderQuarantineB.setTooltip(
				new Tooltip("Put a city under qurantine.\ncity (from): city name\nrounds : number of rounds"));
		this.closeAirportB
				.setTooltip(new Tooltip("Close a city's airport.\ncity (from): city name\nrounds : number ofrounds"));
		this.closeConnectionB.setTooltip(new Tooltip(
				"Close a connection between two cities.\ncity (from) : first city name\ncity (to) : second city name\nrounds : number of rounds"));
		this.developVaccineB
				.setTooltip(new Tooltip("Develop a vaccine against a certain pathogen.\npathogen : pathogen name"));
		this.deployVaccineB.setTooltip(new Tooltip(
				"Deploy a vaccine in against a certain pathogen in a certain city.\ncity (from) city name\npathogen : pathogen name\n"));
		this.developMedicationB
				.setTooltip(new Tooltip("Develop a medication against a certain pathogen.\npathogen : pathogen name"));
		this.deployMedicationB.setTooltip(new Tooltip(
				"Deploy a medication in against a certain pathogen in a certain city.\ncity (from) : city name\npathogen : pathogen name"));
		this.applyHygienicMeasuresB
				.setTooltip(new Tooltip("Randomly increase hygine in a certain city\ncity (from) : city name"));
		this.exertInfluenceB
				.setTooltip(new Tooltip("Reset the economic strength of a certain city\ncity (from) : city name"));
		this.callElectionsB
				.setTooltip(new Tooltip("Reset the political strength of a certain city\ncity (from) : city name"));
		this.launchCampaignB.setTooltip(new Tooltip(
				"The attentiveness of a certain city's population will be increased\ncity (from) : city name"));
		this.autoTurnB.setTooltip(new Tooltip("Let the computer play.\namount : amount of actions"));
		this.medicateBiggestCitiesB.setTooltip(new Tooltip(
				"Medicate a certain amount of the biggest cities.\npathogen : pathogen name\namount : amount of cities to medicate"));
		this.vaccinateBiggestCitiesB.setTooltip(new Tooltip(
				"Vaccinate a certain amount of the biggest cities.\npathogen : pathogen name\namount : amount of cities to vaccinate"));
		// Set input Tooltip for inputs TODO

		// Update and draw
		this.update();
	}

	public void update() {

		// Set the last Action in the Text to the static String where it was saved.
		this.lastAction.setText(lastActionString);

		// Set Points and current Round.
		if (this.currentGameExchange != null) {

			// Set the current round and points.
			this.currentRound.setText(this.currentGame.getRound() + "");
			this.currentPoints.setText(this.currentGame.getPoints() + "");
		}

		// Update the ChoiceBoxes items.
		this.updateChoiceBox();
		
		// Update the Info views
		this.updateInfo();

		// Draw Call for the MapCanvas
		this.drawMap();
	}

	private void updateChoiceBox() {

		// Check if there is a game
		Game currentGame = this.currentGame;
		if (currentGame == null) {
			return;
		}

		// Add all pathogens to the pathogens ChoiceBox if they have changed
		ObservableList<String> allPathogens = this.currentGame.getPathogens().stream().map(p -> p.getName())
				.sorted().collect(FXCollections::<String>observableArrayList, ObservableList<String>::add,
						ObservableList<String>::addAll);
		// Add no pathogen as choice
		allPathogens.add(0, null);
		if (!this.pathogenesCB.getItems().equals(allPathogens)) {
			this.pathogenesCB.setItems(allPathogens);
		}

		// Add all cities to the cities ChoiceBox
		ObservableList<String> allCities = currentGame.getCities().values().stream().map(c -> c.getName()).sorted()
				.collect(FXCollections::<String>observableArrayList, ObservableList<String>::add,
						ObservableList<String>::addAll);
		// Add no city as choice
		allCities.add(0, null);
		// Update ChoiceBox if changed in a new thread to boost performance
		if (!this.citiesCB.getItems().equals(allCities)) {
			this.citiesCB.setItems(allCities);
		}

		// Show only connections of selected city in city to ChoiceBox
		// If no city is selected clear choicebox
		Collection<City> cities = this.getSelectedCity() == null ? City.EMPTY_CITY_SET
				: this.getSelectedCity().getConnections();
		ObservableList<String> citiesTo = cities.stream().map(c -> c.getName()).sorted().collect(
				FXCollections::<String>observableArrayList, ObservableList<String>::add,
				ObservableList<String>::addAll);
		// Add no city as choice
		citiesTo.add(0, null);
		// Update ChoiceBox if changed
		if (!this.citiesToCB.getItems().equals(citiesTo)) {
			this.citiesToCB.setItems(citiesTo);
		}
	}

	public void setGame(GameExchange exchange) {
		if (exchange == null || !this.ready()) {
			return;
		}
		
		this.currentGameExchange = exchange;
		this.currentGame = this.currentGameExchange.getGame();
		this.update();
	}

	/**
	 * Function to detect whether the GUI is ready for a new round to play. The GUI
	 * is ready if there is no active round at the time.
	 * 
	 * @return True if GUI is ready.
	 */
	public boolean ready() {
		return this.currentGameExchange == null;
	}

	// is called from the quitButton from the GuiController.
	@FXML
	public void quit() {
		// Get the primaryStage from any element of the GUI (It can be any element
		// because they all have the same root)
		Stage primaryStage = (Stage) this.autoTurnB.getScene().getWindow();

		// If there was no primaryStage found something went wrong.
		if (primaryStage == null)
			System.err.println("Could not find primary stage");

		// Close the primaryStage and close the Server (terminate the Programm)
		primaryStage.close();
		System.exit(0);
	}

	/**
	 * Updates the text fields with information about the selected pathogen
	 * and the selected city.
	 */
	private void updateInfo() {

		// Get the selected city.
		City selectedCity = this.getSelectedCity();

		// Only set the info of the selected city if there is a selected city.
		if (selectedCity != null) {

			// Get all the infos of the selected city info as Strings.
			String population = selectedCity.getPopulation() + "";
			String economy = selectedCity.getEconomy().toString();
			String goverment = selectedCity.getGovernment().toString();
			String hygiene = selectedCity.getHygiene().toString();
			String awareness = selectedCity.getAwareness().toString();

			// Set all the info to the corresponding textElemt.
			this.population.setText(population);
			this.economy.setText(economy);
			this.goverment.setText(goverment);
			this.hygiene.setText(hygiene);
			this.awareness.setText(awareness);
		}

		// Get the selected pathogen.
		Pathogen selectedPathogen = this.getSelectedPathogen();

		// Only set the info of the selected pathogen if there is a selected pathogen.
		if (selectedPathogen != null) {

			// Get all the infos of the selected Pathogen as String
			String infectivity = selectedPathogen.getInfectivity().toString();
			String mobility = selectedPathogen.getMobility().toString();
			String duration = selectedPathogen.getDuration().toString();
			String lethality = selectedPathogen.getLethality().toString();
			String prevalance = selectedCity != null? selectedCity.getPrevalance() + "": "";

			// Only get 5 chars of the prevalance
			if (prevalance.length() > 5) {
				prevalance = prevalance.substring(0, 5);
			}

			// Set the texts in the GUI to the Info gotten above.
			this.infectivity.setText(infectivity);
			this.mobility.setText(mobility);
			this.duration.setText(duration);
			this.lethality.setText(lethality);
			this.prevalance.setText(prevalance);
		}

		// update the GUI
		drawMap();
	}

	/**
	 * Sets the last action string that is displayed in the GUI
	 * 
	 * @param action Action to be displayed.
	 */
	public void setLastAction(String action) {
		this.lastActionString = action;
		this.update();
	}

	/**
	 * Returns the amount given by the user in the amount text field. If the number
	 * is less or equal to zero or no number is given the function returns 1.
	 * 
	 * @return Amount in text field.
	 */
	private int getAmount() {
		int amount;
		try {
			// Return amount out of the text field
			amount = Integer.parseInt(this.amountT.getText());
		} catch (Exception e) {
			// Invalid option in amount field
			// Return default value
			amount = 1;
		}

		return amount > 0 ? amount : 1;
	}

	/**
	 * Returns the number of rounds given by the user in the rounds text field. If
	 * the number is less or equal to zero or no number is given the function
	 * returns 1.
	 * 
	 * @return Amount in text field.
	 */
	private int getRounds() {
		int rounds;
		try {
			// Return number of rounds out of the text field
			rounds = Integer.parseInt(this.roundsT.getText());
		} catch (Exception e) {
			// Invalid option in rounds field
			// Return default value
			rounds = 1;
		}

		return rounds > 0 ? rounds : 1;
	}

	/**
	 * Returns the city selected by the user in the city choice box
	 * 
	 * @return Selected city.
	 */
	private City getSelectedCity() {
		return this.currentGame.getCity(this.citiesCB.getValue());
	}

	/**
	 * Returns the city selected by the user in the cityTo choice box
	 * 
	 * @return Selected city in the cityTo choice box.
	 */
	private City getSelectedCityTo() {
		return this.currentGame.getCity(this.citiesToCB.getValue());
	}

	/**
	 * Returns the pathogen selected by the user in the pathogens choice box
	 * 
	 * @return Selected pathogen.
	 */
	private Pathogen getSelectedPathogen() {
		return this.currentGame.getPathogen(this.pathogenesCB.getValue());
	}

	@FXML
	private void endRound() {

		// Get amount
		int amount = getAmount();

		// Add actions into action queue
		for (int i = 0; i < amount; i++) {
			GameServer.addReply((Game g) -> new Action(g).toString());
		}

		// Execute first action
		this.executeAction();
	}

	@FXML
	private void autoTurn() {

		// Get amount
		int amount = getAmount();

		// Add actions into action queue
		for (int i = 0; i < amount; i++) {
			GameServer.addReply((Game g) -> Main.solve(g));
		}

		// Execute first action
		this.executeAction();
	}

	@FXML
	private void putUnderQuarantine() {
		// Get the selected city
		City city = this.getSelectedCity();

		// Get the rounds specified by the user
		int rounds = this.getRounds();

		// Execute action if a valid city was selected
		if (city != null) {
			this.executeAction(new Action(ActionType.putUnderQuarantine, this.currentGame, city, rounds));
		}
	}

	@FXML
	private void closeAirport() {
		// Get the selected city
		City city = this.getSelectedCity();

		// Get the rounds specified by the user
		int rounds = this.getRounds();

		// Execute action if a valid city was selected
		if (city != null) {
			this.executeAction(new Action(ActionType.closeAirport, this.currentGame, city, rounds));
		}
	}

	@FXML
	private void closeConnection() {
		// Get the selected city
		City city = this.getSelectedCity();

		// Get the selected cityTo
		City cityTo = this.getSelectedCityTo();

		// Get the rounds specified by the user
		int rounds = this.getRounds();

		// Execute action if two valid cities were selected
		if (city != null && cityTo != null) {
			this.executeAction(new Action(this.currentGame, city, cityTo, rounds));
		}
	}

	@FXML
	private void developVaccine() {

		// Get the selected pathogen
		Pathogen pathogen = this.getSelectedPathogen();

		// Execute action if a valid pathogen was selected
		if (pathogen != null) {
			this.executeAction(new Action(ActionType.developVaccine, this.currentGame, pathogen));
		}
	}

	@FXML
	private void deployVaccine() {

		// Get the selected city
		City city = this.getSelectedCity();

		// Get the selected pathogen
		Pathogen pathogen = this.getSelectedPathogen();

		// Execute action if a valid city and pathogen were selected
		if (city != null && pathogen != null) {
			this.executeAction(new Action(ActionType.deployVaccine, this.currentGame, city, pathogen));
		}
	}

	@FXML
	private void developMedication() {

		// Get the selected pathogen
		Pathogen pathogen = this.getSelectedPathogen();

		// Execute action if a valid pathogen was selected
		if (pathogen != null) {
			this.executeAction(new Action(ActionType.developMedication, this.currentGame, pathogen));
		}
	}

	@FXML
	private void deployMedication() {

		// Get the selected city
		City city = this.getSelectedCity();

		// Get the selected pathogen
		Pathogen pathogen = this.getSelectedPathogen();

		// Execute action if a valid city and pathogen were selected
		if (city != null && pathogen != null) {
			this.executeAction(new Action(ActionType.deployMedication, this.currentGame, city, pathogen).toString());
		}
	}

	/**
	 * Executes an reroll action of the given type in the selected city.
	 * 
	 * @param type Type of action to execute.
	 */
	private void doRerollAction(ActionType type) {

		// Get the selected city
		City city = this.getSelectedCity();

		// Execute action if a valid city was selected
		if (city != null) {
			this.executeAction(new Action(type, this.currentGame, city));
		}
	}

	@FXML
	private void applyHygienicMeasures() {
		this.doRerollAction(ActionType.applyHygienicMeasures);
	}

	@FXML
	private void exertInfluence() {
		this.doRerollAction(ActionType.exertInfluence);
	}

	@FXML
	private void callElections() {
		this.doRerollAction(ActionType.callElections);
	}

	@FXML
	private void launchCampaign() {
		this.doRerollAction(ActionType.launchCampaign);
	}

	@FXML
	private void vaccinateBiggestCities() {

		// Get the selected pathogen
		Pathogen selectedPathogen = this.getSelectedPathogen();

		// If no valid pathogen was selected do nothing and return
		if (selectedPathogen == null) {
			return;
		}

		// Get the selected amount
		int amount = this.getAmount();

		for (int i = 0; i < amount; i++) {

			GameServer.addReply((Game g) -> {
				// Due to the fact, that we check the pathogen for equality by reference
				// we need to get the pathogen out of the current game and cannot use the
				// selected pathogen of the GUI
				Pathogen pathogen = g.getPathogen(selectedPathogen.getName());

				// Search the city with the most uninfected people to maximize the resulting
				// immunity
				City bestCity = g.getCities().values().stream() // Search all cities
						.filter(c -> c.getVaccineDeployed().stream().allMatch(e -> e.getPathogen() != pathogen)) // Filter
																													// cities
																													// that
																													// were
																													// already
																													// vaccinated
						.max((City c1, City c2) -> {
							// Get the healthy population as we want to vaccinate the city which impacts the
							// most people
							double healthyPopulation1 = c1.isInfected(pathogen) ? 1 - c1.getPrevalance() : 1;
							double healthyPopulation2 = c2.isInfected(pathogen) ? 1 - c2.getPrevalance() : 1;

							return (int) (c1.getPopulation() * healthyPopulation1
									- c2.getPopulation() * healthyPopulation2);
						}).orElseGet(() -> null);

				// If no city is infected by the selected pathogen just end the round
				// and delete all vaccinate biggest cities actions
				if (bestCity == null) {
					GameServer.clearReplies();
					return new Action(g).toString();
				}

				// Return the new Action which medicates the city where currently the most
				// people are not yet infected by the pathogen
				return new Action(ActionType.deployVaccine, g, bestCity, pathogen).toString();
			});
		}

		// Execute the Action generated addReply methode above.
		this.executeAction();

	}

	@FXML // Button implementation
	private void medicateBiggestCities() {

		// Get the selected pathogen
		Pathogen selectedPathogen = this.getSelectedPathogen();

		// If no valid pathogen was selected do nothing and return
		if (selectedPathogen == null) {
			return;
		}

		// Get the selected amount
		int amount = this.getAmount();

		for (int i = 0; i < amount; i++) {

			GameServer.addReply((Game g) -> {
				// Due to the fact, that we check the pathogen for equality by reference
				// we need to get the pathogen out of the current game and cannot use the
				// selected pathogen of the GUI
				Pathogen pathogen = g.getPathogen(selectedPathogen.getName());

				// Search the city with the most infected people to maximize the resulting
				// healing
				City bestCity = g.getCities().values().stream() // Search all cities
						.filter(c -> c.isInfected(pathogen)) // Filter cities that are not infected by the
																		// pathogen
						.max((City c1,
								City c2) -> (int) (c1.getPopulation() * c1.getPrevalance()
										- c2.getPopulation() * c2.getPrevalance()))
						.orElseGet(() -> null);

				// If no city is infected by the selected pathogen just end the round
				// and delete all vaccinate biggest cities actions
				if (bestCity == null) {
					GameServer.clearReplies();
					return new Action(g).toString();
				}

				// Return the new Action which medicates the city where currently the most
				// people are influenced by the selected pathogen.
				return new Action(ActionType.deployMedication, g, bestCity, pathogen).toString();
			});
		}

		// Execute the Action generated addReply methode above.
		this.executeAction();

	}

	/**
	 * Executes the given action.
	 * 
	 * @param action Action to execute.
	 */
	private void executeAction(String action) {
		// Check if there is an active GameExchange
		if (this.currentGameExchange == null || !this.currentGameExchange.isAlive()) {
			this.currentGameExchange = null;
			return;
		}

		this.currentGameExchange.sendReply(action);
		this.currentGameExchange = null;
	}

	private void executeAction(Action action) {
		this.executeAction(action.toString());
	}

	/**
	 * Executes the first action out of the action queue.
	 */
	private void executeAction() {
		if (GameServer.hasReplies()) {
			this.executeAction(GameServer.getReply().evaluate(this.currentGame));
		}
	}

	@FXML // Button implementation
	public void exportMap() {

	}

	/**
	 * Draws the map on the canvas
	 */
	public void drawMap() {

		// Initialize the canvas as a 2D graphics object.
		GraphicsContext gc = this.currentMap.getGraphicsContext2D();

		if (this.currentGame == null) {
			return;
		}

		// Clear the Canvas
		gc.setFill(Color.WHITE);
		gc.setStroke(Color.BLACK);
		gc.clearRect(0, 0, this.currentMap.getWidth(), this.currentMap.getHeight());

		// Get the selected city and the selected pathogen
		Collection<City> cities = this.currentGame.getCities().values();
		City selectedCity = this.getSelectedCity();
		Pathogen selectedPathogen = this.getSelectedPathogen();

		// Iterate over all cities find the one we want to print and print those
		cities.stream().filter(c -> !showHealthyCities || !c.isInfected())
				.filter(c -> !showInfectedCities || c.isInfected(selectedPathogen) && selectedPathogen != null
						|| selectedPathogen == null && c.isInfected())
				.filter(c -> selectedCity == null || selectedCity == c
						|| (selectedCity.getConnections().contains(c) && connectionBox.isSelected()))
				.forEach(c -> drawCity(c));
	}

	/**
	 * Draws a city on the canvas including name and prevalence
	 * 
	 * @param city City to draw
	 */
	private void drawCity(City city) {

		Pathogen selectedPathogen = this.getSelectedPathogen();

		// Initialize the canvas as a 2D graphics object.
		GraphicsContext gc = this.currentMap.getGraphicsContext2D();

		// Get all the info from the current city.
		String cityName = city.getName();
		int diameter = city.getPopulation() / 120;
		int x = (int) city.getX() + 180;
		int y = (int) -city.getY() + 90;

		// If no pathogen is selected or the selected pathogen matches the cities
		// outbreak
		// set the prevalence
		double prev = selectedPathogen == null || city.getPathogen() == selectedPathogen ? city.getPrevalance() : 0.0;

		gc.setFill(new Color(prev, 0, 0, prev));

		// adjust x,y so it fits nicely on the canvas.
		x *= 4;
		y *= 5;

		// Draw Population Circle. If wanted.
		if (populationBox.isSelected())
			gc.strokeOval(x - diameter / 2, y - diameter / 2, diameter, diameter);
		// Color for infectedRate. If a pathogen is selected and showInfected is true.
		if (infectedBox.isSelected())
			gc.fillOval(x - diameter / 2, y - diameter / 2, diameter, diameter);
		// Draw City Name. If wanted.
		if (cityNamesBox.isSelected())
			gc.strokeText(cityName, x, y);

	}
	// End of draw map methods.
}
