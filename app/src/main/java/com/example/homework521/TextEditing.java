package com.example.homework521;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class TextEditing {
	private static final String LOG_TAG = TextEditing.class.getSimpleName();
	private static final String FILENAME = "user_texts";

	// 'file' being null indicates not yet initialized
	private static File file;
	private static HashMap<String, String> userTexts;

	public static String getEntry(User u) {
		Objects.requireNonNull(u);
		String name = u.getName();
		if (userTexts.containsKey(name))
			return userTexts.get(u.getName());
		else
			return "";
	}

	public static void putEntry(User u, String text) {
		userTexts.put(u.getName(), text);
		sync();
	}

	private static void load() {
		if (!file.isFile()) {
			userTexts = new HashMap<>();
			return;
		}

		byte[] readArray;
		// try block writes file contents to 'readArray'
		try (FileInputStream in = new FileInputStream(file);
			 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[4096];
			int read;
			while ((read = in.read(buffer)) != -1)
				out.write(buffer, 0, read);
			readArray = out.toByteArray();
		} catch (IOException e) {
			// in case of failure set empty map, report and return
			Log.w(LOG_TAG, "Failed to load: " + e.getMessage());
			userTexts = new HashMap<>();
			return;
		}
		read(readArray);
	}

	private static void read(byte[] array) {
		ByteBuffer buffer = ByteBuffer.wrap(array);
		int usersAmt = buffer.getInt();
		userTexts = new HashMap<>(usersAmt);
		for (int i = 0; i < usersAmt; i++) {
			byte[] username = new byte[buffer.getInt()];
			buffer.get(username);
			byte[] text = new byte[buffer.getInt()];
			buffer.get(text);

			String usernameString = new String(username, StandardCharsets.UTF_8);
			String textString = new String(text, StandardCharsets.UTF_8);
			userTexts.put(usernameString, textString);
		}
	}

	private static synchronized void save() {
		try (FileOutputStream out = new FileOutputStream(file)) {
			saveTo(out);
		} catch (IOException e) {
			Log.w(LOG_TAG, "Failed to save: " + e.getMessage());
		}
	}

	// IOException can only occur when writing to 'stream'
	private static void saveTo(OutputStream stream) throws IOException {
		ByteBuffer intBuffer = ByteBuffer.allocate(4);
		ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();

		// write the number of entries
		bufferOut.write(intBuffer.putInt(0, userTexts.size()).array(), 0, 4);

		// write each entry
		for (Map.Entry<String, String> entry : userTexts.entrySet()) {
			byte[] username = entry.getKey().getBytes(StandardCharsets.UTF_8);
			byte[] text = entry.getValue().getBytes(StandardCharsets.UTF_8);

			bufferOut.write(intBuffer.putInt(0, username.length).array(), 0, 4);
			bufferOut.write(username, 0, username.length);

			bufferOut.write(intBuffer.putInt(0, text.length).array(), 0, 4);
			bufferOut.write(text, 0, text.length);

		}

		bufferOut.writeTo(stream);
		bufferOut.close();
	}

	public static void initIfNecessary(Context c) {
		if (file == null) {
			file = new File(c.getFilesDir(), FILENAME);
			load();
		}
	}

	private static void sync() {
		new Thread(TextEditing::save).start();
	}
}
