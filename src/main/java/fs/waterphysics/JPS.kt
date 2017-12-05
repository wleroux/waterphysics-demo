package fs.waterphysics

class JPS(private val world: World) {
  fun successors(current: JumpNode, target: Int): List<JumpNode> {
    val successors = mutableListOf<JumpNode>()
    for (dir in current.dirs) {
      val jumpNode = jump(current.pos, dir, target)
      if (jumpNode != null) {
        successors.add(jumpNode)
      }
    }
    return successors
  }

  private fun jump(current: Vector2i, dir: Vector2i, target: Int): JumpNode? {
    val next = current + dir
    if (isBlocking(next)) return JumpNode(current, listOf())
    if (world.waterPotential(next) >= target) return JumpNode(next, listOf())

    when (dir) {
      Vector2i.AXIS_Y, Vector2i.AXIS_NEG_Y -> {
        if (!isBlocking(next + Vector2i.AXIS_X) || !isBlocking(next + Vector2i.AXIS_NEG_X))
          return JumpNode(next, Vector2i.X_PLANE + dir)
      }
      Vector2i.AXIS_X, Vector2i.AXIS_NEG_X -> {
        val blocking = blocking(current, next, Vector2i.Y_PLANE)
        if (blocking.isNotEmpty())
          return JumpNode(next, blocking + dir)
      }
    }

    return jump(next, dir, target)
  }

  private fun blocking(current: Vector2i, next: Vector2i, dirs: List<Vector2i>): List<Vector2i> {
    return dirs.filter { isBlocking(current + it) && !isBlocking(next + it) }
  }

  private fun isBlocking(pos: Vector2i)
      = (!world.has(pos) || world[pos].flow == 0)
}

data class JumpNode(val pos: Vector2i, val dirs: List<Vector2i>)