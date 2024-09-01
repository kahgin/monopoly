package monopoly.util

import scalafx.animation.PauseTransition
import scalafx.util.Duration

object Transition {
  /**
   * Pause for a duration
   *
   * @param durationSeconds the duration to pause in seconds
   * @param onFinished      the function to run after the pause
   * @return the pause transition
   */
  def pause(durationSeconds: Double = 1.5)(onFinished: => Unit): PauseTransition = {
    val pause = new PauseTransition(Duration(durationSeconds * 1000))
    pause.onFinished = _ => onFinished
    pause
  }
}
