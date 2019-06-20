package utils

import org.apache.commons.codec.binary.Base64


/**
  * Created by ajayg on 10/20/2017.
  */
object UuidUtils {

  import java.nio.ByteBuffer
  import java.util.UUID

  def getUUID: String = {
    uuidToBase64(UUID.randomUUID().toString)
  }

  private def uuidToBase64(str: String): String = {
    val uuid = UUID.fromString(str)
    val bb = ByteBuffer.wrap(new Array[Byte](16))
    bb.putLong(uuid.getMostSignificantBits)
    bb.putLong(uuid.getLeastSignificantBits)
    Base64.encodeBase64URLSafeString(bb.array)
  }

  private def uuidFromBase64(str: String): String = {
    val bytes = Base64.decodeBase64(str)
    val bb = ByteBuffer.wrap(bytes)
    val uuid = new UUID(bb.getLong, bb.getLong)
    uuid.toString
  }
}
