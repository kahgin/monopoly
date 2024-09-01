package monopoly.view

import monopoly.model.Dice
import monopoly.util.{ResourceLoader, Transition}
import scalafx.scene.control.Button
import scalafx.scene.image.ImageView
import scalafxml.core.macros.sfxml

@sfxml
class DiceController(val diceButton: Button,
                     val diceImage: ImageView) {

  /** Enable the roll button and set up the onAction event */
  def enableRollButton(callback: () => Unit): Unit = {
    diceButton.disable = false
    diceButton.onAction = _ => {
      callback()
    }
  }

  /** Roll the dice and display the result */
  def rollDice(): Int = {
    diceButton.disable = true
    diceImage.setImage(ResourceLoader.resourceImage("dice/dice.gif"))

    val num = Dice.roll()
    Transition.pause() {
      num match {
        case 1 => diceImage.setImage(ResourceLoader.resourceImage("dice/1.png"))
        case 2 => diceImage.setImage(ResourceLoader.resourceImage("dice/2.png"))
        case 3 => diceImage.setImage(ResourceLoader.resourceImage("dice/3.png"))
        case 4 => diceImage.setImage(ResourceLoader.resourceImage("dice/4.png"))
        case 5 => diceImage.setImage(ResourceLoader.resourceImage("dice/5.png"))
        case 6 => diceImage.setImage(ResourceLoader.resourceImage("dice/6.png"))
      }
    }.play()
    num
  }
}
