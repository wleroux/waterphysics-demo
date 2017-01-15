package fs.waterphysics;

import processing.core.PApplet;

import static java.awt.event.KeyEvent.*;

public class Main extends PApplet {
    private int xScale;
    private int yScale;

    public static void main(String[] args) {
        new Main().runSketch(args);
    }

    int gridCols = 48;
    int gridRows = 27;

    @Override
    public void settings() {
        fullScreen();
        xScale = displayWidth / gridCols;
        yScale = displayHeight / gridRows;
    }

    @Override
    public void setup() {
    }


    public void keyPressed() {
        switch (keyCode) {
            case VK_ESCAPE:
                exit();
                break;
        }
    }

    @Override
    public void draw() {
        render();
    }

    private void render() {
        for (int x = 0; x < gridCols; x ++) {
            for (int y = 0; y < gridRows; y++) {
                int screenX = x * xScale;
                int screenY = height - y * yScale;

                fill(255, 255, 255);
                rect(screenX, screenY, xScale, -yScale);
            }
        }
    }
}
