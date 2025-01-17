package com.blikoon.rooster;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via jid/password.
 */
public class LoginActivity extends AppCompatActivity
{

    private static final String TAG="LoginActivity";

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;


    // UI references.
    private AutoCompleteTextView mJidView;
    private EditText mPasswordView;
    private EditText mServerIPView;
    private EditText mServerDomainView;
    private View mProgressView;
    private View mLoginFormView;
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = this;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean loginAlready = prefs.getBoolean("xmpp_logged_in",false);
        if (loginAlready){
            showContactListAty();
        }

        String name = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString("xmpp_jid",null);
        String password = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString("xmpp_password",null);
        String domain = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString("xmpp_domain", null);
        String ip = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString("xmpp_ip", null);

        //Show
        // Set up the login form.
        mJidView = (AutoCompleteTextView) findViewById(R.id.email);
        mJidView.setText(name!=null?name:UserInfo.DEFAULT_USER.name);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setText(password!=null?password:UserInfo.DEFAULT_USER.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mServerDomainView = (EditText) findViewById(R.id.domain);
        mServerDomainView.setText(domain!=null?domain:ServerInfo.DEFALUT_SERVER.hostDomain);
        mServerIPView = (EditText) findViewById(R.id.server_ip);
        mServerIPView.setText(ip!=null?ip:ServerInfo.DEFALUT_SERVER.ip);

        Button mJidSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mJidSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                switch (action)
                {
                    case RoosterConnectionService.UI_AUTHENTICATED:
                        Log.d(TAG,"Got a broadcast to show the main app window");
                        showProgress(false);
                        showContactListAty();
                        break;
                }

            }
        };
        IntentFilter filter = new IntentFilter(RoosterConnectionService.UI_AUTHENTICATED);
        this.registerReceiver(mBroadcastReceiver, filter);
    }

    private void showContactListAty(){
        //Show the main app window
        Intent i2 = new Intent(mContext,ContactListActivity.class);
        startActivity(i2);
        finish();
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        //getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mJidView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mJidView.setError(null);
        mPasswordView.setError(null);
        mServerDomainView.setError(null);
        mServerIPView.setError(null);

        // Store values at the time of the login attempt.
        String email = mJidView.getText().toString();
        String password = mPasswordView.getText().toString();
        String domain = mServerDomainView.getText().toString();
        String ip = mServerIPView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mJidView.setError(getString(R.string.error_field_required));
            focusView = mJidView;
            cancel = true;
        }

        // Check for valid domain
        if (TextUtils.isEmpty(domain)){
            focusView = mServerDomainView;
            cancel = true;
        }

        // Check for valid ipv4
        if (!isIP(ip)){
            focusView = mServerIPView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //showProgress(true);
            //This is where the login login is fired up.
//            Log.d(TAG,"Jid and password are valid ,proceeding with login.");
//            startActivity(new Intent(this,ContactListActivity.class));

            //Save the credentials and login
            saveCredentialsAndLogin();

        }
    }

    private boolean isIP(String ip){
        if (TextUtils.isEmpty(ip)){
            return false;
        }
        String pattern =
                "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
        return ip.matches(pattern);
    }

    private void saveCredentialsAndLogin()
    {
        Log.d(TAG,"saveCredentialsAndLogin() called.");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
                .putString("xmpp_jid", mJidView.getText().toString())
                .putString("xmpp_password", mPasswordView.getText().toString())
                .putString("xmpp_domain", mServerDomainView.getText().toString())
                .putString("xmpp_ip", mServerIPView.getText().toString())
                .commit();

        //Start the service
        Intent i1 = new Intent(this,RoosterConnectionService.class);
        startService(i1);

    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }





    //Check if service is running.
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


}

