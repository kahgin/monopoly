package monopoly.view

import monopoly.Main
import monopoly.Main.{player, prefs, savedVolume}
import scalafx.scene.control.{Button, Slider}
import scalafxml.core.macros.sfxml

@sfxml
class SettingController(val volumeSlider: Slider,
                        val volumeButton: Button) {


  /** Initialize the volume slider */
  volumeSlider.setValue(savedVolume)
  volumeSlider.valueProperty.addListener((_, _, newValue) => {
    val volume = newValue.doubleValue() / 100
    player.setVolume(volume)
    prefs.putDouble("volume", volume * 100)
  })

  /** Navigate back to the previous screen */
  def goBack() = Main.goBack()

  /** Mute the background music */
  def mute(): Unit = {
    player.pause()
    volumeButton.setText("Unmute")
    volumeButton.setOnAction(_ => unmute())
  }

  /** Unmute the background music */
  def unmute(): Unit = {
    player.play()
    volumeButton.setText("Mute")
    volumeButton.setOnAction(_ => mute())
  }
}
