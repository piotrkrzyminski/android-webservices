package android.math.uni.lodz.pl.webservices.service.impl;

import android.math.uni.lodz.pl.webservices.R;
import android.math.uni.lodz.pl.webservices.model.Comment;
import android.math.uni.lodz.pl.webservices.model.Displayable;
import android.math.uni.lodz.pl.webservices.model.Post;
import android.math.uni.lodz.pl.webservices.service.ClientService;
import android.math.uni.lodz.pl.webservices.service.EndpointsConstants;
import android.os.AsyncTask;
import android.text.Html;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Service used for get and send data from and to selected endpoints.
 *
 * @param <T> Response type.
 * @author Piotr Krzyminski
 */
public class DefaultClientService<T extends Displayable> implements ClientService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultClientService.class);

    private Class responseType; // stores data type for Jackson deserialization.

    private TextView view; // component where data will be loaded.

    private ObjectMapper mapper;

    public DefaultClientService(TextView view) {
        this.view = view;
        mapper = new ObjectMapper();
    }

    @Override
    public void displayAllPosts() {
        setDataType(Post.class);
        new GetDataTask().execute(EndpointsConstants.Get.POSTS);
    }

    @Override
    public void displayPostWithId(String id) {
        setDataType(Post.class);
        new GetDataTask().execute(EndpointsConstants.Get.POST + id);
    }

    @Override
    public void displayAllComments() {
        setDataType(Comment.class);
        new GetDataTask().execute(EndpointsConstants.Get.COMMENTS);
    }

    @Override
    public void displayCommentsForPostWithId(String id) {
        setDataType(Comment.class);
        new GetDataTask().execute(EndpointsConstants.Get.COMMENT + id);
    }

    @Override
    public void sendPost(String title, String body) {
        setDataType(Post.class);

        final Post post = new Post(1,1, title, body);
        try {

            String jsonData = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(post);
            new PostDataTask().execute(EndpointsConstants.Post.POST, jsonData);
        } catch (JsonProcessingException e) {
            LOG.error("Data couldn't be send", e);
        }
    }

    private void setDataType(Class responseType) {
        this.responseType = responseType;
    }

    /**
     * Async task for connecting to http endpoint. Allows to send data in json format
     * to selected url.
     */
    private class PostDataTask extends AsyncTask<String, Void, Void> {

        private HttpsURLConnection httpsURLConnection;
        private boolean postSend = false;

        @Override
        protected Void doInBackground(String... strings) {
            try {
                final URL url = new URL(strings[0]);
                final String jsonData = strings[1];
                httpsURLConnection = (HttpsURLConnection) url.openConnection();
                httpsURLConnection.setRequestMethod("POST");

                OutputStream outputStream = new BufferedOutputStream(httpsURLConnection.getOutputStream());
                writeData(outputStream, jsonData);

                httpsURLConnection.connect();
                postSend = true;
            } catch (MalformedURLException e) {
                LOG.error("Error: ", e);
            } catch (IOException e) {
                LOG.error("Connection failed: ", e);
            } finally {
                httpsURLConnection.disconnect();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(postSend) {
                view.setText(R.string.post_send_success);
            } else {
                view.setText(R.string.post_send_failed);
            }
        }

        /**
         * Creates buffered writer and write json data.
         *
         * @param outputStream output stream from url.
         * @param data         data to write.
         * @throws IOException IO exception.
         */
        private void writeData(OutputStream outputStream, String data) throws IOException {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"))) {
                writer.write(data);
                writer.flush();
            }
        }
    }

    /**
     * Async task for connecting to http endpoint. Receive data from url passed in parameter.
     * Json response is deserialize by using Jackson library and convert to string output.
     * At the end string output is printed on screen.
     */
    private class GetDataTask extends AsyncTask<String, Void, List<T>> {

        private HttpsURLConnection httpsURLConnection = null;

        @Override
        protected List<T> doInBackground(String... strings) {

            try {
                URL url = new URL(strings[0]);
                httpsURLConnection = (HttpsURLConnection) url.openConnection();
                if (httpsURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    LOG.debug("Http status 200: OK");
                    final String json = readFromReader(
                            new BufferedInputStream(httpsURLConnection.getInputStream()));

                    return getPostsFromJson(json);
                }
            } catch (MalformedURLException e) {
                LOG.error("Error: ", e);
            } catch (IOException e) {
                LOG.warn("Connection failed: ", e);
            } finally {
                httpsURLConnection.disconnect();
            }

            return Collections.emptyList();
        }

        @Override
        protected void onPostExecute(List<T> data) {
            if (data.isEmpty()) {
                view.setText(R.string.no_posts);
            }
            view.setText(Html.fromHtml(getViewText(data), Html.FROM_HTML_MODE_LEGACY));
        }

        /**
         * Uses Jackson library to deserialize json from response to list of objects.
         *
         * @param jsonResponse json response as string.
         * @return list of items with added data from json.
         */
        private List<T> getPostsFromJson(String jsonResponse) {
            List<T> posts = new ArrayList<>();
            mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            TypeFactory typeFactory = mapper.getTypeFactory();

            try {
                posts = mapper.readValue(jsonResponse,
                        typeFactory.constructCollectionType(List.class, responseType));

                LOG.debug("Json response parsed to list of posts");
            } catch (IOException e) {
                LOG.error("Response cannot be parsed. Error: {}", e.getMessage());
            }

            return posts;
        }

        /**
         * Converts list of generic items to string value using {@link Displayable#display()} method.
         *
         * @param dataList list of items from response.
         * @return build string value.
         */
        private String getViewText(List<T> dataList) {
            StringBuilder builder = new StringBuilder();
            for (T data : dataList) {
                builder.append(data.display());
            }

            return builder.toString();
        }

        /**
         * Creates buffered reader from input stream and read result line by line.
         *
         * @param inputStream input stream.
         * @return string value read from input stream.
         * @throws IOException read exception.
         */
        private String readFromReader(InputStream inputStream) throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            return stringBuilder.toString();
        }
    }
}
