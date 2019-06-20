package actors

import java.io.File

import akka.actor._
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{CannedAccessControlList, PutObjectRequest}
import com.google.inject.Inject
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

case class SendToS3(image: File, imageName: String)

class S3SenderActor @Inject()(val config: Configuration)(
  implicit ec: ExecutionContext
) extends Actor
    with ActorLogging {

  val bucket = config.getString("aws.s3.bucket")

  val awsCreds = new BasicAWSCredentials(
    config.getString("aws.accessKey").get,
    config.getString("aws.secretKey").get
  )

//  val s3 = AmazonS3ClientBuilder
//    .standard()
//    .withRegion(Regions.AP_SOUTH_1) // TODO config it
//    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
//    .build()

  val s3 = new AmazonS3Client(
    new BasicAWSCredentials(config.getString("aws.accessKey").get, config.getString("aws.secretKey").get))

  s3.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_1))

  def receive = {
    case SendToS3(image, imageName) =>
      log.info("about to send {} to s3", imageName)

      val putRequest = new PutObjectRequest(bucket.get, imageName, image)
      putRequest.setCannedAcl(CannedAccessControlList.PublicRead)

      Logger.info("sending to s3: " + imageName)
      if (s3.listBuckets().iterator().hasNext) {
        Logger.info("name : " + s3.listBuckets().iterator().next().getName)
      }

      s3.putObject(putRequest)
      if (!image.delete)
        Logger.info(
          "could not delete original file %s after sending it to s3"
            .format(imageName)
        )

  }

}

object S3SenderActor {

  final val name = "S3SenderActor"

}
