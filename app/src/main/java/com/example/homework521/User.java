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
import java.util.ArrayList;
import java.util.List;

public class User {
	private static final String LOG_TAG = User.class.getSimpleName();
	private static final String FILENAME = "users";

	// 'file' being null indicates not yet initialized
	private static File file;
	private static List<User> users;
	// 'currentUser' being null indicates not logged in
	private static User currentUser;

	private String username;
	private String password;

	private User(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getName() {
		return username;
	}

	public static boolean isUsernameValid(String username) {
		int len = username.length();
		if (len < 4 || len > 48)
			return false;
		return !username.contains(" ");
	}

	public static boolean isPasswordValid(String password) {
		int len = password.length();
		if (len < 4 || len > 64) // too short / too long
			return false;
		if (len > 10)
			return true;

		// check for simple sequences like "1234" or "12345678"
		String unsafePassword = "1234567890";
		boolean convergesWithUnsafe = unsafePassword.substring(0,
				password.length()).equals(password);
		return !convergesWithUnsafe;
	}

	public static boolean attemptLogin(String username, String password) {
		for (User u : users) {
			if (u.username.equals(username)) {
				boolean passwordCorrect = password.equals(u.password);
				if (passwordCorrect) {
					currentUser = u; // switch user
					return true;
				}
				return false; // the password is incorrect
			}
		}
		return false; // no users with the given username found
	}

	public static boolean attemptSignUp(String username, String password) {
		if (!isUsernameValid(username))
			throw new IllegalArgumentException("Invalid username");
		if (!isPasswordValid(password))
			throw new IllegalArgumentException("Invalid password");

		for (User u : users) {
			if (u.username.equals(username))
				return false; // the username is occupied
		}

		currentUser = new User(username, password);
		users.add(currentUser);
		sync();
		return true;
	}

	public static void logOut() {
		currentUser = null;
	}

	public static User getCurrentUser() {
		return currentUser;
	}

	private static void load() {
		if (!file.isFile()) {
			users = new ArrayList<>();
			return;
		}

		byte[] readArray;
		// try block writes file contents to 'readArray'
		try (FileInputStream in = new FileInputStream(file);
			 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1)
				out.write(buffer, 0, read);
			readArray = out.toByteArray();
		} catch (IOException e) {
			// in case of failure set empty list, report and return
			Log.w(LOG_TAG, "Failed to load: " + e.getMessage());
			users = new ArrayList<>();
			return;
		}
		read(readArray);
	}

	private static void read(byte[] array) {
		ByteBuffer buffer = ByteBuffer.wrap(array);
		int usersAmt = buffer.getInt();
		users = new ArrayList<>(usersAmt);
		for (int i = 0; i < usersAmt; i++) {
			byte[] name = new byte[buffer.getInt()];
			buffer.get(name);
			byte[] pass = new byte[buffer.getInt()];
			buffer.get(pass);

			String nameString = new String(name, StandardCharsets.UTF_8);
			String passString = new String(pass, StandardCharsets.UTF_8);
			users.add(new User(nameString, passString));
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

		// write the number of users
		bufferOut.write(intBuffer.putInt(0, users.size()).array(), 0, 4);

		// write each user
		for (User u : users) {
			byte[] name = u.username.getBytes(StandardCharsets.UTF_8);
			byte[] pass = u.password.getBytes(StandardCharsets.UTF_8);

			bufferOut.write(intBuffer.putInt(0, name.length).array(), 0, 4);
			bufferOut.write(name, 0, name.length);

			bufferOut.write(intBuffer.putInt(0, pass.length).array(), 0, 4);
			bufferOut.write(pass, 0, pass.length);
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
		new Thread(User::save).start();
	}
}
