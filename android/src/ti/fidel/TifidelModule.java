package ti.fidel;

import java.io.IOException;
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

@Kroll.module(name = "Tifidel", id = "ti.fidel", propertyAccessors = { "paymentDidComplete" })
public class TifidelModule extends KrollModule {

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
	private static final String LCAT = "💰 TiFidel";

	private static KrollFunction onPaymentDidCompleteCallback;

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
		if (hasProperty(PROP_PAYMENT_DID_COMPLETE)) {
			Object o = getProperty(PROP_PAYMENT_DID_COMPLETE);
			if (o instanceof KrollFunction) {
				onPaymentDidCompleteCallback = (KrollFunction) o;
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
		final TiActivitySupport support = (TiActivitySupport) TiApplication.getAppCurrentActivity();
		final Intent intent = new Intent(TiApplication.getInstance().getApplicationContext(), EnterCardDetailsActivity.class);
		Fidel.FIDEL_LINK_CARD_REQUEST_CODE = support.getUniqueResultCode();
		Log.d(LCAT, "FIDEL_LINK_CARD_REQUEST_CODE: " + Fidel.FIDEL_LINK_CARD_REQUEST_CODE);
		if (TiApplication.isUIThread()) {
			Log.d(LCAT,"present of fidel in UIthread");
			support.launchActivityForResult(intent, Fidel.FIDEL_LINK_CARD_REQUEST_CODE, new PaymentResultHandler());
		} else {
			Log.d(LCAT,"present of fidel outside UIthread ==> sending message");
			TiMessenger.postOnMain(new Runnable() {
				@Override
				public void run() {
					Log.d(LCAT,"in UI thread");
					support.launchActivityForResult(intent, Fidel.FIDEL_LINK_CARD_REQUEST_CODE,
							new PaymentResultHandler());
				}
			});
		}
	}

	private final class PaymentResultHandler implements TiActivityResultHandler {
		public void onError(Activity arg0, int arg1, Exception e) {
			Log.e(LCAT, e.getMessage());
		}

		public void onResult(Activity dummy, int requestCode, int resultCode, Intent data) {
			if (requestCode == Fidel.FIDEL_LINK_CARD_REQUEST_CODE) {
				if (data != null && data.hasExtra(Fidel.FIDEL_LINK_CARD_RESULT_CARD)) {
					LinkResult card = (LinkResult) data.getParcelableExtra(Fidel.FIDEL_LINK_CARD_RESULT_CARD);
					KrollDict event = new KrollDict();
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
					try {
						event.put("metaData", new KrollDict(card.metaData));
					} catch (JSONException e) {
						e.printStackTrace();
					}
					event.put("programId", card.programId);
					event.put("scheme", card.scheme);
					event.put("type", card.type);
					event.put("updated", card.updated);
					event.put("describeContents", card.describeContents());
					if (hasListeners("paymentDidComplete")) {
						fireEvent("paymentDidComplete", event);
					}
					if (onPaymentDidCompleteCallback != null) {
						onPaymentDidCompleteCallback.call(getKrollObject(), event);
					}
					Log.d(LCAT, "event: " + event.toString());
				}
			}
		}
	}
}
