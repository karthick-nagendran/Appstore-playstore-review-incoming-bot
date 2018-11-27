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
import org.json.XML;
import org.testng.annotations.Test;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

public class ClubcardiOS {

    HttpClient client = null;
    HttpRequest request = null;
    HttpResponse response = null;
    HttpPost post = null;

    ArrayList<String> title = new ArrayList<>();
    ArrayList<Integer> rating = new ArrayList<>();
    ArrayList<String> oSVersion = new ArrayList<>();
    ArrayList<String> review = new ArrayList<>();
    ArrayList<String> finalString = new ArrayList<>();


    public JSONArray getJSONData() throws IOException, JSONException {
        client = HttpClientBuilder.create().build();
        request = new HttpGet("https://itunes.apple.com/gb/rss/customerreviews/page=1/id=351841850/sortby=mostrecent/xml");

        response = client.execute((HttpUriRequest) request);
        String xml = EntityUtils.toString(response.getEntity());
        JSONObject obj = XML.toJSONObject(xml);
        JSONObject feed = (JSONObject) obj.get("feed");
        return (JSONArray) feed.get("entry");
    }

    public int dateVerification(String rDate) throws ParseException {
        String onlyDate = rDate.split("T", 0)[0];

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String now = LocalDate.now().toString();
        Date reviewDate = dateFormat.parse(onlyDate);
        Date currentDate = dateFormat.parse(now);
        return (int) ((currentDate.getTime() - reviewDate.getTime()) / (1000 * 60 * 60 * 24));
    }

    public void filterData() throws IOException, JSONException, ParseException {

        JSONArray entry = getJSONData();
        for (int i = 0; i < entry.length() - 1; i++) {
            JSONObject obj = entry.getJSONObject(i);
            String rDate = (String) obj.get("updated");
            int noOfDays = dateVerification(rDate);
            if (noOfDays == 1) {
                title.add((String) obj.get("title"));
                rating.add((Integer) obj.get("im:rating"));
                oSVersion.add((String) obj.get("im:version"));
                JSONArray contentArray = (JSONArray) obj.get("content");
                JSONObject contentObj = (JSONObject) contentArray.get(0);

                String rvString = (String) contentObj.get("content");
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
            finalString.add(String.format("%s\n## %s\n%s\n\nOS: `iOS` %s ", starRating, title.get(i), review.get(i), oSVersion.get(i)));
        }
    }

    @Test
    public void executeCommand() throws JSONException, IOException, ParseException {
        stringBuilder();
        client = HttpClientBuilder.create().build();
        post = new HttpPost("https://mattermost.ocset.net/hooks/w61bhukhjibtuk4t78zaukhr3r");
        JSONObject finalObj = new JSONObject();
        for (String finalS : finalString
                ) {
            finalObj.put("text", finalS);
            post.setEntity(new StringEntity(finalObj.toString(), "UTF8"));
            client.execute(post);
            post.releaseConnection();
            System.out.println("iOS successfully completed");
        }
    }


    public void darecheck() throws ParseException, IOException, JSONException {

        stringBuilder();
    }
}
