var my_decoder = function (result, status, xhr, success, error) {
    if (xhr.status == 401 || xhr.status == 403) {
        window.location.href = "/auth/signIn";
        return;
    }
    if (result.success === true) {
        success(result.success, result.data);
    } else {
        error(result.success, result.message);
    }
};


amplify.request.define("postReactionPOST", "ajax", {
    url: "/ajax/posts/reaction",
    dataType: "json",
    type: "POST",
    contentType: "application/json",
    decoder: my_decoder
});


amplify.request.define("postCommentPOST", "ajax", {
    url: "/ajax/comments",
    dataType: "json",
    type: "POST",
    contentType: "application/json",
    decoder: function (result, status, xhr, success, error) {
        if (xhr.status == 401) {
            window.location.href = "/auth/signIn";
            return;
        }
        if (xhr.responseText)
            success(xhr.responseText);
    }
});

amplify.request.define("subCommentPOST", "ajax", {
    url: "/ajax/subComments",
    dataType: "json",
    type: "POST",
    contentType: "application/json",
    decoder: function (result, status, xhr, success, error) {
        if (xhr.status == 401) {
            window.location.href = "/auth/signIn";
            return;
        }
        if (xhr.responseText)
            success(xhr.responseText);
    }
});

amplify.request.define("commentPUT", "ajax", {
    url: "/ajax/comments",
    dataType: "json",
    type: "PUT",
    contentType: "application/json",
    decoder: function (result, status, xhr, success, error) {
        if (xhr.status == 401) {
            window.location.href = "/auth/signIn";
            return;
        }
        if (xhr.responseText)
            success(xhr.responseText);
    }
});


amplify.request.define("userNotificationViewedGet", "ajax", {
    url: "/ajax/notification/clickaction",
    contentType: "application/json",
    type: "GET"
});

amplify.request.define("commentReactionPOST", "ajax", {
    url: "/ajax/comments/reaction",
    dataType: "json",
    type: "POST",
    contentType: "application/json",
    decoder: my_decoder
});


amplify.request.define("newShowMoreArticlesGET", "ajax", {
    url: "/ajax/posts/new/{pageNumber}",
    contentType: "application/json",
    type: "GET"
});

amplify.request.define("newShowMoreUserArticlesGET", "ajax", {
    url: "/ajax/user/{userID}/posts/new/{pageNumber}",
    contentType: "application/json",
    type: "GET"
});

amplify.request.define("hotShowMoreArticlesGET", "ajax", {
    url: "/ajax/posts/hot/{pageNumber}",
    contentType: "application/json",
    type: "GET"
});

amplify.request.define("newShowMoreCommentsGET", "ajax", {
    url: "/ajax/posts/{postID}/comments/new/{pageNumber}",
    contentType: "application/json",
    type: "GET"
});

amplify.request.define("hotShowMoreCommentsGET", "ajax", {
    url: "/ajax/posts/{postID}/comments/hot/{pageNumber}",
    contentType: "application/json",
    type: "GET"
});


amplify.request.define("hotShowMoreSubCommentsGET", "ajax", {
    url: "/ajax/posts/{postID}/comments/{commentID}/subComments/hot",
    contentType: "application/json",
    type: "GET"
});

