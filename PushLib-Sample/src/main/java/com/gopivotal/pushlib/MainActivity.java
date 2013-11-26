package com.gopivotal.pushlib;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.gopivotal.pushlib.registration.RegistrationListener;
import com.gopivotal.pushlib.util.Const;
import com.gopivotal.pushlib.util.PushLibLogger;
import com.xtreme.commons.ThreadUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends ActionBarActivity {

    private static final String GCM_SENDER_ID = "816486687340";
    private static final String RELEASE_UUID = "efb9783f-a160-4cec-abf1-b51bca14b991";
    private static final String RELEASE_SECRET = "d0bbddc5-f534-4a95-bb49-d90c8e8aec8c";
    private static final String DEVICE_ALIAS = "android_test_device_alias";

    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("hh:mm:ss.SSS");
    private static List<Pair<String, String>> listItems = new ArrayList<Pair<String, String>>();
    private ListView listView;
    private LogAdapter adapter;
    private PushLib pushLib;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adapter = new LogAdapter(getApplicationContext(), listItems);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setDividerHeight(0);
        PushLibLogger.setListener(getLogListener());
        if (listItems.isEmpty()) {
            addLogMessage("Press the \"Register\" button to attempt registration");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void startRegistration() {
        queueLogMessage("Starting registration...");

        final PushLibParameters parameters = new PushLibParameters(GCM_SENDER_ID, RELEASE_UUID, RELEASE_SECRET, DEVICE_ALIAS);
        pushLib = PushLib.init(this, parameters);
        pushLib.startRegistration(new RegistrationListener() {

            @Override
            public void onRegistrationComplete() {
                queueLogMessage("Registration successful.");
            }

            @Override
            public void onRegistrationFailed(String reason) {
                queueLogMessage("Registration failed. Reason is '" + reason + "'.");
            }
        });
    }

    public PushLibLogger.Listener getLogListener() {
        return new PushLibLogger.Listener() {
            @Override
            public void onLogMessage(String message) {
                addLogMessage(message);
            }
        };
    }

    private void queueLogMessage(final String message) {
        if (ThreadUtil.isUIThread()) {
            addLogMessage(message);
        } else {
            ThreadUtil.getUIThreadHandler().post(new Runnable() {
                @Override
                public void run() {
                    addLogMessage(message);
                }
            });
        }
    }

    private void addLogMessage(String message) {
        final String timestamp = dateFormatter.format(new Date());
        listItems.add(new Pair<String, String>(timestamp, message));
        adapter.notifyDataSetChanged();
        listView.setSelection(listItems.size() - 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_register) {
            startRegistration();
        } else if (item.getItemId() == R.id.action_clear_registration) {
            clearRegistration();
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearRegistration() {
        DialogFragment dialog = new ClearRegistrationDialogFragment(new ClearRegistrationDialogFragment.Listener() {

            @Override
            public void onClickResult(int result) {
                final SharedPreferences.Editor editor = getSharedPreferences(Const.TAG_NAME, Context.MODE_PRIVATE).edit();
                if (result == ClearRegistrationDialogFragment.CLEAR_REGISTRATIONS_FROM_GCM || result == ClearRegistrationDialogFragment.CLEAR_REGISTRATIONS_FROM_BOTH) {
                    addLogMessage("Clearing device registration from GCM");
                    editor.remove("gcm_device_registration_id");
                }
                if (result == ClearRegistrationDialogFragment.CLEAR_REGISTRATIONS_FROM_BACK_END || result == ClearRegistrationDialogFragment.CLEAR_REGISTRATIONS_FROM_BOTH) {
                    addLogMessage("Clearing device registration from the back-end");
                    editor.remove("app_version");
                    editor.remove("release_uuid");
                    editor.remove("release_secret");
                    editor.remove("device_alias");
                }
                editor.commit();
            }
        });
        dialog.show(getSupportFragmentManager(), "ClearRegistrationDialogFragment");
    }
}
