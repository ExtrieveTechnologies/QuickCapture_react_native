import React, { useEffect, useState } from 'react';
import {
  StyleSheet,
  View,
  Text,
  Button,
  Alert,
  ScrollView,
  Image,
} from 'react-native';
import {
  startCapture,
  init,
  buildPdfForLastCapture,
} from 'quickcapture_react_native';
import FileViewer from 'react-native-file-viewer';

export default function App() {
  const [images, setImages] = useState([]);

  useEffect(() => {
    init();
  }, []);

  const handleStartCapture = () => {
    startCapture()
      .then((resultString) => {
        const result = JSON.parse(resultString);
        if (result.fileCollection && result.fileCollection.length) {
          setImages(result.fileCollection);
        } else {
          Alert.alert('No images', 'No images were captured');
        }
      })
      .catch((error) => {
        console.error('Error starting capture:', error);
        Alert.alert('Capture Error', `Error: ${error.message}`);
      });
  };

  const handleBuildPdf = async () => {
    try {
      // Your logic to build a PDF and get the file path
      console.log('Building PDF...');
      const pdfPath = await buildPdfForLastCapture(); // Assume buildPdf is a function that builds the PDF and returns the file path

      // Open the PDF with the default PDF viewer
      FileViewer.open(pdfPath, { mimeType: 'application/pdf' })
        .then(() => {
          // Success
          console.log('PDF opened successfully');
        })
        .catch((error) => {
          // Error
          console.error('Error opening PDF:', error);
        });
      Linking.openURL(`file://${pdfPath}`).catch((err) =>
        console.error('Failed to open file:', err)
      );
    } catch (error) {
      console.error('Error building PDF:', error);
    }
  };
  return (
    <View style={styles.container}>
      <Text>Test app</Text>
      <Button title="Start Capture" onPress={handleStartCapture} />
      <ScrollView contentContainerStyle={styles.imageGrid}>
        {images.map((imagePath, index) => (
          <Image
            key={index}
            source={{ uri: `file://${imagePath}` }}
            style={styles.image}
          />
        ))}
      </ScrollView>
      {images.length > 0 && (
        <Button title="Build PDF" onPress={handleBuildPdf} />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  imageGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'center',
    marginTop: 20,
  },
  image: {
    width: 100,
    height: 100,
    margin: 5,
  },
});
