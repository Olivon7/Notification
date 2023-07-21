package com.example.notification;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

public class NotificationActivity extends AppCompatActivity {
    private EditText userInputEditText;
    private Button NotificationButton;

    private static final String NOTIFICATION_CHANNEL_ID = "UV_INDEX_NOTIFICATION_CHANNEL";
    private static final int NOTIFICATION_ID = 1;

    private BroadcastReceiver uvNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int uvIndex = intent.getIntExtra("uvIndex", 1);
            showUVNotification(uvIndex);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        userInputEditText = findViewById(R.id.user_input_edit_text);
        NotificationButton = findViewById(R.id.SendNotification);

        NotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = userInputEditText.getText().toString();
                if (isValidInput(userInput)) {
                    int uvIndex = Integer.parseInt(userInput);
                    showUVNotification(uvIndex);
                } else {
                    // Handle invalid input, e.g., show a toast
                }
            }
        });

        createNotificationChannel();

        // Register the broadcast receiver
        IntentFilter filter = new IntentFilter("UV_NOTIFICATION_ACTION");
        registerReceiver(uvNotificationReceiver, filter);

        // Schedule repeated notifications every 2 hours
        scheduleRepeatingNotifications();
    }

    @Override
    protected void onDestroy() {
        // Unregister the broadcast receiver to avoid memory leaks
        unregisterReceiver(uvNotificationReceiver);
        super.onDestroy();
    }

    private boolean isValidInput(String input) {
        try {
            int number = Integer.parseInt(input);
            return number >= 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "UV Index Notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showUVNotification(int uvIndex) {
        String notificationMessage;
        switch (uvIndex) {
            case 1:
            case 2:
                notificationMessage = "Low risk of UV exposure, don't forget to wear sunscreen.";
                break;
            case 3:
            case 4:
            case 5:
                notificationMessage = "Moderate risk of UV exposure. Please wear sunscreen.";
                break;
            case 6:
            case 7:
                notificationMessage = "High risk of skin damage. Wear sunscreen and seek shade.";
                break;
            case 8:
            case 9:
            case 10:
                notificationMessage = "Very High Risk of sunburn and other medical skin issue. Wear sunscreen, seek shade, and/or stay indoors.";
                break;
            default:
                notificationMessage = "Extreme Risk of medical skin and other issues- Stay indoors. If not possible then wear protective clothing, sunscreen and sunglasses, and seek shade.";
                break;
        }

        Notification notification = createNotification(notificationMessage);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private Notification createNotification(String contentText) {
        Intent notificationIntent = new Intent(this, NotificationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                NOTIFICATION_ID,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        return new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("UV Index Alert")
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.ic_launcher) // Set an appropriate app icon here
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
    }

    private void scheduleRepeatingNotifications() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent(this, UVNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                NOTIFICATION_ID,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Schedule repeating notifications every 2 hours
        long repeatInterval = 2 * 60 * 60 * 1000; // 2 hours in milliseconds
        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + repeatInterval,
                    repeatInterval,
                    pendingIntent
            );
        }
    }
}
