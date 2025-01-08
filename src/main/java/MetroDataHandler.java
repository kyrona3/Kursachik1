package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс для обработки данных о станциях метро.
 */
public class MetroDataHandler {

    private final List<String> stations = new ArrayList<>();
    private final Map<String, Integer> stationIndexMap = new HashMap<>();
    private final Map<String, List<String>> lineStationsMap = new HashMap<>();
    private int[][] adjacencyMatrix;

    public static final int INF = Integer.MAX_VALUE; // Константа для обозначения бесконечности

    /**
     * Получает название станции по индексу.
     *
     * @param index индекс станции
     * @return название станции или null, если индекс неверный
     */
    public String getStationNameByIndex(int index) {
        if (index >= 0 && index < stations.size()) {
            return stations.get(index);
        } else {
            return null;  // Возвращаем null, если индекс неверный
        }
    }

    /**
     * Конструктор, который загружает данные о станциях из CSV-файла.
     *
     * @param resourcePath путь к ресурсу CSV-файла
     * @throws IOException если файл не найден или произошла ошибка чтения
     */
    public MetroDataHandler(String resourcePath) throws IOException {
        loadFromCSV(resourcePath);
    }

    /**
     * Получает карту станций по линиям метро.
     *
     * @return карта, где ключ - цвет линии, а значение - список станций
     */
    public Map<String, List<String>> getLineStations() {
        return lineStationsMap;
    }

    /**
     * Загружает данные о станциях и их соединениях из CSV-файла.
     *
     * @param resourcePath путь к ресурсу CSV-файла
     * @throws IOException если файл не найден или произошла ошибка чтения
     */
    private void loadFromCSV(String resourcePath) throws IOException {
        List<String[]> connections = new ArrayList<>();

        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Файл " + resourcePath + " не найден");
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("name")) continue;  // Пропускаем заголовки

                    String[] parts = line.split(",");
                    if (parts.length < 2) continue; // Пропускаем строки с неполными данными

                    String stationName = parts[0].trim();
                    String lineColor = parts[1].trim();

                    lineStationsMap.putIfAbsent(lineColor, new ArrayList<>());
                    lineStationsMap.get(lineColor).add(stationName);

                    if (!stationIndexMap.containsKey(stationName)) {
                        stations.add(stationName);
                        stationIndexMap.put(stationName, stations.size() - 1);
                    }

                    if (parts.length > 2) {
                        String[] stationConnections = parts[2].split(";");
                        for (String connection : stationConnections) {
                            connection = connection.trim();
                            if (connection.isEmpty()) continue;

                            try {
                                String[] connectionData = connection.split(":");
                                String toStation = connectionData[0].trim();
                                int time = Integer.parseInt(connectionData[1].trim());

                                connections.add(new String[]{stationName, toStation, String.valueOf(time)});
                            } catch (NumberFormatException e) {
                                System.err.println("Ошибка при разборе соединения: " + connection);
                            }
                        }
                    }
                }
            }
        }

        // Инициализация матрицы смежности
        int size = stations.size();
        adjacencyMatrix = new int[size][size];
        for (int[] row : adjacencyMatrix) {
            Arrays.fill(row, INF);
        }
        for (int i = 0; i < size; i++) {
            adjacencyMatrix[i][i] = 0; // Стоимость самоподключений = 0
        }

        // Заполнение матрицы смежности
        for (String[] connection : connections) {
            String fromStation = connection[0];
            String toStation = connection[1];
            int time = Integer.parseInt(connection[2]);

            int fromIndex = stationIndexMap.get(fromStation);
            int toIndex = stationIndexMap.get(toStation);

            adjacencyMatrix[fromIndex][toIndex] = time;
            adjacencyMatrix[toIndex][fromIndex] = time;  // Путь двусторонний
        }
    }

    /**
     * Получает матрицу смежности для станций метро.
     *
     * @return матрица смежности
     */
    public int[][] getAdjacencyMatrix() {
        return adjacencyMatrix;
    }

    /**
     * Получает карту индексов станций.
     *
     * @return карта, где ключ - название станции, а значение - индекс
     */
    public Map<String, Integer> getStationIndexMap() {
        return stationIndexMap;
    }
}