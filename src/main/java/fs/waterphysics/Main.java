package fs.waterphysics;

import processing.core.PApplet;

import java.util.*;

import static fs.waterphysics.Cell.MAX_WATER_LEVEL;
import static java.awt.event.KeyEvent.*;

public class Main extends PApplet {
    private int xScale;
    private int yScale;
    private PriorityQueue<Integer> candidates = new PriorityQueue<>((a,b) -> waterPotential(a) - waterPotential(b));
    private int waterPotentialThreshold;

    enum Mode {
        REALTIME,
        RESOLUTION,
        STEP
    }

    private Mode mode = Mode.STEP;

    public static void main(String[] args) {
        new Main().runSketch(args);
    }

    int gridCols = 48;
    int gridRows = 27;

    Cell[] grid;
    Coordinate coord = new Coordinate(gridCols, gridRows);

    @Override
    public void settings() {
        fullScreen();
        xScale = displayWidth / gridCols;
        yScale = displayHeight / gridRows;
    }

    @Override
    public void setup() {
        grid = new Cell[gridCols * gridRows];
        for (int i = 0; i < grid.length; i ++) {
            grid[i] = new Cell();
        }
    }


    public void keyPressed() {
        switch (keyCode) {
            case VK_ESCAPE:
                exit();
                break;
            case VK_1:
                mode = Mode.REALTIME;
                break;
            case VK_2:
                mode = Mode.RESOLUTION;
                break;
            case VK_3:
                mode = Mode.STEP;
                break;
        }
    }

    @Override
    public void draw() {
        mouseControls();
        
        waterPhysics();

        render();
    }

    int targetIndex = -1;
    Queue<Integer> unvisitedCells = new LinkedList<>();
    Map<Integer, Integer> cameFrom = new HashMap<>();


    private void waterPhysics() {
        do {
            if (targetIndex == -1 && candidates.isEmpty()) {
                calculateCandidates();
                if (Mode.REALTIME.equals(mode)) {
                    break;
                }
            } else if (targetIndex == -1 && !candidates.isEmpty()) {
                int potentialIndex = candidates.poll();
                if (grid[potentialIndex].waterLevel != MAX_WATER_LEVEL) {
                    targetIndex = potentialIndex;
                    cameFrom.put(targetIndex, null);
                    waterPotentialThreshold = waterPotential(targetIndex) + 2;

                    unvisitedCells.clear();
                    unvisitedCells.add(targetIndex);
                }
            } else if (unvisitedCells.isEmpty()){
                for (int index: cameFrom.keySet()) {
                    grid[index].flow = 0;
                }

                cameFrom.clear();
                unvisitedCells.clear();
                targetIndex = -1;

                if (Mode.RESOLUTION.equals(mode)) {
                    break;
                }
            } else {
                int sourceIndex = unvisitedCells.poll();
                if (waterPotential(sourceIndex) >= waterPotentialThreshold) {
                    // Found a source!
                    grid[sourceIndex].waterLevel --;
                    grid[targetIndex].waterLevel ++;

                    int temp = sourceIndex;
                    while (cameFrom.get(temp) != null) {
                        grid[temp].flow --;
                        temp = cameFrom.get(temp);
                    }

                    candidates.remove(sourceIndex);
                    candidates.add(sourceIndex);
                    candidates.add(targetIndex);

                    cameFrom.clear();
                    unvisitedCells.clear();
                    targetIndex = -1;

                    if (Mode.RESOLUTION.equals(mode)) {
                        break;
                    }
                } else {
                    for (int neighbour: coord.neighbours(sourceIndex)) {
                        if (!cameFrom.containsKey(neighbour)) {
                            if (grid[neighbour].flow > 0) {
                                cameFrom.put(neighbour, sourceIndex);
                                unvisitedCells.add(neighbour);
                            }
                        }
                    }
                }
            }

            // Show every step
            if (Mode.STEP.equals(mode)) {
                break;
            }
        } while (true);
    }

    private boolean isWater(int index) {
        return grid[index].waterLevel > 0;
    }

    private int waterPotential(int index) {
        return coord.y(index) * (MAX_WATER_LEVEL + 1) + grid[index].waterLevel;
    }

    private void calculateCandidates() {
        candidates.clear();
        for (int i = 0; i < grid.length; i ++) {
            if (! grid[i].isBlocking) {
                if (isWater(i) || isNeighbourWater(i))
                    candidates.add(i);
            }
            grid[i].flow = grid[i].waterLevel;
        }
    }

    private boolean isNeighbourWater(int index) {
        for (int neighbour: coord.neighbours(index)) {
            if (isWater(neighbour)) {
                return true;
            }
        }

        return false;
    }

    private void mouseControls() {
        if (mousePressed) {
            int x = mouseX / xScale;
            int y = (displayHeight - mouseY) / yScale;
            int index = coord.index(x, y);

            if (mouseButton == LEFT) {
                if (!grid[index].isBlocking) {
                    grid[index].waterLevel = MAX_WATER_LEVEL;
                    grid[index].flow = MAX_WATER_LEVEL;
                }
            } else if (mouseButton == RIGHT) {
                if (!grid[index].isBlocking) {
                    grid[index].waterLevel = 0;
                    grid[index].flow = 0;
                }
            } else if (mouseButton == CENTER) {
                grid[index].isBlocking = true;
                grid[index].waterLevel = 0;
                grid[index].flow = 0;
            } else {
                grid[index].isBlocking = false;
            }
        }
    }

    private void render() {
        for (int x = 0; x < gridCols; x ++) {
            for (int y = 0; y < gridRows; y++) {
                int screenX = x * xScale;
                int screenY = height - y * yScale;
                int index = coord.index(x, y);

                if (!grid[index].isBlocking) {
                    if (index == targetIndex) {
                        fill(255, 0, 0);
                        rect(screenX, screenY, xScale, -yScale);
                    } else if (unvisitedCells.contains(index)) {
                        fill(100, 0, 0);
                        rect(screenX, screenY, xScale, -yScale);
                    } else if (cameFrom.containsKey(index)) {
                        fill(200, 0, 0);
                        rect(screenX, screenY, xScale, -yScale);
                    } else {
                        fill(255, 255, 255);
                        rect(screenX, screenY, xScale, -yScale);
                    }

                    if (grid[index].waterLevel > 0) {
                        float waterHeight = ((float) grid[index].waterLevel / (float) MAX_WATER_LEVEL) * yScale;
                        fill(0, 0, 200, 100);
                        rect(screenX, screenY, xScale, -waterHeight);

                        float flowHeight = ((float) grid[index].flow / (float) MAX_WATER_LEVEL) * yScale;
                        fill(0, 0, 255);
                        rect(screenX + xScale * (7f/8f), screenY, xScale * (1f / 8f), -flowHeight);
                    }
                } else {
                    fill(0, 0, 0);
                    rect(screenX, screenY, xScale, -yScale);
                }
            }
        }
    }
}
