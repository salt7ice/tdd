package com.example.core.domain;

public class Edge {
    private String destination;
    private int weight;
    private double speed;
    private String name;



    public Edge(String destination, int weight,int speed, String name) {
        this.destination = destination;
        this.weight = weight;
        this.name = name;
        this.speed=speed;
    }


    public String getDestination() {
        return destination;
    }

    public double getSpeed() {
        return speed;
    }

    public int getWeight() {
        return weight;
    }

    public String getName() {
        return name;
    }

    public boolean isAutoroute(){
        return name.contains("A");
    }
}