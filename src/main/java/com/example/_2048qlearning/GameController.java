package com.example._2048qlearning;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.net.URL;
import java.util.*;

public class GameController implements Initializable {

    @FXML
    private GridPane gridPane;
    @FXML
    private Label currentScoreLabel;
    @FXML
    private Label bestScoreLabel;
    @FXML
    private Label terminalLabel;
    @FXML
    private Button startPausePlayTrainingButton;
    @FXML
    private Button deserializeQTableButton;
    @FXML
    private Button serializeQTableButton;
    @FXML
    private CheckBox visualiseCheckBox;

    private Q_Training qTraining;
    private SquareManager squareManager;
    private ScoreManager scoreManager;
    private Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    private final int WIN_VALUE = 2048;
    private final List<Pair<Integer, Integer>> emptyPositions = new ArrayList<>();
    private boolean validMove;
    private final Random random = new Random();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.qTraining = new Q_Training(this, random);
        this.squareManager = new SquareManager(this, gridPane, random);
        this.scoreManager = new ScoreManager(this, currentScoreLabel, bestScoreLabel);
        gridPane.requestFocus();

        qTraining.setDaemon(true);
        qTraining.start();
    }
    @FXML
    private void keyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        if(code == KeyCode.R) {
            scoreManager.resetFile();
        }
    }
    @FXML
    private void buttonPressed(ActionEvent event) {
        Object node = event.getSource();
        if (startPausePlayTrainingButton.equals(node)) {
            if(qTraining.isTraining()) {
                //Pausing/Stopping training
                pauseStopQTraining();
            } else {
                //Starting or Resuming/Restart Training
                if(startPausePlayTrainingButton.getText().equals("START\n" +
                        "TRAINING")) {
                    tryStartTraining();
                } else {
                    startResumeRestartTraining();
                }
            }
        } else if(visualiseCheckBox.equals(node)) {
            updateVisualisation();
        } else if(deserializeQTableButton.equals(node)) {
            tryDeserialize();
        } else if(serializeQTableButton.equals(node)) {
            trySerialise();
        }
    }

    public void moveMergeSquares(Square[][] board, String direction, boolean visualise, boolean actual) {
        long oldSquareCount;
        long newSquareCount;
        validMove = false;
        do {
            oldSquareCount = Arrays.stream(board).flatMap(Arrays::stream).filter(Objects::nonNull).count();
            switch (direction) {
                //Moves and Merge the Squares UP
                case "UP" -> moveMergeUP(board, actual);
                //Moves and Merge the Squares DOWN
                case "DOWN" -> moveMergeDOWN(board, actual);
                //Moves and Merge the Squares Left
                case "LEFT" -> moveMergeLEFT(board, actual);
                //Moves and Merge the Squares Right
                case "RIGHT" -> moveMergeRIGHT(board, actual);
            }
            newSquareCount = Arrays.stream(board).flatMap(Arrays::stream).filter(Objects::nonNull).count();

        } while (newSquareCount != oldSquareCount);

        if(actual && validMove) {
            squareManager.createRandomSquare(board, visualise);
        }

        if(visualise) {
            String result = checkTerminal(board);
            if(result != null) {
                Platform.runLater(() -> {
                    if (result.equals("W")) {
                        terminalLabel.setText("2048!");
                    } else {
                        terminalLabel.setText("GAME\nOVER");
                    }
                    terminalLabel.setVisible(true);
                });
            }
        }
    }
    private void moveMergeUP(Square[][] board, boolean actual) {
        //Move Squares UP
        for (int column = 0; column < 4; column++) {
            int freeRow = 0;
            for (int row = 0; row < 4; row++) {
                Square sq = board[column][row];
                if (sq != null) {
                    if (freeRow != row) {
                        sq.changePosition(board, column, freeRow);
                        validMove = true;
                    }
                    freeRow++;
                }
            }
        }
        //Merge Squares UP
        for (int column = 0; column < 4; column++) {
            for (int row = 1; row < 4; row++) {
                squareManager.mergeSquares(board, board[column][row], board[column][row - 1], actual);
            }
        }
    }
    private void moveMergeDOWN(Square[][] board, boolean actual) {
        //Move Squares DOWN
        for (int column = 0; column < 4; column++) {
            int freeRow = 3;
            for (int row = 3; row > -1; row--) {
                Square sq = board[column][row];
                if (sq != null) {
                    if (freeRow != row) {
                        sq.changePosition(board, column, freeRow);
                        validMove = true;
                    }
                    freeRow--;
                }
            }
        }
        //Merge Squares DOWN
        for (int column = 0; column < 4; column++) {
            for (int row = 2; row > -1; row--) {
                squareManager.mergeSquares(board, board[column][row], board[column][row + 1], actual);
            }
        }
    }
    private void moveMergeLEFT(Square[][] board, boolean actual) {
        //Move Squares LEFT
        for (int row = 0; row < 4; row++) {
            int freeCol = 0;
            for (int column = 0; column < 4; column++) {
                Square sq = board[column][row];
                if (sq != null) {
                    if (freeCol != column) {
                        sq.changePosition(board, freeCol, row);
                        validMove = true;
                    }
                    freeCol++;
                }
            }
        }
        //Merge Squares LEFT
        for (int row = 0; row < 4; row++) {
            for (int column = 1; column < 4; column++) {
                squareManager.mergeSquares(board, board[column][row], board[column - 1][row], actual);
            }
        }
    }
    private void moveMergeRIGHT(Square[][] board, boolean actual) {
        //Move Squares RIGHT
        for (int row = 0; row < 4; row++) {
            int freeCol = 3;
            for (int column = 3; column > -1; column--) {
                Square sq = board[column][row];
                if (sq != null) {
                    if (freeCol != column) {
                        sq.changePosition(board, freeCol, row);
                        validMove = true;
                    }
                    freeCol--;
                }
            }
        }
        //Merge Squares RIGHT
        for (int row = 0; row < 4; row++) {
            for (int column = 2; column > -1; column--) {
                squareManager.mergeSquares(board, board[column][row], board[column + 1][row], actual);
            }
        }
    }

    public void updateEmptyPositions(Square[][] board) {
        emptyPositions.clear();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if(board[i][j] == null) {
                    emptyPositions.add(new Pair<>(i, j));
                }
            }
        }
    }

    private void updateVisualisation() {
        qTraining.setVisualiseTraining(visualiseCheckBox.isSelected());
        if(startPausePlayTrainingButton.getText().equals("RESUME")) {
            startPausePlayTrainingButton.setText("RESTART");
        } else if (startPausePlayTrainingButton.getText().equals("RESTART")) {
            startPausePlayTrainingButton.setText("RESUME");
        }
    }

    private void tryStartTraining() {
        alert.setTitle("Confirm Start Training");
        alert.setHeaderText("Are you sure you want to start training?");
        alert.setContentText("- If you wanted training TO effect the saved Q-Table ensure 'Load Q-Table' has been pressed.\n" +
                "- If you wanted training to NOT effect the saved Q-Table ensure 'Load Q-Table' is NOT pressed");

        ButtonType startButton = new ButtonType("Start");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(startButton, cancelButton);

        ButtonType result = alert.showAndWait().orElse(cancelButton);

        if(result == startButton) {
            startResumeRestartTraining();
        }
    }

    public void trySerialise() {
        alert.setTitle("Confirm Save");
        alert.setHeaderText("Are you sure you want to overwrite the previous Q-Table?");
        alert.setContentText("Saving now will overwrite the previous Q-Table with the one currently being used.\n");

        ButtonType saveButton = new ButtonType("Save");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(saveButton, cancelButton);

        ButtonType result = alert.showAndWait().orElse(cancelButton);

        if(result == saveButton) {
            qTraining.serializeQTable();
        }
    }

    private void tryDeserialize() {
        alert.setTitle("Confirm Q-Table Load");
        alert.setHeaderText("Are you sure you want to load the Q-Table?");
        alert.setContentText("Loading the Q-Table will overwrite the Q-Table currently being used.");

        ButtonType loadButton = new ButtonType("Load");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(loadButton, cancelButton);

        ButtonType result = alert.showAndWait().orElse(cancelButton);

        if(result == loadButton) {
            qTraining.deserializeQTable();
            deserializeQTableButton.setDisable(true);
        }
    }

    public void pauseStopQTraining() {
        if(qTraining.isVisualiseTraining()) {
            startPausePlayTrainingButton.setText("RESTART");
        } else {
            startPausePlayTrainingButton.setText("RESUME");
        }
        qTraining.pauseTraining();
        disableInputs(false);
    }

    private void startResumeRestartTraining() {
        if(qTraining.isVisualiseTraining()) {
            startPausePlayTrainingButton.setText("STOP");
        } else {
            startPausePlayTrainingButton.setText("PAUSE");
        }
        qTraining.resumeTraining();
        disableInputs(true);
    }
    private void disableInputs(boolean disable) {
        visualiseCheckBox.setDisable(disable);
        deserializeQTableButton.setDisable(disable);
        serializeQTableButton.setDisable(disable);
    }
    private boolean checkMergeable(Square[][] board) {
        for (int column = 0; column < 4; column++) {
            for (int row = 0; row < 4; row++) {
                if(row > 0 && board[column][row] != null && board[column][row].getValue() == board[column][row -1].getValue() ||
                        column > 0 && board[column][row] != null && board[column][row].getValue() == board[column -1][row].getValue()) {
                    return true;
                }
            }
        }
        return false;
    }
    private boolean checkFull(Square[][] board) {
        return Arrays.stream(board).flatMap(Arrays::stream).noneMatch(Objects::isNull);
    }
    public String checkTerminal(Square[][] board) {
        if (checkWin(board)){
            return "W";
        } else if(checkLoss(board)) {
            return "L";
        }
        return null;
    }
    public boolean checkWin(Square[][] board) {
        return Arrays.stream(board).flatMap(Arrays::stream).filter(Objects::nonNull).anyMatch(square -> square.getValue() >= WIN_VALUE);
    }

    public boolean checkLoss(Square[][] board) {
        return checkFull(board) && !checkMergeable(board);
    }
    public Square[][] getDeepBoardCopy(Square[][] board) {
        Square[][] newBoard = new Square[4][4];
        for (int column = 0; column < 4; column++) {
            for (int row = 0; row < 4; row++) {
                Square sq = board[column][row];
                if(sq != null) {
                    squareManager.createSquareAddToBoard(newBoard, sq.getValue(), column, row, false);
                }
            }
        }
        return newBoard;
    }
    public Pair<Integer, Integer> getRandomEmptyPosition(Square[][] board) {
        updateEmptyPositions(board);
        return emptyPositions.get(random.nextInt(emptyPositions.size()));
    }

    public List<Pair<Integer, Integer>> getEmptyPositions() {
        return emptyPositions;
    }
    public Q_Training getQ_learning() {
        return qTraining;
    }
    public SquareManager getSquareManager() {
        return squareManager;
    }
    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    public boolean isValidMove() {
        return validMove;
    }
    public void setValidMove(boolean validMove) {
        this.validMove = validMove;
    }
    public void setBoardTrainingComplete() {
        Platform.runLater(() -> {
            startPausePlayTrainingButton.setDisable(true);
            startPausePlayTrainingButton.setText("Training\nComplete");
            terminalLabel.setText("Training\nComplete");
            terminalLabel.setVisible(true);
            serializeQTableButton.setDisable(false);
        });
    }
    public void reset(Square[][] board, boolean visualise) {

        validMove = true;

        if(visualise) {
            Platform.runLater(() -> {
                terminalLabel.setVisible(false);
            });
        }

        squareManager.reset(board, visualise);
        scoreManager.reset(visualise);
    }

}