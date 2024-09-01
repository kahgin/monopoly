package monopoly.model

import monopoly.Main
import monopoly.util.BoardCoordinate
import scala.collection.mutable.ArrayBuffer

abstract class Player(val name: String, val icon: Int) {
  private var _cash: Int = _
  private var _position: (Int, Int) = (Main.getCurrentGame.board.size, Main.getCurrentGame.board.size)
  private var _inQueue: Boolean = false
  private val cardList: ArrayBuffer[Card] = ArrayBuffer()
  private val _properties: ArrayBuffer[PropertySquare] = ArrayBuffer()
  var queueRounds: Int = 0

  /** Return player's position. */
  def position: (Int, Int) = _position

  /** Move the player to a new position on the board */
  def move(newPosition: (Int, Int)): Boolean = {
    val currentPosition = BoardCoordinate.xyToPosition(_position._1, _position._2, Main.getCurrentGame.board.size)
    val newBoardPosition = BoardCoordinate.xyToPosition(newPosition._1, newPosition._2, Main.getCurrentGame.board.size)

    val passedStart = newBoardPosition < currentPosition
    if (passedStart) cashIn(Main.getCurrentGame.cashInPassStart)

    _position = newPosition
    passedStart
  }

  /** Return whether the player is in queue. */
  def inQueue: Boolean = _inQueue

  /** Buy or upgrade a property. */
  def buyOrUpgradeProperty(property: PropertySquare): Unit = {
    cashOut(property.price)
    _properties += property
  }

  /** Sell a property owned by the player. */
  def sellProperty(property: PropertySquare): Unit = {
    cashIn((property.price + property.rank * property.upgradeValue).toInt)
    _properties -= property
    property.sell()
  }

  /** Return whether the player is bankrupt. */
  def isBankrupt: Boolean = assets <= 0

  /** Return whether the player is lacking cash. */
  def isShortInCash(amount: Int): Boolean = _cash < amount && assets >= amount

  /** Returns the player's owned properties. */
  def properties: Seq[PropertySquare] = _properties

  /** Returns the player's total assets. */
  def assets: Int = _properties.map(_.price).sum + _cash

  /** Check if the player can afford to buy or upgrade */
  def canAfford(amount: Int): Boolean = _cash >= amount

  /** Debited the player's cash by the amount specified. */
  def cashIn(amount: Int): Unit = _cash += amount

  /** Credited the player's cash by the amount specified. */
  def cashOut(amount: Int): Unit = _cash -= amount

  /** Returns the player's current cash balance. */
  def cash: Int = _cash

  /** Enter the queue. */
  def enterQueue(): Unit = _inQueue = true

  /** Exit the queue. */
  def exitQueue(): Unit = {
    _inQueue = false
    queueRounds = 0
  }

  /** Add a card to the player's card list. */
  def addCard(card: Card): Unit = cardList += card

  /** Check if the player has a specific card. */
  def hasCard(card: Card): Boolean = cardList.exists(_.text == card.text)

  /** Use a card from the player's card list. */
  def useCard(card: Card): Unit = cardList -= card
}

class Human(_name: String, _icon: Int) extends Player(_name, _icon)

class Bot(_name: String, _icon: Int) extends Player(_name, _icon) {
  /**
   * Make a decision based on a random number generator.
   * The decision is to do something is 85% of the time.
   * @return true if the bot decides to do something or false otherwise.
   */
  def makeDecision: Boolean = scala.util.Random.nextInt(100) < 85

  /**
   * Use a card based on a random number generator.
   * The bot will use a card 85% of the time if it can afford the rent.
   * @return true if the bot decides to use a card or false otherwise.
   */
  def useSkipRentCard(property: PropertySquare): Boolean = if (canAfford(property.rent)) scala.util.Random.nextInt(100) < 85 else true

  /**
   * Choose a square to move to based on the bot's properties and cash.
   * The bot will choose a property if it can afford it, otherwise it will choose a fallback square.
   * @return the square the bot chooses to move to.
   */
  def chooseSquare: Square = {
    if (properties.nonEmpty) {
      val affordableProperties = properties.filter(_.price < cash)
      if (affordableProperties.nonEmpty) {
        affordableProperties(scala.util.Random.nextInt(affordableProperties.size))
      } else {
        chooseFallbackSquare()
      }
    } else {
      chooseFallbackSquare()
    }
  }

  /**
   * Fallback square which is the Start square if the bot has no properties or cannot afford any properties.
   * @return the fallback square.
   */
  private def chooseFallbackSquare(): Square = {
    // Need to ensure that the Start square is always the first square on the board
    Main.getCurrentGame.board.squares.find(_.isInstanceOf[StartSquare]).getOrElse(Main.getCurrentGame.board.squares.head)
  }
}
