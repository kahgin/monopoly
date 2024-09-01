package monopoly.model

import monopoly.factory.BoardFactory
import monopoly.util.BoardCoordinate

class Game(val playerCount: Int = 2, val board: Board = BoardFactory.createDefaultBoard()) {

  var players: Array[Player]                 = new Array[Player](playerCount)
  var startingPlayers: Array[Player]         = new Array[Player](playerCount)
  private var _turnOrder: Array[Int]         = Array.range(0, playerCount)
  private var _currentTurnIndex: Int         = 0      // Index of the current player in the turn order
  private var _currentRound: Int             = 1      // Current round of the game
  val maxRounds: Int                         = 25     // Maximum rounds in the game
  val roundsInQueue: Int                     = 2      // Number of rounds a player has to be in the queue
  private val cashInStartGame: Int           = 2000   // Cash given to players at the start of the game
  val cashInPassStart: Int                   = 200    // Cash given to players when they pass Start

  /** Initialize players */
  def initialize(): Unit = {
    for (i <- 0 until playerCount) {
      players(i).cashIn(cashInStartGame)
    }
  }

  /** Return the current round */
  def currentRound: Int = _currentRound

  /** Return the current turn index */
  private def currentTurnIndex: Int = _currentTurnIndex

  /** Return the current player */
  def currentPlayer: Player = players(turnOrder(currentTurnIndex))

  /** Set the turn order for the players */
  def turnOrder_(order: Array[Int]): Unit = {
    require(order.length == players.length, "Turn order must include all players")
    _turnOrder = order
    _currentTurnIndex = 0
  }

  /** Return the turn order for the players */
  def turnOrder: Array[Int] = _turnOrder

  /** Pass the turn to the next player */
  def nextTurn(): Boolean = {
    _currentTurnIndex = (_currentTurnIndex + 1) % players.length
    currentTurnIndex match {
      case 0 =>
        _currentRound += 1
        true
      case _ =>
        false
    }
  }

  /**
   * Check if a property is upgradable
   * @param property property to check
   * @return a tuple of two booleans, the first is true if the property can be upgraded, the second is true if the player can afford the upgrade
   */
  def propertyUpgradable(property: PropertySquare): (Boolean, Boolean) = {
    val canUpgrade = property.rank < property.maxRank
    val canAfford = currentPlayer.canAfford(property.price)
    (canUpgrade, canAfford)
  }

  /** Send a player to the queue */
  def goToQueueEffect(): Unit = {
    currentPlayer.move((0, board.size))
    currentPlayer.enterQueue()
  }

  /** Manage the turn for a player in the queue */
  def queueTurn(): Boolean = {
    currentPlayer.queueRounds += 1
    if (currentPlayer.queueRounds >= roundsInQueue) {
      currentPlayer.exitQueue()
      true
    } else {
      false
    }
  }

  /**
   * Transfer money between two players
   * @param from player sending money
   * @param to player receiving money
   * @param amount amount of money to transfer
   */
  def transferMoneyBetweenPlayers(from: Player, to: Player, amount: Int): Unit = {
    from.cashOut(amount)
    to.cashIn(amount)
  }

  /** Player buy or upgrade a property */
  def playerBuyOrUpgradeProperty(player: Player, property: PropertySquare): Unit = {
    player.buyOrUpgradeProperty(property)
    if (property.owner != null) property.upgrade() else property.buy(player)
  }

  /** Roll the dice */
  def rollDice(): Int = {
    val diceRoll = Dice.roll()
    diceRoll
  }

  /** Move a player by a number of squares, return true if they passed Start */
  def movePlayer(player: Player, diceRoll: Int): Boolean = {
    val currentPosition = BoardCoordinate.xyToPosition(player.position._1, player.position._2, board.size)
    val newPosition = (currentPosition + diceRoll) % (board.size * 4) + 1

    player.move(BoardCoordinate.positionToXY(newPosition, board.size))
  }

  /** Remove a player from the game */
  def removePlayer(player: Player): Unit = {
    val playerIndex = players.indexOf(player)
    players = players.filter(_ != player)
    _turnOrder = _turnOrder.filter(_ != playerIndex)
    if (currentTurnIndex >= _turnOrder.length) _currentTurnIndex = 0
    _turnOrder = _turnOrder.map(index => if (index > playerIndex) index - 1 else index)
  }

  /** Get the square at a specific position */
  def getSquareAtPosition(position: (Int, Int)): Square = board.squares.find(_.location == position).get

  /** Manage a bankrupt player */
  def bankruptPlayer(player: Player): Boolean = {
    if (player.properties.nonEmpty) {
      player.properties.foreach(property => {
        player.sellProperty(property)
        property.sell()
      })
    }
    removePlayer(player)
    if (isGameOver) {
      true
    } else {
      nextTurn()
      false
    }
  }

  /** Check if the game is over */
  def isGameOver: Boolean = _currentRound > maxRounds || players.count(!_.isBankrupt) <= 1

  /** Get the winner and loser of the game once it is over */
  def gameOver(): (Player, Player) = {
    val sortedPlayers = startingPlayers.sortBy(_.assets)
    val loser = sortedPlayers.head
    val winner = sortedPlayers.last
    (winner, loser)
  }
}
