package it.polimi.ingsw.client.view.GUI.GUIController;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import it.polimi.ingsw.client.view.GUI.registry.PlayerColorRegistry;
import it.polimi.ingsw.network.messages.RankingUpdateMessage;
import it.polimi.ingsw.network.snapshots.PlayerSnapshot;
import it.polimi.ingsw.server.database.RankingEntry;

import java.util.List;

/**
 * Controller for the ranking/results screen.
 */
public class GUIRankingController {

    /** Container showing the result cards of the just-finished match */
    @FXML private HBox matchResultsBox;

    /** Container holding the rows of the global ranking */
    @FXML private VBox rankingBox;

    /** Label displaying the local player's current global ranking position */
    @FXML private Label myPositionLabel;

    /** Label shown when the server is disconnecting from the client */
    @FXML private Label closingLabel;


    /**
     * Populates the ranking screen with the finished match results and
     * the current global ranking data. Called by GUIHandler.onRankingUpdate()
     *
     * @param rankingMessage the message containing the global ranking list and the local player's ranking position
     * @param matchPlayers the players of the just-finished match
     */
    public void populate(RankingUpdateMessage rankingMessage, List<PlayerSnapshot> matchPlayers) {
        buildMatchResults(matchPlayers);
        buildGlobalRanking(rankingMessage.getRanking(), rankingMessage.getPlayerRankPosition());
    }

    /**
     * Displays the shutdown message when the server notifies that it is closing
     * or the connection is being terminated.
     * Called by GUIHandler.onShutdown() to signal the server disconnected
     */
    public void showClosingMessage() {
        closingLabel.setVisible(true);
        closingLabel.setManaged(true);
    }

    /**
     * Builds the result cards for the just-finished match, ordering players
     * by descending score and visually locate the winner.
     *
     * @param players the players to include in the match result section
     */
    private void buildMatchResults(List<PlayerSnapshot> players) {
        matchResultsBox.getChildren().clear();

        List<PlayerSnapshot> sorted = players.stream()
                .sorted((a, b) -> Integer.compare(b.getPoints(), a.getPoints()))
                .toList();

        for (int i = 0; i < sorted.size(); i++) {
            VBox card = new VBox(6);
            PlayerSnapshot p = sorted.get(i);
            String hex = PlayerColorRegistry.getInstance().getHex(p.getNickname());
            boolean isFirst = (i == 0);

            card.setAlignment(Pos.CENTER);
            card.setPrefWidth(160);
            card.setStyle(
                    "-fx-background-color: #1a1a10; " +
                            "-fx-border-color: " + hex + "; " +
                            "-fx-border-width: " + (isFirst ? "2.5" : "1.2") + "; " +
                            "-fx-border-radius: 10; " +
                            "-fx-background-radius: 10; " +
                            "-fx-padding: 16 12 16 12;"
            );

            // Position
            Label posLabel = new Label(medalFor(i));
            posLabel.setStyle(
                    "-fx-font-size: " + (isFirst ? "28px" : "18px") + "; "
            );

            // Nickname
            Label nickLabel = new Label(p.getNickname());
            nickLabel.setStyle(
                    "-fx-text-fill: " + hex + "; " +
                            "-fx-font-size: 13px; " +
                            "-fx-font-weight: bold;"
            );

            // Points
            Label pointsLabel = new Label(p.getPoints() + " pts");
            pointsLabel.setStyle(
                    "-fx-text-fill: #f0e0b0; " +
                            "-fx-font-size: " + (isFirst ? "22px" : "16px") + "; " +
                            "-fx-font-weight: bold;"
            );

            // Food
            Label foodLabel = new Label("🍖 " + p.getFood());
            foodLabel.setStyle("-fx-text-fill: #888866; -fx-font-size: 11px;");

            card.getChildren().addAll(posLabel, nickLabel, pointsLabel, foodLabel);
            matchResultsBox.getChildren().add(card);
        }
    }

    /**
     * Builds the rows of the global ranking and highlights the local player if their position is available.
     *
     * @param ranking the ordered global ranking entries
     * @param myPosition the local player's ranking position, or {@code -1} if the player is not currently ranked
     */
    private void buildGlobalRanking(List<RankingEntry> ranking, int myPosition) {
        rankingBox.getChildren().clear();

        if (ranking.isEmpty()) {
            Label empty = new Label("No players in ranking yet.");
            empty.setStyle("-fx-text-fill: #555544; -fx-font-size: 12px;");
            rankingBox.getChildren().add(empty);
        } else {
            for (int i = 0; i < ranking.size(); i++) {
                RankingEntry entry = ranking.get(i);
                boolean isMe = (i + 1) == myPosition;

                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle(
                        "-fx-padding: 6 12 6 12; " +
                                "-fx-background-radius: 6; " +
                                "-fx-background-color: " + (isMe ? "#2a2a16" : "#1a1a10") + "; " +
                                (isMe ? "-fx-border-color: #ffcc00; -fx-border-width: 1; -fx-border-radius: 6;" : "")
                );

                // Position
                Label posLabel = new Label(String.format("%2d.", i + 1));
                posLabel.setPrefWidth(30);
                posLabel.setStyle(
                        "-fx-text-fill: " + (isMe ? "#ffcc00" : "#555544") + "; " +
                                "-fx-font-size: 12px; -fx-font-weight: bold;"
                );

                // Nickname
                Label nickLabel = new Label(entry.getNickname());
                nickLabel.setPrefWidth(180);
                nickLabel.setStyle(
                        "-fx-text-fill: " + (isMe ? "#ffcc00" : "#d0c8a0") + "; " +
                                "-fx-font-size: 12px;" +
                                (isMe ? " -fx-font-weight: bold;" : "")
                );

                // Wins
                Label winsLabel = new Label("Wins: " + entry.getWins());
                winsLabel.setPrefWidth(80);
                winsLabel.setStyle("-fx-text-fill: #888866; -fx-font-size: 11px;");

                // Points average
                Label avgLabel = new Label(
                        String.format("Avg score: %.1f", entry.getAvgScore())
                );
                avgLabel.setStyle("-fx-text-fill: #888866; -fx-font-size: 11px;");

                row.getChildren().addAll(posLabel, nickLabel, winsLabel, avgLabel);
                rankingBox.getChildren().add(row);
            }
        }

        // Local player position label
        if (myPosition == -1) {
            myPositionLabel.setText("You are not in the ranking yet (no wins).");
        } else {
            myPositionLabel.setText("Your global position: #" + myPosition);
        }
    }

    /**
     * Returns the medal emoji (top 3) or text (4 +) associated with the given ranking index.
     *
     * @param index the zero-based ranking index
     * @return a medal emoji for the top three positions, or the numeric placement for lower positions
     */
    private String medalFor(int index) {
        return switch (index) {
            case 0 -> "🥇";
            case 1 -> "🥈";
            case 2 -> "🥉";
            default -> (index + 1) + ".";
        };
    }
}