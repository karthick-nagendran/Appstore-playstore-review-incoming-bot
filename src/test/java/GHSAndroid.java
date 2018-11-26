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

public class GHSAndroid {


    HttpClient client = null;
    HttpRequest request = null;
    HttpResponse response = null;
    HttpPost post = null;

    ArrayList<Integer> rating = new ArrayList<>();
    ArrayList<String> review = new ArrayList<>();
    ArrayList<String> finalString = new ArrayList<>();


    public JSONArray getJSONData() throws IOException, JSONException {
        client = HttpClientBuilder.create().build();
        request = new HttpGet("https://still-plateau-10039.herokuapp.com/reviews?id=com.tesco.grocery.view");

        response = client.execute((HttpUriRequest) request);
        String json = EntityUtils.toString(response.getEntity());
        return new JSONArray(json);
    }

    public int dateVerification(String rDate) throws ParseException {

        DateFormat responseDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        String now = LocalDate.now().toString();
        DateFormat localDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date reviewDate = responseDateFormat.parse(rDate);
        Date currentDate = localDateFormat.parse(now);
        return (int) ((currentDate.getTime() - reviewDate.getTime()) / (1000 * 60 * 60 * 24));
    }

    public void filterData() throws IOException, JSONException, ParseException {

        JSONArray entry = getJSONData();
        for (int i = 0; i < entry.length() - 1; i++) {
            JSONObject obj = entry.getJSONObject(i);
            String rDate = (String) obj.get("date");
            int noOfDays = dateVerification(rDate);
            if (noOfDays == 1) {
                rating.add((Integer) obj.get("score"));

                String rvString = (String) obj.get("text");
                byte[] bytes = rvString.getBytes("ISO-8859-1");
                String rvStringFormatted = new String(bytes, "UTF-8");
                review.add(rvStringFormatted);
            } else if (noOfDays > 1)
                break;
        }
    }

    public void stringBuilder() throws JSONException, ParseException, IOException {
        filterData();
        for (int i = 0; i < review.size(); i++) {
            String starRating = ":star:";
            int rate = rating.get(i);
            while (rate > 1) {
                starRating = starRating + " :star:";
                rate--;
            }
            finalString.add(String.format("%s\n\n %s\n\n*OS* : `Android` ", starRating, review.get(i)));
        }
    }



    public void executeCommand() throws JSONException, IOException, ParseException {
        stringBuilder();
        client = HttpClientBuilder.create().build();
        post = new HttpPost("https://hooks.slack.com/services/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");
        JSONObject finalObj = new JSONObject();
        for (String finalS : finalString
                ) {
            finalObj.put("text", finalS);
            post.setEntity(new StringEntity(finalObj.toString(), "UTF8"));
            client.execute(post);
            post.releaseConnection();
        }
    }


    public void darecheck() throws ParseException, IOException, JSONException {

        stringBuilder();
        for (String finalS : finalString
                ) {
            System.out.println(finalS);
        }

    }
}
