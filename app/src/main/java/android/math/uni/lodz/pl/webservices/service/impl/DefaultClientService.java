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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Service used for connect to http endpoint and load data from response to activity view.
 *
 * @param <T> Response type.
 * @author Piotr Krzyminski
 */
public class DefaultClientService<T extends Displayable> implements ClientService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultClientService.class);

    private Class responseType; // stores data type for Jackson deserialization.

    private TextView view; // component where data will be loaded.

    public DefaultClientService(TextView view) {
        this.view = view;
    }

    @Override
    public void displayAllPosts() {
        setResponseType(Post.class);
        new GetDataTask().execute(EndpointsConstants.Get.POSTS);
    }

    @Override
    public void displayPostWithId(String id) {
        setResponseType(Post.class);
        new GetDataTask().execute(EndpointsConstants.Get.POST + id);
    }

    @Override
    public void displayAllComments() {
        setResponseType(Comment.class);
        new GetDataTask().execute(EndpointsConstants.Get.COMMENTS);
    }

    @Override
    public void displayCommentsForPostWithId(String id) {
        setResponseType(Comment.class);
        new GetDataTask().execute(EndpointsConstants.Get.COMMENT + id);
    }

    private void setResponseType(Class responseType) {
        this.responseType = responseType;
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
                LOG.error("Error: {}", e.getMessage());
            } catch (IOException e) {
                LOG.warn("Connection failed");
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
            ObjectMapper mapper = new ObjectMapper();
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
