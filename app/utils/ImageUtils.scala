package utils

import models.services.ThumbSize
import play.api.Configuration

/**
  * Created by Android SD-1 on 24-06-2017.
  */
object ImageUtils {

  def generateUrl(config: Configuration, imageKey: String, thumbSize: ThumbSize): String =
    "http://%s.s3.amazonaws.com/%s".format(
      config.getString("aws.s3.bucket").get,
      getImageName(imageKey, thumbSize)
    )

  def getImageName(imageKey: String, thumbSize: ThumbSize): String = imageKey + thumbSize.suffix + ".jpg"


}
