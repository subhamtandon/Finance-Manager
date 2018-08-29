package com.example.initish.finance_manager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {


    private static final String TAG = "";
    SignInButton button;
    Button signup,signin;
    private static final int RC_SIGN_IN = 2;
    GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFirebaseAuth;
    private EditText username;
    private EditText passwd;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAuth = FirebaseAuth.getInstance();

        button = (SignInButton) findViewById(R.id.button);
        signup = (Button) findViewById(R.id.signup);
        signin=(Button) findViewById(R.id.signin);
        button.setOnClickListener(this);
        signup.setOnClickListener(this);
        signin.setOnClickListener(this);


        if (mFirebaseAuth.getCurrentUser() != null) {
            //profile activity here
            finish();
            startActivity(new Intent(getApplicationContext(), Main2Activity.class));
        }

        username = (EditText) findViewById(R.id.username);

        passwd = (EditText) findViewById(R.id.passwd);

        signin = (Button) findViewById(R.id.signin);

        progressDialog = new ProgressDialog(this);


    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

        progressDialog.setMessage("Signing in...");
        progressDialog.show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }
    private boolean isEmailValid(String email) {

        return email.contains("@");
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        Log.d("info", "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("info", "signInWithCredential:success");
                            finish();
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            startActivity(new Intent(getApplicationContext(), Main2Activity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("info", "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });

    }
    public void userLogin(){

        String email = username.getText().toString().trim();

        String password = passwd.getText().toString().trim();
        if(email.isEmpty()){
            username.setError("E mail is required");
            username.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            username.setError("E mail is required");
            username.requestFocus();
            return;
        }
        if(password.isEmpty()){
            passwd.setError("Password is required");
            passwd.requestFocus();
            return;
        }
        if(password.length()<6){
            passwd.setError("Minimum length of Password is 6");
            passwd.requestFocus();
            return;
        }
        progressDialog.setMessage("Logging in...");
        progressDialog.show();

     mFirebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
         @Override
         public void onComplete(@NonNull Task<AuthResult> task) {
             if(task.isSuccessful()){
                progressDialog.dismiss();
                 Intent intent=new Intent(MainActivity.this,Main2Activity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
             }
             else{
                 progressDialog.dismiss();
                 Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
             }

         }
     });


    }

    private void updateUI(FirebaseUser user) {
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signup:
                startActivity(new Intent(this,SignupActivity.class));
                break;
            case R.id.button:
                signIn();
                break;
            case R.id.signin:
                userLogin();
        }
    }

}
