module com.example._2048qlearning {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example._2048qlearning to javafx.fxml;
    exports com.example._2048qlearning;
}