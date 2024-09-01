package monopoly.view

import scalafx.scene.layout.{Pane, StackPane}
import scalafx.scene.text.Text
import scalafxml.core.macros.sfxml

@sfxml
class CardController(val card: StackPane,
                     val cardBack: Pane,
                     val content: Text) {

  private var isRevealed = false

  /** Update the visibility of card elements */
  private def updateVisibility(showBack: Boolean, showContent: Boolean): Unit = {
    cardBack.visible = showBack
    content.visible = showContent
  }

  /** Display the back of the card */
  def displayBackSide(): Unit = updateVisibility(showBack = true, showContent = false)

  /** Display the front of the card */
  def displayFrontSide(): Unit = updateVisibility(showBack = false, showContent = content.text.value.nonEmpty)

  /** Set number content on the card */
  def setNumber(value: Int): Unit = {
    content.text = value.toString
    content.styleClass.add("font-large")
    if (isRevealed) displayFrontSide() else displayBackSide()
  }

  /** Set text content on the card */
  def setText(text: String): Unit = {
    content.text = text
    if (isRevealed) displayFrontSide() else displayBackSide()
  }

  /** Animate the card flip */
  def revealCard(): Unit = {
    isRevealed = true
    displayFrontSide()
  }
}
