package monopoly.view

import monopoly.Main
import monopoly.model._
import scalafx.geometry.Pos
import scalafx.scene.text.Text
import scalafx.scene.image.ImageView
import scalafx.scene.layout.StackPane
import scalafxml.core.macros.sfxml

@sfxml
class SquareController(val squareFXML: StackPane,
                       val name: Text,
                       val price: Text,
                       val color: StackPane,
                       val p1b1: ImageView,
                       val p1b2: ImageView,
                       val p1b3: ImageView,
                       val p2b1: ImageView,
                       val p2b2: ImageView,
                       val p2b3: ImageView,
                       val chanceText: Text,
                       val cornerText: Text) {

  private val buildingIcons: Array[ImageView] = Array(p1b1, p1b2, p1b3, p2b1, p2b2, p2b3)

  /** Hide all building icons. */
  private def hideBuildingIcons(): Unit = buildingIcons.foreach(_.setVisible(false))

  /**
   * Update the square.
   * @param property The square to update.
   */
  def updateIcon(property: PropertySquare): Unit = {
    val game = Main.getCurrentGame
    property.owner match {
      case owner if owner != null => // Show the building icons for the square owner
        val playerIndex = game.turnOrder.indexOf(game.players.indexOf(property.owner))
        val icons = if (playerIndex == 0) List(p1b1, p1b2, p1b3) else List(p2b1, p2b2, p2b3)
        icons.zipWithIndex.foreach { case (icon, index) => icon.setVisible(index < property.rank) }
      case _ => // Hide all building icons if the square is unowned
        List(p1b1, p1b2, p1b3, p2b1, p2b2, p2b3).foreach(_.setVisible(false))
    }
  }

  /**
   * Initialize the square.
   * @param square The square to initialize.
   * @param board The board the square is on.
   */
  def initializeSquare(square: Square, board: Board): Unit = {
    square match {
      case property: PropertySquare => initializePropertySquare(property)
      case chance: ChanceSquare => initializeChanceSquare(chance)
      case _ => initializeCornerSquare(square, board)
    }
  }

  /**
   * Initialize the square square.
   * @param square The square square to initialize.
   */
  private def initializePropertySquare(square: PropertySquare): Unit = {
    name.setText(square.name)
    price.setText(s"$$${square.price}")
    color.setStyle(s"-fx-background-color: ${square.color.hex};")
    hideBuildingIcons()
  }

  /**
   * Initialize the chance square.
   * @param square The chance square to initialize.
   */
  private def initializeChanceSquare(square: ChanceSquare): Unit = chanceText.setText(square.name)

  /**
   * Initialize the corner square.
   * @param square The corner square to initialize.
   * @param board The board the corner square is on.
   */
  private def initializeCornerSquare(square: Square, board: Board): Unit = {
    cornerText.setText(square.name)
    square.location match {
      case(board.size, board.size) =>
        squareFXML.id = s"cornerStart"
        StackPane.setAlignment(cornerText, Pos.BottomRight)
        cornerText.setStyle("-fx-text-alignment: right; -fx-font-size: 30px")
      case(0, 0) =>
        squareFXML.id = "cornerVIP"
        StackPane.setAlignment(cornerText, Pos.TopLeft)
        cornerText.setStyle("-fx-text-alignment: left; -fx-font-size: 30px")
      case(board.size, 0) =>
        squareFXML.id = "cornerGoToQueue"
        StackPane.setAlignment(cornerText, Pos.TopRight)
        cornerText.setStyle("-fx-text-alignment: right; -fx-font-size: 30px")
      case(0, board.size) =>
        squareFXML.id = "cornerQueue"
        StackPane.setAlignment(cornerText, Pos.BottomLeft)
        cornerText.setStyle("-fx-text-alignment: left; -fx-font-size: 30px")
    }
  }
}
