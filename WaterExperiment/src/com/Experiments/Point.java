package com.Experiments;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by arash on 10/04/14.
 */
public class Point {
    public int x;
    public int y;
    public Vector2 speed;
    public float mass;

    public Point(int x, int y, Vector2 speed, float mass) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.mass = mass;
    }
}
