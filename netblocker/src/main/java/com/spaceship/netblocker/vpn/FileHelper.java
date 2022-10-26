package com.spaceship.netblocker.vpn;

import android.content.Context;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructPollfd;

import com.spaceship.netblocker.utils.Slog;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



final class FileHelper {

    
    public static InputStream openRead(Context context, String filename) throws IOException {
        try {
            return context.openFileInput(filename);
        } catch (FileNotFoundException e) {
            return context.getAssets().open(filename);
        }
    }

    
    public static OutputStream openWrite(Context context, String filename) throws IOException {
        File out = context.getFileStreamPath(filename);

        // Create backup
        out.renameTo(context.getFileStreamPath(filename + ".bak"));

        return context.openFileOutput(filename, Context.MODE_PRIVATE);
    }

    
    public static int poll(StructPollfd[] fds, int timeout) throws ErrnoException, InterruptedException {
        while (true) {
            if (Thread.interrupted())
                throw new InterruptedException();
            try {
                return Os.poll(fds, timeout);
            } catch (ErrnoException e) {
                if (e.errno == OsConstants.EINTR)
                    continue;
                throw e;
            }
        }
    }

    public static FileDescriptor closeOrWarn(FileDescriptor fd, String tag, String message) {
        try {
            if (fd != null)
                Os.close(fd);
        } catch (ErrnoException e) {
            Slog.INSTANCE.e("closeOrWarn: " + message, e);
        } finally {
            return null;
        }
    }

    public static <T extends Closeable> T closeOrWarn(T fd, String tag, String message) {
        try {
            if (fd != null)
                fd.close();
        } catch (Exception e) {
            Slog.INSTANCE.e("closeOrWarn: " + message, e);
        } finally {
            return null;
        }
    }
}
