package fs.waterphysics.demo;

import processing.core.PApplet;

import java.awt.event.KeyEvent;
import java.util.*;

import static fs.waterphysics.demo.DemoCell.MAX_WATER_LEVEL;

public class Demo extends PApplet {
    enum Mode {
        REALTIME,
        RESOLUTION,
        STEP
    }

    private DemoCell[] grid;
    private int xScale;
    private int yScale;
    private int waterPotentialThreshhold;
    private Mode mode = Mode.REALTIME;

    public static void main(String[] args) {
        new Demo().runSketch(args);
    }

    int gridCols = 48;
    int gridRows = 27;
    DemoCoordinate coord = new DemoCoordinate(gridCols, gridRows);
    PriorityQueue<Integer> candidates = new PriorityQueue<>((a, b) ->
            waterPotential(a) - waterPotential(b)
    );


    @Override
    public void settings() {
        fullScreen();

        xScale = displayWidth / gridCols;
        yScale = displayHeight / gridRows;
    }

    @Override
    public void setup() {
        grid = new DemoCell[gridCols * gridRows];
        for (int i = 0; i < gridCols; i ++) {
            for (int j = 0; j < gridRows; j ++) {
                DemoCell cell = new DemoCell(false, 0);
                grid[coord.index(i, j)] = cell;
            }
        }

        resetLoop();
    }

    public void mouseDown(int button, int mouseX, int mouseY) {
        int x = (mouseX / xScale);
        int y = gridRows - 1 - (mouseY / yScale);
        DemoCell cell = grid[coord.index(x, y)];
        if (button == LEFT){
            cell.waterLevel = MAX_WATER_LEVEL;
        } else if (button == RIGHT) {
            cell.waterLevel = 0;
        } else if (button == CENTER) {
            cell.isBlocking = true;
        } else {
            cell.isBlocking = false;
        }
    }

    @Override
    public void keyPressed() {
        switch (keyCode) {
            case KeyEvent.VK_1:
                mode = Mode.REALTIME;
                break;
            case KeyEvent.VK_2:
                mode = Mode.RESOLUTION;
                break;
            case KeyEvent.VK_3:
                mode = Mode.STEP;
                break;
            case KeyEvent.VK_ESCAPE:
                exit();
                break;
        }
    }

    private void resetLoop() {
        // Water Physics
        candidates.clear();
        for (int i = 0; i < coord.size(); i ++) {
            if (!grid[i].isBlocking) {
                if (isWater(i) || isNeighbourWater(i)) {
                    candidates.add(i);
                }
            }
        }

        for (int i = 0; i < coord.size(); i ++) {
            grid[i].outFlow = grid[i].isBlocking ? 0 : grid[i].waterLevel;
        }
    }

    private boolean isNeighbourWater(int i) {
        for (int neighbour: coord.neighbours(i)) {
            if (isWater(neighbour))
                return true;
        }

        return false;
    }

    private boolean isWater(int i) {
        return grid[i].waterLevel > 0;
    }

    int targetIndex = -1;
    Queue<Integer> unvisitedQueue = new LinkedList<>();
    Map<Integer, Integer> cameFrom = new HashMap<>();


    @Override
    public void draw() {
        if (mousePressed) {
            mouseDown(mouseButton, mouseX, mouseY);
        }


        do {
            if (targetIndex == -1 && candidates.isEmpty()) {
                resetLoop();
                if (Mode.REALTIME.equals(mode)) {
                    break;
                }
            } else if (targetIndex == -1) {
                int possibleCandidate = candidates.poll();
                if (grid[possibleCandidate].waterLevel < MAX_WATER_LEVEL) {
                    targetIndex = possibleCandidate;
                    waterPotentialThreshhold = waterPotential(targetIndex) + 2;
                    cameFrom.clear();
                    unvisitedQueue.clear();
                    unvisitedQueue.add(targetIndex);
                    cameFrom.put(targetIndex, null);
                }
            } else if (unvisitedQueue.isEmpty()) {
                for (int puddle : cameFrom.keySet()) {
                    grid[puddle].outFlow = 0;
                }

                targetIndex = -1;
                if (Mode.RESOLUTION.equals(mode)) {
                    break;
                }
            } else {
                int sourceIndex = unvisitedQueue.poll();
                if (waterPotential(sourceIndex) >= waterPotentialThreshhold) {
                    // Found source!
                    grid[targetIndex].waterLevel += 1;
                    grid[sourceIndex].waterLevel -= 1;

                    int index = sourceIndex;
                    while (cameFrom.get(index) != null) {
                        grid[index].outFlow--;
                        index = cameFrom.get(index);
                    }

                    candidates.remove(sourceIndex);
                    candidates.add(sourceIndex);
                    candidates.add(targetIndex);

                    targetIndex = -1;
                    if (Mode.RESOLUTION.equals(mode)) {
                        break;
                    }
                } else {
                    // Find other source
                    for (int neighbour : coord.neighbours(sourceIndex)) {
                        if (grid[neighbour].outFlow > 0) {
                            if (!cameFrom.containsKey(neighbour)) {
                                unvisitedQueue.add(neighbour);
                                cameFrom.put(neighbour, sourceIndex);
                            }
                        }
                    }
                }
            }
        } while (Mode.REALTIME.equals(mode) || Mode.RESOLUTION.equals(mode));

        render();
    }

    private void render() {
        for (int i = 0; i < gridCols; i ++) {
            for (int j = 0; j < gridRows; j++) {
                int index = coord.index(i, j);
                if (cameFrom.containsKey(index)) {
                    fill(200, 255, 200);
                } else if (unvisitedQueue.contains(index)) {
                    fill(255, 200, 200);
                } else {
                    fill(255, 255, 255);
                }

                int screenX = i * xScale;
                int screenY = height - j * yScale;

                rect(screenX, screenY, xScale, -yScale);

                DemoCell cell = grid[coord.index(i, j)];
                if (cell.isBlocking) {
                    fill(0, 0, 0);
                    rect(screenX, screenY, xScale, -yScale);
                } else if (cell.waterLevel > 0) {
                    fill(0, 0, 200, 100);
                    float waterHeight = yScale * ((float) cell.waterLevel / (float) MAX_WATER_LEVEL);
                    rect(screenX, screenY, xScale, -waterHeight);
                }
            }
        }
    }

    private int waterPotential(int index) {
        return coord.y(index) * (MAX_WATER_LEVEL + 1 + 5) + grid[index].waterLevel;
    }
}
