import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

public class ClubcardAndroid {

    HttpClient client = null;
    HttpRequest request = null;
    HttpResponse response = null;
    HttpPost post = null;

    ArrayList<Integer> rating = new ArrayList<>();
    ArrayList<String> review = new ArrayList<>();
    ArrayList<String> finalString = new ArrayList<>();
    ArrayList<String> reviewDate = new ArrayList<>();


    private JSONArray getJSONData() throws IOException, JSONException {
        client = HttpClientBuilder.create().build();
        request = new HttpGet("https://still-plateau-10039.herokuapp.com/reviews?id=com.tesco.clubcardmobile");

        response = client.execute((HttpUriRequest) request);
        String json = EntityUtils.toString(response.getEntity());
        return new JSONArray(json);
    }

    private int dateVerification(String rDate) throws ParseException {
        DateFormat responseDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        String now = LocalDate.now().toString();
        DateFormat localDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date reviewDate = responseDateFormat.parse(rDate);
        Date currentDate = localDateFormat.parse(now);
        return (int) ((currentDate.getTime() - reviewDate.getTime()) / (1000 * 60 * 60 * 24));
    }

    private void filterData() throws IOException, JSONException, ParseException {
        JSONArray entry = getJSONData();
        System.out.println(entry);
        for (int i = 0; i < entry.length() - 1; i++) {
            JSONObject obj = entry.getJSONObject(i);
            String rDate = (String) obj.get("date");
            int noOfDays = dateVerification(rDate);
            if (noOfDays == 1) {
                reviewDate.add(rDate);
                rating.add((Integer) obj.get("score"));

                String rvString = (String) obj.get("text");
                byte[] bytes = rvString.getBytes("ISO-8859-1");
                String rvStringFormatted = new String(bytes, "UTF-8");
                review.add(rvStringFormatted);
            } else if (noOfDays > 1)
                break;
        }
    }

    private void stringBuilder() throws JSONException, ParseException, IOException {
        filterData();
        for (int i = 0; i < review.size(); i++) {
            StringBuilder starRating = new StringBuilder(":star:");
            int rate = rating.get(i);
            while (rate > 1) {
                starRating.append(" :star:");
                rate--;
            }
            finalString.add(String.format("%s\n\n %s\n\n**OS** : `Android` ", starRating.toString(), review.get(i)));
        }
    }


    @Test
    public void executeCommand() throws JSONException, IOException, ParseException {
        stringBuilder();
        client = HttpClientBuilder.create().build();
        post = new HttpPost("https://mattermost.ocset.net/hooks/w61bhukhjibtuk4t78zaukhr3r");
        for (int i = 0; i < review.size(); i++) {
            JSONObject finalJson = new JSONObject();
            finalJson.put("username", "REVIEWS");

            JSONArray attachmentArray = new JSONArray();
            JSONObject attachmentJson = new JSONObject();

            attachmentJson.put("author_name", "Android");
            attachmentJson.put("fallback", "Android Reviews");
            attachmentJson.put("color", colourSet(rating.get(i)));
            attachmentJson.put("pretext", starBuilder(rating.get(i)));
            attachmentJson.put("text", review.get(i));

            attachmentArray.put(attachmentJson);

            JSONObject fieldsJson = new JSONObject();
            fieldsJson.put("short", true);
            fieldsJson.put("title", "date");
            fieldsJson.put("value", reviewDate.get(i));

            JSONArray fieldsArray = new JSONArray();
            fieldsArray.put(fieldsJson);

            attachmentJson.put("fields", fieldsArray);
            finalJson.put("attachments", attachmentArray);
            post.setEntity(new StringEntity(finalJson.toString()));
            client.execute(post);
            post.releaseConnection();
            System.out.println(finalJson.toString());
        }
        System.out.println("Android reviews done");
    }

    private String colourSet(int rating) {
        String tagColour = "";
        switch (rating) {
            case 1:
            case 2:
                tagColour = "#FF0000";
                break;
            case 3:
                tagColour = "#FF8C00";
                break;
            case 4:
            case 5:
                tagColour = "#008000";
        }
        return tagColour;
    }

    private String starBuilder(int rate) {
        StringBuilder starRating = new StringBuilder(":star:");
        while (rate > 1) {
            starRating.append(" :star:");
            rate--;
        }
        return starRating.toString();
    }

    public void darecheck() throws ParseException, IOException, JSONException {
        stringBuilder();
    }
}
