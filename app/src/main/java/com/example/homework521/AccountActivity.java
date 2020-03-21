package com.example.homework521;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class AccountActivity extends AppCompatActivity {
	private TextView accountUsernameLabel;
	private EditText accountTextEditor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account);
		initViews();

		accountUsernameLabel.setText(getString(R.string.account_username_info,
				User.getCurrentUser().getName()));
		accountTextEditor.setText(TextEditing.getEntry(User.getCurrentUser()));
	}

	private void initViews() {
		accountUsernameLabel = findViewById(R.id.accountUsernameLabel);
		accountTextEditor = findViewById(R.id.accountTextEditor);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.account, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.logOutMenuItem) {
			logOut();
			return true;
		}
		return false;
	}

	// must be run before User.logOut to prevent NullPointerException
	private void save() {
		String text = accountTextEditor.getText().toString();
		TextEditing.putEntry(User.getCurrentUser(), text);
	}

	private void logOut() {
		save();
		User.logOut();
		startActivity(new Intent(this, LoginActivity.class));
		finish();
	}
}
