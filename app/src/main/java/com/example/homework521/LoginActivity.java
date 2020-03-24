package com.example.homework521;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.security.Permissions;

public class LoginActivity extends AppCompatActivity {
	private static final String SP_USE_EXTERNAL_STORAGE_KEY = "use_external_storage";
	private static final int REQUEST_WRITE_EXTERNAL = 42;

	private EditText usernameInput;
	private EditText passwordInput;
	private Button logInButton;
	private Button signUpButton;
	private CheckBox useExternalStorageCheckbox;

	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		preferences = getPreferences(MODE_PRIVATE);
		boolean useExternalStorage = preferences.getBoolean(SP_USE_EXTERNAL_STORAGE_KEY, false);

		initViews();
		logInButton.setOnClickListener(v -> handleLoginClick());
		signUpButton.setOnClickListener(v -> handleSignUpClick());
		useExternalStorageCheckbox.setOnCheckedChangeListener((v, checked) -> {
			if (checked) {
				boolean access = PackageManager.PERMISSION_GRANTED ==
						ContextCompat.checkSelfPermission(LoginActivity.this,
								Manifest.permission.WRITE_EXTERNAL_STORAGE);
				if (!access) {
					useExternalStorageCheckbox.setChecked(false);
					ActivityCompat.requestPermissions(LoginActivity.this,
							new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
							REQUEST_WRITE_EXTERNAL);
				}
			}
			User.initIfNecessary(LoginActivity.this, checked);
		});
		useExternalStorageCheckbox.setChecked(useExternalStorage);

		User.initIfNecessary(this, useExternalStorage);
		TextEditing.initIfNecessary(this);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == REQUEST_WRITE_EXTERNAL) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				useExternalStorageCheckbox.setChecked(true);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		preferences.edit().putBoolean(SP_USE_EXTERNAL_STORAGE_KEY,
				useExternalStorageCheckbox.isChecked()).apply();
	}

	private void initViews() {
		usernameInput = findViewById(R.id.usernameInput);
		passwordInput = findViewById(R.id.passwordInput);
		logInButton = findViewById(R.id.logInButton);
		signUpButton = findViewById(R.id.signUpButton);
		useExternalStorageCheckbox = findViewById(R.id.useExternalStorageCheckbox);
	}

	private void handleLoginClick() {
		String username = usernameInput.getText().toString();
		String password = passwordInput.getText().toString();

		int warningString = -1;
		if (username.isEmpty())
			warningString = R.string.empty_username;
		else if (password.isEmpty())
			warningString = R.string.empty_password;
		else if (!User.isUsernameValid(username))
			warningString = R.string.invalid_username;
		else if (!User.isPasswordValid(password))
			warningString = R.string.invalid_password;

		if (warningString == -1) { // all clear
			boolean successfulLogin = User.attemptLogin(username, password);
			if (successfulLogin) {
				logIn();
			} else {
				Toast.makeText(this, R.string.login_fail, Toast.LENGTH_SHORT).show();
			}
		} else { // display a warning
			Toast.makeText(this, warningString, Toast.LENGTH_SHORT).show();
		}
	}

	private void handleSignUpClick() {
		String username = usernameInput.getText().toString();
		String password = passwordInput.getText().toString();

		int warningString = -1;
		if (username.isEmpty())
			warningString = R.string.empty_username;
		else if (password.isEmpty())
			warningString = R.string.empty_password;
		else if (!User.isUsernameValid(username))
			warningString = R.string.invalid_username;
		else if (!User.isPasswordValid(password))
			warningString = R.string.invalid_password;

		if (warningString != -1) { // display a warning
			Toast.makeText(this, warningString, Toast.LENGTH_SHORT).show();
			return;
		}

		boolean successfulSignUp = User.attemptSignUp(username, password);
		if (successfulSignUp) {
			logIn();
		} else {
			Toast.makeText(this, R.string.username_taken, Toast.LENGTH_SHORT).show();
		}
	}

	private void logIn() {
		startActivity(new Intent(this, AccountActivity.class));
		finish();
	}
}
