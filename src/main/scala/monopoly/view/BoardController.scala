package monopoly.view

import monopoly.model.{Bot, Human, Player, PropertySquare, Square}
import monopoly.util.ResourceLoader
import scalafxml.core.macros.sfxml
import scalafx.scene.layout.{GridPane, HBox, StackPane, VBox}
import javafx.{scene => jfxs}
import monopoly.Main
import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.image.ImageView
import scalafx.scene.shape.Circle
import scalafx.scene.paint.Color
import scalafx.scene.transform.Rotate

@sfxml
class BoardController(val boardPane: GridPane,
                      val diceContainer: HBox) {

  private val game = Main.getCurrentGame
  private var playerIcons: Map[Player, StackPane] = Map.empty
  private var squareControllers: Map[(Int, Int), SquareController#Controller] = Map.empty
  private var playerContainers: Map[(Int, Int), VBox] = Map.empty
  private var diceController: DiceController#Controller = _
  private var onSquareClick: Option[((Int, Int)) => Unit] = None

  private val iconSize = 50
  private val propertySize = (100, 150)
  private val chanceSize = (150, 100)
  private val cornerSize = (150, 150)
  private val maxIconsPerSquare = game.players.length

  /** Update the boardPane with the new player positions */
  def movePlayers(): Unit = {
    clearExistingIcons()
    Main.getCurrentGame.players.zipWithIndex.foreach(player => updatePlayerPosition(player._1))
  }

  /** Enable square click handling for VIP Pass */
  def enableSquareClick(onClick: ((Int, Int)) => Unit): Unit = onSquareClick = Some(onClick)

  /** Disable square click handling */
  def disableSquareClick(): Unit = onSquareClick = None

  /**
   * Handle dice roll based on player type
   * If the player is a bot, roll the dice and call the callback function
   * If the player is a human, enable the roll button and wait for the player to roll the dice
   * @param player the player rolling the dice
   * @param onComplete the callback function to be called after the dice is rolled
   */
  def handleDiceRoll(player: Player, onComplete: Int => Unit): Unit = {
    player match {
      case _: Bot =>
        val roll = diceController.rollDice()
        onComplete(roll)
      case _: Human =>
        diceController.enableRollButton(() => {
          val roll = diceController.rollDice()
          onComplete(roll)
        })
    }
  }

  /**
   * Update the square on the board
   * @param square the square to update
   */
  def updateSquare(square: Square): Unit = {
    val controller = squareControllers(square.location)
    square match {
      case property: PropertySquare => controller.updateIcon(property)
      case _ => // Do nothing
    }
  }

  /** Initialize the boardPane */
  private def initialize(): Unit = {
    loadDice()
    for (square <- game.board.squares) {
      loadSquare(square, getRotation(square.location._1, square.location._2))
    }
    movePlayers()
  }

  /** Clear existing player icons */
  private def clearExistingIcons(): Unit = {
    playerIcons.values.foreach(icon => {
      val position = icon.getUserData.asInstanceOf[(Int, Int)]
      playerContainers.get(position).foreach(_.children.remove(icon))
    })
    playerIcons = Map.empty
  }

  /**
   * Update the player position on the board
   * @param player the player to update
   */
  private def updatePlayerPosition(player: Player): Unit = {
    val position = player.position
    val playerIcon = createPlayerIcon(player)
    playerIcon.setUserData(position)  // Store the position for later use

    playerContainers.get(position).foreach { container =>
      if (container.children.size < maxIconsPerSquare) {
        container.children.add(playerIcon)
      }

      // Counter-rotate the player icon to keep it upright
      val cellRotation = getRotation(position._1, position._2)
      playerIcon.transforms += new Rotate {
        angle = -cellRotation
        pivotX = iconSize / 2
        pivotY = iconSize / 2
      }.delegate
    }
    playerIcons += (player -> playerIcon)
  }

  /**
   * Create a player container
   * @param inputPrefWidth the preferred width of the container
   * @param inputPrefHeight the preferred height of the container
   * @return
   */
  private def createPlayerContainer(inputPrefWidth: Double, inputPrefHeight: Double): VBox = {
    new VBox {
      spacing = 4
      alignment = Pos.Center
      maxWidth = inputPrefWidth
      maxHeight = inputPrefHeight
      minWidth = inputPrefWidth
      minHeight = inputPrefHeight
    }
  }

  /**
   * Create player icon
   * @param player the player to create the icon for
   * @return the player icon
   */
  private def createPlayerIcon(player: Player): StackPane = {
    val iconImage = ResourceLoader.resourceImage(s"character/${player.icon}.png")

    val circle = new Circle {
      radius = iconSize / 2
      fill = Color.White
      styleClass += "player-board"
    }

    val imageView = new ImageView(iconImage) {
      fitWidth = iconSize
      fitHeight = iconSize
    }

    val clipCircle = new Circle {
      radius = iconSize / 2
      centerX = iconSize / 2
      centerY = iconSize / 2
    }

    imageView.clip = clipCircle

    new StackPane {
      children = Seq(circle, imageView)
      maxWidth = iconSize
      maxHeight = iconSize
    }
  }

  /** Load the dice roll UI */
  private def loadDice(): Unit = {
    val diceLoader = ResourceLoader.resourceFXML("Dice")
    val diceHBox = diceLoader.getRoot[jfxs.layout.HBox]
    diceContainer.children.add(diceHBox)
    diceController = diceLoader.getController[DiceController#Controller]
  }

  /**
   * Get the rotation of the square based on its position
   * @param x the x-coordinate of the square
   * @param y the y-coordinate of the square
   * @return the rotation of the square
   */
  private def getRotation(x: Int, y: Int): Int = {
    if (y == game.board.size) 0     // Bottom row
    else if (x == 0) 90             // Left column
    else if (y == 0) 180            // Top row
    else 270                        // Right column
  }

  /**
   * Load a square onto the board
   * @param square the square to load
   * @param rotation the rotation of the square
   */
  private def loadSquare(square: Square, rotation: Int): Unit = {
    val squareLoader =
      if (square.isInstanceOf[PropertySquare]) ResourceLoader.resourceFXML("PropertySquare")
      else if (square.name == "Chance") ResourceLoader.resourceFXML("ChanceSquare")
      else ResourceLoader.resourceFXML("CornerSquare")
    val cellNode = squareLoader.getRoot[jfxs.layout.StackPane]
    val squareController = squareLoader.getController[SquareController#Controller]
    squareController.initializeSquare(square, game.board)
    // Rotate the property cell node if necessary
    if (square.isInstanceOf[PropertySquare] && rotation != 0) {
      cellNode.setRotate(rotation)
      if (rotation == 90 || rotation == 270) GridPane.setMargin(cellNode, Insets(0, 0, 0, 25))
    }

    // Set the square click handler
    cellNode.onMouseClicked = _ => {
      onSquareClick.foreach(callback => callback(square.location._1, square.location._2))
    }

    boardPane.add(cellNode, square.location._1, square.location._2)
    squareControllers += (square.location -> squareController)

    val playerContainer = createPlayerContainer(
      if (square.isInstanceOf[PropertySquare]) propertySize._1 else if (square.name == "Chance") chanceSize._1 else cornerSize._1,
      if (square.isInstanceOf[PropertySquare]) propertySize._2 else if (square.name == "Chance") chanceSize._2 else cornerSize._2
    )
    cellNode.getChildren.add(playerContainer)
    playerContainers += (square.location -> playerContainer)
  }

  initialize()
}
