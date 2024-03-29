package com.quickcapture;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.extrieve.quickcapture.sdk.Config;
import com.extrieve.quickcapture.sdk.ImgHelper;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.module.annotations.ReactModule;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;


@ReactModule(name = QuickcaptureModule.NAME)
public class QuickcaptureModule extends ReactContextBaseJavaModule {
  public static final String NAME = "Quickcapture";
  private static ArrayList<String> captureimages = new ArrayList<>();
  private final int REQUEST_CODE_PERMISSIONS = 1001;
  public int REQUEST_CODE_FILE_RETURN = 1002;
  private Activity activity;
  private Context context;
  private ImgHelper ImageHelper;
  private final ReactApplicationContext reactContext;
  private Promise cameraPromise;

  public QuickcaptureModule(ReactApplicationContext reactContext) {
    super(reactContext);
    context = reactContext.getApplicationContext();
    reactContext.addActivityEventListener(mActivityEventListener);
    ImageHelper = new ImgHelper(context);
    this.reactContext = reactContext;
    try {
      ImageHelper.Init();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    Log.d("AMK_DEBUG", "QuickcaptureModule: init success ");
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
      if (requestCode == REQUEST_CODE_FILE_RETURN && resultCode == Activity.RESULT_OK) {
        if (cameraPromise != null) {
          // Process the image, get the path, and resolve the promise
          String imagePath = "path_to_captured_image"; // Implement image path retrieval logic
          Boolean Status = (Boolean) Objects.requireNonNull(data.getExtras()).get("STATUS");
          String Description = (String) data.getExtras().get("DESCRIPTION");
          if (Boolean.FALSE.equals(Status)) {
            String imageCaptureLog = "Description : " + Description +
              ".Exception: " + Config.CaptureSupport.LastLogInfo;
            Log.d("INFO", imageCaptureLog);
            activity.finishActivity(REQUEST_CODE_FILE_RETURN);
            OnActivityFail("onActivityResult",imageCaptureLog);
            return;
          }
          captureimages = (ArrayList<String>) data.getExtras().get("fileCollection");
          if (captureimages == null || captureimages.isEmpty()){
            OnActivityFail("onActivityResult","Captured images empty");
            return;
          }
          OnActivitySuccess();
        }
      }
    }
  };
  @ReactMethod
  private void startCapture(ReadableMap options, Promise inPromise) {
    Log.d("AMK_DEBUG 2", "startCapture: init success ");
    // before starting camera - configuration can set
    // String quality = ImageHelper.getCurrentImageQuality();
    Activity currentActivity = getCurrentActivity();

    if (currentActivity == null) {
      inPromise.reject("ERROR", "Activity doesn't exist");
      return;
    }
    this.activity = currentActivity;
    this.cameraPromise = inPromise;

    try {
      // moving to camera activity in library
      Intent CameraIntent = new Intent(activity, Class.forName("com.extrieve.quickcapture.sdk.CameraHelper"));
      // photoURI = CameraSupport.CamConfigClass.OutputPath;
      Uri photoURI = Uri.parse(Config.CaptureSupport.OutputPath);
      activity.grantUriPermission(activity.getPackageName(), photoURI,
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
      if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
        CameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
      }
      activity.startActivityForResult(CameraIntent, REQUEST_CODE_FILE_RETURN);
      // someActivityResultLauncher.launch(CameraIntent);
      Log.d("AMK_DEBUG 3", "startCapture: Intent success ");

    } catch (Exception e) {
      //Toast.makeText(activity, "Failed to open camera + ", Toast.LENGTH_LONG).show();
      Log.d("AMK_DEBUG 3", Objects.requireNonNull(e.getMessage()));
      e.printStackTrace();
    }
  }

  @ReactMethod
  private void buildPdfFileForLastCaptureSet(Promise inPromise) {
    AsyncTask.execute(() -> {

      String internalAppPath = BuildStoragePath();

      assert internalAppPath != null;
      File dir = new File(internalAppPath);

      if (!dir.exists()) {
        boolean mkdirs = dir.mkdirs();
        if(!mkdirs)  inPromise.reject("Directory creation failed");
      }
      String strPDFFile = dir + "/PDF_OUTPUT_" + UUID.randomUUID() + ".pdf";
      // String strTifFile = dir + "/TIF_OUTPUT_" + UUID.randomUUID() + ".tif";

      // String tiffFile = CameraHelper.GetTiffForLastCapture(strTifFile);
      String pdfFileResposne = null;
      try {
        pdfFileResposne = ImageHelper.GetPDFForLastCapture(strPDFFile);
        String[] parts = pdfFileResposne.split(":::");
        if (parts.length > 1) {
          if(Objects.equals(parts[0], "FAILED")){
            inPromise.reject(parts[1]);
            return;
          }
          String pdfFile = parts[1]; // The second element in the array should be your file path
          showToast("Success.Output Created", Gravity.CENTER);
          inPromise.resolve(pdfFile);
        } else {
          System.err.println("The input string does not contain ':::' or is not in the expected format.");
        }
      } catch (IOException e) {
        //throw new RuntimeException(e);
        inPromise.reject(e.getMessage());
      }
    });
  }

  @ReactMethod
  public void init(final Promise promise) {
    final Activity activity = getCurrentActivity();
    if (activity == null) {
      promise.reject("Activity not available", "Cannot request permissions without an active activity");
      return;
    }
    this.activity = activity;; // Assign activity reference
    SetConfig();
    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
      // Permission is already granted
      promise.resolve(true);
    } else {
      // Permission has not been granted yet, request it.
      ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSIONS);

      // Use the onActivityResult method to handle permission request result
      reactContext.addActivityEventListener(new BaseActivityEventListener() {
        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
          if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
              // Permission granted
              promise.resolve(true);
            } else {
              // Permission denied
              promise.reject("Permission denied", "User denied camera permission.");
            }

            // Remove the listener to avoid memory leaks
            reactContext.removeActivityEventListener(this);
          }
        }
      });
    }
  }


  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  public void multiply(double a, double b, Promise promise) {
    promise.resolve(a * b);
  }

  private void SetConfig() {

    ImageHelper.SetPageLayout(4);//A1-A7(1-7),PHOTO,CUSTOM,ID(8,9,10)

    ImageHelper.SetImageQuality(ImgHelper.ImageQuality.Document_Quality.ordinal());//0,1,2 - Photo_Quality, Document_Quality, Compressed_Document

    ImageHelper.SetDPI(200);//int dpi_val = 100, 150, 200, 300, 500, 600;

    //ImageHelper.setMaxSize(150);

    //can set output file path
    Config.CaptureSupport.OutputPath = BuildStoragePath();

    // MaxPage = not set / 0 / 1 - single shot mode
    // MaxPage > 1 - Multi capture mode
    // CameraSupport.CamConfigClass.MaxPage = 5;

    Config.CaptureSupport.CaptureReview = true;

    //Config.CaptureSupport.UseDefaultCamera = false;

    Config.CaptureSupport.EnableTouchToFocus = false;

    //Capture sound
    Config.CaptureSupport.CaptureSound = false;

    Config.CaptureSupport.EnableFlash = true;

    Config.CaptureSupport.CameraToggle = Config.CaptureSupport.CameraToggleType.ENABLE_BACK_DEFAULT;
    //0-Disable camera toggle option
    //1-Enable camera toggle option with Front camera by default
    //2-Enable camera toggle option with Back camera by default

    Config.CaptureSupport.DocumentCropping = Config.CaptureSupport.CroppingType.AssistedCapture;

    Config.CaptureSupport.ColorMode = Config.CaptureSupport.ColorModes.RBG;

    //Config.CaptureSupport.CaptureProfile = CAMERA_CAPTURE_REVIEW;
  }

  private String BuildStoragePath() {
    if (this.activity == null)
      return null;
    ContextWrapper c = new ContextWrapper(this.activity);
    return Objects.requireNonNull(c.getExternalFilesDir(".QCImages")).getAbsolutePath();
  }

  /**
   * Shows a {@link Toast} on the UI thread.
   *
   * @param text The message to show
   */
  private void showToast(final String text, final int TPosition) {
    activity.runOnUiThread(() -> {
      final Toast toast = Toast.makeText(activity, text, Toast.LENGTH_SHORT);
      toast.setGravity(TPosition, 0, 10);
      toast.show();
      Handler handler = new Handler();
      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          toast.cancel();
        }
      }, 500);
    });
  }

  /* Return Section */
  class returnClass {
    ArrayList<String> fileCollection = null;
    String STATUS = "SUCCESS";
    String DESCRIPTION = "Success";
  }

  private void OnActivitySuccess() {
    try {
      returnClass dtRtn = new returnClass();
      dtRtn.fileCollection = captureimages;

      // Create a JSONObject
      JSONObject jsonObject = new JSONObject();

      // Assuming captureimages is a collection (like List, Set, etc.)
      JSONArray jsonArray = new JSONArray(dtRtn.fileCollection);
      jsonObject.put("fileCollection", jsonArray);

      String jsonString = jsonObject.toString();
      //result.success(jsonString);
      if (cameraPromise != null) {
        //cameraPromise.resolve(jsonString);
        Log.d("INFO", "Return Success");
        cameraPromise.resolve(jsonString);
      }
      cameraPromise = null;
      activity.finishActivity(REQUEST_CODE_FILE_RETURN);
    } catch (Exception e) {
      e.printStackTrace();
      // Handle the exception appropriately
    }
  }

  private void OnActivityFail(String MethodName, String FailedDescription) {
    Intent intent = new Intent();
    intent.putExtra("STATUS", false);

    String desc = "Failed : at " + MethodName;
    if (FailedDescription != null && !FailedDescription.isEmpty())
      desc += " " + FailedDescription;

    // Create a JSONObject
    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put("STATUS", "FAILURE");
      jsonObject.put("DESCRIPTION", desc);

      String jsonString = jsonObject.toString();
      if (cameraPromise != null) {
        cameraPromise.resolve(jsonString);
      }
      activity.finishActivity(REQUEST_CODE_FILE_RETURN);
      cameraPromise = null;
    } catch (Exception e) {
      e.printStackTrace();
      // Handle the exception appropriately
    }
  }
}
