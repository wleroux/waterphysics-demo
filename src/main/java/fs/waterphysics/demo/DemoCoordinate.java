package fs.waterphysics.demo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FracturedSkies on 1/15/2017.
 */
public class DemoCoordinate {

    private final int cols;
    private final int rows;

    public DemoCoordinate(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
    }

    public int index(int x, int y) {
        return y * cols + x;
    }

    public int x(int index) {
        return index % cols;
    }

    public int y(int index) {
        return index / cols;
    }

    public int size() {
        return cols * rows;
    }

    public List<Integer> neighbours(int index) {
        List<Integer> neighbours = new ArrayList<>();
        int x = x(index), y = y(index);
        if (y + 1 < rows)
            neighbours.add(index + cols);
        if (x > 0)
            neighbours.add(index - 1);
        if (x + 1 < cols)
            neighbours.add(index + 1);
        if (y > 0)
            neighbours.add(index - cols);
        return neighbours;
    }
}
