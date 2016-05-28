package com.elong.tourpal.imageasyncloader.core;

import android.os.Build;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ImageTaskExecutor
 *
 * @author tao.chen1
 */
public class ImageTaskExecutor implements Executor {
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final int MAX_POOL_SIZE = 4;

    private static int sNumOfCpuCores = Runtime.getRuntime().availableProcessors();
    private static volatile ImageTaskExecutor sInstance = null;

    private final ThreadPoolExecutor mThreadPool;
    private final LinkedBlockingStack<Runnable> mTaskWorkQueue;

    public static ImageTaskExecutor getInstance() {
        if (sInstance == null) {
            synchronized (ImageTaskExecutor.class) {
                if (sInstance == null) {
                    sInstance = new ImageTaskExecutor();
                }
            }
        }
        return sInstance;
    }

    private ImageTaskExecutor() {
        mTaskWorkQueue = new LinkedBlockingStack<>();
        int poolSize = Math.min(sNumOfCpuCores, MAX_POOL_SIZE);
        mThreadPool = new ThreadPoolExecutor(poolSize, poolSize, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mTaskWorkQueue, new DefaultThreadFactory());
        if (Build.VERSION.SDK_INT >= 10) {
            mThreadPool.allowCoreThreadTimeOut(true);
        }
    }

    @Override
    public void execute(Runnable command) {
        mThreadPool.execute(command);
    }

    public static class LinkedBlockingStack<T> extends LinkedBlockingDeque<T> {

        private static final long serialVersionUID = -4114786347960826192L;
        private int mImageTaskOrder = ImageTaskOrder.FIRST_IN_FIRST_OUT;

        public void setTaskOrder(int order) {
            mImageTaskOrder = order;
        }

        @Override
        public boolean offer(T e) {
            if (mImageTaskOrder == ImageTaskOrder.LAST_IN_FIRST_OUT) {
                return super.offerFirst(e);
            } else {
                return super.offer(e);
            }
        }

        @Override
        public T remove() {
            if (mImageTaskOrder == ImageTaskOrder.LAST_IN_FIRST_OUT) {
                return super.removeFirst();
            } else {
                return super.remove();
            }
        }
    }

    private static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        private static final String sPre = "image-executor-pool-";
        private static final String sPost = "-thread-";

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = sPre + poolNumber.getAndIncrement() + sPost;
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
