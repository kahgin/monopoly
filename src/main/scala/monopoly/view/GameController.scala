package monopoly.view

import monopoly.model._
import monopoly.Main
import monopoly.util.{Color, ResourceLoader, Transition}
import scalafx.scene.control.{Alert, Button, ButtonType}
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.layout.{BorderPane, HBox, StackPane, VBox}
import scalafx.application.Platform
import scalafx.geometry.NodeOrientation
import scalafxml.core.macros.sfxml
import javafx.{scene => jfxs}
import scalafx.scene.image.ImageView
import scalafx.scene.text.Text
import scala.util.Random

@sfxml
class GameController(val gameRoot: BorderPane,
                     val endGameButton: Button,
                     val gameDetail: VBox,
                     val defaultGameAction: VBox,
                     val titleDefault: Text,
                     val descDefault: Text,
                     val buttonSelectionGameAction: VBox,
                     val titleButton: Text,
                     val descButton: Text,
                     val containerButton: StackPane,
                     val yesButton: Button,
                     val noButton: Button,
                     val cardSelectionGameAction: VBox,
                     val titleCard: Text,
                     val descCard: Text,
                     val containerCard: HBox,
                     val endGameGameAction: VBox,
                     val winImage: ImageView,
                     val loseImage: ImageView) {

  private val game: Game = Main.getCurrentGame
  private var canClickBoard: Boolean = false
  private var playerControllers: Map[Player, PlayerController#Controller] = Map.empty
  private var boardController: BoardController#Controller = _
  private var isCardSelected = false
  private val cardController = loadCardController()
  private var actionCallback: Option[Boolean => Unit] = None

  /** Initialize the game */
  private def initialize(): Unit = {
    // Load the bot detail
    val botDashboard = loadPlayerDashboard(game.players(1))
    botDashboard.setNodeOrientation(NodeOrientation.RightToLeft)
    gameDetail.getChildren.add(1,botDashboard)
    // Load the player detail
    val humanDashboard = loadPlayerDashboard(game.players(0))
    gameDetail.getChildren.add(humanDashboard)
    // Load the board
    val boardView = loadBoard()
    gameRoot.setCenter(boardView)

    game.initialize()
    updatePlayerUI()
    startGame()
  }

  /** Start the game by determining who goes first */
  private def startGame(): Unit = {
    val players = game.players
    val shuffledIndices = Random.shuffle(players.indices.toList)
    val cardOptions = shuffledIndices.map { index =>
      val cardController = loadCardController()
      cardController.setNumber(index + 1)
      (cardController, index)
    }

    setCardGameAction("Who Goes First", "Select a card to determine who goes first.", cardOptions) { selectedIndex: Int =>
      val turnOrder = Array.fill(players.length)(-1)
      turnOrder(0) = selectedIndex
      val remainingIndices = shuffledIndices.filter(_ != selectedIndex)
      remainingIndices.zipWithIndex.foreach { case (playerIndex, orderIndex) => turnOrder(orderIndex + 1) = playerIndex }
      descCard.setText(s"${players(turnOrder(0)).name} goes first!")

      Transition.pause() {
        game.turnOrder_(turnOrder)
        setPlayerUIColor()
        executePlayerTurn()
      }.play()
    }
  }

  /** Update player container colors after selecting who goes first */
  private def setPlayerUIColor(): Unit = {
    Platform.runLater {
      val playerDashboard = gameDetail.getChildren.toArray.collect { case hBox: jfxs.layout.HBox => hBox }
      val blueColors = (Color.blue.hex, Color.blueDim.hex)
      val pinkColors = (Color.pink.hex, Color.pinkDim.hex)

      for (playerIndex <- game.players.indices) {
        val dashboard = playerDashboard(playerIndex + 1) // Skip the first hBox
        val playerColor = dashboard.getChildren.get(0).asInstanceOf[jfxs.layout.VBox]
        val assetSection = playerColor.getChildren.get(1).asInstanceOf[jfxs.layout.VBox]
        val (mainColor, dimColor) = if (game.turnOrder.indexOf(playerIndex) == 0) pinkColors else blueColors
        playerColor.setStyle(s"-fx-background-color: $mainColor;")
        assetSection.setStyle(s"-fx-background-color: $dimColor;")
      }
    }
  }

  /** Update player UI */
  private def updatePlayerUI(): Unit = {
    Platform.runLater {
      for ((player, controller) <- playerControllers) controller.updatePlayerDashboard(player)
      boardController.movePlayers()
    }
  }

  /** Update property square UI */
  private def updateSquareUI(square: Square): Unit = {
    Platform.runLater {
      boardController.updateSquare(square)
    }
  }

  /**
   * Initiates a player's turn, main entry point for the game loop
   * If the player is a bot, the turn is executed automatically
   * If the player is a human, the player is prompted to roll the dice
   */
  private def executePlayerTurn(): Unit = {
    if (game.isGameOver) {
      handleGameOver()
    } else {
      if (game.currentPlayer.inQueue) handleQueueTurn() else handleDefaultTurn()
    }
  }

  /**
   * Move the player by the dice roll
   * @param diceRoll The number rolled on the dice
   */
  private def handleMovePlayer(diceRoll: Int): Unit = {
    val passedStart = game.movePlayer(game.currentPlayer, diceRoll)
    updatePlayerUI()
    if (passedStart) setDefaultGameAction(s"Collect $$${game.cashInPassStart}", s"${game.currentPlayer.name} passed Start and received $$${game.cashInPassStart}.") {
      handleLanding(game.getSquareAtPosition(game.currentPlayer.position))
    } else {
      handleLanding(game.getSquareAtPosition(game.currentPlayer.position))
    }
  }

  /**
   * Handle the effects of landing on a square
   * @param square The square the player landed on
   */
  private def handleLanding(square: Square): Unit = {
    setDefaultGameAction(s"Landed on ${square.name}", s"${game.currentPlayer.name} landed on ${square.name}.") {
      square match {
        case _: StartSquare => finishTurn() // Do nothing when landing on Start
        case _: QueueSquare => finishTurn() // Do nothing when landing on Queue
        case _: GoToQueueSquare => handleGoToQueueEffect()
        case _: VIPPassSquare => handleVIPPassEffect()
        case _: PropertySquare => handlePropertyEffect(square.asInstanceOf[PropertySquare])
        case _: ChanceSquare => handleChanceEffect()
      }
    }
  }

  /**
   * Mark current player's turn as finish and proceed to the next player's turn only when
   * the current player is not bankrupt and has enough cash to continue playing
   */
  private def finishTurn(): Unit = {
    val player: Player = game.currentPlayer

    player match {
      case _ if player.isBankrupt =>
        if (game.bankruptPlayer(player)) handleGameOver() else executePlayerTurn()
      case _ if player.isShortInCash(0) =>
        handleShortInCash()
      case _ =>
        val newRound = game.nextTurn()
        val remainingRounds = game.maxRounds - game.currentRound + 1
        if (newRound && remainingRounds <= 5 && remainingRounds > 0) {
          setDefaultGameAction("Game About to End", s"Only $remainingRounds round(s) left!")(executePlayerTurn())
        } else executePlayerTurn()
    }
  }

  /** Handle the game over state */
  private def handleGameOver(): Unit = {
    val (winner, loser) = game.gameOver()
    endGameButton.setId("gameOverButton")
    endGameButton.setText("Back to Main Menu")
    endGameButton.setOnAction(_ => Main.showWelcome())
    // Show the game over screen
    hideAllGameAction()
    endGameGameAction.setVisible(true)
    winImage.setImage(ResourceLoader.resourceImage(s"character/${winner.icon}.png"))
    loseImage.setImage(ResourceLoader.resourceImage(s"character/${loser.icon}.png"))
  }

  /** Handle the effects of landing on a VIP Pass square */
  private def handleVIPPassEffect(): Unit = {
    game.currentPlayer match {
      case _: Bot =>
        val chosenSquare = game.currentPlayer.asInstanceOf[Bot].chooseSquare
        game.currentPlayer.move(chosenSquare.location)
        updatePlayerUI()
        handleLanding(game.getSquareAtPosition(game.currentPlayer.position))
      case _: Human =>
        canClickBoard = true
        setDefaultGameAction("Landed on VIP Pass", "Click the square to land on.") {
          boardController.enableSquareClick { newPosition: (Int, Int) =>
            game.currentPlayer.move(newPosition)
            updatePlayerUI()
            handleLanding(game.getSquareAtPosition(newPosition))
            boardController.disableSquareClick()
          }
        }
    }
  }

  /** Handle the effects of landing on Go To Queue square */
  private def handleGoToQueueEffect(): Unit = {
    val ownSkipQueueCard = game.currentPlayer.hasCard(CardDeck.skipQueue)
    if (ownSkipQueueCard) {
      game.currentPlayer match {
        case _: Bot =>
          useSkipQueueCard(true) // Use Skip Queue card whenever the bot owns it
        case _: Human =>
          showOfferCard(CardDeck.skipQueue)
          waitForButtonClick(useSkipQueueCard)
      }
    } else defaultGoToQueue()

    // Logic for handling whether the player wants to use the Skip Queue card
    def useSkipQueueCard(useCard: Boolean): Unit = {
      if (useCard) {
        game.currentPlayer.useCard(CardDeck.skipQueue)
        setDefaultGameAction("Skip Queue", s"${game.currentPlayer.name} used skip queue card.")(finishTurn())
      } else defaultGoToQueue()
    }

    // Default logic for sending the player to the queue
    def defaultGoToQueue(): Unit = {
      setDefaultGameAction("Go To Queue", s"${game.currentPlayer.name} is sent to queue.") {
        game.goToQueueEffect()
        updatePlayerUI()
        finishTurn()
      }
    }
  }

  /** Handle the effects of a player being in the queue */
  private def handleQueueTurn(): Unit = {
    if (game.queueTurn()) {
      setDefaultGameAction("Exit Queue", s"${game.currentPlayer.name} has been in the queue for ${game.roundsInQueue} rounds and is now out.")(handleDefaultTurn())
    } else {
      setDefaultGameAction("In Queue", s"${game.currentPlayer.name} is in the queue for ${game.currentPlayer.queueRounds} round(s).")(finishTurn())
    }
  }

  /** Handle the default turn for a player */
  private def handleDefaultTurn(): Unit = {
    game.currentPlayer match {
      case _: Bot =>
        setDefaultGameAction(s"${game.currentPlayer.name}'s turn", s"Waiting for ${game.currentPlayer.name} to roll the dice.") {
          boardController.handleDiceRoll(game.currentPlayer, (roll: Int) => handleMovePlayer(roll))
        }
      case _: Human =>
        setDefaultGameAction(s"${game.currentPlayer.name}'s turn", s"Click the roll button to roll the dice.") {
          boardController.handleDiceRoll(game.currentPlayer, (roll: Int) => handleMovePlayer(roll))
        }
    }
  }

  /**
   * Handle the effects of landing on a property square
   * @param property The property square landed on
   */
  private def handlePropertyEffect(property: PropertySquare): Unit = {
    property.owner match {
      case null => offerToBuyProperty(property)
      case owner if owner == game.currentPlayer => offerToUpgradeProperty(property)
      case owner if owner != game.currentPlayer => handlePayRent(property)
    }
  }

  /**
   * Offer the player the opportunity to wantToBuy the property
   * @param property The property to be bought
   */
  private def offerToBuyProperty(property: PropertySquare): Unit = {
    if (game.currentPlayer.canAfford(property.price)) {
      game.currentPlayer match {
        case _: Bot =>
          val wantToBuy = game.currentPlayer.asInstanceOf[Bot].makeDecision
          handleBuyProperty(property, wantToBuy)
        case _: Human =>
          showOfferProperty("Land For Sale", s"Do you want to purchase ${property.name} for $$${property.price}?", "Purchase", property)
          waitForButtonClick { wantToBuy => handleBuyProperty(property, wantToBuy) }
      }
    } else setDefaultGameAction("Not Enough Money", s"${game.currentPlayer.name} doesn't have enough money to purchase ${property.name}.")(finishTurn())
  }

  /**
   * Logic for handling whether the player wants to buy the property
   * @param property The property to be bought
   * @param wantToBuy Whether the player wants to buy the property
   */
  private def handleBuyProperty(property: PropertySquare, wantToBuy: Boolean): Unit = {
    if (wantToBuy) {
      setDefaultGameAction("Land Sold", s"${game.currentPlayer.name} purchased ${property.name} for $$${property.price}.") {
        game.playerBuyOrUpgradeProperty(game.currentPlayer, property)
        updateSquareUI(property)
        updatePlayerUI()
        finishTurn()
      }
    } else setDefaultGameAction("Pass", s"${game.currentPlayer.name} decided not to purchase ${property.name}.") {finishTurn()}
  }

  /**
   * Offer the player the opportunity to upgrade the property
   * @param property The property to be upgraded
   */
  private def offerToUpgradeProperty(property: PropertySquare): Unit = {
    game.propertyUpgradable(property) match { // (canUpgrade, canAfford)
      case (true, true) =>
        game.currentPlayer match {
          case _: Bot =>
            val upgrade = game.currentPlayer.asInstanceOf[Bot].makeDecision
            handleUpgradeProperty(property, upgrade)
          case _: Human =>
            showOfferProperty("Upgrade Property", s"Do you want to upgrade ${property.name} for $$${property.price}?", "Upgrade", property)
            waitForButtonClick { upgrade => handleUpgradeProperty(property, upgrade) }
        }
      case (false, _) =>
        setDefaultGameAction("Max Rank", s"${property.name} is already at max rank.") {finishTurn()}
      case (_, false) =>
        setDefaultGameAction("Not Enough Money", s"${game.currentPlayer.name} doesn't have enough money to upgrade ${property.name}.") {finishTurn()}
    }
  }

  /**
   * Logic for handling whether the player wants to upgrade the property
   * @param property The property to be upgraded
   * @param wantToUpgrade Whether the player wants to upgrade the property
   */
  private def handleUpgradeProperty(property: PropertySquare, wantToUpgrade: Boolean): Unit = {
    if (wantToUpgrade) {
      game.playerBuyOrUpgradeProperty(game.currentPlayer, property)
      setDefaultGameAction("Property Upgraded", s"${property.name} is now in rank ${property.rank}.") {
        updateSquareUI(property)
        updatePlayerUI()
        finishTurn()
      }
    } else setDefaultGameAction("Pass", s"${game.currentPlayer.name} decided not to upgrade ${property.name}.") {finishTurn()}
  }

  /**
   * Pay rent to the owner of the property
   * @param property The property to pay rent for
   */
  private def handlePayRent(property: PropertySquare): Unit = {
    val ownSkipRentCard = game.currentPlayer.hasCard(CardDeck.skipRent)
    if (ownSkipRentCard) {
      game.currentPlayer match {
        case _: Bot =>
          val useCard = game.currentPlayer.asInstanceOf[Bot].useSkipRentCard(property)
          handleUseSkipRentCard(useCard, property)
        case _: Human =>
          showOfferCard(CardDeck.skipRent)
          waitForButtonClick { useCard: Boolean => handleUseSkipRentCard(useCard, property) }
      }
    } else defaultPayRent(property)

    // Logic for handling whether the player wants to use the Skip Rent card
    def handleUseSkipRentCard(useCard: Boolean, property: PropertySquare): Unit = {
      if (useCard) {
        game.currentPlayer.useCard(CardDeck.skipRent)
        setDefaultGameAction("Skip Rent", s"${game.currentPlayer.name} used the Skip Rent card.") {finishTurn()}
      } else defaultPayRent(property)
    }

    // Default logic for paying rent
    def defaultPayRent(property: PropertySquare): Unit = {
      setDefaultGameAction("Pay Rent To Opponent", s"${game.currentPlayer.name} paid $$${property.rent} to ${property.owner.name} for landing on ${property.name}.") {
        game.transferMoneyBetweenPlayers(game.currentPlayer, property.owner, property.rent)
        updatePlayerUI()
        finishTurn()
      }
    }
  }

  /** Handle the effects of landing on a Chance square */
  private def handleChanceEffect(): Unit = {
    game.currentPlayer match {
      case _: Bot =>
        setDefaultGameAction("Landed on Chance Square", s"${game.currentPlayer.name} is picking a card.") {
          val selectedCard = CardDeck.drawCards(1).head // Drawing only 1 card
          handleCardEffect(selectedCard)
        }
      case _: Human =>
        val drawnCards = CardDeck.drawCards()
        val cardControllers = drawnCards.map { card =>
          val cardController = loadCardController()
          cardController.setText(card.text)
          (cardController, card)
        }
        setCardGameAction("Landed on Chance Square", "Pick a card.", cardControllers) {
          (selectedCard: Card) => {
            descCard.setText(selectedCard.cardType.message)
            Transition.pause() {
              handleCardEffect(selectedCard)
            }.play()
          }
        }
    }

    // Handle the card effect after the card is selected
    def handleCardEffect(selectedCard: Card): Unit = {
      selectedCard match {
        case CardDeck.goToQueue =>
          game.currentPlayer match {
            case _: Bot =>
              setDefaultGameAction("Card Effect", selectedCard.text) {
                handleGoToQueueEffect()
              }
            case _: Human =>
              handleGoToQueueEffect()
          }
        case CardDeck.goToVIP =>
          game.currentPlayer match {
            case _: Bot =>
              setDefaultGameAction("Card Effect", selectedCard.text) {
                game.currentPlayer.move(0, 0)
                updatePlayerUI()
                handleVIPPassEffect()
              }
            case _: Human =>
              game.currentPlayer.move(0, 0)
              updatePlayerUI()
              handleVIPPassEffect()
          }
        case _ =>
          selectedCard.effect.apply(game) match {
            case Some(message) =>
              setDefaultGameAction("Card Added to Inventory", message)(finishTurn())
            case None =>
              setDefaultGameAction("Card Effect", selectedCard.text) {
                updatePlayerUI()
                finishTurn()
              }
          }
      }
    }
  }

  /** Handle the case when the player is short in cash, player either sells properties or declares bankrupt */
  private def handleShortInCash(): Unit = {
    game.currentPlayer match {
      case bot: Bot =>
      setDefaultGameAction("Not Enough Money", s"${bot.name} is selling assets.") {
        // Cover the debt by selling the cheapest properties first
        val sortedProperties = bot.properties.sortBy(_.price)
        for (property <- sortedProperties if bot.isShortInCash(0)) {
          bot.sellProperty(property)
          updatePlayerUI()
          updateSquareUI(property)
        }
        if (bot.isBankrupt) handleSellProperty(false) else finishTurn()
      }
      case _: Human =>
        setButtonGameAction ("Not Enough Money", s"Sell a property or declare bankrupt.", "Sell", "Bankrupt", None)
        waitForButtonClick(handleSellProperty)
    }
  }

  /**
   * Handle the selling of a property
   * @param wantToSell Whether the player wants to sell a property
   */
  private def handleSellProperty(wantToSell: Boolean): Unit = {
    if (wantToSell) {
      canClickBoard = true
      setDefaultGameAction("Sell a property", "Click the property to sell.") {
        boardController.enableSquareClick { property: (Int, Int) =>
          val square = game.getSquareAtPosition(property).asInstanceOf[PropertySquare]

          game.currentPlayer.sellProperty(square)
          updatePlayerUI()
          updateSquareUI(square)

          if (game.currentPlayer.cash >= 0) {
            // Player has enough cash, finish the turn
            boardController.disableSquareClick()
            finishTurn()
          } else {
            // Player still doesn't have enough cash, prompt to sell more properties
            setDefaultGameAction("Not Enough Cash", "Please sell more properties to cover your debt.") {
              handleSellProperty(true)  // Recursive call to keep selling
            }
          }
        }
      }
    } else {
      game.removePlayer(game.currentPlayer)
      if (game.isGameOver) handleGameOver() else {
        game.nextTurn()
        executePlayerTurn()
      }
    }
  }

  /**
   * Load the player detail UI
   * @param player The player to load the details for
   * @return The player detail UI
   */
  private def loadPlayerDashboard(player: Player): jfxs.Node = {
    val playerLoader = ResourceLoader.resourceFXML("Player")
    val playerDetail = playerLoader.getRoot[jfxs.Node]
    val playerController = playerLoader.getController[PlayerController#Controller]
    playerController.setPlayerDashboard(player)
    playerControllers += (player -> playerController)
    playerDetail
  }

  /**
   * Load the board UI
   * @return The board UI
   */
  private def loadBoard(): jfxs.layout.GridPane = {
    val boardLoader = ResourceLoader.resourceFXML("Board")
    val board = boardLoader.getRoot[jfxs.layout.GridPane]
    boardController = boardLoader.getController[BoardController#Controller]
    board
  }

  /**
   * Load the card controller
   * @return Card controller
   */
  private def loadCardController(): CardController#Controller = {
    val cardLoader = ResourceLoader.resourceFXML("Card")
    cardLoader.getRoot[StackPane]
    cardLoader.getController[CardController#Controller]
  }

  /**
   * Set the default game action
   * @param title The title of the game action
   * @param desc  The description of the game action
   */
  private def setDefaultGameAction(title: String, desc: String)(onComplete: => Unit): Unit = {
    hideAllGameAction()
    defaultGameAction.setVisible(true)
    titleDefault.setText(title)
    descDefault.setText(desc)
    Transition.pause() {
      onComplete
    }.play()
  }

  /**
   * Set the button game action
   * @param title The title of the game action
   * @param desc The description of the game action
   * @param yes The text for the yes button
   * @param no The text for the no button
   * @param content The content to be displayed
   */
  private def setButtonGameAction(title: String, desc: String, yes: String, no: String, content: Option[jfxs.Node]): Unit = {
    hideAllGameAction()
    buttonSelectionGameAction.setVisible(true)
    titleButton.setText(title)
    descButton.setText(desc)
    yesButton.setText(yes)
    noButton.setText(no)

    if (!containerButton.getChildren.isEmpty) containerButton.getChildren.clear()
    content.foreach(containerButton.children.add)

    yesButton.onAction = _ => handleButtonClick(true)
    noButton.onAction = _ => handleButtonClick(false)
  }

  /**
   * Handle button click
   * @param decision The decision made by the player
   */
  private def handleButtonClick(decision: Boolean): Unit = {
    buttonSelectionGameAction.setVisible(false)
    actionCallback.foreach(_(decision))
    actionCallback = None
  }

  /**
   * Wait for button click from player
   * @param onComplete The function to be executed when the button is clicked
   */
  private def waitForButtonClick(onComplete: Boolean => Unit): Unit = {
    actionCallback = Some(onComplete)
  }

  /**
   * Offer the player to use a card when the player owns it
   * @param card The card to be used
   */
  private def showOfferCard(card: Card): Unit = {
    cardController.setText(s"${card.text}")
    cardController.displayFrontSide()
    val title = s"Use ${card.text.stripSuffix(".")} Card"
    val desc = s"Do you want to use the ${card.text.toLowerCase.stripSuffix(".")} card?"
    setButtonGameAction(title, desc, "Use", "Skip", Some(cardController.card.delegate))
  }

  /**
   * Offer the player to wantToBuy or upgrade a property
   * @param title The title of the game action
   * @param desc The description of the game action
   * @param yesButton The text for the yes button
   * @param property The property to be bought or upgraded
   */
  private def showOfferProperty(title: String, desc: String, yesButton: String, property: PropertySquare): Unit = {
    val square = ResourceLoader.resourceFXML("PropertySquare")
    val cellNode = square.getRoot[jfxs.layout.StackPane]
    val squareController = square.getController[SquareController#Controller]
    squareController.initializeSquare(property, Main.getCurrentGame.board)
    setButtonGameAction(title, desc, yesButton, "Pass", Some(cellNode))
  }

  /**
   * Set the card game action
   * @param title The title of the game action
   * @param desc The description of the game action
   * @param cards The cards to be displayed
   * @param onComplete The function to be executed when the card is selected
   */
  private def setCardGameAction[T](title: String, desc: String, cards: List[(CardController#Controller, T)])(onComplete: T => Unit): Unit = {
    hideAllGameAction()
    cardSelectionGameAction.setVisible(true)
    titleCard.setText(title)
    descCard.setText(desc)
    isCardSelected = false
    cards.foreach { case (cardController, value) =>
      cardController.displayBackSide()
      cardController.card.setOnMouseClicked(_ => {
        if (!isCardSelected) {
          isCardSelected = true
          cardController.revealCard()

          Transition.pause() {
            cards.filterNot(_._1 == cardController).foreach(_._1.revealCard())
            Transition.pause() {
              onComplete(value)
            }.play()
          }.play()
        }
      })
    }
    if (!containerCard.getChildren.isEmpty) containerCard.getChildren.clear()
    containerCard.children = cards.map(_._1.card)
    containerCard.setSpacing(cards.size match {
      case 2 => 60
      case 3 => 10
      case _ => 0
    })
  }

  /** Hide all game actions */
  private def hideAllGameAction(): Unit = {
    defaultGameAction.setVisible(false)
    buttonSelectionGameAction.setVisible(false)
    cardSelectionGameAction.setVisible(false)
    endGameGameAction.setVisible(false)
  }

  /** Navigate to the previous screen */
  def goBack(): Unit = Main.goBack()

  /** Navigate to how to play screen */
  def showTutorial(): Unit = Main.showTutorial()

  /** Navigate to the setting screen */
  def showSetting(): Unit = Main.showSetting()

  /** Ends the game */
  def endGame(): Unit = {
    val alert = new Alert(AlertType.Confirmation) {
      initOwner(Main.stage)
      title = "End Game"
      headerText = "Are you sure you want to end the game?"
      contentText = "All progress will be lost."
      buttonTypes = Seq(ButtonType.Yes, ButtonType.No)
    }
    alert.showAndWait() match {
      case Some(ButtonType.Yes) => Main.showWelcome()
      case _ => // Do nothing if No is selected or the dialog is closed
    }
  }

  initialize()
}
