package com.example.safetyapp;

import android.content.SharedPreferences;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.safetyapp.Activities.MainActivity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.content.Context.MODE_PRIVATE;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Before
    public void setUp() {
        // Set up any prerequisites for your test
        String username = "TestUser";
        SharedPreferences preferences = InstrumentationRegistry.getInstrumentation().getContext().getSharedPreferences("user", MODE_PRIVATE);
        preferences.edit().putString("username", username).apply();
    }

    @Test
    public void userLoggedIn_displaysUserInfo() {
        // Assuming you have a valid username in SharedPreferences
        String username = "TestUser";
        SharedPreferences preferences = InstrumentationRegistry.getInstrumentation().getContext().getSharedPreferences("user", MODE_PRIVATE);
        preferences.edit().putString("username", username).apply();

        // Launch the MainActivity
        ActivityScenario.launch(MainActivity.class);

        // Check if the user info is displayed correctly
        Espresso.onView(withId(R.id.usernameDisplay)).check(ViewAssertions.matches(ViewMatchers.withText(username)));
    }

    @Test
    public void logoutButton_LogsOutUser() {
        // Launch the MainActivity
        ActivityScenario.launch(MainActivity.class);

        // Click on the logout button
        Espresso.onView(withId(R.id.btn_logout)).perform(ViewActions.click());

        // Check if FirebaseAuth.getInstance().signOut() is called
        // (You might need to mock FirebaseAuth for this)
    }

    // Add more tests for other UI interactions as needed

    @After
    public void tearDown() {
        // Clean up any resources used in the tests
    }
}
