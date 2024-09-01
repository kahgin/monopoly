package monopoly.tutorial

case class Tutorial(imagePath: String, description: String)

object Tutorial {
  val steps: List[Tutorial] = List(
    Tutorial("tutorial/1.png",
      "The game will start with the first player rolling the dice. " +
      "Depending on the square dice reaches, player may be entitled to purchase real estate, " +
      "or be obliged to pay rent, draw a Chance card, Go To Queue, etc..."),
    Tutorial("tutorial/2.png",
      "When a player lands on a property not own by anyone, they can choose to purchase it. " +
      "Once bought, the player can upgrade it the next time they land on it."),
    Tutorial("tutorial/3.png",
      "If a player lands on a property owned by another player, they must pay rent."),
    Tutorial("tutorial/4.png",
      "Each time a player lands on, or passes Start, the player will receive $200."),
    Tutorial("tutorial/5.png",
      "If the player lands on Go To Queue, they will be sent to the Queue square and wait for the next round. " +
      "However, if the player lands on Queue, they are just passing by and nothing happens."),
    Tutorial("tutorial/6.png",
      "If the player lands on VIP Pass square, they can choose which square they want to land on."),
    Tutorial("tutorial/7.png",
      "If the player lands on a Chance square, they must draw a card. " +
      "The card will have instructions that the player must follow."),
    Tutorial("tutorial/8.png",
      "The bottom dashboard displays the player's financial status while the top dashboard displays the opponent's financial status."),
    Tutorial("tutorial/9.png",
      "A player can choose to end the game at any time by clicking the 'End Game' button."),
    Tutorial("tutorial/10.png",
      "If a player runs out of money, they are eliminated. " +
      "The game continues until all players complete 25 rounds, and the player with the most money at the end wins.")
  )
}
