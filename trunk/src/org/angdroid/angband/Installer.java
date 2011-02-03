package org.angdroid.angband;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.util.Log;
import android.os.Environment;
import android.os.Handler;

public class Installer {

	private StateManager state = null;
	private Handler handler = null;
	private static Thread installWorker = null;

	public Installer(StateManager s, Handler h) {
		state = s;
		handler = h;
	}

	public synchronized void checkInstall() {
		if (needsInstall()) {
			if (state.installState == StateManager.InstallState.Unknown) {
				state.installState = StateManager.InstallState.InProgress;
			
				handler.sendEmptyMessage(AngbandDialog.Action.ShowProgress.ordinal());

				installWorker = new Thread() {  
						public void run() {
							install();
							handler.sendEmptyMessage(AngbandDialog.Action.DismissProgress.ordinal());
						}
					};
				installWorker.start();
			}
			else {
				return; // install is in error or in progress, cancel
			}
		}
		else {
			state.installState = StateManager.InstallState.Success;
		}
	}

	public boolean needsInstall() {
		// validate sdcard here
		String check = Environment.getExternalStorageState();
		Log.v("Angband", "media check:" + check);
		if (check.compareTo(Environment.MEDIA_MOUNTED) != 0) {
			state.installState = StateManager.InstallState.MediaNotReady;
			return true;
		}

		return (Preferences.getInstalledVersion().compareTo(Preferences.getVersion()) != 0);
	}

	public void install() {
		boolean success = true;
		for(int i = 0; i < Preferences.getInstalledPlugins().length; i++) {
			success = extractPluginResources(Preferences.getInstalledPlugins()[i]);
			if (!success) break;
		}
		if (success) {
			Preferences.setInstalledVersion(Preferences.getVersion());
			state.installState = StateManager.InstallState.Success;
		}
		else
			state.installState = StateManager.InstallState.Failure;
	}

	private boolean extractPluginResources(int plugin) {
		//Log.d("Angband","extractPluginResources "+plugin);
		boolean result = true;
		try {
			File f = new File(Preferences.getAngbandFilesDirectory(plugin));
			f.mkdirs();
			String abs_path = f.getAbsolutePath();
			Log.v("Angband", "installing to " + abs_path);

			ZipInputStream zis = Plugins.getPluginZip(plugin);
			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null) {
				String ze_name = ze.getName();
				Log.v("Angband", "extracting " + ze_name);

				String filename = abs_path + "/" + ze_name;
				File myfile = new File(filename);

				if (ze.isDirectory()) {
					myfile.mkdirs();
					continue;
				}

				byte contents[] = new byte[(int) ze.getSize()];

				FileOutputStream fos = new FileOutputStream(myfile);
				int remaining = (int) ze.getSize();

				int totalRead = 0;

				while (remaining > 0) {
					int readlen = zis.read(contents, 0, remaining);
					fos.write(contents, 0, readlen);
					totalRead += readlen;
					remaining -= readlen;
				}

				fos.close();
				
				// perform a basic length validation
				myfile = new File(filename);
				if (myfile.length() != totalRead) {
					Log.v("Angband", "Installer.length mismatch: " + filename);
					throw new IllegalStateException();					
				}
				
				zis.closeEntry();
			}
			zis.close();
		} catch (Exception e) {
			result = false;
			Log.v("Angband", "error extracting files: " + e);
		}
		return result;
	}

	public static void waitForInstall() {
		if (installWorker != null) {
			try {
				installWorker.join();
				installWorker = null;
			} catch (Exception e) {
				Log.d("Angband","installWorker "+e.toString());
			}
		}
	}

	/*
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// The directory is now empty so delete it
		//Log.d("Angband", "delete old file: "+dir.getAbsolutePath());
		return dir.delete();
	}
	*/
}