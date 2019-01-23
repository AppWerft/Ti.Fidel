package ti.fidel;

import java.io.IOException;
import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.io.TiBaseFile;
import org.appcelerator.titanium.io.TiFileFactory;
import org.appcelerator.titanium.util.TiActivityResultHandler;
import org.appcelerator.titanium.util.TiActivitySupport;
import org.appcelerator.titanium.util.TiUIHelper;
import org.json.JSONException;
import org.json.JSONObject;
import com.fidel.sdk.Fidel;
import com.fidel.sdk.LinkResult;
import com.fidel.sdk.view.EnterCardDetailsActivity;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;

@Kroll.module(name = "Tifidel", id = "ti.fidel", propertyAccessors = { "onPaymentDidComplete" })
public class TifidelModule extends KrollModule implements TiActivityResultHandler {
	private static final String LCAT = "💰 TiFidel";
	private static TifidelModule _instance;
	@Kroll.constant
	public static final String COUNTRY_UNITED_KINGDOM = "UNITED_KINGDOM";
	@Kroll.constant
	public static final String COUNTRY_UNITED_STATES = "UNITED_STATES";
	@Kroll.constant
	public static final String COUNTRY_JAPAN = "JAPAN";
	@Kroll.constant
	public static final String COUNTRY_SWEDEN = "SWEDEN";
	@Kroll.constant
	public static final String COUNTRY_IRELAND = "IRELAND";

	private static final String PROP_PAYMENT_DID_COMPLETE = "paymentDidComplete";
	private static final String PROP_ONPAYMENT_DID_COMPLETE = "onPaymentDidComplete";
	private static final String PROP_ERROR = "Error";
	private static KrollFunction onPaymentDidCompleteCallback;
	private static KrollFunction onErrorCallback;

	public TifidelModule() {
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app) {
	}

	@Kroll.method
	public void init(KrollDict opts) {
		if (opts.containsKeyAndNotNull("apiKey")) {
			Fidel.apiKey = opts.getString("apiKey");
		}
		if (opts.containsKeyAndNotNull("programId")) {
			Fidel.programId = opts.getString("programId");
		}
		if (opts.containsKeyAndNotNull("bannerImage")) {
			Fidel.bannerImage = loadImageFromApplication(opts.getString("apiKey"));
		}
		if (opts.containsKeyAndNotNull("companyName")) {
			Fidel.companyName = opts.getString("companyName");
		}
		if (opts.containsKeyAndNotNull("country")) {
			Fidel.country = Fidel.Country.valueOf(opts.getString("country"));
		}
		if (opts.containsKeyAndNotNull("privacyURL")) {
			Fidel.privacyURL = opts.getString("privacyURL");
		}
		if (opts.containsKeyAndNotNull("metaData")) {
			Fidel.metaData = new JSONObject(opts.getKrollDict("metaData"));
		}
		if (opts.containsKeyAndNotNull(PROP_PAYMENT_DID_COMPLETE)) {
			Object o = opts.get(PROP_PAYMENT_DID_COMPLETE);
			if (o instanceof KrollFunction) {
				onPaymentDidCompleteCallback = (KrollFunction) o;
			}
		}
		if (hasProperty(PROP_ONPAYMENT_DID_COMPLETE)) {
			Object o = getProperty(PROP_ONPAYMENT_DID_COMPLETE);
			if (o instanceof KrollFunction) {
				onPaymentDidCompleteCallback = (KrollFunction) o;
				Log.w(LCAT, "onPaymentDidCompleteCallback imported");
			} else
				Log.w(LCAT, PROP_ONPAYMENT_DID_COMPLETE +" isn't a Krollfunction");
		} else
			Log.w(LCAT, PROP_PAYMENT_DID_COMPLETE + " is missing");
		if (hasProperty("onErrorComplete")) {
			Object o = getProperty("onErrorComplete");
			if (o instanceof KrollFunction) {
				onErrorCallback = (KrollFunction) o;
			}
		}
		Log.d(LCAT, "country: " + Fidel.country.toString());
		Log.d(LCAT, "programmId: " + Fidel.programId);

	}

	private Bitmap loadImageFromApplication(String imageName) {
		Bitmap bitmap = null;
		String url = null;
		try {
			url = resolveUrl(null, imageName);
			TiBaseFile file = TiFileFactory.createTitaniumFile(new String[] { url }, false);
			bitmap = TiUIHelper.createBitmap(file.getInputStream());
		} catch (IOException e) {
			Log.e(LCAT, "Fidel only supports local image files " + url);
		}
		return bitmap;
	}

	@Kroll.method
	public void createForm() {
		present();
	}

	@Kroll.method
	public void startScanner() {
		Fidel.autoScan = true;
		present();
	}

	@Kroll.method
	public void present() {
		_instance = this;
		final TiActivitySupport support = (TiActivitySupport) TiApplication.getAppCurrentActivity();
		final Intent intent = new Intent(TiApplication.getInstance().getApplicationContext(),
				EnterCardDetailsActivity.class);
		Fidel.FIDEL_LINK_CARD_REQUEST_CODE = support.getUniqueResultCode();
		if (TiApplication.isUIThread()) {
			Log.d(LCAT, "present of fidel in UIthread");
			support.launchActivityForResult(intent, Fidel.FIDEL_LINK_CARD_REQUEST_CODE, this);
		} else {
			Log.d(LCAT, "present of fidel outside UIthread ==> sending message");
			TiMessenger.postOnMain(new Runnable() {
				@Override
				public void run() {
					support.launchActivityForResult(intent, Fidel.FIDEL_LINK_CARD_REQUEST_CODE, _instance);
				}
			});
		}
	}

	@Override
	public void onError(Activity activity, int requestCode, Exception e) {
		Log.e(LCAT, e.getMessage());
		if (onErrorCallback != null) {
			KrollDict event = new KrollDict();
			event.put("message", e.getMessage());
			event.put("error", e.toString());
			onErrorCallback.call(getKrollObject(), event);
		}
	}

	@Override
	public void onResult(Activity dummy, int requestCode, int resultCode, Intent data) {
		if (requestCode == Fidel.FIDEL_LINK_CARD_REQUEST_CODE) {
			if (data != null && data.hasExtra(Fidel.FIDEL_LINK_CARD_RESULT_CARD)) {
				LinkResult card = (LinkResult) data.getParcelableExtra(Fidel.FIDEL_LINK_CARD_RESULT_CARD);
				HashMap<String, Object> event = new HashMap<String, Object>();
				event.put("accountId", card.accountId);
				event.put("countryCode", card.countryCode);
				event.put("created", card.created);
				event.put("expDate", card.expDate);
				event.put("expMonth", card.expMonth);
				event.put("expYear", card.expYear);
				event.put("id", card.id);
				event.put("lastNumbers", card.lastNumbers);
				event.put("live", card.live);
				event.put("mapped", card.mapped);
				event.put("programId", card.programId);
				event.put("scheme", card.scheme);
				event.put("type", card.type);
				event.put("updated", card.updated);
				event.put("describeContents", card.describeContents());
				Log.d(LCAT, "event: " + event.toString());
				if (card.metaData != null) {
					try {
						event.put("metaData", new KrollDict(card.metaData));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} 
				if (hasListeners(PROP_PAYMENT_DID_COMPLETE)) {
					fireEvent(PROP_PAYMENT_DID_COMPLETE, event);
				}
				if (onPaymentDidCompleteCallback != null) {
					onPaymentDidCompleteCallback.callAsync(getKrollObject(), event);
					Log.d(LCAT,"events sent back to JS layer");
				} else Log.w(LCAT, "onPaymentDidCompleteCallback  is null, cannot send back data.");

			} else
				Log.w(LCAT, "invalid intent data");
		}
	}
}
