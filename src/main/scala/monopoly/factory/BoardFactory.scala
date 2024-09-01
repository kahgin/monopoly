package monopoly.factory

import monopoly.model._
import monopoly.util.{BoardCoordinate, Color}

object BoardFactory {
  def createDefaultBoard(): Board = {
    val boardSize = 6
    val spaces = Array(
      StartSquare(BoardCoordinate.positionToXY(1, boardSize), "Start"),
      new PropertySquare(BoardCoordinate.positionToXY(2, boardSize), "Roti Canai", 100, Color.redLight),
      new PropertySquare(BoardCoordinate.positionToXY(3, boardSize), "Cendol", 120, Color.redLight),
      new PropertySquare(BoardCoordinate.positionToXY(4, boardSize), "Lok Lok", 140, Color.redDark),
      new PropertySquare(BoardCoordinate.positionToXY(5, boardSize), "Rojak", 160, Color.redDark),
      new PropertySquare(BoardCoordinate.positionToXY(6, boardSize), "Apam Balik", 180, Color.redDark),
      QueueSquare(BoardCoordinate.positionToXY(7, boardSize), "Queue"),
      new PropertySquare(BoardCoordinate.positionToXY(8, boardSize), "Hor Fun", 200, Color.orangeLight),
      new PropertySquare(BoardCoordinate.positionToXY(9, boardSize), "Hokkien Mee", 220, Color.orangeLight),
      ChanceSquare(BoardCoordinate.positionToXY(10, boardSize), "Chance"),
      new PropertySquare(BoardCoordinate.positionToXY(11, boardSize), "Nasi Lemak", 260, Color.orangeDark),
      new PropertySquare(BoardCoordinate.positionToXY(12, boardSize), "Roasted Chicken Rice", 280, Color.orangeDark),
      VIPPassSquare(BoardCoordinate.positionToXY(13, boardSize), "VIP Pass"),
      new PropertySquare(BoardCoordinate.positionToXY(14, boardSize), "Otak Otak", 300, Color.purpleLight),
      new PropertySquare(BoardCoordinate.positionToXY(15, boardSize), "Satay", 320, Color.purpleLight),
      new PropertySquare(BoardCoordinate.positionToXY(16, boardSize), "Fried Nian Gao", 340, Color.purpleDark),
      new PropertySquare(BoardCoordinate.positionToXY(17, boardSize), "Goreng Pisang", 360, Color.purpleDark),
      new PropertySquare(BoardCoordinate.positionToXY(18, boardSize), "Kaya Toast", 380, Color.purpleDark),
      GoToQueueSquare(BoardCoordinate.positionToXY(19, boardSize), "Go To Queue"),
      new PropertySquare(BoardCoordinate.positionToXY(20, boardSize), "Durian", 400, Color.greenLight),
      new PropertySquare(BoardCoordinate.positionToXY(21, boardSize), "Ketupat", 420, Color.greenLight),
      ChanceSquare(BoardCoordinate.positionToXY(22, boardSize), "Chance"),
      new PropertySquare(BoardCoordinate.positionToXY(23, boardSize), "Char Kway Teow", 460, Color.greenDark),
      new PropertySquare(BoardCoordinate.positionToXY(24, boardSize), "Asam Laksa", 480, Color.greenDark)
    )
    new Board(boardSize, spaces)
  }
}
