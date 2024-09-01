package monopoly.model

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

sealed trait CardType {
  def message: String
}
case object Good extends CardType {
  override def message: String = "Congratulations."
}
case object Bad extends CardType {
  override def message: String = "Better luck next time."
}

trait CardEffect {
  def apply(game: Game): Option[String]
}

case class Card(text: String, cardType: CardType, effect: CardEffect)

object CardDeck {
  val advanceToStart: Card = Card("Advance to start.", Good, (game: Game) => {
    game.currentPlayer.move(game.board.size, game.board.size)
    None
  })

  val bankErrorInYourFavor: Card = Card("Bank error in your favor, receive $200.", Good, (game: Game) => {
    game.currentPlayer.cashIn(200)
    None
  })

  val birthday: Card = Card("It's your birthday, collect $100 from everyone.", Good, (game: Game) => {
    val otherPlayers: Array[Player] = game.players.filter(_ != game.currentPlayer)
    game.currentPlayer.cashIn(100 * otherPlayers.length)
    otherPlayers.foreach(_.cashOut(100))
    None
  })

  val skipRent: Card = Card("Skip paying rent.", Good, (game: Game) => {
    game.currentPlayer.addCard(CardDeck.skipRent)
    Some(s"Skip rent card added to ${game.currentPlayer.name} inventory.") // Card stored & return message
  })

  val skipQueue: Card = Card("Skip queuing.", Good, (game: Game) => {
    game.currentPlayer.addCard(CardDeck.skipQueue)
    Some(s"Skip queue card added to ${game.currentPlayer.name} inventory.") // Card stored & return message
  })

  val goToVIP: Card = Card("Go to VIP Pass.", Good, (game: Game) => {
    // Handled in the game controller
    None
  })

  val payTax: Card = Card("Pay tax of $100.", Bad, (game: Game) => {
    game.currentPlayer.cashOut(100)
    None
  })

  val goToQueue: Card = Card("Go to queue.", Bad, (_: Game) => {
    // Handled in the game controller
    None
  })

  val buyCookies: Card = Card("You buy a few bags of cookies. Yum! Pay $50.", Bad, (game: Game) => {
    game.currentPlayer.cashOut(50)
    None
  })

  val doctorsFee: Card = Card("Doctor's fee, pay $50.", Bad, (game: Game) => {
    game.currentPlayer.cashOut(50)
    None
  })

  /** All cards in the deck. */
  private val cards: ArrayBuffer[Card] = ArrayBuffer(
    advanceToStart, bankErrorInYourFavor, birthday, skipRent, skipQueue, goToVIP, payTax, goToQueue, buyCookies, doctorsFee
  )

  /** Draw n cards from the deck. */
  def drawCards(n: Int = 3): List[Card] = Random.shuffle(cards).take(n).toList

}