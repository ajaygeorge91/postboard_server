package utils

import models.{Article, User}

/**
  * Created by ajayg on 10/16/2017.
  */
object StringUtils {

  def getTrimmedContent(content: String): String = {
    content
      .trim
      .replaceAll("(\r?\n){2,}", "\n")
      .replaceAll("( )+", " ")
  }

  def getTitle(a: Article): String = {
    org.apache.commons.lang3.StringUtils.abbreviate(
      a.title.getOrElse(a.content.getOrElse("")), 100) +
      " - " +
      a.createdBy.getOrElse(User("")).fullName.getOrElse("") +
      " | PostBoard.Online"
  }

}
