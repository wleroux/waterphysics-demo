package fs.waterphysics

data class Vector2i(val x: Int, val y: Int) {
  companion object {
    val AXIS_X = Vector2i(1, 0)
    val AXIS_NEG_X = Vector2i(-1, 0)
    val AXIS_Y = Vector2i(0, 1)
    val AXIS_NEG_Y = Vector2i(0, -1)
    val NEIGHBORS = listOf(AXIS_X, AXIS_NEG_X, AXIS_Y, AXIS_NEG_Y)
    val X_PLANE = listOf(AXIS_X, AXIS_NEG_X)
    val Y_PLANE = listOf(AXIS_Y, AXIS_NEG_Y)
  }

  operator fun plus(d: Vector2i) = Vector2i(x + d.x, y + d.y)
  operator fun minus(d: Vector2i) = Vector2i(x - d.x, y - d.y)
}