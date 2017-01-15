package fs.waterphysics;

import java.util.ArrayList;
import java.util.List;

public class Coordinate {

    private final int width;
    private final int height;

    public Coordinate(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int index(int x, int y) {
        return y * width + x;
    }

    public int x(int index) {
        return index % width;
    }

    public int y(int index) {
        return index / width;
    }

    public List<Integer> neighbours(int index) {
        int x = x(index), y = y(index);
        List<Integer> neighbours = new ArrayList<>();
        if (y + 1 < height)
            neighbours.add(index + width);
        if (x > 0)
            neighbours.add(index - 1);
        if (x + 1 < width)
            neighbours.add(index + 1);
        if (y > 0)
            neighbours.add(index - width);
        return neighbours;
    }
}
