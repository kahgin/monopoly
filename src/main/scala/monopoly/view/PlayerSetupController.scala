package monopoly.view

import monopoly.Main
import monopoly.model.{Bot, Human}
import monopoly.util.ResourceLoader
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button, TextField}
import scalafx.scene.image.ImageView
import scalafxml.core.macros.sfxml
import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters.{asScalaIteratorConverter, mapAsJavaMapConverter}

@sfxml
class PlayerSetupController (val charImage: ImageView,
                             val previousButton: Button,
                             val nextButton: Button,
                             val selectButton: Button,
                             val nameField: TextField) {

  /** Counter for the current character image */
  private var counter = 1

  /** Count the number of character images */
  private val charImageNum: Int = {
    val imagesPath = "/monopoly/images/character/"
    val resource = Option(getClass.getResource(imagesPath)).getOrElse(
      throw new IllegalStateException(s"Resource path not found: $imagesPath")
    )

    val path = if (resource.getProtocol == "jar") {
      val fs = java.nio.file.FileSystems.newFileSystem(resource.toURI, Map.empty[String, AnyRef].asJava)
      fs.getPath(imagesPath)
    } else {
      Paths.get(resource.toURI)
    }

    Files.list(path).iterator().asScala.count(_.toString.endsWith(".png"))
  }

  /** Go back to the previous screen */
  def goBack(): Unit = Main.goBack()

  /** Go to the next character image */
  def handleNext(): Unit = {
    if (counter < charImageNum) {
      counter += 1
      updateCounter()
    }
  }

  /** Go to the previous character image */
  def handlePrevious(): Unit = {
    if (counter > 1) {
      counter -= 1
      updateCounter()
    }
  }

  /** Update the displayed character image and the state of the buttons */
  private def updateCounter(): Unit = {
    val imagePath = s"character/$counter.png"
    charImage.setImage(ResourceLoader.resourceImage(imagePath))
    previousButton.setDisable(counter == 1)
    nextButton.setDisable(counter == charImageNum)
  }

  /** Save the selected character */
  def handleSelect(): Int = {
    previousButton.setDisable(true)
    nextButton.setDisable(true)
    selectButton.setDisable(true)
    counter // Return the selected character
  }

  /** Handle the start game button */
  def handleStartGame(): Unit = {
    if (selectButton.disabled.get() && nameField.text.value.nonEmpty) {
      val playerName = nameField.text.value
      val characterId = counter

      val newGame = Main.createNewGame()
      newGame.players = Array(
        new Human(playerName, characterId),
        new Bot("Bot", (characterId % charImageNum) + 1)
      )
      newGame.startingPlayers = newGame.players.clone()

      Main.showGame()
    } else {
      val alert = new Alert(AlertType.Error) {
        initOwner(Main.stage)
        title = "Error"
        headerText = "Please select a character and enter your name"
      }
      alert.showAndWait()
    }
  }
}
