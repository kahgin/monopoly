package monopoly

import monopoly.util.ResourceLoader
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import javafx.{scene => jfxs}
import monopoly.model.Game
import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.media.MediaPlayer
import java.util.prefs.Preferences
import scala.collection.mutable

object Main extends JFXApp {

  private var currentGame: Game = _
  private val navigationHistory: mutable.ListBuffer[jfxs.layout.BorderPane] = mutable.ListBuffer()
  val prefs: Preferences = Preferences.userNodeForPackage(this.getClass)
  var savedVolume: Double = prefs.getDouble("volume", 50)

  // Play the background music upon launching the application
  val player: MediaPlayer = ResourceLoader.resourceAudio("bgm")
  player.setVolume(savedVolume / 100)
  player.setCycleCount(MediaPlayer.Indefinite)
  player.play()

  // Load the root layout
  private val loader = ResourceLoader.resourceFXML("RootLayout")
  private val roots = loader.getRoot[jfxs.layout.BorderPane]

  stage = new PrimaryStage {
    icons += ResourceLoader.resourceImage("monopoly.png")
    title = "Monopoly"
    scene = new Scene {
      stylesheets += getClass.getResource("view/BasicTheme.css").toString
      root = roots
    }
  }

  /** Navigate to a new screen */
  private def navigateTo(resourcePath: String): Unit = {
    val root = ResourceLoader.resourceFXML(resourcePath).getRoot[jfxs.layout.BorderPane]
    navigationHistory += roots.getCenter.asInstanceOf[jfxs.layout.BorderPane]
    roots.setCenter(root)
  }

  /** Navigate back to the previous screen */
  def goBack(): Unit = {
    if (navigationHistory.nonEmpty) {
      val previousRoot = navigationHistory.remove(navigationHistory.size - 1)
      roots.setCenter(previousRoot)
    }
  }

  /** Display the welcome screen */
  def showWelcome(): Unit = navigateTo("Welcome")

  /** Display the character selection screen */
  def showPlayerSetup(): Unit = navigateTo("PlayerSetup")

  /** Display the setting screen */
  def showSetting(): Unit = navigateTo("Setting")

  /** Display the info screen */
  def showTutorial(): Unit = navigateTo("Tutorial")

  /** Display the game screen */
  def showGame(): Unit = {
    val root = ResourceLoader.resourceFXML("Game").getRoot[jfxs.layout.BorderPane]
    navigationHistory += roots.getCenter.asInstanceOf[jfxs.layout.BorderPane]
    roots.setCenter(root)
  }

  /** Create a new game */
  def createNewGame(): Game = {
    currentGame = new Game()
    currentGame
  }

  /** Get the current game */
  def getCurrentGame: Game = {
    if (currentGame == null) throw new IllegalStateException("No game has been created yet.")
    currentGame
  }

  showWelcome()
}
