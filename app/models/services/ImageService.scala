package models.services

import java.io.File


trait ImageService {

  def sendImage(file: File): (String, Float)

}
