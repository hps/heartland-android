package com.example.app;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class BTSelection {
    private static Handler mHandler = null;
    private static ScheduledExecutorService thread = null;

    private BTSelection() {
    }

    public static BTSelection getInstance(){
        mHandler = new Handler();
        thread = Executors.newSingleThreadScheduledExecutor();
        return new BTSelection();
    }

    public void Selection(final BluetoothDevice device, final SelectionCallback selectionCallback){
        thread.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (selectionCallback != null) {
                                selectionCallback.onSelection(device);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public interface SelectionCallback{
        void onSelection(BluetoothDevice device);
    }
}
