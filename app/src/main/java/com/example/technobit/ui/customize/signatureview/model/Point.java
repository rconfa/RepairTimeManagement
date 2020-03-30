package com.example.technobit.ui.customize.signatureview.model;

public class Point {
    public final float x;
    public final float y;
    public final long time;

    public Point(float x, float y, long time) {
        this.x = x;
        this.y = y;
        this.time = time;
    }

    private float distanceTo(Point start) {
        return (float) (Math.sqrt(Math.pow((x - start.x), 2) + Math.pow((y - start.y), 2)));
    }

    public float velocityFrom(Point start) {
        return distanceTo(start) / (this.time - start.time);
    }
}
