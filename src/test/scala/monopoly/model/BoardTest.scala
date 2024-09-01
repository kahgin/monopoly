package monopoly.model

import monopoly.factory.BoardFactory
import monopoly.util.BoardCoordinate
import org.scalatest.funsuite.AnyFunSuite

class BoardTest extends AnyFunSuite {

  test("Check if the board is created with the correct number of squares and aligned output") {
    val board = BoardFactory.createDefaultBoard()

    // Expected maximum number of squares for a standard board setup
    val expectedMaxSquares = board.size * 4

    // Column widths
    val nameWidth = 25
    val locationWidth = 15
    val positionWidth = 10

    // Print the header
    println(s"%-${nameWidth}s%-${locationWidth}s%-${positionWidth}s".format("Name", "Location", "Position"))
    println("-" * (nameWidth + locationWidth + positionWidth))

    // Print each row with formatted strings
    board.squares.foreach { square =>
      val position = BoardCoordinate.xyToPosition(square.location._1, square.location._2, board.size)
      println(s"%-${nameWidth}s%-${locationWidth}s%-${positionWidth}s".format(square.name, square.location.toString, position.toString))
    }

    // Assertions to validate board creation
    assert(board.squares.nonEmpty, "The board should not be empty.")
    assert(board.squares.length == expectedMaxSquares, s"The board has too many or too little squares. It should have $expectedMaxSquares squares.")
  }

}
