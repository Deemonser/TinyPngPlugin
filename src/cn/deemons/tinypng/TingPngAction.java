package cn.deemons.tinypng;

import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.squareup.okhttp.*;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class TingPngAction extends AnAction {
    static Logger logger = Logger.getLogger("UploadFileAction");

    final static String url = "https://tinypng.com/web/shrink";

    private static int currentIndex = 0;
    private ArrayList<File> pictureFiles = new ArrayList<>();
    private Project project;
    private static boolean cancelTiny = false;

    private String parantPath = "";

    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getProject();


        VirtualFile[] selectedFiles = chooseFile();


        if (selectedFiles.length == 0) {
            return;
        }

        setParentPath(selectedFiles);

        pictureFiles.clear();

        filterAllPictures(selectedFiles);


        tinyPng();

    }

    /**
     * 文件根路径
     *
     * @param selectedFiles 文件
     */
    private void setParentPath(VirtualFile[] selectedFiles) {
        try {
            if (selectedFiles.length == 1) {
                VirtualFile file = selectedFiles[0];
                if (file.isDirectory()) {
                    parantPath = file.getPath() + "/";
                } else {
                    parantPath = file.getParent().getPath() + "/";
                }
            } else {
                parantPath = selectedFiles[0].getParent().getPath() + "/";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 选择文件
     */
    private VirtualFile[] chooseFile() {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, true, false, false, false, true);
        return FileChooser.chooseFiles(descriptor, project, project.getBaseDir());
    }


    /**
     * 过滤文件
     *
     * @param selectedFiles 文件
     */
    private void filterAllPictures(VirtualFile[] selectedFiles) {
        for (int i = 0; i < selectedFiles.length; i++) {
            VirtualFile selectedFile = selectedFiles[i];
            String selectedFileName = selectedFile.getName().toLowerCase();
            if (selectedFile.isDirectory()) {
                if (!selectedFileName.equals("build")) {
                    VirtualFile[] directoryChildren = selectedFile.getChildren();
                    filterAllPictures(directoryChildren);
                }
            } else if (selectedFileName.endsWith(".jpg") || selectedFileName.endsWith(".png") || selectedFileName.endsWith(".jpeg")) {
                logger.info("path: " + selectedFile.getPath());
                pictureFiles.add(new File(selectedFile.getPath()));
                if (i >= selectedFiles.length - 1) {
                    return;
                }
            }
        }
    }

    private void tinyPng() {
        if (pictureFiles == null || pictureFiles.size() == 0) return;

        Progress dialog = new Progress();
        dialog.setTitle("Progress");
        dialog.setMax(pictureFiles.size());
        dialog.setValue(0);
        dialog.pack();

        dialog.setCancelListener(() -> cancelTiny = true);
        dialog.addString("ParentFile:  " + parantPath);
        dialog.addString("");

        cancelTiny = false;
        currentIndex = 0;

        //获取可用线程数量
        int availableProcessors = Runtime.getRuntime().availableProcessors();

        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(availableProcessors * 4);

        for (int i = 0; i < pictureFiles.size(); i++) {
            File file = pictureFiles.get(i);

            fixedThreadPool.execute(() -> {

                try {
                    if (!cancelTiny) {
                        UploadBean uploadBean = uploadImage(file);

                        SwingUtilities.invokeLater(() -> {

                            dialog.setValue(++currentIndex);
                            dialog.addString(file.getPath().replace(parantPath, ""),
                                    uploadBean == null ? file.length() : uploadBean.getInput().getSize(),
                                    uploadBean == null ? file.length() : uploadBean.getOutput().getSize(),
                                    uploadBean == null ? 0 : 1 - uploadBean.getOutput().getRatio()
                            );
                            dialog.canFinish();

                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        dialog.setValue(++currentIndex);
                        dialog.showError(file.getAbsolutePath() + "  " + e.getMessage());
                        dialog.canFinish();
                    });
                }

            });
        }

        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    /**
     * 上传
     *
     * @param sourceFile
     * @return
     * @throws IOException
     */
    public UploadBean uploadImage(File sourceFile) throws IOException {
        if (sourceFile == null || !sourceFile.exists()) return null;


        Request request = new Request.Builder()
                .addHeader("content-length", String.valueOf(sourceFile.length()))
                .addHeader("Content-Type", "image/jpeg")
                .addHeader("referer", "https://tinypng.com/")
                .url(url)
                .post(RequestBody.create(MediaType.parse("image/jpeg"), sourceFile))
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setReadTimeout(30, TimeUnit.SECONDS);
        okHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
        Response response = okHttpClient.newCall(request).execute();

        if (!response.isSuccessful()) return null;

        UploadBean uploadBean = new Gson().fromJson(response.body().string(), UploadBean.class);

        if (uploadBean == null || uploadBean.getOutput() == null || uploadBean.getOutput().getUrl().isEmpty())
            return null;

        InputStream inputStream = okHttpClient.newCall(new Request.Builder()
                .get()
                .url(uploadBean.getOutput().getUrl())
                .build()
        ).execute().body().byteStream();


        saveToFile(sourceFile.getAbsolutePath(), inputStream);

        return uploadBean;
    }


    /**
     * 保存图片
     *
     * @param fileName
     * @param in
     * @throws IOException
     */
    private void saveToFile(String fileName, InputStream in) throws IOException {

        int BUFFER_SIZE = 1024;
        byte[] buf = new byte[BUFFER_SIZE];
        int size = 0;

        BufferedInputStream bis = new BufferedInputStream(in);
        FileOutputStream fos = new FileOutputStream(fileName);

        //保存文件
        while ((size = bis.read(buf)) != -1) {
            fos.write(buf, 0, size);
        }

        fos.close();
        bis.close();
    }


    public static void main(String[] args) {


        try {
            UploadBean uploadBean = new TingPngAction().uploadImage(new File("/Users/deemons/Documents/thumb-1920-411820.jpg"));
            System.out.print(uploadBean.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}