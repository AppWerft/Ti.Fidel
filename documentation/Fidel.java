package com.fidel.sdk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import com.fidel.sdk.view.EnterCardDetailsActivity;
import org.json.JSONObject;

public class Fidel {
   public static final String FIDEL_DEBUG_TAG = "Fidel.DEBUG";
   public static int FIDEL_LINK_CARD_REQUEST_CODE = 1624;
   public static String FIDEL_LINK_CARD_RESULT_CARD = "card";
   public static Bitmap bannerImage = null;
   public static boolean autoScan = false;
   public static String programId = null;
   public static String apiKey = null;
   public static JSONObject metaData = null;
   public static Fidel.Country country = null;
   public static String companyName = "Company Name";
   public static String privacyURL;
   public static String deleteInstructions = "going to your account settings";

   public static void present(Activity startingActivity) {
      Intent var1 = new Intent(startingActivity, EnterCardDetailsActivity.class);
      startingActivity.startActivityForResult(var1, FIDEL_LINK_CARD_REQUEST_CODE);
   }

   public static enum Country {
      UNITED_KINGDOM,
      UNITED_STATES,
      IRELAND,
      SWEDEN,
      JAPAN;
   }
}