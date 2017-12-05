package fs.waterphysics

import processing.core.PApplet

import java.util.*

import fs.waterphysics.Cell.Companion.MAX_WATER_LEVEL
import fs.waterphysics.Vector2i.Companion.AXIS_NEG_X
import fs.waterphysics.Vector2i.Companion.AXIS_NEG_Y
import fs.waterphysics.Vector2i.Companion.AXIS_X
import fs.waterphysics.Vector2i.Companion.AXIS_Y
import processing.core.PConstants
import java.awt.event.KeyEvent.*
import java.util.function.ToIntFunction

class Main : PApplet() {
  companion object {
    private var gridCols = 48
    private var gridRows = 27
  }
  private val comparator = Comparator.comparingInt(ToIntFunction<Vector2i> { world.waterPotential(it) })

  private var xScale: Int = 0
  private var yScale: Int = 0

  private var mode = Mode.STEP

  private val world = World(Array(gridCols * gridRows, {Cell()}), Dimension(gridCols, gridRows))
  private val jps = JPS(world)

  private val candidates = PriorityQueue(comparator)
  private var waterPotentialThreshold: Int = 0
  private var targetPos: Vector2i? = null
  private var visitedCells: MutableSet<JumpNode> = mutableSetOf()
  private var unvisitedCells: Queue<JumpNode> = PriorityQueue(Comparator.comparingInt({ node:JumpNode -> world.waterPotential(node.pos)}).reversed())
  private var cameFrom: MutableMap<Vector2i, Vector2i?> = HashMap()

  private var mouseMode = MouseMode.SELECT

  internal enum class Mode {
    REALTIME,
    RESOLUTION,
    STEP
  }

  override fun settings() {
    fullScreen()
    xScale = displayWidth / gridCols
    yScale = displayHeight / gridRows
  }

  override fun keyPressed() {
    when (keyCode) {
      VK_ESCAPE -> exit()
      VK_1 -> mode = Mode.REALTIME
      VK_2 -> mode = Mode.RESOLUTION
      VK_3 -> mode = Mode.STEP
    }
  }

  override fun draw() {
    mouseControls()

    waterPhysics()

    render()
  }


  private fun waterPhysics() {
    do {
      if (targetPos == null && candidates.isEmpty()) {
        calculateCandidates()
        if (Mode.REALTIME == mode) {
          break
        }
      } else if (targetPos == null && !candidates.isEmpty()) {
        while (candidates.isNotEmpty()) {
          val potentialIndex = candidates.poll()
          if (world[potentialIndex].waterLevel != MAX_WATER_LEVEL) {
            targetPos = potentialIndex
            cameFrom.put(targetPos!!, null)
            waterPotentialThreshold = world.waterPotential(targetPos!!) + 2

            visitedCells.clear()
            unvisitedCells.clear()
            unvisitedCells.add(JumpNode(targetPos!!, Vector2i.NEIGHBORS))
            break
          }
        }
      } else if (unvisitedCells.isEmpty()) {
        for (entry in cameFrom) {
          val to = entry.key
          var from = entry.value
          if (from != null) {
            val dir = when {
              to.x > from.x -> AXIS_X
              to.x < from.x -> AXIS_NEG_X
              to.y > from.y -> AXIS_Y
              to.y < from.y -> AXIS_NEG_Y
              else -> throw IllegalArgumentException()
            }

            while (from != to) {
              world[from].flow = 0
              from += dir
            }
          }

          world[to].flow = 0
        }

        cameFrom.clear()
        unvisitedCells.clear()
        targetPos = null

        if (Mode.RESOLUTION == mode) {
          break
        }
      } else {
        val sourceNode = unvisitedCells.poll()
        visitedCells.add(sourceNode)
        val sourcePos = sourceNode.pos
        if (world.waterPotential(sourcePos) >= waterPotentialThreshold) {
          // Found a source!
          val sourceWaterLevel = world[sourcePos].waterLevel - 1
          val targetWaterLevel = world[targetPos!!].waterLevel + 1

          world[sourcePos].waterLevel = sourceWaterLevel
          world[targetPos!!].waterLevel = targetWaterLevel

          var nextNode = sourcePos
          while (cameFrom[nextNode] != null) {
            val prevNode = cameFrom[nextNode]!!
            world[nextNode].flow--
            nextNode = prevNode
          }

          candidates.remove(sourcePos)
          candidates.add(sourcePos)

          if (targetWaterLevel != MAX_WATER_LEVEL)
            candidates.add(targetPos)

          cameFrom.clear()
          unvisitedCells.clear()
          targetPos = null

          if (Mode.RESOLUTION == mode) {
            break
          }
        } else {
          for (successor in jps.successors(sourceNode, waterPotentialThreshold)) {
            if (!cameFrom.containsKey(successor.pos)) {
              cameFrom[successor.pos] = sourceNode.pos
              unvisitedCells.add(successor)
            }
          }
        }
      }

      // Show every step
      if (Mode.STEP == mode) {
        break
      }
    } while (true)
  }

  private fun isWater(index: Vector2i): Boolean {
    return world[index].waterLevel > 0
  }

  private fun calculateCandidates() {
    candidates.clear()
    for (index in world.indices) {
      if (!world[index].isBlocking) {
        if (isWater(index) || isNeighbourWater(index))
          candidates.add(index)
      }
      world[index].flow = world[index].waterLevel
    }
  }

  private fun isNeighbourWater(pos: Vector2i): Boolean {
    return Vector2i.NEIGHBORS.filter { world.has(pos + it) }.any { isWater(pos + it) }
  }

  internal enum class MouseMode {
    SELECT,
    ADD,
    REMOVE
  }

  private fun mouseControls() {
    if (mousePressed) {
      val x = mouseX / xScale
      val y = (displayHeight - mouseY) / yScale
      val index = Vector2i(x, y)
      if (!world.has(index)) {
        return
      }

      if (mouseButton == PConstants.RIGHT) {
        if (mouseMode == MouseMode.SELECT) {
          mouseMode = if (world[index].waterLevel > 0) MouseMode.REMOVE else MouseMode.ADD
        }

        if (!world[index].isBlocking) {
          when (mouseMode) {
            Main.MouseMode.ADD -> {
              world[index].waterLevel = MAX_WATER_LEVEL
              world[index].flow = MAX_WATER_LEVEL
            }
            Main.MouseMode.REMOVE -> {
              world[index].waterLevel = 0
              world[index].flow = 0
            }
            else -> {}}
        }
      } else if (mouseButton == PConstants.LEFT) {
        if (mouseMode == MouseMode.SELECT) {
          mouseMode = if (world[index].isBlocking) MouseMode.REMOVE else MouseMode.ADD
        }
        when (mouseMode) {
          Main.MouseMode.ADD -> {
            world[index].isBlocking = true
            world[index].waterLevel = 0
            world[index].flow = 0
          }
          Main.MouseMode.REMOVE -> {
            world[index].isBlocking = false
          }
          else -> { }
        }
      }
    } else {
      mouseMode = MouseMode.SELECT
    }
  }

  private fun render() {
    for (x in 0 until gridCols) {
      for (y in 0 until gridRows) {
        val screenX = x * xScale
        val screenY = height - y * yScale
        val index = Vector2i(x, y)

        if (!world[index].isBlocking) {
          if (index == targetPos) {
            fill(255f, 0f, 0f)
            rect(screenX.toFloat(), screenY.toFloat(), xScale.toFloat(), (-yScale).toFloat())
          } else if (visitedCells.filter { it.dirs.isNotEmpty() }.map {it.pos}.contains(index)) {
            fill(0f, 0f, 0f)
            rect(screenX.toFloat(), screenY.toFloat(), xScale.toFloat(), (-yScale).toFloat())
          } else if (unvisitedCells.map{it.pos}.contains(index)) {
            fill(100f, 0f, 0f)
            rect(screenX.toFloat(), screenY.toFloat(), xScale.toFloat(), (-yScale).toFloat())
          } else if (cameFrom.containsKey(index)) {
            fill(200f, 0f, 0f)
            rect(screenX.toFloat(), screenY.toFloat(), xScale.toFloat(), (-yScale).toFloat())
          } else {
            fill(255f, 255f, 255f)
            rect(screenX.toFloat(), screenY.toFloat(), xScale.toFloat(), (-yScale).toFloat())
          }

          if (world[index].waterLevel > 0) {
            val waterHeight = world[index].waterLevel.toFloat() / MAX_WATER_LEVEL.toFloat() * yScale
            fill(0f, 0f, 200f, 100f)
            rect(screenX.toFloat(), screenY.toFloat(), xScale.toFloat(), -waterHeight)

            val flowHeight = world[index].flow.toFloat() / MAX_WATER_LEVEL.toFloat() * yScale
            fill(0f, 0f, 255f)
            rect(screenX + xScale * (7f / 8f), screenY.toFloat(), xScale * (1f / 8f), -flowHeight)
          }
        } else {
          fill(0f, 0f, 0f)
          rect(screenX.toFloat(), screenY.toFloat(), xScale.toFloat(), (-yScale).toFloat())
        }
      }
    }
  }
}
