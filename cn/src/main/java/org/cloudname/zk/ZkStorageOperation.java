package org.cloudname.zk;

import org.cloudname.CoordinateListener;
import org.cloudname.StorageOperation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: dybdahl
 * Date: 03.02.12
 * Time: 12:31
 * To change this template use File | Settings | File Templates.
 */
public class ZkStorageOperation implements StorageOperation {
    private boolean isDone = false;
    List<Callback> callbacks = Collections.synchronizedList(new ArrayList());

    @Override
    public boolean waitForCompletionMillis(int milliSeconds) {
        System.out.println("Waiting for operation");
        final CountDownLatch latch = new CountDownLatch(1);
        registerCallback(new Callback() {
            @Override
            public void success() {
                latch.countDown();
            }
        });
        try {
            return latch.await(milliSeconds, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public void registerCallback(Callback callback) {
        System.out.println("Register callback!!!!!");
        boolean runCallback;
        synchronized (this) {
            runCallback = isDone;
            if (!runCallback) {
                callbacks.add(callback);
            }
        }
        if (runCallback)  {
            callback.success();
        }
    }

    @Override
    public boolean isDone() {
        synchronized (this) {
            return isDone;
        }
    }

    public Callback getSystemCallback() {
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$ Get system callback");
        return new Callback() {
            @Override
            public void success() {
                synchronized (this) {
                    if (isDone) {
                        return;
                    }
                    isDone = true;
                }
                for (Callback callback : callbacks) {
                    callback.success();
                }
            }
        };
    }
}
