# Ti.Fidel

Axways Titanioum module for communication with Fidel system. Fidel uses opencv to scan credit cards and send card data to portal

## Prerequisites

Getting creds from [Fidel](http://fidel.uk/). The creds you can find under the right top account button. Here you find SDK keys (public and secret). Both you need for usage.

##  Google services

The library uses googles auth library. This library has a dependency to core and basement part of google services. If you use further modules which uses google play services you have to ensure that the same version is in use. maybe you have to play with *.jar files in module/lib folder.


## Building aars
In folder gradle is a gradle script `build.gradle`. With it you can resolcve dependencies.

```
gradle tasks
gradle getDeps
```
After this you can copy all archives to `android/lib`

## Basic usage


```javascript
const Fidel = require('ti.fidel');
Fidel.init({
	programId : "XXX",
	apiKey : "XXX",
	companyName : "Example",
	privacyURL : "https://example.com/privacy-policy",
	deleteInstructions : "Delete your card using the app",
	country : Fidel.COUNTRY_UNITED_KINGDOM,
	paymentDidComplete : onResult
});	
Fidel.paymentDidComplete = onResult;
Fidel.addEventListener("paymentDidComplete",onResult)
Fidel.present();
function onResult(e) {
	console.log(e);
}
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
* autoScan: Boolean
* apiKey: String
* autoScan: Boolean
* companyName: String
* country: 1 of countries above
* deleteInstructions: String
* metadata: Object
* privacyUrl: String
* programmId: String


### present()

### startScanner()

### createForm()

## Callbacks

You can use as event listener, as property in init-method or as a module property, see example above.

`paymentDidComplete`

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

### Example for result

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