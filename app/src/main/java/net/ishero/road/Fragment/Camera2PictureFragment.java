package net.ishero.road.Fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import net.ishero.road.R;
import net.ishero.road.View.AutoFitTextureView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


@TargetApi(21)
public class Camera2PictureFragment extends Fragment implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {


    /**
     * Conversion from screen rotation to JPEG orientation.
     */

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();//SparseIntArrays map integers to integers,there can be gaps in the indices

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private static final String FRAGMENT_DIALOG = "dialog";


    static {

        ORIENTATIONS.append(Surface.ROTATION_0, 90);

        ORIENTATIONS.append(Surface.ROTATION_90, 0);

        ORIENTATIONS.append(Surface.ROTATION_180, 270);

        ORIENTATIONS.append(Surface.ROTATION_270, 180);

    }


    /**
     * Tag for the {@link Log}.
     */

    private static final String TAG = "Camera2BasicFragment";


    /**
     * Camera state: Showing camera preview.
     */

    private static final int STATE_PREVIEW = 0;


    /**
     * Camera state: Waiting for the focus to be locked.
     */

    private static final int STATE_WAITING_LOCK = 1;


    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */

    private static final int STATE_WAITING_PRECAPTURE = 2;


    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */

    private static final int STATE_WAITING_NON_PRECAPTURE = 3;


    /**
     * Camera state: Picture was taken.
     */

    private static final int STATE_PICTURE_TAKEN = 4;


    /**
     * Max preview width that is guaranteed by Camera2 API
     */

    private static final int MAX_PREVIEW_WIDTH = 1920;


    /**
     * Max preview height that is guaranteed by Camera2 API
     */

    private static final int MAX_PREVIEW_HEIGHT = 1080;


    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * <p>
     * {@link TextureView}.
     */
    /**SurfaceView的工作方式是创建一个置于应用窗口之后的新窗口。这种方式的效率非常高，因为SurfaceView窗口刷新的时候不需要重绘应用程序的窗口（android普通窗口的视图绘制机制是一层一层的，任何一个子元素或者是局部的刷新都会导致整个视图结构全部重绘一次，因此效率非常低下，不过满足普通应用界面的需求还是绰绰有余），但是SurfaceView也有一些非常不便的限制。
     * 因为SurfaceView的内容不在应用窗口上，所以不能使用变换（平移、缩放、旋转等）。也难以放在ListView或者ScrollView中，不能使用UI控件的一些特性比如View.setAlpha()。
     *为了解决这个问题 Android 4.0中引入了TextureView。
     */

    //使用textureview第一步建立surfaceTextureListener
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener

            = new TextureView.SurfaceTextureListener() {


        //dangTextureView的draw方法被调用后将textureview显示出来后就会回调onSurfaceTextureAvailable方法之后皆可以接收绘图请求
        @Override

        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {

            openCamera(width, height);//这里我们调用了openCamera方法打开相机，并接收镜头数据，将其绘制在textureview上

        }


        @Override

        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {

            configureTransform(width, height);

        }


        @Override

        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {

            return true;

        }


        @Override

        public void onSurfaceTextureUpdated(SurfaceTexture texture) {

        }


    };


    /**
     * ID of the current {@link CameraDevice}.
     */

    private String mCameraId;


    /**
     * An {@link AutoFitTextureView} for camera preview.
     */

    private AutoFitTextureView mTextureView;


    /**
     * A {@link CameraCaptureSession } for camera preview.
     */

    private CameraCaptureSession mCaptureSession;


    /**
     * A reference to the opened {@link CameraDevice}.
     */

    private CameraDevice mCameraDevice;


    /**
     * The {@link Size} of camera preview.
     */

    private Size mPreviewSize;


    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {


        @Override

        public void onOpened(@NonNull CameraDevice cameraDevice) {

            // This method is called when the camera is opened.  We start camera preview here.

            mCameraOpenCloseLock.release();

            mCameraDevice = cameraDevice;

            createCameraPreviewSession();

        }


        @Override

        public void onDisconnected(@NonNull CameraDevice cameraDevice) {

            mCameraOpenCloseLock.release();

            cameraDevice.close();

            mCameraDevice = null;

        }


        @Override

        public void onError(@NonNull CameraDevice cameraDevice, int error) {

            mCameraOpenCloseLock.release();

            cameraDevice.close();

            mCameraDevice = null;

            Activity activity = getActivity();

            if (null != activity) {

                activity.finish();

            }

        }


    };


    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */

    private HandlerThread mBackgroundThread;


    /**
     * A {@link Handler} for running tasks in the background.
     */

    private Handler mBackgroundHandler;


    /**
     * An {@link ImageReader} that handles still image capture.
     */

    private ImageReader mImageReader;


    /**
     * This is the output file for our picture.
     */

    private File mFile;


    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * <p>
     * still image is ready to be saved.
     */

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener

            = new ImageReader.OnImageAvailableListener() {

        //acquireNextImage获取下一个最新的可用Image，没有则返回null
        //建议对批处理/后台处理使用acquireNextImage（）
        //过多的调用acquireLatestImage()（大于getMaxImages()），而没有调用Image.close()的话，将会抛出IllegalStateException
        @Override
        public void onImageAvailable(ImageReader reader) {
            //后台句柄发送一个post请求开启一个runnable()用来存储图片
            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
        }
    };


    /**
     * {@link CaptureRequest.Builder} for the camera preview
     */


    private CaptureRequest.Builder mPreviewRequestBuilder;


    /**
     * {@link CaptureRequest} generated by {@link #mPreviewRequestBuilder}
     */

    private CaptureRequest mPreviewRequest;


    /**
     * The current state of camera state for taking pictures.
     *
     * @see #mCaptureCallback
     */

    private int mState = STATE_PREVIEW;


    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    //控制线程并发数量

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);


    /**
     * Whether the current camera device supports Flash or not.
     */

    private boolean mFlashSupported;

    /**
     *Whether the current camera device supports AutoFocus
     */
    private boolean mAutoFocusSupported;

    /**
     * Orientation of the camera sensor
     */

    private int mSensorOrientation;


    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
     */

    //保存操作，并将图片回现到surfaceview上
    private CameraCaptureSession.CaptureCallback mCaptureCallback

            = new CameraCaptureSession.CaptureCallback() {


        private void process(CaptureResult result) {

            switch (mState) {

                case STATE_PREVIEW: {

                    // We have nothing to do when the camera preview is working normally.

                    break;

                }

                case STATE_WAITING_LOCK: {

                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);

                    if (afState == null) {

                        captureStillPicture();

                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||

                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {

                        // CONTROL_AE_STATE can be null on some devices

                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);

                        if (aeState == null ||

                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {

                            mState = STATE_PICTURE_TAKEN;

                            captureStillPicture();

                        } else {

                            runPrecaptureSequence();

                        }

                    }

                    break;

                }

                case STATE_WAITING_PRECAPTURE: {

                    // CONTROL_AE_STATE can be null on some devices

                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);

                    if (aeState == null ||

                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||

                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {

                        mState = STATE_WAITING_NON_PRECAPTURE;

                    }

                    break;

                }

                case STATE_WAITING_NON_PRECAPTURE: {

                    // CONTROL_AE_STATE can be null on some devices

                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);

                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {

                        mState = STATE_PICTURE_TAKEN;

                        captureStillPicture();

                    }

                    break;

                }

            }

        }


        @Override

        public void onCaptureProgressed(@NonNull CameraCaptureSession session,

                                        @NonNull CaptureRequest request,

                                        @NonNull CaptureResult partialResult) {

            process(partialResult);

        }


        @Override

        public void onCaptureCompleted(@NonNull CameraCaptureSession session,

                                       @NonNull CaptureRequest request,

                                       @NonNull TotalCaptureResult result) {

            process(result);

        }


    };


    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */

    private void showToast(final String text) {

        final Activity activity = getActivity();

        if (activity != null) {

            activity.runOnUiThread(new Runnable() {

                @Override

                public void run() {

                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();

                }

            });

        }

    }


    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * <p>
     * is at least as large as the respective texture view size, and that is at most as large as the
     * <p>
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * <p>
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * <p>
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended output
     *                          <p>
     *                          class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param aspectRatio       The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */

    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth, int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight && option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth && option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }
        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }


    public static Camera2PictureFragment newInstance() {

        return new Camera2PictureFragment();

    }


    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,

                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_camera2_picture, container, false);

    }


    @Override

    public void onViewCreated(final View view, Bundle savedInstanceState) {

        view.findViewById(R.id.pic).setOnClickListener(this);
        mTextureView =  view.findViewById(R.id.texture2);

    }


    @Override

    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        mFile = new File(getActivity().getExternalFilesDir(null)+ "pic.jpg");

    }


    @Override

    public void onResume() {

        super.onResume();

        startBackgroundThread();


        // When the screen is turned off and turned back on, the SurfaceTexture is already

        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open

        // a camera and start preview from here (otherwise, we wait until the surface is ready in

        // the SurfaceTextureListener).

        if (mTextureView.isAvailable()) {

            openCamera(mTextureView.getWidth(), mTextureView.getHeight());

        } else {

            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);

        }

    }


    @Override

    public void onPause() {

        closeCamera();

        stopBackgroundThread();

        super.onPause();

    }








    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */

    @SuppressWarnings("SuspiciousNameCombination")

    private void setUpCameraOutputs(int width, int height) {
        Activity activity = getActivity();//返回此fragment当前与之关联的Activity
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);//获取手机相机管理对象
        try {
            for (String cameraId : manager.getCameraIdList()) {//获取该手机可以使用的相机id
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);//取得对应id相机的功能列表
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);//获取前置摄像头功能的功能编号
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {//禁用前置摄像头
                    continue;
                }
                int[] afAvailableModes =characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
                if(afAvailableModes.length==0||(afAvailableModes.length==1&&afAvailableModes[0]==CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_OFF)){
                    mAutoFocusSupported=false;
                }else {
                    mAutoFocusSupported=true;
                }
                // 获取摄像头支持的配置属性
                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }
                // 可以拍的最大图片大小，首先获得在map中获取图片格式为jpeg图片的输出大小的键值对，传入一个size的comparator比较器将它按一定的顺序排列，之后再用集合类中max方法获取其中最大的size。
                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                //按照你的期望创建一个ImageReader，这里我们将imagereader创建为摄像头可以的最大长和宽，格式为jpeg，过多的image数量可能触发oom
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, /*maxImages*/2);
                //添加新图像可用回调
                //@params handler 如果你喜欢回调在你指定的线程里面去执行，就指定handler，不然就传空，回调会在当前线程里面去执行
                //当没有传handler并且调用线程没有looper的时候，抛出IllegalArgumentExeption
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
                // Find out if we need to swap dimension to get the preview size relative to sensor
                // 获取显示的角度
                int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                //no inspection Constant Conditions
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:

                    case Surface.ROTATION_180:

                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {

                            swappedDimensions = true;

                        }

                        break;

                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }

                        break;

                    default:

                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);

                }

                Point displaySize = new Point();
                //获得activity的大小也就是屏幕尺寸
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;
                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;

                }
                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }
                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }


                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera

                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of

                // garbage capture data.

                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);
                // We fit the aspect ratio of TextureView to the size of preview we picked.
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getHeight(), mPreviewSize.getWidth());

                }
                // Check if the flash is supported.
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;
                mCameraId = cameraId;
                return;
            }

        } catch (CameraAccessException e) {

            e.printStackTrace();

        } catch (NullPointerException e) {

          e.printStackTrace();
        }

    }


    /**
     * Opens the camera specified by {@link Camera2BasicFragment#mCameraId}.
     */

    private void openCamera(int width, int height) {


        setUpCameraOutputs(width, height);

        configureTransform(width, height);

        Activity activity = getActivity();

        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);

        try {

            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {

                throw new RuntimeException("Time out waiting to lock camera opening.");

            }

            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);

        } catch (CameraAccessException e) {

            e.printStackTrace();

        } catch (InterruptedException e) {

            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);

        }

    }


    /**
     * Closes the current {@link CameraDevice}.
     */

    private void closeCamera() {

        try {

            mCameraOpenCloseLock.acquire();

            if (null != mCaptureSession) {

                mCaptureSession.close();

                mCaptureSession = null;

            }

            if (null != mCameraDevice) {

                mCameraDevice.close();

                mCameraDevice = null;

            }

            if (null != mImageReader) {

                mImageReader.close();

                mImageReader = null;

            }

        } catch (InterruptedException e) {

            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);

        } finally {

            mCameraOpenCloseLock.release();

        }

    }


    /**
     * Starts a background thread and its {@link Handler}.
     */

    private void startBackgroundThread() {

        mBackgroundThread = new HandlerThread("CameraBackground");

        mBackgroundThread.start();

        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());

    }


    /**
     * Stops the background thread and its {@link Handler}.
     */

    private void stopBackgroundThread() {

        mBackgroundThread.quitSafely();

        try {

            mBackgroundThread.join();

            mBackgroundThread = null;

            mBackgroundHandler = null;

        } catch (InterruptedException e) {

            e.printStackTrace();

        }

    }


    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */

    private void createCameraPreviewSession() {

        try {

            SurfaceTexture texture = mTextureView.getSurfaceTexture();

            assert texture != null;


            // We configure the size of default buffer to be the size of camera preview we want.

            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());


            // This is the output Surface we need to start preview.

            Surface surface = new Surface(texture);


            // We set up a CaptureRequest.Builder with the output Surface.

            mPreviewRequestBuilder

                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            //摄像头获取的数据在surface上显示
            mPreviewRequestBuilder.addTarget(surface);


            // Here, we create a CameraCaptureSession for camera preview.
            //获取surface列表
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),

                    new CameraCaptureSession.StateCallback() {


                        @Override

                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {

                            // The camera is already closed

                            if (null == mCameraDevice) {

                                return;

                            }


                            // When the session is ready, we start displaying the preview.

                            mCaptureSession = cameraCaptureSession;

                            try {

                                // Auto focus should be continuous for camera preview.

                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,

                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                                // Flash is automatically enabled when necessary.

                                setAutoFlash(mPreviewRequestBuilder);


                                // Finally, we start displaying the camera preview.

                                mPreviewRequest = mPreviewRequestBuilder.build();
                                //连续获取图片请求

                                mCaptureSession.setRepeatingRequest(mPreviewRequest,

                                        mCaptureCallback, mBackgroundHandler);

                            } catch (CameraAccessException e) {

                                e.printStackTrace();

                            }

                        }


                        @Override

                        public void onConfigureFailed(

                                @NonNull CameraCaptureSession cameraCaptureSession) {

                            showToast("Failed");

                        }

                    }, null

            );

        } catch (CameraAccessException e) {

            e.printStackTrace();

        }

    }


    /**
     * Configures the necessary {@link Matrix} transformation to `mTextureView`.
     * <p>
     * This method should be called after the camera preview size is determined in
     * <p>
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */

    private void configureTransform(int viewWidth, int viewHeight) {

        Activity activity = getActivity();

        if (null == mTextureView || null == mPreviewSize || null == activity) {

            return;

        }

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

        Matrix matrix = new Matrix();

        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);

        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());

        float centerX = viewRect.centerX();

        float centerY = viewRect.centerY();

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {

            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());

            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);

            float scale = Math.max(

                    (float) viewHeight / mPreviewSize.getHeight(),

                    (float) viewWidth / mPreviewSize.getWidth());

            matrix.postScale(scale, scale, centerX, centerY);

            matrix.postRotate(90 * (rotation - 2), centerX, centerY);

        } else if (Surface.ROTATION_180 == rotation) {

            matrix.postRotate(180, centerX, centerY);

        }

        mTextureView.setTransform(matrix);

    }


    /**
     * Initiate a still image capture.
     */

    private void takePicture() {

        if(mAutoFocusSupported) {
            lockFocus();
        }else{
            captureStillPicture();
        }

    }


    /**
     * Lock the focus as the first step for a still image capture.
     */

    private void lockFocus() {

        try {

            // This is how to tell the camera to lock focus.

            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,

                    CameraMetadata.CONTROL_AF_TRIGGER_START);


            // Tell #mCaptureCallback to wait for the lock.

            mState = STATE_WAITING_LOCK;

            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,

                    mBackgroundHandler);

        } catch (CameraAccessException e) {

            e.printStackTrace();

        }

    }


    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * <p>
     * we get a response in {@link #mCaptureCallback} from {@link #lockFocus()}.
     */

    private void runPrecaptureSequence() {

        try {

            // This is how to tell the camera to trigger.

            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,

                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);

            // Tell #mCaptureCallback to wait for the precapture sequence to be set.

            mState = STATE_WAITING_PRECAPTURE;

            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,

                    mBackgroundHandler);

        } catch (CameraAccessException e) {

            e.printStackTrace();

        }

    }


    /**
     * Capture a still picture. This method should be called when we get a response in
     * <p>
     * {@link #mCaptureCallback} from both {@link #lockFocus()}.
     */

    private void captureStillPicture() {

        try {

            final Activity activity = getActivity();

            if (null == activity || null == mCameraDevice) {

                return;

            }

            // This is the CaptureRequest.Builder that we use to take a picture.

            final CaptureRequest.Builder captureBuilder =

                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            captureBuilder.addTarget(mImageReader.getSurface());


            // Use the same AE and AF modes as the preview.

            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,

                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            setAutoFlash(captureBuilder);


            // Orientation

            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));


            CameraCaptureSession.CaptureCallback CaptureCallback

                    = new CameraCaptureSession.CaptureCallback() {


                @Override

                public void onCaptureCompleted(@NonNull CameraCaptureSession session,

                                               @NonNull CaptureRequest request,

                                               @NonNull TotalCaptureResult result) {

                    Toast.makeText(activity.getApplicationContext(), "Image Saved!", Toast.LENGTH_SHORT).show();

                    Log.d(TAG, mFile.toString());

                    unlockFocus();

                }

            };


            mCaptureSession.stopRepeating();

            mCaptureSession.abortCaptures();

            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);

        } catch (CameraAccessException e) {

            e.printStackTrace();

        }

    }


    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */

    private int getOrientation(int rotation) {

        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)

        // We have to take that into account and rotate JPEG properly.

        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.

        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.

        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;

    }


    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * <p>
     * finished.
     */

    private void unlockFocus() {

        try {

            // Reset the auto-focus trigger

            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,

                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);

            setAutoFlash(mPreviewRequestBuilder);

            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,

                    mBackgroundHandler);

            // After this, the camera will go back to the normal state of preview.

            mState = STATE_PREVIEW;

            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,

                    mBackgroundHandler);

        } catch (CameraAccessException e) {

            e.printStackTrace();

        }

    }


    @Override

    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.pic: {

                takePicture();

                break;

            }


        }

    }


    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {

        if (mFlashSupported) {

            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,

                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

        }

    }


    /**
     * Saves a JPEG {@link Image} into the specified {@link File}.
     */

    private static class ImageSaver implements Runnable {


        /**
         * The JPEG image
         */

        private final Image mImage;

        /**
         * The file we save the image into.
         */

        private final File mFile;


        ImageSaver(Image image, File file) {

            mImage = image;

            mFile = file;

        }


        @Override

        public void run() {

            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();//获取图片矩阵

            byte[] bytes = new byte[buffer.remaining()];

            buffer.get(bytes);


            FileOutputStream output = null;

            try {


                output = new FileOutputStream(mFile);
                output.write(bytes);
                Log.d(TAG, "run: "+mFile.toString());


            } catch (IOException e) {

                e.printStackTrace();

            } finally {

                mImage.close();

                if (null != output) {

                    try {

                        output.close();

                    } catch (IOException e) {

                        e.printStackTrace();

                    }

                }

            }

        }


    }


    /**
     * Compares two {@code Size}s based on their areas.
     */

    static class CompareSizesByArea implements Comparator<Size> {


        @Override

        public int compare(Size lhs, Size rhs) {

            // We cast here to ensure the multiplications won't overflow

            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -

                    (long) rhs.getWidth() * rhs.getHeight());

        }


    }








}
/**
 * 使用 CameraDevice.createCaptureRequest(int) 方法获取一个 CaptureRequest.Builder 对象。其中的 int 取值为：
 *
 * TEMPLATE_PREVIEW ： 用于创建一个相机预览请求。相机会优先保证高帧率而不是高画质。适用于所有相机设备。
 * TEMPLATE_STILL_CAPTURE ： 用于创建一个拍照请求。相机会优先保证高画质而不是高帧率。适用于所有相机设备。
 * TEMPLATE_RECORD ： 用于创建一个录像请求。相机会使用标准帧率，并设置录像级别的画质。适用于所有相机设备。
 * TEMPLATE_VIDEO_SNAPSHOT ： 用于创建一个录像时拍照的请求。相机会尽可能的保证照片质量的同时不破坏正在录制的视频质量。适用于硬件支持级别高于 LEGACY 的相机设备。
 * TEMPLATE_ZERO_SHUTTER_LAG ： 用于创建一个零延迟拍照的请求。相机会尽可能的保证照片质量的同时不损失预览图像的帧率，3A（自动曝光、自动聚焦、自动白平衡）都为 auto 模式。只适用于支持 PRIVATE_REPROCESSING 和 YUV_REPROCESSING 的相机设备。
 * TEMPLATE_MANUAL ： 用于创建一个手动控制相机参数的请求。相机所有自动控制将被禁用，后期处理参数为预览质量，手动控制参数被设置为合适的默认值，需要用户自己根据需求来调整各参数。适用于支持 MANUAL_SENSOR 的相机设备。
 */



