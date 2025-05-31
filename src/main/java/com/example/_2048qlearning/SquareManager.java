package com.example._2048qlearning;

import javafx.application.Platform;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.Random;

public class SquareManager {

    private final GameController gameController;
    private final GridPane gridPane;
    private final Random random;

    public SquareManager(GameController gameController, GridPane gridPane, Random random) {
        this.gameController = gameController;
        this.gridPane = gridPane;
        this.random = random;
    }

    public void createStartSquares(Square[][] board, boolean visualise) {
        createRandomSquare(board, visualise);
        createRandomSquare(board, visualise);
    }

    public void createRandomSquare(Square[][] board, boolean visualise) {
         int value = getRandomSquareValue();
         int column ;
         int row;

         Pair<Integer, Integer> pair = gameController.getRandomEmptyPosition(board);
         column = pair.getKey();
         row = pair.getValue();

         createSquareAddToBoard(board, value, column, row, visualise);
    }

    public void createSquareAddToBoard(Square[][] board, int value, int column, int row, boolean visualise) {
        Square square = new Square(gameController, value, column, row, visualise);

        board[column][row] = square;

        if (visualise) {
            //Add to gridPane
            Platform.runLater(() -> {
                gridPane.getChildren().add(square);
                GridPane.setColumnIndex(square, square.getColumn());
                GridPane.setRowIndex(square, square.getRow());
            });
        }

    }


    public void destroySquare(Square[][] board, Square square) {
        board[square.getColumn()][square.getRow()] = null;

        if (square.isVisualise()) {
            Platform.runLater(() -> {
                gridPane.getChildren().remove(square);
            });
        }
    }

    public void mergeSquares(Square[][] board, Square sq1, Square sq2, boolean actual) {
        if (sq1 != null && sq2 != null) {
            if (sq1.getValue() == sq2.getValue()) {
                sq2.updateValue();
                destroySquare(board, sq1);
                if(actual) {
                    gameController.getScoreManager().addScore(sq2);
                }
                gameController.setValidMove(true);
            }
        }
    }

    private int getRandomSquareValue() {
        double value = random.nextDouble();
        if (value <= 0.9) {
            return 2;
        }
        return 4;
    }

    public void reset(Square[][] board, boolean visualise) {
        createStartSquares(board, visualise);
    }

}
