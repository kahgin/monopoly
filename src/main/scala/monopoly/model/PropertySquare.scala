package monopoly.model

import monopoly.util.Color

class PropertySquare(val location: (Int, Int),
                     val name: String,
                     private val basePrice: Int,
                     val color: Color.Value) extends Square {

  /** The maximum rank of the property is 3. */
  val maxRank: Int = 3
  /** The player who owns the property. */
  var owner: Player = _
  /** Initial rank of the property is 0, highest rank is 3. */
  var rank: Int = 0
  /** The percentage increase in price for each upgrade. */
  val upgradeValue: Double = 0.1

  /**
   * Calculate the price to buy & upgrade the property, based on the current rank.
   * @return the price to buy or upgrade the property.
   */
  def price: Int = if (rank == 0) basePrice else (basePrice * (1.0 + upgradeValue * rank)).toInt

  /**
   * Calculate the rent to charge when a player lands on an opponent's property.
   * The rent is 20% of the property price for each rank.
   * @return the rent to charge when a player lands on an opponent's property.
   */
  def rent: Int = (price * 0.2).toInt

  /** Purchase the property. */
  def buy(player: Player): Unit = {
    owner = player
    rank = 1
  }
  
  /** Sell the property. */
  def sell(): Unit = {
    owner = null
    rank = 0
  }

  /** Upgrade the property if the rank is less than 3. */
  def upgrade(): Unit = if (rank < 3) rank += 1
}
