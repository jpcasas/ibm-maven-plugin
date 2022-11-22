package io.github.jpcasas.ibm.plugin.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.maven.plugin.logging.Log;

public class ProcessOutputLogger extends Thread {

    private InputStream is;
    private Log log;

    public ProcessOutputLogger(InputStream is, Log log) {
        this.is = is;
        this.log = log;
    }

    @Override
    public void run() {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        String line = null;
        try {
            while (true) {
                line = bufferedReader.readLine();
                if (line != null) {
                    log.info(line);
                    line = null;
                } else {
                    Thread.sleep(500);
                }
            }
        } catch (IOException ioe) {

            ioe.printStackTrace();
        } catch (InterruptedException ie) {

        } catch (Exception e) {

            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                // ignore this one
            }
        }
    }
}