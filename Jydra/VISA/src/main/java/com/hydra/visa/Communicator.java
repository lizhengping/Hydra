package com.hydra.visa;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Hwaipy
 */
class Communicator {

  private final InputStream inputStream;
  private final OutputStream outputStream;
  private final BlockingQueue<byte[]> outputQueue = new LinkedBlockingQueue<>();
  private final BlockingQueue<byte[]> inputQueue = new LinkedBlockingQueue<>();
  private boolean running = false;
  private ArrayList<Byte> readBuffer = new ArrayList<>();

  public Communicator(InputStream inputStream, OutputStream outputStream) {
    this.inputStream = inputStream;
    this.outputStream = outputStream;
  }

  public void start() {
    synchronized (this) {
      if (running) {
        return;
      }
      running = true;
    }
    new Thread(new Runnable() {

      @Override
      public void run() {
        while (running) {
          try {
            byte[] data = outputQueue.take();
            outputStream.write(data);
          } catch (InterruptedException | IOException ex) {
            ex.printStackTrace(System.err);
            running = false;
          }
        }
      }
    }).start();
    new Thread(new Runnable() {

      @Override
      public void run() {
        while (running) {
          try {
            int read = inputStream.read();
            if (read == -1) {
              running = false;
            }
            if (read == 10) {
              byte[] data = new byte[readBuffer.size()];
              for (int i = 0; i < data.length; i++) {
                data[i] = readBuffer.get(i);
              }
              readBuffer.clear();
              inputQueue.offer(data);
            } else {
              readBuffer.add((byte) read);
            }
          } catch (IOException ex) {
            ex.printStackTrace(System.err);
            running = false;
          }
        }
      }
    }).start();
  }

  public void sendLater(byte[] data) {
    outputQueue.offer(data);
  }

  public byte[] read(long timeout) throws InterruptedException {
    return inputQueue.poll(timeout, TimeUnit.MILLISECONDS);
  }
}
