package com.example._2048qlearning;

import javafx.application.Platform;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import static javafx.scene.paint.Color.WHITE;

public class Square extends StackPane {

    private GameController gameController;
    private int value;
    private int column;
    private int row;

    private Rectangle rectangle;
    private Text text;
    private boolean visualise;

    public Square(GameController gameController, int value, int column, int row, boolean visualise) {
        this.gameController = gameController;
        this.value = value;
        this.column = column;
        this.row = row;
        this.visualise = visualise;

        if(visualise) {
            Platform.runLater(() -> {
                this.rectangle = new Rectangle(98, 98, getColorFromValue());
                this.text = new Text(String.valueOf(value));
                text.setFont(new Font(40));
                this.getChildren().addAll(rectangle, text);
            });
        } else {
            this.rectangle = null;
            this.text = null;
        }
    }

    public void changePosition(Square[][] board, int newCol, int newRow) {
        //Changing board position
        board[column][row] = null;

        this.column = newCol;
        this.row = newRow;
        board[column][row] = this;

        if (visualise) {
            Platform.runLater(() -> {
                GridPane.setColumnIndex(this, column);
                GridPane.setRowIndex(this, row);
            });
        }
    }

    public void updateValue() {
        this.value += value;

        if(visualise) {
            Platform.runLater(() -> {
                this.text.setText(String.valueOf(value));
                this.rectangle.setFill(getColorFromValue());
            });
        }
    }

    private Paint getColorFromValue() {
        return switch (value) {
            case 2 -> Color.web("#FFCDD2");   // Light pink
            case 4 -> Color.web("#FFAB91");   // Light coral
            case 8 -> Color.web("#FF8A65");   // Light orange
            case 16 -> Color.web("#FF7043");  // Orange
            case 32 -> Color.web("#FF5722");  // Bright red-orange
            case 64 -> Color.web("#F57F17");  // Golden yellow
            case 128 -> Color.web("#F4B400"); // Yellow
            case 256 -> Color.web("#C6FF00"); // Lime green
            case 512 -> Color.web("#69F0AE"); // Turquoise
            case 1024 -> Color.web("#40C4FF");// Sky blue
            case 2048 -> Color.web("#7C4DFF");// Purple
            default -> WHITE;
        };
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public int getValue() {
        return value;
    }

    public boolean isVisualise() {
        return visualise;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
