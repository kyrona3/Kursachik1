package main.java;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Главный класс приложения для расчета кратчайшего пути в метро.
 */
public class Metro extends Application {

    private static final Logger logger = LogManager.getLogger(Metro.class);
    private MetroDataHandler dataHandler;
    private PathfindingAlgorithm pathfindingAlgorithm;

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Метод, который запускает приложение и создает пользовательский интерфейс.
     *
     * @param primaryStage основная стадия приложения
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Кратчайший путь в метро СПб");
        logger.info("Запуск приложения...");

        // Загрузка данных о станциях
        try {
            dataHandler = new MetroDataHandler("/stations.csv");
        } catch (IOException e) {
            logger.error("Ошибка загрузки данных о станциях: " + e.getMessage(), e);
            return;
        }
        pathfindingAlgorithm = new PathfindingAlgorithm(dataHandler);

        // Основной макет
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // Сетка для отображения карты и станций
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));
        root.setCenter(grid);

        // Отображение карты метро
        try (InputStream imageStream = getClass().getResourceAsStream("/metro.png")) {
            if (imageStream == null) {
                throw new IOException("Файл metro.png не найден");
            }
            Image metroMap = new Image(imageStream);
            ImageView imageView = new ImageView(metroMap);
            imageView.setFitWidth(800);
            imageView.setPreserveRatio(true);
            grid.add(imageView, 0, 0, 4, 1);
        } catch (IOException e) {
            logger.error("Ошибка загрузки изображения: " + e.getMessage(), e);
        }

        // Выпадающие списки для выбора станций
        ComboBox<String> startBox = new ComboBox<>();
        ComboBox<String> endBox = new ComboBox<>();

        // Кнопки для управления
        Button calculateButton = new Button("Рассчитать");
        Button exitButton = new Button("Выход");
        Label resultLabel = new Label("Кратчайший путь: ");

        // Обновление ComboBox для выбора станций по линии
        VBox stationSelectionBox = new VBox(10);
        stationSelectionBox.setPadding(new Insets(10));
        stationSelectionBox.setAlignment(Pos.CENTER);

        dataHandler.getLineStations().forEach((lineColor, stations) -> {
            Button startButton = new Button("Начальная станция (" + lineColor + ")");
            Button endButton = new Button("Конечная станция (" + lineColor + ")");
            startButton.setOnAction(event -> updateComboBox(startBox, lineColor));
            endButton.setOnAction(event -> updateComboBox(endBox, lineColor));
            HBox buttonRow = new HBox(10, startButton, endButton);
            buttonRow.setAlignment(Pos.CENTER);
            stationSelectionBox.getChildren().add(buttonRow);
        });

        grid.add(stationSelectionBox, 0, 1, 4, 1);

        // Логика обработки кнопки "Рассчитать"
        calculateButton.setOnAction(event -> {
            try {
                String startStation = startBox.getSelectionModel().getSelectedItem();
                String endStation = endBox.getSelectionModel().getSelectedItem();

                if (startStation == null || endStation == null) {
                    resultLabel.setText("Пожалуйста, выберите начальную и конечную станции.");
                    return;
                }

                // Получаем кратчайший путь и время
                var result = pathfindingAlgorithm.calculateShortestPathWithDetails(startStation, endStation);

                List<String> path = result.getKey(); // Кратчайший маршрут
                int shortestTime = result.getValue(); // Время в пути

                if (shortestTime == PathfindingAlgorithm.INF) {
                    resultLabel.setText("Путь недоступен.");
                    logger.warn("Путь недоступен между станциями: {} и {}", startStation, endStation);
                } else {
                    String pathStr = String.join(" -> ", path);
                    resultLabel.setText("Кратчайшее время: " + shortestTime + " минут.");
                    logger.info("Кратчайший путь рассчитан: {}. Время в пути: {} минут.", pathStr, shortestTime);
                }
            } catch (Exception e) {
                resultLabel.setText("Ошибка при расчете пути: " + e.getMessage());
                logger.error("Ошибка при расчете пути: " + e.getMessage(), e);
            }
        });

        // Логика кнопки "Выход"
        exitButton.setOnAction(event -> {
            logger.info("Приложение закрыто пользователем."); // Логирование выхода
            primaryStage.close();
        });

        // Нижний контейнер для кнопок и результата
        HBox bottomBox = new HBox(15, calculateButton, exitButton, resultLabel);
        bottomBox.setPadding(new Insets(10));
        bottomBox.setAlignment(Pos.CENTER);
        root.setBottom(bottomBox);

        // Левая панель для выпадающих списков
        VBox leftBox = new VBox(15, new Label("Начальная станция:"), startBox, new Label("Конечная станция:"), endBox);
        leftBox.setPadding(new Insets(10));
        leftBox.setAlignment(Pos.CENTER_LEFT);
        root.setLeft(leftBox);

        // Настройка и отображение сцены
        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Обновляет содержимое ComboBox для выбранной линии метро.
     *
     * @param box      ComboBox для обновления
     * @param lineColor Цвет линии метро
     */
    private void updateComboBox(ComboBox<String> box, String lineColor) {
        List<String> stationsForLine = dataHandler.getLineStations().getOrDefault(lineColor, List.of());
        box.getItems().setAll(stationsForLine);
    }
}