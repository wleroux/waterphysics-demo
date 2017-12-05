package fs.waterphysics

class World(val grid: Array<Cell>, val dimension: Dimension) {
  operator fun get(pos: Vector2i): Cell {
    return grid[dimension.index(pos.x, pos.y)]
  }

  fun waterPotential(pos: Vector2i): Int {
    return pos.y * (Cell.MAX_WATER_LEVEL + 1) + get(pos).waterLevel
  }

  val indices: List<Vector2i> get() {
    return (0 until dimension.width).flatMap { x ->
      (0 until dimension.height).map { y ->
        Vector2i(x, y)
      }
    }
  }

  fun has(pos: Vector2i): Boolean {
    return pos.x in (0 until dimension.width) && pos.y in (0 until dimension.height)
  }
}