package com.example.weighing.service;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface MwicLibrary extends Library {
    MwicLibrary INSTANCE = Native.load("mwrf32", MwicLibrary.class);

    int rf_init(int port, int baud);
    int rf_exit(int icdev);
    int rf_card(int icdev, int mode, byte[] snr);
    int rf_authentication(int icdev, int mode, int block, byte[] key);
    int rf_read(int icdev, int block, byte[] data);
    int rf_write(int icdev, int block, byte[] data);

}
