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

@Kroll.module(name = "Tifidel", id = "ti.fidel", propertyAccessors = { "onCardLinkSuccess" })
public class TifidelModule extends KrollModule implements TiActivityResultHandler {
	private static final String LCAT = "TiAPI💰TiFidel";
	private static TifidelModule that;
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

	private static final String PROP_CARD_LINK_SUCCESS = "cardLinkSuccess";
	private static final String PROP_ONCARD_LINK_SUCCESS = "onCardLinkSuccess";
	private static final String PROP_ERROR = "Error";
	private static KrollFunction onCardLinkSuccessCallback;
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
			Fidel.bannerImage = loadImage(opts.getString("bannerImage"));
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
		if (opts.containsKeyAndNotNull(PROP_CARD_LINK_SUCCESS)) {
			Object o = opts.get(PROP_CARD_LINK_SUCCESS);
			if (o instanceof KrollFunction) {
				onCardLinkSuccessCallback = (KrollFunction) o;
			}
		}
		if (hasProperty(PROP_ONCARD_LINK_SUCCESS)) {
			Object o = getProperty(PROP_ONCARD_LINK_SUCCESS);
			if (o instanceof KrollFunction) {
				onCardLinkSuccessCallback = (KrollFunction) o;
			} else
				Log.w(LCAT, PROP_ONCARD_LINK_SUCCESS + " isn't a Krollfunction");
		} else
			Log.w(LCAT, PROP_ONCARD_LINK_SUCCESS + " is missing");
		if (hasProperty("onErrorComplete")) {
			Object o = getProperty("onErrorComplete");
			if (o instanceof KrollFunction) {
				onErrorCallback = (KrollFunction) o;
			}
		}
	}

	private Bitmap loadImage(String imageName) {
		Bitmap bitmap = null;
		try {
			TiBaseFile file = TiFileFactory.createTitaniumFile(new String[] { resolveUrl(null, imageName) }, false);
			bitmap = TiUIHelper.createBitmap(file.getInputStream());
		} catch (IOException e) {
			Log.e(LCAT, "Fidel only supports local image files " + imageName);
			return null;
		}
		Log.d(LCAT, "bannerImage: " + bitmap.getWidth() + "x" + bitmap.getHeight() + " byteCounts: " + bitmap.getByteCount());
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
		that= this;
		final TiActivitySupport support = (TiActivitySupport) TiApplication.getAppCurrentActivity();
		final Intent intent = new Intent(TiApplication.getInstance().getApplicationContext(),
				EnterCardDetailsActivity.class);
		Fidel.FIDEL_LINK_CARD_REQUEST_CODE = support.getUniqueResultCode();
		if (TiApplication.isUIThread()) {
			Log.d(LCAT, " byteCounts of bannerImage: " + Fidel.bannerImage.getByteCount());
			support.launchActivityForResult(intent, Fidel.FIDEL_LINK_CARD_REQUEST_CODE, that);
		} else {
			TiMessenger.postOnMain(new Runnable() {
				@Override
				public void run() {
					support.launchActivityForResult(intent, Fidel.FIDEL_LINK_CARD_REQUEST_CODE, that);
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
				if (hasListeners(PROP_CARD_LINK_SUCCESS)) {
					fireEvent(PROP_CARD_LINK_SUCCESS, event);
				}
				if (onCardLinkSuccessCallback != null) {
					onCardLinkSuccessCallback.callAsync(getKrollObject(), event);
					Log.d(LCAT, "events sent back to JS layer");
				} else
					Log.w(LCAT, "onCardLinkSuccessCallback  is null, cannot send back data.");

			} else
				Log.w(LCAT, "invalid intent data");
		}
	}
}
