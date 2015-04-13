package com.geogie.Homework0407_InfiniteScrolling;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements InfiniteScrolling.Callback{
    private InfiniteScrolling infiniteScrolling;

    /**
     * 分页信息
     */
    private int currentPager = 1;
    private int pagerSize = 9;
    /**
     * 装图片的URL地址
     */
    public List<String> imageURLList;

    /**
     * 标记是否为加载状态
     */
    private boolean isLoading;
    private Handler handler = new Handler();
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        infiniteScrolling = (InfiniteScrolling) findViewById(R.id.infiniteScrolling);

        // 设置监听
        infiniteScrolling.setCallback(this);
        imageURLList = new ArrayList<String>();

        // 加载图片的地址
        InputStream open = null ;
        BufferedReader br = null ;
        try {
            open = getResources().getAssets().open("imageurl.txt");
            br = new BufferedReader(new InputStreamReader(open));
            String line;
            while ((line = br.readLine()) != null) {
                imageURLList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (open != null) {
                try {
                    open.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        reloadImages();
    }

    /**
     * 加载成功图片的个数
     */
    private int loadSuccessNumber;

    public void reloadImages() {
        loadSuccessNumber = 0;
        isLoading = true;
        int startIndex = (currentPager - 1) * pagerSize;
        int endIndex = startIndex + pagerSize;
        if(startIndex > imageURLList.size()) {
            return ;
        } else if(endIndex > imageURLList.size()) {
            endIndex = imageURLList.size() ;
        }

        for (int i = startIndex; i < endIndex; i++) {
            reloadImage(imageURLList.get(i));
        }
        currentPager += 1;
    }

    private void reloadImage(String URL) {

        String imageName = getImageName(URL);

        if (!isInSDCard(imageName)) {
            loadImageFromURL(URL);
        } else {
            loadImageFromSDCard(URL);
        }
    }

    private void loadImageFromURL(final String URLString) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                String imageName = getImageName(URLString);
                File baseFile = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                if (!baseFile.exists()) {
                    baseFile.mkdirs();
                }
                File file = new File(baseFile, imageName);

                HttpURLConnection urlConnection = null;
                BufferedInputStream bis = null;
                BufferedOutputStream bos = null;
                byte[] buff = null ;

                try {
                    URL url = new URL(URLString);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(5000);
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    if (urlConnection.getResponseCode() == 200) {
                        bis = new BufferedInputStream(
                                urlConnection.getInputStream());
                        bos = new BufferedOutputStream(
                                new FileOutputStream(file));
                        buff = new byte[1024 * 8];
                        int len;
                        while ((len = bis.read(buff)) != -1) {
                            bos.write(buff, 0, len);
                            bos.flush();
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (bos != null) {
                        try {
                            bos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    buff = null ;
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        loadImageFromSDCard(URLString);
                    }
                });
            }
        }).start();
    }

    private void loadImageFromSDCard(String URL) {
        String imageName = getImageName(URL);
        File baseFile = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(baseFile, imageName);

        if (file.exists()) {
            BitmapFactory.Options option = new BitmapFactory.Options() ;
            option.inJustDecodeBounds = true ;
            BitmapFactory.decodeFile(file.getAbsolutePath(), option);
            int oldWidth = option.outWidth ;
            int width = 200 ;
            int ratioWidth = oldWidth / width ;

            option.inSampleSize = ratioWidth ;
            option.inPreferredConfig = Bitmap.Config.RGB_565 ;
            option.inJustDecodeBounds = false ;
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), option);

            if (bitmap != null) {
                infiniteScrolling.addImage(bitmap);
            }
        }

        loadSuccessNumber += 1;
        if (loadSuccessNumber == pagerSize) {
            isLoading = false;
            loadSuccessNumber = 0 ;
        }
    }

    private boolean isInSDCard(String imageName) {
        File baseFile = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(baseFile, imageName).exists();
    }

    @Override
    public void isBottom() {
        //if (!isLoading) {
        reloadImages();
        //}
    }
    public String getImageName(String URL) {
        return URL.replaceAll(":*/+", "_");
    }
}
