# Ti.Fidel

Axways Titanioum module for communication with Fidel system. Fidel uses opencv to scan credit cards and send card data to portal.

<img src="https://raw.githubusercontent.com/AppWerft/Ti.Fidel/master/documentation/screen.jpg" width="320"/>

## Prerequisites

Getting creds from [Fidel](http://fidel.uk/). The creds you can find under the right top account button. Here you find SDK keys. Here you need the public key. 


## Handling/testing

* You can test the opencv scanner only with real cards. Screenshots from google search doesn't work. This scanned cards you cannot link!
* The form only accepts test nummers from fidel webpage (4444000000004* und 5555000000005*).  With this numbers you can test linking. If you use one number twice, then a warning on adb console appears. In next version of SDK this event will exposed to SDK.

##  Google services

The library uses some libraries from googles playservice suite. For this dependency a requirement is inside timodule.xml. 

## Building aars
In folder android is a gradle script `build.gradle`. This script resolves the dependencies. Go into `android` folder and type

```
gradle tasks
gradle getDeps
```
In folder `lib` are now all libraries. Some libraries comes from Titanium SDK, therefore you have to remove these 3 files below:

* runtime-VERSION.aar
* support-annotations-VERSION.jar
* common-VERSION.jar

If you want to edit the module it is a good idea to extract the jar from fidel.aar (android-sdk-1.2.2.aar) and copy into a extrafolder and point the class path to this file. This help the IDE.

## Basic usage


```javascript
const Fidel = require('ti.fidel');
Fidel.init({
	programId : "e1d65be8-1011-42ad-ab6d-6………",
	apiKey : "pk_test_660a132e-cc54-4734………",
	country : Fidel.COUNTRY_UNITED_KINGDOM
});	
Fidel.setCheckbox({
	companyName : "Example",
	privacyURL : "https://example.com/privacy-policy",
	deleteInstructions : "Delete your card using the app",
});
Fidel.onCardLinkSuccess = onResult;
Fidel.addEventListener("CardLinkSuccess",onResult)
Fidel.present();
function onResult(e) {
	console.log(e);
}
```
## Properties in tiapp.xml
Optionally to setting of credential vars as property in `init()` you can put it into `tiapp.xml`

```xml
<property name="FIDEL_PROGRAMID" type="string">e1d65be8-1011-42a6………</property>
<property name="FIDEL_APIKEY" type="string">pk_test_6607………</property>
<property name="FIDEL_PROGRAMID" type="string">e1d65be8-1011-42a6………</property>
<property name="FIDEL_COUNTRY" type="string">JAPAN</property>
```
In this case you can ommit the `init()` call.

```javascript
const Fidel = require('ti.fidel');
Fidel.onCardLinkSuccess = function onResult(e) {
    console.log(e);
};
Fidel.startScan();
```


## Constants

### Countries

* Fidel.COUNTRY\_UNITED\_KINGDOM
* Fidel.COUNTRY\_UNITED\_STATES
* Fidel.COUNTRY\_SWEDEN
* Fidel.COUNTRY\_JAPAN  
* Fidel.COUNTRY\_IRLAND


## Methods

### init(object)
#### keys of object (parameters of init())
* apiKey: String
* programmId: String
* autoScan: Boolean
* country: one of countries above
* metadata: Object
* bannerImage: String  

### setCheckbox(object)
#### keys of object (parameters of setCheckbox())
* deleteInstructions: String //(Maximum 60 characters);
* privacyUrl: String  //(must be a valid URL)
* companyName: String //(Maximum 60 characters);

### present()
Generic method.

### startScanner()
Convenience method for direct call the scanner


## Callbacks

You can use as event listener, as property in init-method or as a module property, see example above.

`onCardLinkSuccess` resp.  `cardLinkSuccess`

With these properties:

* accountId
* countryCode
* created
* expDate
* expMonth
* expYear
* id
* lastNumbers
* live
* mapped
* metaData
* programId
* schemetype
* updated
* describeContents

## Errors

The error callback contains a textual message and one of the following codes:

####    Fidel.ERROR\_USER_CANCELED 
Sometimes it's useful to know if the user canceled the card linking process so please check for this error, if that's the case.

####    Fidel.ERROR\_INVALID_URL 
If you provide an invalid Fidel.privacyURL, you will receive this error. Please make sure your URL matches the Patterns.WEB_URL pattern.

#### Fidel.ERROR\_STRING\_OVER\_THE\_LIMIT 
We send this error in case your Fidel.deleteInstructions or Fidel.companyName exceed 60 characters.
 
#### Fidel.ERROR\_MISSING\_MANDATORY\_INFO 
Some of the mandatory information necessary to configure the SDK were not provided. The following are the mandatory info you need to provide:

*        Fidel.apiKey
*        Fidel.programId
 


### Example for positive result

```
{
  "id": "e3fff00f-ab85-4a0a-b572-08b464ebf67a",
  "accountId": "a30e933f-cde1-4ac1-9a5e-acd329497a48",
  "programId": "e3fff00f-ab85-4a0a-b572-08b464ebf67a",
  "metadata": {
    "id": "this-is-the-metadata-id",
    "customKey1": "customValue1",
    "customKey2": "customValue2"
  },
  "provider": "mastercard",
  "type": "master-card",
  "lastNumbers": "3183",
  "expMonth": 1,
  "expYear": 2018,
  "expDate": "2018-01-01T00:00:00.000Z",
  "countryCode": "GBR",
  "mapped": true,
  "live": true,
  "created": "2017-02-13T17:02:12.535Z",
  "updated": "2017-02-13T17:17:02.833Z"
}
```

