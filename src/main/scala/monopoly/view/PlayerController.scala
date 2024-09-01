package monopoly.view

import monopoly.model.Player
import monopoly.util.ResourceLoader
import scalafx.scene.image.ImageView
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.text.Text
import scalafxml.core.macros.sfxml

@sfxml
class PlayerController(val playerDashboard: HBox,
                       val playerColor: VBox,
                       val playerName: Text,
                       val assetSection: VBox,
                       val playerCash: Text,
                       val playerIcon: ImageView,
                       val playerAsset: Text) {

  /** Update the player's cash & assets */
  def updatePlayerDashboard(player: Player): Unit = {
    playerCash.setText(player.cash.toString)
    playerAsset.setText(player.assets.toString)
  }

  /** Set the player details upon entering the game */
  def setPlayerDashboard(player: Player): Unit = {
    playerName.setText(player.name)
    playerCash.setText(player.cash.toString)
    playerIcon.setImage(ResourceLoader.resourceImage(s"character/${player.icon}.png"))
    playerAsset.setText(player.assets.toString)
  }
}
