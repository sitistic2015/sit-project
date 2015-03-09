package fr.istic.sit.coordsender;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Pair;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class CoordsSenderService extends Service {

    public static final int MSG_ZONE = 1;
    public static final int MSG_POINT = 2;

    @Override
    public void onCreate() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Coordinates coords = msg.getData().getParcelable("coord");
            switch (msg.what) {
                case MSG_POINT:
                    //send point to the server
                    new RequestTask(MSG_POINT, coords, msg.replyTo).execute();
                    break;
                case MSG_ZONE:
                    //send zone to the server
                    new RequestTask(MSG_ZONE, coords, msg.replyTo).execute();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());
}
