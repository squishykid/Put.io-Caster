package com.google.sample.cast.refplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.sample.cast.refplayer.utils.Utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class AddTransfers extends FragmentActivity {
	
	//int fragmentType;

    private EditText textUrls;
	
	SharedPreferences sharedPrefs;

    private static String tokenWithStuff;
    private static String token;
    public final static String baseUrl = "https://api.put.io/v2/";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_addtransfer);
		
		if (getIntent().getAction() != null) {
			if (getIntent().getScheme().matches("magnet")) {
				//fragmentType = -1;//Utils.ADDTRANSFER_URL;
			}
		}
		
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		token = sharedPrefs.getString("token", null);
		if (token == null || token.isEmpty()) {
			Intent videoBrowserActivity = new Intent(this, VideoBrowserActivity.class);
			startActivity(videoBrowserActivity);
			finish();
		}
        tokenWithStuff = "?oauth_token=" + token;
		
		TextView textTitle = (TextView) findViewById(R.id.dialog_title);
		textTitle.setText(getString(R.string.addtransferstitle));
        textUrls = (EditText) findViewById(R.id.edittext_addtransfer_urls);
        try {
            textUrls.setText(getIntent().getDataString());
        } catch (NullPointerException e) {

        }
		
		Button addButton = (Button) findViewById(R.id.button_addtransfer_add);
		addButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
                addUrl();
			}
		});
		
		Button cancelButton = (Button) findViewById(R.id.button_addtransfer_cancel);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}
	
	/*private void addUrl() {
		if (getEnteredUrls().isEmpty()) {
			Intent addTransferIntent = new Intent(AddTransfers.this, VideoBrowserActivity.class);//TODO DO JSON STUFF HERE
			addTransferIntent.putExtra("mode", -1);//Utils.ADDTRANSFER_URL);
			addTransferIntent.putExtra("url", getEnteredUrls());
			startActivity(addTransferIntent);
			finish();
		} else {
			Toast.makeText(AddTransfers.this, getString(R.string.nothingenteredtofetch), Toast.LENGTH_LONG).show();
		}
	}*/

    private void addUrl() {
        if (!getEnteredUrls().isEmpty()) {
            Utils.addTransfersAsync(this, getIntent(), getEnteredUrls(), tokenWithStuff);
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(AddTransfers.this, getString(R.string.nothingenteredtofetch), Toast.LENGTH_LONG).show();
        }
    }

    private String getEnteredUrls() {
        return textUrls.getText().toString();
    }
}