package com.example._2048qlearning;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("RL_2048.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("2048");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(event -> {
            GameController gameController = fxmlLoader.getController();
            gameController.pauseStopQTraining();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Exit");
            alert.setHeaderText("Are you sure you want to close the application?");
            alert.setContentText("Training has been paused after completion of the last episode\n\nWould you like to save the QTable before exiting? (Saving will not quit the game)");

            ButtonType saveButton = new ButtonType("Save");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType exitButton = new ButtonType("Exit");

            alert.getButtonTypes().setAll(cancelButton, saveButton, exitButton);

            alert.getDialogPane().lookupButton(saveButton).addEventFilter(ActionEvent.ACTION, e -> {
                gameController.trySerialise();

                e.consume();
            });

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == exitButton) {
                // Allow the window to close without saving
                stage.close();
            } else if (result.isPresent() && result.get() == cancelButton) {
                // Cancel the exit operation
                event.consume();
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}