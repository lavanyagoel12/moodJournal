package com.example.nu_mad_sp2023_final_project_10;

public class Mood {

    private int color;
    private String name;
    private int type;

    public Mood() {}

    public Mood(int color, String name, int type) {
        this.color = color;
        this.name = name;
        this.type = type;
    }

    public int getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    // -1 -> negative
    // 0 -> neutral
    // 1 -> positive
    public int getType() { return type; }
}
