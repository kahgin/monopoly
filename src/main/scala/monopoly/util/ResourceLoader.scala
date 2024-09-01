package monopoly.util

import monopoly.Main
import scalafx.scene.image.Image
import scalafx.scene.media.{Media, MediaPlayer}
import scalafxml.core.{FXMLLoader, NoDependencyResolver}

object ResourceLoader {

  /**
   * Load an FXML file from the view folder
   * @param path The path to the FXML file
   * @return The FXMLLoader object
   */
  def resourceFXML(path: String): FXMLLoader = {
    val resource = Main.getClass.getResource(s"view/$path.fxml")
    if (resource == null) throw new IllegalArgumentException(s"FXML not found: $path.fxml")
    val loader = new FXMLLoader(resource, NoDependencyResolver)
    loader.load()
    loader
  }

  /**
   * Load an image file from any folder within the images folder
   * @param path The path to the image file
   * @return The path to the image file
   */
  def resourceImage(path: String): Image = {
    val resource = Main.getClass.getResourceAsStream(s"images/$path")
    if (resource == null) throw new IllegalArgumentException(s"Image not found: $path")
    new Image(resource)
  }

  /**
   * Load an audio file from the resources folder
   * @param path The path to the audio file
   * @return The path to the audio file
   */
  def resourceAudio(path: String): MediaPlayer = {
    val resource = Main.getClass.getResource(s"audio/$path.mp3")
    if (resource == null) throw new IllegalArgumentException(s"Audio not found: $path")
    val media = new Media(resource.toString)
    new MediaPlayer(media)
  }
}
