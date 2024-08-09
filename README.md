# quickcapture_react_native

A React Native port of quickcapture for easy integration into mobile applications.

The Quickcapture plugin offers functionalities for capturing documents and optical codes (like QR codes and barcodes), building PDFs from captured images, and generating QR and barcode images from provided data. This document guides you through the installation and utilization of the Quickcapture plugin in your React Native application.

## Installation

Follow these steps to install the Quickcapture plugin using either npm or Yarn, depending on your preference:

**Using npm**

1. Add the Package:
```bash
npm install @extrieve_technologies/quickcapture_react_native
```
2. Link the Package:
If you are using React Native 0.60 or above, autolinking will handle the rest. For iOS, you must run:
```bash
cd ios && pod  install && cd..
```

3. Rebuild Your Application:

Rebuild your application to ensure all native dependencies are properly linked.
For iOS:
```bash
npx react-native run-ios
```
For Android:

```bash
npx react-native run-android
```
**Using Yarn**

1. Add the Package:
```bash
yarn add @extrieve_technologies/quickcapture_react_native
```

## Usage
1. First, import the necessary modules and initialize quickcapture:

	```js
	import {
		//If need to activate the SDK without restrictions - optional
		activateLicense,
		//TO initialise license - mandatory
		initialise,
		//For Document Capture capabilities, add following - optional
		captureDocument,
		buildPdfForLastDocumentCapture,
		//For Bar/QR Code capabilities, add following - optional
		captureOpticalCode,
		generateQRCode,
		generateBarcode,
	} from  '@extrieve_technologies/quickcapture_react_native';

	// Initialize Quickcapture
	//activateLicense('Your Android license key', 'Your iOS license key');
	activateLicense(androidLicense, iosLicense).then((response) => {
		//response - true/false
	});

	//call plugin initialisation
	initialise();
	```
2.  **captureDocument** : To start capturing images, call startCapture. This function returns a promise that resolves with the JSON string having an array of the captured images file path:

	```js
	captureDocument().then((resultString) => {
		const result = JSON.parse(resultString);
	})
	.catch((error) => {
		console.error('Error starting capture:', error);
	});
	```
3.  **buildPdfForLastDocumentCapture** : To build a PDF from the last capture set, use buildPdfForLastCapture. This function also returns a promise that resolves with the path to the generated PDF:

	```js
	buildPdfForLastCapture().then(pdfFilePath  => {
		console.log(`Generated PDF path: ${pdfFilePath}`);
	}).catch(error  => {
		console.error(`PDF generation error: ${error}`);
	});
	```

3.  **captureOpticalCode** : To Captures an optical code (QR code or barcode) using the device camera.Will returns a promise that resolves to the scanned code data in JSON string
	```js
	captureOpticalCode().then((resultString) => {
		//handle JSON data here.
		const  result = JSON.parse(resultString);
		console.log('Scanned code : ', result.DATA);
	})
	.catch((error) => {
		console.error('Error Scanning code:', error);
	});
	```

3.  **generateQRCode** : Generates a QR code from provided data.Need to provide a data in string.in return, a promise that resolves data with Base64 data of the generated QR code image will get.
	```js
	generateQRCode(qrText).then((base64String) => {
		// use barcode image in Base64 data format here.
		//image encoded and a PNG image in Base64 format
		//setBase64Image(base64String);
		Alert.alert('The QR code was generated successfully');
	})
	.catch((error) => {
		console.error('Error generating QR code:', error);
		Alert.alert('QR Generation Error', `Error: ${error.message}`);
	});
	```

3.  **generateBarcode** : Generates a BAR code from provided data.Need to provide a data in string and also can specify if the BARcode with text needed.In return, a promise that resolves data with Base64 data of the generated BAR code image will get.
	```js
	generateBarcode(qrText, enableText).then((base64String) => {
		// use barcode image in Base64 data format here.
		//image encoded and a PNG image in Base64 format
		//setBase64Image(base64String);
		Alert.alert('Barcode generated successfully.');
	})
	.catch((error) => {
		console.error('Error generating QR code:', error);
		Alert.alert('QR Generation Error', `Error: ${error.message}`);
	});
	```
## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License
MIT

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
