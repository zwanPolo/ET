package comp5620.sydney.edu.au.et.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import comp5620.sydney.edu.au.et.R;
import comp5620.sydney.edu.au.et.model.Customer;
import comp5620.sydney.edu.au.et.model.Group;
import comp5620.sydney.edu.au.et.model.Restaurant;

import static android.support.constraint.Constraints.TAG;

public class LoginActivity extends Activity {

    private RadioGroup rg;
    private String type;
    private List<Customer> allCustomers;
    private List<Restaurant> allRestaurants;
    private List<Group> allGroups;
    private List<Group> showGroups;
    private Customer theCustomer;
    private Restaurant theRestaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        rg = (RadioGroup)findViewById(R.id.radioGroup);
        type= "customer";

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int radio = rg.getCheckedRadioButtonId();

                if(radio == R.id.rbLoin_restaurant)
                {
                    type = "restaurant";
                }
                else if(radio == R.id.rbLogin_customer)
                {
                    type = "customer";
                }
            }
        });

        Button login = (Button) findViewById(R.id.btLogin);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText username_et = findViewById(R.id.login_username);
                EditText password_et = findViewById(R.id.login_password);

                String username = username_et.getText().toString();
                String password = password_et.getText().toString();

                if(type.equals("customer")) {

                    for(Customer customer : allCustomers)
                    {
                        if(username.equals(customer.getUsername()))
                        {
                            if(password.equals(customer.getPassword())) {
                                theCustomer = customer;

                                for(Group oneGroup : allGroups)
                                {
                                    int currentNumber = oneGroup.members.size();
                                    boolean exist = false;
                                    // Get the members' information
                                    for (Map<String, String> member : oneGroup.members.values()) {
                                        for (Map.Entry<String, String> field : member.entrySet()) {
                                            if(field.getValue().equals(theCustomer.getUsername()))
                                            {
                                                exist = true;
                                                break;
                                            }
                                        }
                                        if(exist)
                                        {
                                            break;
                                        }
                                    }
                                    if(!exist)
                                    {
                                        if(currentNumber < Integer.parseInt(oneGroup.getNumberOfPeople()) && oneGroup.getType().equals("Public")) {
                                            showGroups.add(oneGroup);
                                        }
                                    }
                                }

                                Intent intent = new Intent(LoginActivity.this,MainCustomerActivity.class);
                                intent.putExtra("currentCustomer", (Serializable) theCustomer);
                                intent.putExtra("allRestaurants", (Serializable) allRestaurants);
                                intent.putExtra("allGroups", (Serializable) allGroups);
                                intent.putExtra("showGroups", (Serializable) showGroups);
                                startActivity(intent);

                                finish();
                                return;
                            }
                            else {
                                // The password is wrong
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setTitle("Wrong password")
                                        .setMessage("The password is wrong.")
                                        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                // Back to the login page
                                            }
                                        });

                                builder.create().show();
                                return;
                            }
                        }
                    }

                    // The username is wrong
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle("Wrong username")
                            .setMessage("The username is wrong.")
                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Back to the login page
                                }
                            });

                    builder.create().show();
                    return;

                }
                else if (type.equals("restaurant")) {

                    for(Restaurant restaurant : allRestaurants)
                    {
                        if(username.equals(restaurant.getUsername()))
                        {
                            if(password.equals(restaurant.getPassword())) {
                                theRestaurant = restaurant;
                                Intent intent = new Intent(LoginActivity.this,MainRestaurantActivity.class);
                                intent.putExtra("currentRestaurant", (Serializable) theRestaurant);
                                startActivity(intent);

                                finish();
                                return;
                            }
                            else {
                                // The password is wrong
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setTitle("Wrong password")
                                        .setMessage("The password is wrong.")
                                        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                // Back to the login page
                                            }
                                        });

                                builder.create().show();
                                return;
                            }
                        }
                    }
                    // The username is wrong
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle("Wrong username")
                            .setMessage("The username is wrong.")
                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Back to the login page
                                }
                            });

                    builder.create().show();
                    return;
                }
            }
        });

        Button register = (Button) findViewById(R.id.btRegister);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                intent.putExtra("allCustomers", (Serializable) allCustomers);
                intent.putExtra("allRestaurants", (Serializable) allRestaurants);
                startActivity(intent);

                finish();
            }
        });


        // Read customers from database
        allCustomers = new ArrayList<>();
        readCustomersFromServer();
        // Read restaurants from database
        allRestaurants = new ArrayList<>();
        readRestaurantsFromServer();
        // Read groups from database
        allGroups = new ArrayList<>();
        showGroups = new ArrayList<>();
        readGroupsFromServer();

    }

    // Get all customer accounts from server
    private void readCustomersFromServer()
    {
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference date = mReference.child("customers");

        // Read from the database
        date.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                allCustomers.clear();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    // TODO: handle the post

                    Customer oneCustomer = postSnapshot.getValue(Customer.class);

                    allCustomers.add(oneCustomer);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    // Get all restaurant accounts from server
    private void readRestaurantsFromServer()
    {
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference date = mReference.child("restaurants");

        // Read from the database
        date.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                allRestaurants.clear();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    // TODO: handle the post

                    Restaurant oneRestaurant = postSnapshot.getValue(Restaurant.class);

                    allRestaurants.add(oneRestaurant);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    // Get all groups from server
    private void readGroupsFromServer()
    {
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference date = mReference.child("groups");

        // Read from the database
        date.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                allGroups.clear();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    // TODO: handle the post

                    Group oneGroup = postSnapshot.getValue(Group.class);

                    allGroups.add(oneGroup);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }
}
