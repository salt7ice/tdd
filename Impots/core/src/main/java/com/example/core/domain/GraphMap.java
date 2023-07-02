package com.example.core.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;

public class GraphMap {


    private Set<String> nodes;
    private Map<String, List<Edge>> adjacencyList;
    private final int INFINITY = Integer.MAX_VALUE;
    private String endNode;
    private String startNode;

    public GraphMap() {
        nodes = new HashSet<>();
        adjacencyList = new HashMap<>();
    }

    public void addEdge(String source, String destination, int weight, int speed, String name) {
        nodes.add(source);
        nodes.add(destination);

        adjacencyList.computeIfAbsent(source, k -> new ArrayList<>()).add(new Edge(destination, weight, speed, name));
        adjacencyList.computeIfAbsent(destination, k -> new ArrayList<>()).add(new Edge(source, weight, speed, name));
    }



    public List<Edge> getNeighbors(String node) {
        return adjacencyList.getOrDefault(node, new ArrayList<>());
    }

    public Set<String> getNodes() {
        return nodes;
    }

    public Integer getNumNodes() {
        return nodes.size();
    }





    public List<String> getShortestTrajectory(boolean sansAutoroute) {
        Map<String, Integer> distances = new HashMap<>();
        Map<String, Boolean> visited = new HashMap<>();
        Map<String, String> previousNode = new HashMap<>();
        Map<String, String> previousRoad = new HashMap<>();

        for (String node : this.getNodes()) {
            distances.put(node, INFINITY);
            visited.put(node, false);
        }
        distances.put(this.startNode, 0);

        for (int i = 0; i < this.getNumNodes() - 1; i++) {
            String currentNode = findMinDistance(distances, visited);
            visited.put(currentNode, true);

            List<Edge> neighbors = this.getNeighbors(currentNode);

            for (Edge edge : neighbors) {
                if (!edge.isAutoroute() || !sansAutoroute) {
                    String neighborNode = edge.getDestination();
                    int weight = edge.getWeight();
                    int newDistance = distances.get(currentNode) + weight;

                    if (!visited.get(neighborNode) && newDistance < distances.get(neighborNode)) {
                        distances.put(neighborNode, newDistance);
                        previousNode.put(neighborNode,currentNode);
                        previousRoad.put(neighborNode,edge.getName());
                    }
                }
            }
        }
        List<String> result = new ArrayList<>();
        while (!endNode.equals(startNode)){
            result.add(0,endNode);
            result.add(0,previousRoad.get(endNode));
            endNode = previousNode.get(endNode);
        }
        result.add(0,startNode);
        return result;
    }

    public List<String> getFastestPath(boolean sansAutoroutes) {
        Map<String, Double> distances = new HashMap<>();
        Map<String, Boolean> visited = new HashMap<>();
        Map<String, String> previousNode = new HashMap<>();
        Map<String, String> previousRoad = new HashMap<>();

        for (String node : this.getNodes()) {
            distances.put(node, Double.MAX_VALUE);
            visited.put(node, false);
        }
        distances.put(this.startNode, 0.);

        for (int i = 0; i < this.getNumNodes() - 1; i++) {
            String currentNode = findMinTime(distances, visited);
            visited.put(currentNode, true);

            List<Edge> neighbors = this.getNeighbors(currentNode);

            for (Edge edge : neighbors) {
                if (!edge.isAutoroute() || !sansAutoroutes) {
                    String neighborNode = edge.getDestination();

                    double time = (edge.getWeight() * 60) / edge.getSpeed();
                    BigDecimal newDistance = BigDecimal.valueOf(distances.get(currentNode) + time);
                    newDistance = newDistance.setScale(1, RoundingMode.HALF_UP);

                    if (!visited.get(neighborNode) && newDistance.doubleValue() < distances.get(neighborNode)) {
                        distances.put(neighborNode, newDistance.doubleValue());
                        previousNode.put(neighborNode,currentNode);
                        previousRoad.put(neighborNode,edge.getName());
                    }

                }
            }
        }
        List<String> result = new ArrayList<>();
        while (!endNode.equals(startNode)){
            result.add(0,endNode);
            result.add(0,previousRoad.get(endNode));
            endNode = previousNode.get(endNode);
        }
        result.add(0,startNode);
        //Collections.reverse(result);
        return result;
    }





    private String findMinTime(Map<String, Double> distances, Map<String, Boolean> visited) {
        Double minDistance = Double.MAX_VALUE;
        String minNode = null;

        for (Map.Entry<String, Double> entry : distances.entrySet()) {
            String node = entry.getKey();
            Double distance = entry.getValue();

            if (!visited.get(node) && distance < minDistance) {
                minDistance = distance;
                minNode = node;
            }
        }
        return minNode;
    }

    private String findMinDistance(Map<String, Integer> distances, Map<String, Boolean> visited) {
        int minDistance = INFINITY;
        String minNode = null;

        for (Map.Entry<String, Integer> entry : distances.entrySet()) {
            String node = entry.getKey();
            int distance = entry.getValue();

            if (!visited.get(node) && distance < minDistance) {
                minDistance = distance;
                minNode = node;
            }
        }
        return minNode;
    }


    public void setStart(String startNode) {
        this.startNode = startNode;
    }

    public void setEnd(String endNode) {
        this.endNode = endNode;
    }


}