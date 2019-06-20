package utils

import models._

/**
  * Created by Android SD-1 on 18-03-2017.
  */
object CommentUtils {


  def paginateSubComments(commentList: List[Comment], orderBy: String): List[Comment] = {
    commentList.map(c => truncateSubComments(c, orderBy))
  }

  /**
    * Taking first 3 if all 3 are good else take 2 or 1 depend on the ranking
    * Max take 3
    */
  private def truncateSubComments(comment: Comment, orderBy: String): Comment = {
    val subList = comment.subCommentListP.getOrElse(PaginatedResult.get[SubComment](0, 0, "", List.empty)).result
    val subCommentCount = comment.commentMeta.getOrElse(CommentMeta()).subCommentCount.getOrElse(0).toLong
    val paginatedSubList = subList.size match {
      case 0 =>
        PaginatedResult.get(0, 0, orderBy, subList)
      case 1 =>
        PaginatedResult.get(0, 1, orderBy, subList)
      case 2 =>
        if (checkQualified(subList.head, subList(1))) {
          PaginatedResult.get(0, 2, orderBy, subList)
        } else {
          PaginatedResult.get(0, 1, orderBy, subList.take(1))
        }
      case _ =>
        if (checkQualified(subList.head, subList(1)) && checkQualified(subList(1), subList(2))) {
          PaginatedResult.get(0, 3, orderBy, subList.take(3))
        } else if (checkQualified(subList.head, subList(1))) {
          PaginatedResult.get(0, 2, orderBy, subList.take(2))
        } else {
          PaginatedResult.get(0, 1, orderBy, subList.take(1))
        }
    }
    comment.copy(subCommentListP = Some(paginatedSubList.copy(totalCount = subCommentCount)))
  }

  /**
    * check whether the second subComment is valuable enough to include in the list
    * Checking if the subComment got at least 75% of the first one
    */
  private def checkQualified(first: SubComment, second: SubComment): Boolean = {
    val f = first.subCommentMeta.getOrElse(SubCommentMeta()).ratingSum.getOrElse(0)
    val s = second.subCommentMeta.getOrElse(SubCommentMeta()).ratingSum.getOrElse(0)
    if (f * .75 <= s)
      true
    else
      false
  }

}