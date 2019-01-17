package android.math.uni.lodz.pl.webservices;

import android.app.Activity;
import android.math.uni.lodz.pl.webservices.service.ClientService;
import android.math.uni.lodz.pl.webservices.service.impl.DefaultClientService;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Main activity. Shows data from http response.
 *
 * @author Piotr Krzyminski.
 */
public class MainActivity extends Activity {

    private ClientService clientService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createListeners();

        TextView resultView = findViewById(R.id.posts_view);


        clientService = new DefaultClientService<>(resultView);
        clientService.displayAllPosts();
    }

    /**
     * Creates listeners and add it to controls.
     */
    public void createListeners() {
        EditText postIdInput = findViewById(R.id.post_id_edit);
        EditText postIdComments = findViewById(R.id.post_id_comments);
        Button postsButton = findViewById(R.id.all_posts_button);
        Button commentsButton = findViewById(R.id.all_comments_buton);

        postsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clientService.displayAllPosts();
            }
        });

        commentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clientService.displayAllComments();
            }
        });

        postIdInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clientService.displayPostWithId(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // do nothing
            }
        });

        postIdComments.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clientService.displayCommentsForPostWithId(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // do nothing
            }
        });
    }
}
