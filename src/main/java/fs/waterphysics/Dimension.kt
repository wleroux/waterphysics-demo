package fs.waterphysics

data class Dimension(val width: Int, val height: Int) {
  fun index(x: Int, y: Int): Int {
    return y * width + x
  }
}
