package android.math.uni.lodz.pl.webservices.model;

/**
 * Post model object.
 *
 * @author Piotr Krzyminski
 */
public class Post implements Displayable {

    private int userId;

    private int id;

    private String title;

    private String body;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String display() {
        return "<b>" + title + "</b><br><br>" + body + "<br><br><br>";
    }
}
