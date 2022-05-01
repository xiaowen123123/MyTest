package com.sxw.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.chaquo.python.PyObject;
import com.chaquo.python.android.AndroidPlatform;
import com.chaquo.python.Python;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {

    private static final int TAKE_PHOTO = 1;
    private static final String TAG = "xiaowen";
    private Button takePhoto;
    private ImageView imageView;
    private Uri imageUri;
    private Button upload;
    private TextView tip;
    private Bitmap bitmap;
    private String imagePath = null;
    private String imageName;
    private int hasView = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //申请相机权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        //主线程使用网络请求
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        takePhoto = findViewById(R.id.take_photo);
        imageView = findViewById(R.id.imageView);
        upload = findViewById(R.id.upload);
        tip = findViewById(R.id.tip);

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 创建File对象，用于存储拍照后的图片
//                 File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
//                 Log.e(TAG, String.valueOf(getExternalCacheDir()));
//                imageName = "output_image.jpg";

                //通过时间戳创建文件名
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPG_" + timeStamp;
                File outputImage = new File(getExternalCacheDir(), imageFileName + ".jpg");
                imageName = imageFileName + ".jpg";
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 进行一个判断
                // 如果运行设备的系统版本低于Android 7.0,就调用Uri的fromFile()方法将File对象转换成Uri对象
                // 这个Uri对象标识着output_image.jpg这张图片的本地真实路径
                // 否则，就调用FileProvider的getUriForFile()方法将File对象转换成一个封装过的Uri对象
                // getUriForFile()方法接收 3个参数，第一个参数要求传入Context对象，第二个参数可以是任意唯一的字符串，第三个参数则是我们刚刚创建的File对象
                // 之所以要进行这样一层转换，是因为从Android 7.0系统开始，直接使用本地真实路径的Uri被认为是不安全的，会抛出一个FileUriExposedException 异常
                // 而FileProvider则是一种特殊的内容提供器，它使用了和内容提供器类似的机制来对数据进行保护，可以选择性地将封装过的Uri共享给外部，从而提高了应用的安全性
                if (Build.VERSION.SDK_INT >= 24) {
                    imageUri = FileProvider.getUriForFile(MainActivity.this, "com.example.myapplication.fileprovider", outputImage);
                } else {
                    imageUri = Uri.fromFile(outputImage);
                }

                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, TAKE_PHOTO);
                }
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasView == 0) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("提示")
                            .setMessage("请先拍摄照片")
                            .setPositiveButton("确定", null)
                            .show();
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            uploadImage(view, imageName);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        imageView.setImageBitmap(bitmap);
                        hasView = 1;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            default:
                break;
        }
    }

    /**
     * 上传图片功能
     *
     * @param view
     * @param imageName
     * @return 新图片的路径
     * @throws IOException
     * @throws JSONException
     */
    public void uploadImage(View view, String imageName) throws IOException, JSONException {
        String url = "http://192.168.42.28:5000/upload";
        OkHttpClient okHttpClient = new OkHttpClient();
        File file = new File(getExternalCacheDir(), imageName);
        Log.e(TAG, "图片路径为" + file.getAbsolutePath());
        String filePath = file.getAbsolutePath();
        if (!file.exists()) {
            file.mkdir();
        }
        //通过python上传文件
        initPython();
        Python py = Python.getInstance();
        PyObject po = py.getModule("client").callAttr("sock_client_image", filePath);
//        String result = po.toJava(String.class);
//        Log.e(TAG,result);
//        Looper.prepare();
//        Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
//        Looper.loop();


//        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
//        MultipartBody multipartBody = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("file", file.getName(), requestBody)
//                .build();
//        Request request = new Request.Builder()
//                .url(url)
//                .post(multipartBody)
//                .build();
//        okHttpClient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.e("上传图片失败", "onFailure: " + e.toString());
//                Looper.prepare();
//                Toast.makeText(MainActivity.this, "上传图片失败", Toast.LENGTH_LONG).show();
//                Looper.loop();
//
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                Log.e("上传图片", "onResponse: " + response.body().string());
//                if (response.code() == 200) {
//                    Looper.prepare();
//                    Toast.makeText(MainActivity.this, "识别成功", Toast.LENGTH_LONG).show();
//                    Looper.loop();
//                }
//            }
//        });
    }

    // 初始化Python环境
    void initPython() {
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
    }

    // 调用python代码
    void callPythonCode() {
        Python py = Python.getInstance();
        py.getModule("client").callAttr("sock_client_image");
    }

    public void click(View view) {
        initPython();
        callPythonCode();
    }
}