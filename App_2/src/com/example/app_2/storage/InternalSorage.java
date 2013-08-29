package com.example.app_2.storage;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.content.Context;
import android.util.Log;

import com.example.app_2.App_2;

public class InternalSorage {
	
	public static boolean writeToInternalFile(byte[] bytes, String filename) {
		FileOutputStream outputstream;
		try {
			outputstream = App_2.getAppContext().openFileOutput(filename, Context.MODE_PRIVATE);
			outputstream.write(bytes);
			outputstream.close();
		} catch (Exception e) {
			Log.w("InternalStorage", "Error writing", e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Reads from the internal file byte array
	 * 
	 * @param filename
	 *            file name
	 * @param actv
	 *            activity
	 * @return byte array, else null
	 */
	public static byte[] readFromInternalFile(String filename) {
		byte[] bytes = new byte[100];
		byte b = -1;
		int i = 0;

		FileInputStream fi;
		try {
			fi = App_2.getAppContext().openFileInput(filename);

			do {
				b = (byte) fi.read();
				bytes[i] = b;
				i++;
			} while (b != -1);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return bytes;
	}

	/**
	 * Delete internal file specified by filename and context
	 * 
	 * @param filename
	 *            file name
	 * @param c
	 *            context
	 */
	public static void deleteInternalFile(String filename) {
		App_2.getAppContext().deleteFile(filename);
	}

}
