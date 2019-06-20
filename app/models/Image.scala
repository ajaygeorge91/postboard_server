package models

import models.services.{MediumThumb, Original}
import play.api.Configuration
import play.api.libs.json.Json
import utils.{Constants, ImageUtils}

/**
  * Created by ajayg on 10/2/2016.
  */

case class Image(
                  imageKey: Option[String] = None,
                  imageURL: Option[String] = None,
                  imageURLThumbnail: Option[String] = None,
                  heightByWidth: Option[Float] = None
                )

object Image {

  implicit val jsonFormat = Json.format[Image]

  def getOptionalImage(
                        imageKey: Option[String] = None,
                        heightByWidth: Option[Float] = None
                      ): Option[Image] =
    imageKey map (f => Image(imageKey = imageKey, heightByWidth = heightByWidth))

  def getFullImageObj(imageKey: Option[String], heightByWidth: Option[Float], config: Configuration): Option[Image] = {
    imageKey match {
      case Some(imgKey) =>
        Some(Image(
          imageKey = imageKey,
          imageURL = Some(ImageUtils.generateUrl(config, imageKey.getOrElse(""), Original)),
          imageURLThumbnail = Some(ImageUtils.generateUrl(config, imageKey.getOrElse(""), MediumThumb)),
          heightByWidth = heightByWidth
        ))
      case None =>
        None
    }
  }

  def getString(image: Image): String = {
    image.imageKey.map(f => f + Constants.AVATAR_SEPARATOR + image.heightByWidth.getOrElse(0F)).getOrElse("")
  }

  def getFromString(imageString: String, config: Configuration): Option[Image] = {
    imageString match {
      case "" => None
      case Constants.AVATAR_SEPARATOR => None
      case _ =>
        val imageKey = imageString.split(Constants.AVATAR_SEPARATOR).headOption.getOrElse("")
        val heightByWidth = imageString.split(Constants.AVATAR_SEPARATOR).lastOption.map(f => f.toFloat)
        if (imageKey.contains("http://") || imageKey.contains("https://")) {
          Some(Image(imageURL = Some(imageKey),heightByWidth = heightByWidth,imageURLThumbnail = Some(imageKey)))
        } else {
          getFullImageObj(
            imageKey = Some(imageKey),
            heightByWidth = heightByWidth,
            config = config)
        }
    }
  }

}
