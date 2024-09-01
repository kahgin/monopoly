package monopoly.model

abstract class Square {
  def name: String
  def location: (Int, Int)
}

case class StartSquare(location: (Int, Int), name: String) extends Square
case class ChanceSquare(location: (Int, Int), name: String) extends Square
case class QueueSquare(location: (Int, Int), name: String) extends Square
case class VIPPassSquare(location: (Int, Int), name: String) extends Square
case class GoToQueueSquare(location: (Int, Int), name: String) extends Square
