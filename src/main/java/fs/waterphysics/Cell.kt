package fs.waterphysics

data class Cell(var waterLevel: Int = 0, var isBlocking: Boolean = false, var flow: Int = 0) {
  companion object {
    val MAX_WATER_LEVEL = 4
  }
}
