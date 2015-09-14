package com.vinaysshenoy.okulusdemo;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.vinaysshenoy.okulusdemo.fragments.ComparisonFragment;
import com.vinaysshenoy.okulusdemo.fragments.NetworkFragment;
import com.vinaysshenoy.okulusdemo.fragments.RoundedRectanglesFragment;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState == null) {
            loadComparisonFragment();
        }
    }

    private void loadComparisonFragment() {

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_content, Fragment.instantiate(this, ComparisonFragment.class.getName()),
                        "fragment_comparison")
                .commit();
    }

    private void loadRoundRectFragment() {

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_content,
                        Fragment.instantiate(this, RoundedRectanglesFragment.class.getName()),
                        "fragment_rounded_rectangles")
                .commit();
    }

    private void loadNetworkFragment() {

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_content,
                        Fragment.instantiate(this, NetworkFragment.class.getName()),
                        "fragment_network")
                .commit();
    }


}
