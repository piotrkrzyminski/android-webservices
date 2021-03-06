package android.math.uni.lodz.pl.webservices.service;

import android.math.uni.lodz.pl.webservices.model.Post;

public interface ClientService {

    /**
     * Displays all posts as string value on view.
     */
    void displayAllPosts();

    /**
     * Displays post with specified id on view.
     *
     * @param id id of post.
     */
    void displayPostWithId(String id);

    /**
     * Displays all comments on view.
     */
    void displayAllComments();

    /**
     * Displays comment for post with specified id.
     *
     * @param id id of post which comment will be shown.
     */
    void displayCommentsForPostWithId(String id);

    /**
     * Sends new post to web service.
     *
     * @param title post title.
     * @param body post body.
     */
    void sendPost(String title, String body);
}
