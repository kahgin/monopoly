package monopoly.view

import monopoly.Main
import scalafxml.core.macros.sfxml

@sfxml
class WelcomeController {
  /** Start the game */
  def startGame(): Unit = Main.showPlayerSetup()

  /** Exit the game */
  def exitGame(): Unit = System.exit(0)

  /** Navigate to setting screen */
  def showSetting(): Unit = Main.showSetting()

  /** Navigate to how to play screen */
  def showTutorial(): Unit = Main.showTutorial()
}
