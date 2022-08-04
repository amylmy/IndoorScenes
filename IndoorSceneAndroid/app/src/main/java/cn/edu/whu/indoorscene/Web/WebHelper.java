package cn.edu.whu.indoorscene.Web;

import android.os.Message;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import cn.edu.whu.indoorscene.Fragment.MapFragment;

public class WebHelper {

    public final static String baseURL = "http://139.196.73.25:8080";

    private String responseString = "";

    public WebHelper(){

    }

    // LMY: Upload data to server, url need to add "/classify_upload"
    public void PostData(final String str) {
        Thread con = new Thread(){
            @Override
            public void run(){
                try {
                    synchronized (this) {
                        HttpClient Client = new DefaultHttpClient();
                        String URL1 = baseURL + "/classify";
                        Log.i("GO", URL1);
                        HttpPost httpPost = new HttpPost(URL1);
                        MultipartEntity reqEntity = new MultipartEntity();
                        File imgPath = new File(str);
                        reqEntity.addPart("imagefile", new FileBody(imgPath));
                        httpPost.setEntity(reqEntity);
                        Log.i("GO", "Entering the server session");
                        HttpResponse response1 = Client.execute(httpPost);
                        Log.i("GO", "Response Ok");
                        HttpEntity entity = response1.getEntity();
                        Log.i("GO", "Entity Ok");
                        responseString = getASCIIContentFromEntity(entity);
                        if(responseString != null) {
                            JSONObject jsonObject = new JSONObject(responseString);
                            int isSuccess = jsonObject.getInt("success");
                            if (isSuccess == 1) {
                                Log.i("GO", "GET RESULTS SUCCESS!");
                                Message msg = Message.obtain();
                                msg.what = 1;
                                msg.obj = jsonObject;
                                MapFragment.wifiHandler.sendMessage(msg);
                            } else {
                                Log.i("GO", "NOT SUCCESS");
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
        con.start();
    }

    public String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException {
        InputStream in = entity.getContent();
        StringBuffer out = new StringBuffer();
        int n = 1;
        while (n > 0) {
            byte[] b = new byte[4096];
            n =  in.read(b);
            if (n > 0) out.append(new String(b, 0, n));
        }
        return out.toString();
    }
}