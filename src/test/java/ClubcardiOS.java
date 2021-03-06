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
import java.io.UnsupportedEncodingException;
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
    ArrayList<String > reviewDate = new ArrayList<>();


    private JSONArray getJSONData() throws IOException, JSONException {
        client = HttpClientBuilder.create().build();
        request = new HttpGet("https://itunes.apple.com/gb/rss/customerreviews/page=1/id=<App_ID 9 digits app id from devloper account>/sortby=mostrecent/xml");

        response = client.execute((HttpUriRequest) request);
        String xml = EntityUtils.toString(response.getEntity());
        JSONObject obj = XML.toJSONObject(xml);
        JSONObject feed = (JSONObject) obj.get("feed");
        return (JSONArray) feed.get("entry");
    }

    private int dateVerification(String onlyDate) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String now = LocalDate.now().toString();
        Date reviewDate = dateFormat.parse(onlyDate);
        Date currentDate = dateFormat.parse(now);
        return (int) ((currentDate.getTime() - reviewDate.getTime()) / (1000 * 60 * 60 * 24));
    }

    private void filterData() throws IOException, JSONException, ParseException {
        JSONArray entry = getJSONData();
        System.out.println(entry);
        for (int i = 0; i < entry.length() - 1; i++) {
            JSONObject obj = entry.getJSONObject(i);
            String rDate = (String) obj.get("updated");
            String onlyDate = rDate.split("T", 0)[0];
            int noOfDays = dateVerification(onlyDate);
            if (noOfDays == 1) {
                reviewDate.add(onlyDate);
                title.add(formatString((String) obj.get("title")));
                rating.add((Integer) obj.get("im:rating"));
                oSVersion.add((String) obj.get("im:version"));
                JSONArray contentArray = (JSONArray) obj.get("content");
                JSONObject contentObj = (JSONObject) contentArray.get(0);
                review.add(formatString((String) contentObj.get("content")));
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
            finalString.add(String.format("%s\n## %s\n%s\n\nOS: `iOS` %s ", starRating.toString(), title.get(i), review.get(i), oSVersion.get(i)));
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

            attachmentJson.put("author_name", "iOS");
            attachmentJson.put("fallback", "iOS Reviews");
            attachmentJson.put("color", colourSet(rating.get(i)));
            attachmentJson.put("pretext", starBuilder(rating.get(i)));
            attachmentJson.put("text", review.get(i));
            attachmentJson.put("title", title.get(i));

            attachmentArray.put(attachmentJson);


            JSONObject fieldsJson = new JSONObject();
            fieldsJson.put("short", true);
            fieldsJson.put("title", "App Version");
            fieldsJson.put("value", oSVersion.get(i));

            JSONObject fieldJsonDate = new JSONObject();
            fieldJsonDate.put("short",true);
            fieldJsonDate.put("title","Date");
            fieldJsonDate.put("value",reviewDate.get(i));


            JSONArray fieldsArray = new JSONArray();
            fieldsArray.put(fieldsJson);
            fieldsArray.put(fieldJsonDate);

            attachmentJson.put("fields", fieldsArray);
            finalJson.put("attachments", attachmentArray);
            post.setEntity(new StringEntity(finalJson.toString()));
            client.execute(post);
            post.releaseConnection();
            System.out.println(finalJson.toString());
        }
        System.out.println("iOS reviews done");
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

    private String  formatString(String unformattedString ) throws UnsupportedEncodingException {
        byte[] bytes = unformattedString.getBytes("ISO-8859-1");
        return new String(bytes, "UTF-8");
    }

    public void darecheck() throws ParseException, IOException, JSONException {
    }
}
