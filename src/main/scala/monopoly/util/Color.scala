package monopoly.util

object Color extends Enumeration {
  protected case class ColorValue(hex: String) extends super.Val
  import scala.language.implicitConversions
  implicit def valueToColorVal(x: Value): ColorValue = x.asInstanceOf[ColorValue]

  /** Square Color */
  val redLight: ColorValue = ColorValue("#fd6a59")
  val redDark: ColorValue = ColorValue("#e45140")
  val orangeLight: ColorValue = ColorValue("#ffc139")
  val orangeDark: ColorValue = ColorValue("#fea639")
  val purpleLight: ColorValue = ColorValue("#8167cc")
  val purpleDark: ColorValue = ColorValue("#6641bf")
  val greenLight: ColorValue = ColorValue("#47b6a9")
  val greenDark: ColorValue = ColorValue("#19a46e")

  /** Player Color */
  val blue: ColorValue = ColorValue("#048CD6")
  val blueDim: ColorValue = ColorValue("#036296")
  val pink: ColorValue = ColorValue("#FC7DA8")
  val pinkDim: ColorValue = ColorValue("#ca6486")
}
