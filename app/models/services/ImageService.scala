package models.services

import java.io.File
import java.util.UUID
import javax.inject.Named

import actors.{S3SenderActor, SendToS3}
import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.Inject
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.ScaleMethod.Bicubic
import com.sksamuel.scrimage.nio.JpegWriter
import play.api.Configuration
import utils.{ImageUtils, UuidUtils}

import scala.concurrent.ExecutionContext

/**
  * Created by ajayg on 10/5/2016.
  */
class ImageService @Inject()(
                              val config: Configuration,
                              val system: ActorSystem,
                              @Named(S3SenderActor.name) val s3SenderRouter: ActorRef)(implicit ec: ExecutionContext) {

  def sendImage(file: File): (String, Float) = {
    processPostImage(file)
  }

  //
  //  val s3SenderRouter: ActorRef =
  //    system.actorOf(Props[S3SenderActor].withRouter(SmallestMailboxPool(4)), "s3-sender-router")

  /**
    * Generates a key for the image and returns it immediately, while sending the
    * image to be processed asynchronously with akka.
    */
  def processPostImage(image: File): (String, Float) = {
    val imageKey = UuidUtils.getUUID
    val heightByWidth = getHeightByWidth(image)
    genPostThumb(image, imageKey)
    (imageKey, heightByWidth)
  }


  def getHeightByWidth(image: File): Float = {
    val imageBuf = Image.fromFile(image)
    val height = imageBuf.height
    val width = imageBuf.width

    height.toFloat / width.toFloat * 100
  }

  def genPostThumb(image: File, imageKey: String): Unit = {

    val images = generatePostThumbs(image, imageKey)
    image.delete() // saving up disk space

    //      s3SenderRouter ! SendToS3(image, imageKey + ".jpg")
    images foreach { imageTuple =>
      val (imageFile, imageName) = imageTuple
      s3SenderRouter ! SendToS3(imageFile, imageName)
    }

  }

  def generatePostThumbs(image: File, imageKey: String): List[(File, String)] = {
    postThumbSizes map (t => generateThumb(thumbSize = t, image = image, imageKey = imageKey))
  }

  def generateThumb(thumbSize: ThumbSize, image: File, imageKey: String): (File, String) = {
    implicit val writer = JpegWriter().withCompression(80).withProgressive(true)

    val imageBuf = Image.fromFile(image)
    val height = imageBuf.height
    val width = imageBuf.width
    val imageName = ImageUtils.getImageName(imageKey, thumbSize)

    val (newWidth, newHeight) = newSizesFor(thumbSize, width, height)

    val scaledImage = imageBuf.scaleTo(newWidth, newHeight, Bicubic)
    val destFile = scaledImage.output(new File(image.getParentFile, imageName))
    val heightByWidth: Float = newHeight.toFloat / newWidth.toFloat * 100
    (destFile, imageName)

  }


  val postThumbSizes = List(MediumThumb, Original)
  //  val postThumbSizes = List(MediumThumb, ExtraLargeThumb, Original)

  def newSizesFor(thumbSize: ThumbSize, originalWidth: Int, originalHeight: Int): (Int, Int) = {
    var newWidth: Double = originalWidth.toDouble
    var newHeight: Double = originalHeight.toDouble

    if (newWidth > thumbSize.width) {
      newWidth = thumbSize.width.toDouble
      newHeight = originalHeight * newWidth / originalWidth
    }
    if (newHeight > thumbSize.height) {
      val oldHeight = newHeight
      newHeight = thumbSize.height.toDouble
      newWidth = newHeight * newWidth / oldHeight
    }

    (newWidth.toInt, newHeight.toInt)
  }
}



sealed case class ThumbSize(width: Int, height: Int, suffix: String)

object MediumThumb extends ThumbSize(200, 200, "-m")

// encouraging 2:1 images
object ExtraLargeThumb extends ThumbSize(700, 1400, "-xl")

object Original extends ThumbSize(Integer.MAX_VALUE, Integer.MAX_VALUE, "")

