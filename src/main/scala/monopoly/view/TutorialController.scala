package monopoly.view

import monopoly.Main
import monopoly.tutorial.Tutorial.steps
import monopoly.util.ResourceLoader
import scalafx.scene.control.Button
import scalafx.scene.image.ImageView
import scalafx.scene.text.Text
import scalafxml.core.macros.sfxml

@sfxml
class TutorialController(val tutorialImage: ImageView,
                         val tutorialText: Text,
                         val previousButton: Button,
                         val nextButton: Button) {

  // Counter for the current step in the instructions
  private var currentStep = 0

  // Initialize the controller
  private def initialize(): Unit = {
    tutorialImage.setImage(ResourceLoader.resourceImage(steps.head.imagePath))
    tutorialText.setText(steps.head.description)
    updateStep()
  }

  /** Navigate back to the previous screen */
  def goBack(): Unit = Main.goBack()

  /** Navigate to the next step in the instructions */
  def handleNext(): Unit = {
    if (currentStep < steps.length - 1) {
      currentStep += 1
      updateStep()
    } else if (currentStep == steps.length - 1) {
      nextButton.disable = true
    }
  }

  /** Navigate to the previous step in the instructions */
  def handlePrevious(): Unit = {
    if (currentStep > 0) {
      currentStep -= 1
      updateStep()
    } else if (currentStep == 0) {
      previousButton.disable = true
    }
  }

  /** Update the step in the instructions */
  private def updateStep(): Unit = {
    val step = steps(currentStep)
    tutorialImage.setImage(ResourceLoader.resourceImage(step.imagePath))
    tutorialText.setText(step.description)
    previousButton.setDisable(currentStep == 0)
    nextButton.setDisable(currentStep == steps.length - 1)
  }

  initialize()
}
