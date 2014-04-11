package com.Experiments;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.sun.prism.Texture;

import static com.badlogic.gdx.math.MathUtils.PI2;
import static com.badlogic.gdx.math.MathUtils.PI;
/**
 * Created by arash on 10/04/14.
 */
public class WaterSpringSimulation {
    private static int NUM_POINTS = 80;
    private static int WIDTH = 600;
    private static float SPRING_CONSTANT= 0.005f;
    private static float SPRING_CONSTANT_BASELINE = 0.005f;

    private static int Y_OFFSET = 200;
    private static float DAMPING = 0.99f;
    private static int ITERATION = 5;
    private float offset;

    private Point[] points;

    private int NUM_BACKGROUND_WAVES = 7;
    private static int BACKGROUND_WAVE_MAX_HEIGHT = 6;
    private float BACKGROUND_WAVE_COMPRESSION = 0.1f;
    // Amounts by which a particular sine is offset
    private float[] sineOffsets;
    // Amounts by which a particular sine is amplified
    private float[] sineAmplitudes;
    // Amounts by which a particular sine is stretched
    private float[] sineStretches;
    // Amounts by which a particular sine's offset is multiplied
    private float[] offsetStretches;
    // Set each sine's values to a reasonable random value
    private String tableContent = "";

    public WaterSpringSimulation() {
        sineOffsets = new float[NUM_BACKGROUND_WAVES];
        sineAmplitudes = new float[NUM_BACKGROUND_WAVES];
        sineStretches = new float[NUM_BACKGROUND_WAVES];
        offsetStretches = new float[NUM_BACKGROUND_WAVES];

        for (int i = 0; i < NUM_BACKGROUND_WAVES; i++) {
            sineOffsets[i] = PI + PI2 * (float)Math.random();
            sineAmplitudes[i] = (float) Math.random() * BACKGROUND_WAVE_MAX_HEIGHT;
            sineStretches[i] = (float) Math.random() * BACKGROUND_WAVE_COMPRESSION;
            offsetStretches[i] = (float) Math.random() * BACKGROUND_WAVE_COMPRESSION;

        }
        makeWavePoints();
    }

    private Mesh createMesh(){
        float[] verts = new float[NUM_POINTS * 3];
        for (int i = 0; i < points.length; i++) {
            verts[i*3 +1] = points[i].x;
            verts[i*3 +1] = points[i].y;
            verts[i*3 +2] = 0;
        }
        Mesh mesh = new Mesh(false, points.length,0, new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
        mesh.setVertices(verts);
        return mesh;
    }

    private void makeWavePoints (){
        points = new Point[NUM_POINTS];
        for (int i = 0; i < NUM_POINTS; i++) {
            points[i] = new Point(i/NUM_POINTS * WIDTH, Y_OFFSET,new Vector2(0,0),1f);
        }
    }

    private void updateWavePoints(float dt){
        for (int i = 0; i < ITERATION; i++) {
            for (int j = 0; j < points.length; j++) {
                Point p = points[j];
                float force = 0;
                float dy;
                float forceFromLeft;
                float forceFromRight;
                float forceToBaseLine;

                if(j ==0){ //wrap to left to right
                    dy = points[points.length -1 ].y - p.y;
                    forceFromLeft = SPRING_CONSTANT * dy;
                } else {
                    dy = points[j -1 ].y - p.y;
                    forceFromLeft = SPRING_CONSTANT * dy;
                }
                if( j == points.length -1 ){ //wrap to right-to-left
                    dy = points[0].y - p.y;
                    forceFromRight = SPRING_CONSTANT * dy;
                } else {
                    dy = points[j + 1].y - p.y;
                    forceFromRight = SPRING_CONSTANT * dy;
                }
                dy = Y_OFFSET - p.y;
                forceToBaseLine = SPRING_CONSTANT_BASELINE * dy;
                force = forceFromLeft + forceFromRight + forceToBaseLine;
                float acceleration = force / p.mass;
                p.speed.y += p.y;
            }
        }
    }

    private void update(float dt){
        offset+=dt;
        updateWavePoints(dt);
    }



}
