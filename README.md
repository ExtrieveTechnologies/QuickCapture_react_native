# quickcapture_react_native

A React Native port of quickcapture for easy integration into mobile applications.

## Installation

```sh
npm install @extrieve_technologies/quickcapture_react_native
```

## Usage

1. First, import the necessary modules and initialize quickcapture:

```js
import { init, startCapture, buildPdfForLastCapture } from '@extrieve_technologies/quickcapture_react_native';

// Initialize quickcapture
init();
```
2. To start capturing images, call startCapture. This function returns a promise that resolves with the JSON string having an array of the captured images file path:

```js
startCapture().then(imagePath => {
  console.log(`Captured image path: ${imagePath}`);
}).catch(error => {
  console.error(`Capture error: ${error}`);
});
```
3. To build a PDF from the last capture set, use buildPdfForLastCapture. This function also returns a promise that resolves with the path to the generated PDF:

```js
buildPdfForLastCapture().then(pdfFilePath => {
  console.log(`Generated PDF path: ${pdfFilePath}`);
}).catch(error => {
  console.error(`PDF generation error: ${error}`);
});
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
