import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'quickcapture' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const Quickcapture = NativeModules.Quickcapture
  ? NativeModules.Quickcapture
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function init() {
  Quickcapture.init();
  return true;
}

export function startCapture() {
  return new Promise((resolve) => {
    Quickcapture.startCapture({ saveToGallery: true })
      .then((imagePath) => {
        console.log(imagePath);
        resolve(imagePath); // Resolve the outer promise with the image path
      })
      .catch((error) => {
        console.error(error);
        eject(error); // Reject the outer promise with the error
      });
  });
}

export function buildPdfForLastCapture() {
  return new Promise((resolve) => {
    Quickcapture.buildPdfFileForLastCaptureSet()
      .then((pdfFilePath) => {
        console.log(pdfFilePath);
        resolve(pdfFilePath); // Resolve the outer promise with the image path
      })
      .catch((error) => {
        console.error(error);
        eject(error); // Reject the outer promise with the error
      });
  });
}
