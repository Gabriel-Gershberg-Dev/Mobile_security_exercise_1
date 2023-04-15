package com.example.mobilesecurityexercise1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText txtUserName;
    private TextInputEditText txtPassword;
    private MaterialButton loginBtu;
    private String userName;
    private String password;
    private String secretContactName;
    private String secretContactNumber;
    private float brightnessVal;
    private String secretSpeechWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        setSecretDetails("afeka","afeka23","Bar","12345",50, "University");
        loginBtu.setOnClickListener(view -> {
            try {
                checkContactPermission();
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    private void findViews() {
        txtUserName=findViewById(R.id.main_txt_userName);
        txtPassword=findViewById(R.id.main_txt_password);
        loginBtu=findViewById(R.id.main_btu_login);
    }

    private void setSecretDetails(String userName, String password, String secretContactName, String secretContactNumber, float brightnessVal, String secretSpeechWord){
        this.userName=userName;
        this.password=password;
        this.secretContactName=secretContactName;
        this.secretContactNumber=secretContactNumber;
        this.brightnessVal=brightnessVal;
        this.secretSpeechWord=secretSpeechWord;
    }

    private boolean checkLoginDetails(){
        return userName.equals(txtUserName.getText().toString()) &&
                password.equals(txtPassword.getText().toString()) ;
    }

    private boolean checkIfDeveloper(){
       int developerStatus= Settings.Secure.getInt(getApplicationContext().getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED , 0);
       return developerStatus==1;
    }
    private boolean checkContact(){
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " = ? AND " +
                ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?";
        String[] selectionArgs = new String[]{secretContactName,secretContactNumber};

        // Query the contacts database
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                null
        );

        // Check if the cursor has any rows
        boolean exists = false;
        if (cursor != null) {
            exists = cursor.moveToFirst();
            cursor.close();
        }

        return exists;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkSpeech();
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void checkContactPermission() throws Settings.SettingNotFoundException {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    100);

        } else {
            checkSpeech();
        }

    }


    private boolean passCheck() throws Settings.SettingNotFoundException {

       return checkIfDeveloper() && checkLoginDetails() && checkContact() && checkBrightness() ;
    }
    private boolean checkBrightness() throws Settings.SettingNotFoundException {
        int brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        float brightnessPercentage = brightness / 255f * 100;
        return brightnessPercentage < brightnessVal;
    }
    private void toast(boolean status){
        if(status)
            Toast.makeText(this,"Welcome hacker", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this,"Try again ;)", Toast.LENGTH_LONG).show();
    }

    private Boolean checkSpeech(){
        PackageManager packageManager = getPackageManager();
        if (packageManager.resolveActivity(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0) != null) {
            // Create an intent for the speech recognition activity
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            // Start the activity and wait for the result
            startActivityForResult(intent, 1);
        } else {

            Toast.makeText(this, "Speech recognition not supported on this device.", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            // Get the speech recognition results
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            // Use the first result as the user's input
            String userInput = results.get(0);

            // Do something with the user's input
            try {
                toast(passCheck() && compareSpeechWord(userInput));
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean compareSpeechWord(String word){
        return secretSpeechWord.equals(word);
    }

}
