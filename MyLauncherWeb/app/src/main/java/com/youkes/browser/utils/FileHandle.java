package com.youkes.browser.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileHandle {
    private static FileInputStream isfile;
    private static boolean isRunning = false;
    private static Thread thread;

    public static boolean isIsRun(){
        return isRunning;
    }

    public static void readFile(final String filename){
        if(thread !=null && thread.isAlive()){
            thread.interrupt();
            thread = null;
            if(isfile != null){
                isfile = null;
            }
        }
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                isRunning = true;
                LogUtil.e("FileHandle");
                while(isRunning) {
                    try {
                        Thread.sleep(2000);
                        isfile = new FileInputStream(new File(filename));
                        int length = 0;
                        byte[] content = new byte[2048];
                        length = isfile.read(content, length, content.length - length);
                        if (length != 0) {
                            Constant.CurrentNumber = 0;
                            Constant.isResetPlay = true;
                        }
                    } catch (Exception e) {
                        isRunning = false;
                        e.printStackTrace();
                        LogUtil.e("e" + e.toString());
                    }
                }
                if(isfile != null){
                    isfile = null;
                }
            }
        });
        thread.start();
    }
    public static void stopFileHandle() {
        isRunning = false;
        if(thread != null){
            thread.interrupt();
            thread = null;
        }
    }

}
