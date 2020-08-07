package ak.gunsariyas.algofocus;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class UserDetails extends AppCompatActivity {
    private static final int REQUEST_LOCATION = 1;
    ImageView imageView;
    TextView txtUsername, txtEmail, txtlocation;
    Button logoutButton;
    String longitude;
    String latitude;
    //initialize Location provider for getting recent location
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        //for location use
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        logoutButton = findViewById(R.id.login_button);
        imageView = findViewById(R.id.imageView);
        txtUsername = findViewById(R.id.txtUsername);
        txtlocation = findViewById(R.id.showLocation);
        txtEmail = findViewById(R.id.txtEmail);

        boolean loggedOut = AccessToken.getCurrentAccessToken() == null;

        // Check for existing Google Sign In account, if the user is already signed in
// the GoogleSignInAccount will be non-null.
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);


        //check is user logged in using facebook
        if (!loggedOut) {
            Picasso.with(this).load(Profile.getCurrentProfile().getProfilePictureUri(200, 200)).placeholder(R.mipmap.ic_launcher).into(imageView);
            txtUsername.setText(Profile.getCurrentProfile().getName());
            txtEmail.setText(Profile.getCurrentProfile().getId());

            //Using Graph API
            getUserProfile(AccessToken.getCurrentAccessToken());
        }
        //check is user logged in using google
        else if (account != null) {
            Picasso.with(this).load(account.getPhotoUrl()).placeholder(R.mipmap.ic_launcher).into(imageView);
            txtUsername.setText(account.getDisplayName());
            txtEmail.setText(account.getEmail());
            updateUI(account);
        }

        //logout button
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(UserDetails.this)
                        .setTitle("Logout")
                        .setMessage("Sure you want to logout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth.getInstance().signOut();
                                LoginManager.getInstance().logOut();

                                updateUI(account);
                                startActivity(new Intent(UserDetails.this, MainActivity.class));
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                builder.show();

            }
        });
    }

    //getting existing user details
    private void getUserProfile(AccessToken currentAccessToken) {
        GraphRequest request = GraphRequest.newMeRequest(
                currentAccessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d("TAG", object.toString());
                        try {
                            String first_name = object.getString("first_name");
                            String last_name = object.getString("last_name");
                            String email = object.getString("email");
                            String id = object.getString("id");
                            String image_url = "https://graph.facebook.com/" + id + "/picture?type=normal";

                            txtUsername.setText("First Name: " + first_name + "\nLast Name: " + last_name);
                            txtEmail.setText(email);
                            Picasso.with(UserDetails.this).load(image_url).into(imageView);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name,last_name,email,id");
        request.setParameters(parameters);
        request.executeAsync();

    }

    private void updateUI(GoogleSignInAccount account) {
        if (account == null) {
            startActivity(new Intent(UserDetails.this, MainActivity.class));
            super.onBackPressed();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {

                                longitude = String.valueOf(location.getLongitude());
                                latitude = String.valueOf(location.getLatitude());
                                txtlocation.setText("Longitude: " + longitude + "\n" + "Latitude: " + latitude);
                            }
                        }
                    });
        }
    }

}