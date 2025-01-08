package main.java;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Класс, реализующий алгоритм Дейкстры для поиска кратчайшего пути.
 */
public class PathfindingAlgorithm {
    public static final int INF = Integer.MAX_VALUE; // Константа для обозначения бесконечности
    private final MetroDataHandler dataHandler;

    /**
     * Конструктор, который инициализирует обработчик данных о метро.
     *
     * @param dataHandler обработчик данных о станциях метро
     */
    public PathfindingAlgorithm(MetroDataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    /**
     * Вычисляет кратчайший путь между станциями и возвращает маршрут и время в пути.
     *
     * @param startStation название начальной станции
     * @param endStation   название конечной станции
     * @return Пара: список станций на кратчайшем пути и время в пути
     */
    public Map.Entry<List<String>, Integer> calculateShortestPathWithDetails(String startStation, String endStation) {
        // Получаем индекс станций
        Map<String, Integer> stationIndexMap = dataHandler.getStationIndexMap();
        int start = stationIndexMap.getOrDefault(startStation, -1);
        int end = stationIndexMap.getOrDefault(endStation, -1);

        if (start == -1 || end == -1) {
            return new AbstractMap.SimpleEntry<>(List.of(), INF); // Станции не найдены
        }

        // Получаем матрицу смежности
        int[][] graph = dataHandler.getAdjacencyMatrix();
        int n = graph.length;
        int[] distances = new int[n];
        boolean[] visited = new boolean[n];
        int[] predecessors = new int[n];

        Arrays.fill(distances, INF);
        Arrays.fill(predecessors, -1);
        distances[start] = 0;

        // Очередь с приоритетом для работы с наименьшими расстояниями
        PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.comparingInt(a -> distances[a]));
        pq.add(start);

        // Алгоритм Дейкстры
        while (!pq.isEmpty()) {
            int current = pq.poll();
            if (visited[current]) continue;
            visited[current] = true;

            // Обрабатываем все соседние вершины
            for (int neighbor = 0; neighbor < n; neighbor++) {
                if (graph[current][neighbor] != INF && !visited[neighbor]) {
                    int newDist = distances[current] + graph[current][neighbor];
                    if (newDist < distances[neighbor]) {
                        distances[neighbor] = newDist;
                        predecessors[neighbor] = current;
                        pq.add(neighbor);
                    }
                }
            }
        }

        // Восстановление маршрута
        List<String> path = new ArrayList<>();
        if (distances[end] == INF) {
            return new AbstractMap.SimpleEntry<>(path, INF); // Путь недоступен
        }

        // Восстанавливаем путь, начиная с конечной станции
        for (int at = end; at != -1; at = predecessors[at]) {
            path.add(dataHandler.getStationNameByIndex(at)); // Добавляем станцию в путь
        }

        Collections.reverse(path); // Разворачиваем путь, так как мы восстанавливали его в обратном порядке

        return new AbstractMap.SimpleEntry<>(path, distances[end]);
    }

    /**
     * Вычисляет кратчайший путь между станциями.
     *
     * @param startStation название начальной станции
     * @param endStation   название конечной станции
     * @return кратчайшее время в минутах или INF, если путь невозможен
     */
    public int calculateShortestPath(String startStation, String endStation) {
        // Получаем индекс станций
        Map<String, Integer> stationIndexMap = dataHandler.getStationIndexMap();
        int start = stationIndexMap.getOrDefault(startStation, -1);
        int end = stationIndexMap.getOrDefault(endStation, -1);

        if (start == -1 || end == -1) {
            return INF; // Станции не найдены
        }

        // Получаем матрицу смежности
        int[][] graph = dataHandler.getAdjacencyMatrix();
        int n = graph.length;
        int[] distances = new int[n];
        boolean[] visited = new boolean[n];

        Arrays.fill(distances, INF);
        distances[start] = 0;

        // Очередь с приоритетом для работы с наименьшими расстояниями
        PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.comparingInt(a -> distances[a]));
        pq.add(start);

        // Алгоритм Дейкстры
        while (!pq.isEmpty()) {
            int current = pq.poll();
            if (visited[current]) continue;
            visited[current] = true;

            // Обрабатываем все соседние вершины
            for (int neighbor = 0; neighbor < n; neighbor++) {
                if (graph[current][neighbor] != INF && !visited[neighbor]) {
                    int newDist = distances[current] + graph[current][neighbor];
                    if (newDist < distances[neighbor]) {
                        distances[neighbor] = newDist;
                        pq.add(neighbor);
                    }
                }
            }
        }

        return distances[end]; // Возвращаем кратчайшее время
    }
}