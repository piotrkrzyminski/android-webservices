package android.math.uni.lodz.pl.webservices.service;

/**
 * Specified endpoints for web service.
 */
public interface EndpointsConstants {

    String PREFIX = "https://jsonplaceholder.typicode.com/";

    interface Get {
        String POSTS = PREFIX + "posts";
        String POST = PREFIX + "posts/";
        String COMMENTS = PREFIX + "posts/1/comments";
        String COMMENT = PREFIX + "comments?postId=";
    }

    interface Post {
        String POST = PREFIX + "posts";
    }
}
