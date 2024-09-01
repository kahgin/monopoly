package monopoly.util

object BoardCoordinate {
  /**
   * (OpenAI, 2024)
   * Convert a position on the board to its corresponding (x, y) coordinates.
   * The board is divided into 4 sides, each with a length of `size`.
   * The bottom row is side 0, the left column is side 1, the top row is side 2, and the right column is side 3.
   * @param position the position on the board.
   * @param size the length of each side of the board.
   * @return the (x, y) coordinates of the position.
   */
  def positionToXY(position: Int, size: Int): (Int, Int) = {
    val adjustedPosition = position - 1
    val side = adjustedPosition / size
    val offset = adjustedPosition % size

    side match {
      case 0 => (size - offset, size)    // Bottom row
      case 1 => (0, size - offset)       // Left column
      case 2 => (offset, 0)              // Top row
      case 3 => (size, offset)           // Right column
      case _ => throw new IllegalArgumentException(s"Invalid position: $position")
    }
  }

  /**
   * (OpenAI, 2024)
   * Convert (x, y) coordinates to a position on the board.
   * @param x the x-coordinate.
   * @param y the y-coordinate.
   * @param size the length of each side of the board.
   * @return the position on the board.
   */
  def xyToPosition(x: Int, y: Int, size: Int): Int = {
    if (y == size) size - x               // Bottom row
    else if (x == 0) 2 * size - y         // Left column
    else if (y == 0) 2 * size + x         // Top row
    else if (x == size) 3 * size + y      // Right column
    else throw new IllegalArgumentException(s"Invalid coordinates: ($x, $y)")
  }
}
