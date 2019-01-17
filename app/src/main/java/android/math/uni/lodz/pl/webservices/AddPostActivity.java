package android.math.uni.lodz.pl.webservices;

import android.app.Activity;
import android.math.uni.lodz.pl.webservices.model.Post;
import android.math.uni.lodz.pl.webservices.service.ClientService;
import android.math.uni.lodz.pl.webservices.service.impl.DefaultClientService;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Create new post activity.
 */
public class AddPostActivity extends Activity {

    private ClientService clientService;

    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        resultTextView = findViewById(R.id.post_response);

        clientService = new DefaultClientService<>(resultTextView);
    }

    /**
     * Action assigned to send button. Get text passed by user, create post object and send data.
     * @param view button.
     */
    public void onPostSendAction(View view) {
        TextView titleTextView = findViewById(R.id.post_title);
        TextView bodyTextView = findViewById(R.id.post_body);

        final String title = titleTextView.getText().toString();
        final String body = bodyTextView.getText().toString();

        if(title.isEmpty() && body.isEmpty()) {
            resultTextView.setText(R.string.post_empty_data);
        } else {
            clientService.sendPost(title, body);
            titleTextView.setText(null);
            bodyTextView.setText(null);
        }
    }
}
