package com.example.homework521;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
	private EditText usernameInput;
	private EditText passwordInput;
	private Button logInButton;
	private Button signUpButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		initViews();
		logInButton.setOnClickListener(v -> handleLoginClick());
		signUpButton.setOnClickListener(v -> handleSignUpClick());

		User.initIfNecessary(this);
		TextEditing.initIfNecessary(this);
	}

	private void initViews() {
		usernameInput = findViewById(R.id.usernameInput);
		passwordInput = findViewById(R.id.passwordInput);
		logInButton = findViewById(R.id.logInButton);
		signUpButton = findViewById(R.id.signUpButton);
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
