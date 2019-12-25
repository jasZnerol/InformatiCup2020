package app.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingDeque;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import app.App;
import app.game.Game;
import app.solver.GameEvaluater;
import app.solver.Main;
import javafx.application.Platform;

public class GameServer {

	static double games = 0;
	static double wins = 0;
	
	private HttpServer server;
	private static LinkedBlockingDeque<GameEvaluater> repliesToSend = new LinkedBlockingDeque<>();
	public static final GameEvaluater LOCK = new GameEvaluater() {
		@Override
		public String evaluate(Game currentGame) {

			System.err.println("Blocking deque Lock was executed!");
			GameServer.addReply(GameServer.LOCK);

			return Main.solve(currentGame);
		}
	};

	public GameServer() {
		try {
			this.server = HttpServer.create(new InetSocketAddress(50123), 0);
			HttpContext context = server.createContext("/");
			context.setHandler(GameServer::handleRequest);
			server.start();
		} catch (IOException e) {
			System.err.println("Could not create Server!");
			System.exit(1);
		}
	}

	private static void handleRequest(HttpExchange exchange) {
		if (!exchange.getRequestMethod().equals("POST")) {
			System.err.println("Invalid request!");
		}

		new Thread(() -> {
			GameExchange ge = new GameExchange(exchange);
			String outcome = ge.getGame().getOutcome();
			if (!outcome.equals("pending")) {
				if (outcome.equals("win")) {
					wins++;
				}
				games++;
				outcome = outcome.equals("loss")? outcome: outcome + " ";
				System.out.println("Game Nr. " + games + " was a " + outcome + " | Current winrate = " + (100 * wins / games)  + "%");
			}
			
			GameEvaluater eval = null;
			synchronized (GameServer.class) {
				if (hasReplies() && peekReply() == LOCK) {	
					eval = (Game g) -> Main.solve(g);
				} else if (hasReplies()) {
					eval = getReply();
				} else if (App.guiController != null && App.guiController.ready()) {
					ge.playGui();
				} else {
					// Detected more than one game. CLose GUI and enable auto play
					addReply(LOCK);

					Platform.runLater(() -> {
						App.guiController.executeAction(Main.solve(ge.getGame()));
						App.guiController.close();
					});

					eval = (Game g) -> Main.solve(g);
				}
			}

			if (eval != null) {
				ge.sendReply(eval.evaluate(ge.getGame()));
			}
		}).start();
	}

	public static void addReply(GameEvaluater reply) {
		repliesToSend.add(reply);
	}

	public static GameEvaluater getReply() {
		return repliesToSend.poll();
	}

	public static GameEvaluater peekReply() {
		return repliesToSend.peek();
	}

	public static boolean hasReplies() {
		return !repliesToSend.isEmpty();
	}

	public static void clearReplies() {
		repliesToSend.clear();
	}
}