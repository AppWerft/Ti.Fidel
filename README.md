# Ti.Fidel

Axways Titanioum module for communication with Fidel system. Fidel uses opencv to scan credit cards and send card data to portal

## Prerequisites

Getting creds from [Fidel](http://fidel.uk/). The creds you can find under the right top account button. Here you find SDK keys (public and secret). Both you need for usage.

##  Google services

The library uses googles auth library. This library has a dependency to core and basement part of google services. If you use further modules which uses google play services you have to ensure that the same version is in use. maybe you have to play with *.jar files in module/lib folder.

## Basic usage


```javascript
const Fidel = require('ti.fidel');
Fidel.init({
	programId : "XXX",
	apiKey : "XXX",
	companyName : "Example,
	privacyURL : "https://example.com/privacy-policy",
	deleteInstructions : "Delete your card using the app",
	country : Fidel.COUNTRY_UNITED_KINGDOM
});	
Fidel.present();
```
