package com.example.viniciussouza.ponto;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.facebook.FacebookSdk;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    public  CallbackManager mCallbackManager;
    Button btnLogin;
    Button btnCriarConta;
    Button btnFacebook;
    EditText txtLogin;
    EditText txtSenha;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = (Button) findViewById(R.id.btn_login);
        btnCriarConta = (Button) findViewById(R.id.link_signup);
        btnFacebook = (Button) findViewById(R.id.login_button_facebook);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    finish();
                    startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                }
            }
        };

       btnLogin.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

           final String TAG = "MyActivity";
           txtLogin = (EditText) findViewById(R.id.input_email);
           txtSenha = (EditText) findViewById(R.id.input_password);

           String email =  txtLogin.getText().toString();
           String password = (String) txtSenha.getText().toString();

           mAuth.signInWithEmailAndPassword(email, password)
                   .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                       @Override
                       public void onComplete(@NonNull Task<AuthResult> task) {
                       Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                       if (!task.isSuccessful()) {
                           Log.w(TAG, "signInWithEmail:failed", task.getException());
                           AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
                           alert.setTitle("Erro");
                           alert.setMessage("Não foi possível criar a conta." + task.getException());

                           alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int whichButton) {
                                   startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
                               }
                           });
                           alert.show();
                       }else {
                           checkIfEmailVerified();
                           //startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                       }
                       }
                   });
           }
       });

        btnFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initialize Facebook Login button
                mCallbackManager = CallbackManager.Factory.create();
                LoginButton loginButton = (LoginButton) findViewById(R.id.login_button_facebook);
                loginButton.setReadPermissions("email", "public_profile");

                loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
                    }

                    @Override
                    public void onError(FacebookException error) {

                        AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
                        alert.setTitle("Erro");
                        alert.setMessage("Não foi possível criar a conta." + error.getMessage());

                        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
                            }
                        });
                        alert.show();
                    }
                });
            }
        });

        btnCriarConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        //Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            //Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void checkIfEmailVerified()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user.isEmailVerified())
        {
            // user is verified, so you can finish this activity or send user to activity which you want.
            finish();
            Toast.makeText(LoginActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
        }
        else
        {
            // email is not verified, so just prompt the message to the user and restart this activity.
            // NOTE: don't forget to log out the user.
            FirebaseAuth.getInstance().signOut();

            //restart this activity
            AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
            alert.setTitle("Verificar conta");
            alert.setMessage("Você ainda não confirmou sua conta. Acesse seu e-mail cadastrado e verifique sua conta.");

            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    startActivity(new Intent(LoginActivity.this, LoginActivity.class));
                }
            });
            alert.show();
        }
    }
}
