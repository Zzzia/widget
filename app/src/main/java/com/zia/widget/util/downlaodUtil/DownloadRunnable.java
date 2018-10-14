package com.zia.widget.util.downlaodUtil;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadRunnable implements Runnable {

    private String url, path, fileName;
    private int fileLength, currentFileLength = 0;
    private DownloadListener downloadListener;
    private int position;

    public DownloadRunnable(String url, String path, String fileName) {
        this(url, path, fileName, null);
    }

    public DownloadRunnable(String url, String path, String fileName, DownloadListener downloadListener) {
        this.url = url;
        this.path = DownLoaderHelper.optimizeRoot(path);
        this.fileName = fileName;
        this.downloadListener = downloadListener;
    }

    @Override
    public void run() {
        if (!DownLoaderHelper.mkDir(path)) {
            Log.e("DownloadRunnable", "创建文件路径失败..");
            return;
        }

        HttpURLConnection conn = null;
        InputStream inputStream = null;

        try {

            url = ReLocateUtil.getLocate(url);
            URL httpUrl = new URL(url);

            conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setConnectTimeout(5000);
            conn.setDoInput(true);
            conn.connect();

            fileLength = conn.getContentLength();
            Log.d("DownloadRunnable", fileName + " Size: " + DownLoaderHelper.convertSize(fileLength));

            inputStream = conn.getInputStream();
            loadSteam(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (conn != null) {
                conn.disconnect();
            }
//            Thread.currentThread().interrupt();
        }
    }

    //写入本地
    private void loadSteam(InputStream inputStream) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        FileOutputStream fileOut = new FileOutputStream(path + fileName);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOut);
        byte[] buf = new byte[4096];
        int lastRatio = 0;
        int length = bufferedInputStream.read(buf);
        while (length != -1) {
            bufferedOutputStream.write(buf, 0, length);
            length = bufferedInputStream.read(buf);
            currentFileLength = currentFileLength + length;
            float ratio = (float) currentFileLength / (float) fileLength * 100;
            if (downloadListener != null && lastRatio != (int) (ratio * 100)) {//变化超过0.01时回调
                downloadListener.getRatio(ratio, currentFileLength, fileLength);
                lastRatio = (int) (ratio * 100);
            }
        }
        bufferedOutputStream.close();
        bufferedInputStream.close();
        if (downloadListener != null) downloadListener.getRatio(100f, fileLength, fileLength);
    }
}
