package monopoly.model

object Dice {
  /**
   * Roll a dice and return a random number between 1 and 6.
   * @return a random number between 1 and 6.
   */
  def roll(): Int = {
    val random = new scala.util.Random
    random.nextInt(6) + 1
  }
}
