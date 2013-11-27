package com.gopivotal.pushlib;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.gopivotal.pushlib.registration.RegistrationListener;
import com.gopivotal.pushlib.util.Const;
import com.gopivotal.pushlib.util.PushLibLogger;
import com.xtreme.commons.StringUtil;
import com.xtreme.commons.ThreadUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private static final String GCM_SENDER_ID = "816486687340";
    private static final String RELEASE_UUID = "efb9783f-a160-4cec-abf1-b51bca14b991";
    private static final String RELEASE_SECRET = "d0bbddc5-f534-4a95-bb49-d90c8e8aec8c";
    private static final String DEVICE_ALIAS = "android_test_device_alias";

    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("hh:mm:ss.SSS");
    private static final int[] baseRowColours = new int[] {0xddeeff, 0xddffee, 0xffeedd};

    private static int currentBaseRowColour = 0;
    private static List<LogItem> logItems = new ArrayList<LogItem>();

    private ListView listView;
    private LogAdapter adapter;
    private PushLib pushLib;
    private String logAsString;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adapter = new LogAdapter(getApplicationContext(), logItems);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setDividerHeight(0);
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(getLogItemLongClickListener());
        PushLibLogger.setListener(getLogListener());
        if (logItems.isEmpty()) {
            addLogMessage("Press the \"Register\" button to attempt registration");
        }
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scrollToBottom();
    }

    private void startRegistration() {
        updateCurrentBaseRowColour();
        addLogMessage("Starting registration...");

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String gcmSenderId = prefs.getString(SettingsActivity.PREFERENCE_GCM_SENDER_ID, null);
        final String releaseUuid = prefs.getString(SettingsActivity.PREFERENCE_RELEASE_UUID, null);
        final String releaseSecret = prefs.getString(SettingsActivity.PREFERENCE_RELEASE_SECRET, null);
        final String deviceAlias = prefs.getString(SettingsActivity.PREFERENCE_DEVICE_ALIAS, null);
        addLogMessage("GCM Sender ID: '" + gcmSenderId + "'\nRelease UUID: '" + releaseUuid + "'\nRelease Secret: '" + releaseSecret + "'\nDevice Alias: '" + deviceAlias + "'");

        final RegistrationParameters parameters = new RegistrationParameters(gcmSenderId, releaseUuid, releaseSecret, deviceAlias);
        pushLib = PushLib.init(this);
        pushLib.startRegistration(parameters, new RegistrationListener() {

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
        final LogItem logItem = new LogItem(timestamp, message, baseRowColours[currentBaseRowColour]);
        logItems.add(logItem);
        adapter.notifyDataSetChanged();
        scrollToBottom();
    }

    private void scrollToBottom() {
        listView.setSelection(logItems.size() - 1);
    }

    private void updateCurrentBaseRowColour() {
        currentBaseRowColour = (currentBaseRowColour + 1) % baseRowColours.length;
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
        } else if (item.getItemId() == R.id.action_edit_registration_parameters) {
            editRegistrationParameters();
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearRegistration() {
        final DialogFragment dialog = new ClearRegistrationDialogFragment(new ClearRegistrationDialogFragment.Listener() {

            @Override
            public void onClickResult(int result) {
                if (result != ClearRegistrationDialogFragment.CLEAR_REGISTRATIONS_CANCELLED) {
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
            }
        });
        dialog.show(getSupportFragmentManager(), "ClearRegistrationDialogFragment");
    }

    public AdapterView.OnItemLongClickListener getLogItemLongClickListener() {
        return new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                final int originalViewBackgroundColour = adapter.getBackgroundColour(position);
                final LogItem logItem = (LogItem) adapter.getItem(position);
                final DialogFragment dialog = new LogItemLongClickDialogFragment(new LogItemLongClickDialogFragment.Listener() {

                    @Override
                    public void onClickResult(int result) {
                        if (result == LogItemLongClickDialogFragment.COPY_ITEM) {
                            final ClipData clipData = ClipData.newPlainText("log item text", logItem.message);
                            final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            clipboardManager.setPrimaryClip(clipData);
                            Toast.makeText(MainActivity.this, "Log item copied to clipboard", Toast.LENGTH_SHORT).show();
                        } else if (result == LogItemLongClickDialogFragment.COPY_ALL_ITEMS) {
                            final ClipData clipData = ClipData.newPlainText("log text", getLogAsString());
                            final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            clipboardManager.setPrimaryClip(clipData);
                            Toast.makeText(MainActivity.this, "Log copied to clipboard", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.show(getSupportFragmentManager(), "LogItemLongClickDialogFragment");
                return true;
            }
        };
    }

    private void editRegistrationParameters() {
        final Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public String getLogAsString() {
        final List<String> lines = new LinkedList<String>();
        for (final LogItem logItem : logItems) {
            lines.add(logItem.timestamp + "\t" + logItem.message);
        }
        return StringUtil.join(lines, "\n");
    }
}
