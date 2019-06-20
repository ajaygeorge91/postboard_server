$(function () {

    var commentReplyInputTemplate = document.getElementById('commentReplyInputTemplate');
    var modalCommentEditContentTemplate = document.getElementById('modalCommentEditContentTemplate');
    var userNotificationListTemplate = document.getElementById('userNotificationListTemplate');


// post like and dislike

    $("#contentList")
        .on('click', "button.like", function () {
            var postID = $(this).attr("id").slice(0, -5); // removing "_like"
            amplify.publish("like_ui", postID);
            amplify.publish("update_ratingTotal_ui", postID, 1);
        })
        .on('click', "button.dislike", function () {
            var postID = $(this).attr("id").slice(0, -8); // removing "_dislike"
            amplify.publish("dislike_ui", postID);
            amplify.publish("update_ratingTotal_ui", postID, -1);
        })
        .on('click', "button.liked", function () {
            var postID = $(this).attr("id").slice(0, -5); // removing "_dislike"
            amplify.publish("neutral_ui", postID);
            amplify.publish("update_ratingTotal_ui", postID, 0);
        })
        .on('click', "button.disliked", function () {
            var postID = $(this).attr("id").slice(0, -8); // removing "_dislike"
            amplify.publish("neutral_ui", postID);
            amplify.publish("update_ratingTotal_ui", postID, 0);
        });

    amplify.subscribe("neutral_ui", function (postID) {
        var likeId = postID.concat("_like");
        var dislikeId = postID.concat("_dislike");
        $('#' + likeId).removeClass("liked").addClass("like");
        $('#' + dislikeId).removeClass("disliked").addClass("dislike");
    });

    amplify.subscribe("dislike_ui", function (postID) {
        var likeId = postID.concat("_like");
        var dislikeId = postID.concat("_dislike");
        $('#' + likeId).removeClass("liked").addClass("like");
        $('#' + dislikeId).removeClass("dislike").addClass("disliked");
    });

    amplify.subscribe("like_ui", function (postID) {
        var likeId = postID.concat("_like");
        var dislikeId = postID.concat("_dislike");
        $('#' + likeId).removeClass("like").addClass("liked");
        $('#' + dislikeId).removeClass("disliked").addClass("dislike");
    });

    amplify.subscribe("update_ratingTotal_ui", function (postID, offset) {
        var ratingValueId = postID.concat("_rating");
        var ratingSumId = postID.concat("_ratingSum");
        var ratingValue = parseInt($('#' + ratingValueId).text());
        var ratingSum = parseInt($('#' + ratingSumId).text());
        if (ratingValue == -1) {
            ratingSum += 1;
        } else if (ratingValue == 1) {
            ratingSum -= 1;
        }
        ratingSum += offset;
        $('#' + ratingSumId).text(ratingSum);
        $('#' + ratingValueId).text(offset);

        var jsonData = {
            rating: offset,
            articleID: postID
        };


        amplify.request({
            resourceId: "postReactionPOST",
            data: JSON.stringify(jsonData),
            success: function (status, data) {
                console.log(postID + " : " + data);
            },
            error: function (status, message) {
                console.log(message);
            }
        });
    });


// comment like and dislike

    $("#commentList")
        .on('click', "i.comment_like", function () {
            var commentID = $(this).attr("id").slice(0, -5); // removing "_like"
            amplify.publish("comment_like_ui", commentID);
            amplify.publish("update_commentRatingTotal_ui", commentID, 1);
        })
        .on('click', "i.comment_dislike", function () {
            var commentID = $(this).attr("id").slice(0, -8); // removing "_dislike"
            amplify.publish("comment_dislike_ui", commentID);
            amplify.publish("update_commentRatingTotal_ui", commentID, -1);
        })
        .on('click', "i.comment_liked", function () {
            var commentID = $(this).attr("id").slice(0, -5); // removing "_dislike"
            amplify.publish("comment_neutral_ui", commentID);
            amplify.publish("update_commentRatingTotal_ui", commentID, 0);
        })
        .on('click', "i.comment_disliked", function () {
            var commentID = $(this).attr("id").slice(0, -8); // removing "_dislike"
            amplify.publish("comment_neutral_ui", commentID);
            amplify.publish("update_commentRatingTotal_ui", commentID, 0);
        })
        .on('click', ".commentReplyButton", function () {
            amplify.publish("close_all_comment_reply_input"); // first close other opened reply views
            amplify.publish("show_comment_reply_input", this.id);
        })
        .on('click', ".sub_comment_cancel", function () {
            amplify.publish("close_all_comment_reply_input");
        })
        .on('click', ".sub_comment_submit", function () {
            amplify.publish("add_sub_comment", this.id, $('#' + this.id + "_subCommentText").val());
        })
        .on('click', ".hotShowMoreSubComments", function () {
            var postID = $(".comment_submit").attr('id');
            var commentID = $(this).attr("id");
            var div = "#" + commentID + "_subCommentList";
            amplify.request("hotShowMoreSubCommentsGET",
                {
                    commentID: commentID,
                    articleID: postID
                },
                function (result) {
                    $(div).html(result);
                }
            );
        });

    $(".comment_submit").click(function () {
        amplify.publish("add_comment", this.id, $("#comment_text").val());
    });

    $("#modal_comment_edit_content").on('click', ".comment_edit_submit", function () {
        amplify.publish("edit_comment", this.id, $('#' + this.id + "_edited_content").val());
    });


    amplify.subscribe("add_comment", function (postID, commentText) {

        var jsonData = {
            content: commentText,
            articleID: postID
        };

        amplify.request({
            resourceId: "postCommentPOST",
            data: JSON.stringify(jsonData),
            success: function (commentHtml) {
                $("#commentList").prepend(commentHtml);
                $("#comment_text").val("");
            },
            error: function (message) {
                console.log(message);
            }
        });

    });

    amplify.subscribe("add_sub_comment", function (commentID, subCommentText) {

        var jsonData = {
            content: subCommentText,
            commentID: commentID
        };

        amplify.request({
            resourceId: "subCommentPOST",
            data: JSON.stringify(jsonData),
            success: function (subCommentHtml) {
                $('#' + commentID + "_subCommentList").prepend(subCommentHtml);
                amplify.publish("close_all_comment_reply_input");
            },
            error: function (message) {
                console.log(message);
            }
        });

    });


    amplify.subscribe("edit_comment", function (commentID, commentText) {
        var jsonData = {
            content: commentText,
            commentID: commentID
        };

        amplify.request({
            resourceId: "commentPUT",
            data: JSON.stringify(jsonData),
            success: function (subCommentHtml) {
                amplify.publish("close_comment_edit_modal");
                $('#' + commentID.concat("_content")).text(commentText);
            },
            error: function (message) {
                console.log(message);
            }
        });

    });


    // commentReplyButton on click
    amplify.subscribe("show_comment_reply_input", function (commentID) {
        var commentReplyInput = $('#' + commentID.concat("_replyInput"));
        $(commentReplyInput).html(Mustache.render(commentReplyInputTemplate.innerHTML, {
            commentID: commentID,
            avatarURL: $("#avatarURL").attr("src")
        }));
    });

    // commentCancelButton on click
    amplify.subscribe("close_all_comment_reply_input", function () {
        $('.comment_reply_input_field').html("");
    });

    // close comment edit modal
    amplify.subscribe("close_comment_edit_modal", function () {
        $('#modal_comment_edit').modal('hide');
    });


    amplify.subscribe("comment_neutral_ui", function (postID) {
        var likeId = postID.concat("_like");
        var dislikeId = postID.concat("_dislike");
        $('#' + likeId).removeClass("comment_liked").addClass("comment_like");
        $('#' + dislikeId).removeClass("comment_disliked").addClass("comment_dislike");
    });

    amplify.subscribe("comment_dislike_ui", function (postID) {
        var likeId = postID.concat("_like");
        var dislikeId = postID.concat("_dislike");
        $('#' + likeId).removeClass("comment_liked").addClass("comment_like");
        $('#' + dislikeId).removeClass("comment_dislike").addClass("comment_disliked");
    });

    amplify.subscribe("comment_like_ui", function (postID) {
        var likeId = postID.concat("_like");
        var dislikeId = postID.concat("_dislike");
        $('#' + likeId).removeClass("comment_like").addClass("comment_liked");
        $('#' + dislikeId).removeClass("comment_disliked").addClass("comment_dislike");
    });

    amplify.subscribe("update_commentRatingTotal_ui", function (commentID, offset) {
        var ratingValueId = commentID.concat("_rating");
        var ratingSumId = commentID.concat("_ratingSum");
        var ratingValue = parseInt($('#' + ratingValueId).text());
        var ratingSum = parseInt($('#' + ratingSumId).text());
        if (ratingValue == -1) {
            ratingSum += 1;
        } else if (ratingValue == 1) {
            ratingSum -= 1;
        }
        ratingSum += offset;
        $('#' + ratingSumId).text(ratingSum);
        $('#' + ratingValueId).text(offset);

        var jsonData = {
            rating: offset,
            commentID: commentID
        };

        amplify.request({
            resourceId: "commentReactionPOST",
            data: JSON.stringify(jsonData),
            success: function (status, data) {
                // console.log(commentID + " : " + data);
            },
            error: function (status, message) {
                console.log(message);
            }
        });
    });

    amplify.subscribe("postCommentPostResultHTML", function (commentHtml) {
        $("#commentList").prepend(commentHtml);
        $("#comment_text").val("");
    });


    $('#modal_image_full_screen').on('show.bs.modal', function (e) {
        var image_src = $(e.relatedTarget).data('image-url');
        $('#modal_image_full_screen_src').attr('src', image_src);
    });


    // commentEditLink on click
    $('#modal_comment_edit').on('show.bs.modal', function (e) {
        var comment_id = $(e.relatedTarget).data('comment-id');
        var comment_content = $('#' + comment_id + '_content').text();
        $('#modal_comment_edit_content').html(Mustache.render(modalCommentEditContentTemplate.innerHTML, {
            commentID: comment_id,
            avatarURL: $("#avatarURL").attr("src"),
            content: comment_content
        }));
    });


// authentication

    $("#auth_button").click(function () {
        var email = $("#auth_email").val();
        var password = $("#auth_password").val();
        var rememberMe = $("#auth_remember_me").is(":checked");
        var jsonData = {
            email: email,
            password: password,
            rememberMe: rememberMe
        };


        amplify.request({
            resourceId: "auth#signIn",
            data: JSON.stringify(jsonData),
            success: function (status, data) {
                amplify.store("_auth_token", data.token);
            },
            error: function (status, message) {
                console.log(message);
            }
        });


    });


    $("#userNotificationIcon").on('click', function () {
        amplify.request("userNotificationViewedGet",
            function (result) {
                console.log("userNotificationViewedGet")
            }
        );
    });


    $("#hotShowMoreArticles").on('click', function () {
        var pageNumber = parseInt($("#hotArticlesPageNumber").val()) + 1;
        amplify.request("hotShowMoreArticlesGET",
            {
                pageNumber: pageNumber
            },
            function (result) {
                $("#contentList").append(result);
                $("#hotArticlesPageNumber").val(pageNumber);
                amplify.publish("remove_duplicate_post_items");
            }
        );
    });

    $("#newShowMoreArticles").on('click', function () {
        var pageNumber = parseInt($("#newArticlesPageNumber").val()) + 1;
        amplify.request("newShowMoreArticlesGET",
            {
                pageNumber: pageNumber
            },
            function (result) {
                $("#contentList").append(result);
                $("#newArticlesPageNumber").val(pageNumber);
                amplify.publish("remove_duplicate_post_items");
            }
        );
    });


    $("#newShowMoreUserArticles").on('click', function () {
        var pageNumber = parseInt($("#newArticlesPageNumber").val()) + 1;
        var otherUserProfileID = $("#otherUserProfileID").val();
        amplify.request("newShowMoreUserArticlesGET",
            {
                pageNumber: pageNumber,
                userID: otherUserProfileID
            },
            function (result) {
                $("#contentList").append(result);
                $("#newArticlesPageNumber").val(pageNumber);
                amplify.publish("remove_duplicate_post_items");
            }
        );
    });


    $("#hotShowMoreComments").on('click', function () {
        var pageNumber = parseInt($("#hotCommentsPageNumber").val()) + 1;
        var postID = $(".comment_submit").attr('id');
        amplify.request("hotShowMoreCommentsGET",
            {
                pageNumber: pageNumber,
                articleID: postID
            },
            function (result) {
                $("#commentList").append(result);
                $("#hotCommentsPageNumber").val(pageNumber);
                amplify.publish("remove_duplicate_comment_items");
            }
        );
    });


    amplify.subscribe("remove_duplicate_post_items", function () {
        $('.post').each(function () {
            var aDivs = $('[id="' + this.id + '"]');
            aDivs.slice(1).remove();
        });
    });

    amplify.subscribe("remove_duplicate_comment_items", function () {
        $('.comment_wrapper').each(function () {
            var aDivs = $('[id="' + this.id + '"]');
            aDivs.slice(1).remove();
            // $('[id="' + this.id + '"]').concat('_wrapper').slice(1).remove();
        });
    });


    amplify.subscribe("minuteHouseKeeping", function () {
        if (ws) {
            ws.send("ws_send_minuteHouseKeeping");
        }
    });


    // fire every 10 minutes
    setInterval(function () {
        amplify.publish('minuteHouseKeeping')
    }, 600000);

    amplify.subscribe("user_notification_list_refresh", function () {
        var l = amplify.store.sessionStorage("user_notification_list");
        var unreadCount = 0;
        for (var i in l) {
            if (!l[i].read) {
                unreadCount++;
            }
        }
        $('#user_notification_list').html(Mustache.render(userNotificationListTemplate.innerHTML, {
            data: l
        }));
        $('#user_notification_count').html(unreadCount == 0 ? "" : unreadCount);
    });

    amplify.subscribe("user_notification_list_new", function (item) {
        var l = amplify.store.sessionStorage("user_notification_list");
        var found = false;
        for (var i in l) {
            if (l[i].id == item.id) {
                l[i] = item;
                found = true;
                break;
            }
        }
        if (!found) {
            l = [item].concat(l);
        }
        amplify.store.sessionStorage("user_notification_list", l);
        amplify.publish("user_notification_list_refresh");
    });


    //ws

    var ws;
    var wsUrl = $("body").data("ws-url-admin-dashboard");
    if (wsUrl) {
        ws = new WebSocket(wsUrl);
        ws.onmessage = function (event) {
            var message;
            message = JSON.parse(event.data);
            if (message.messageType.toLowerCase() === "user_notification_list") {
                amplify.store.sessionStorage("user_notification_list", message.data);
                amplify.publish("user_notification_list_refresh");
            } else if (message.messageType.toLowerCase() === "user_notification") {
                amplify.publish("user_notification_list_new", message.data);
            }
            return message;
        };
        return $("#msgform").submit(function (event) {
            event.preventDefault();
            ws.send($("#msgtext").val());
            return $("#msgtext").val("");
        });
    }

});

$(document).ready(function () {

});
