package fs.waterphysics.demo;

public class DemoCell {
    public static final int MAX_WATER_LEVEL = 16;
    boolean isBlocking = false;
    int waterLevel = 4;
    int outFlow = 0;

    public DemoCell(boolean isBlocking, int waterLevel) {
        this.isBlocking = isBlocking;
        this.waterLevel = waterLevel;
    }
}
