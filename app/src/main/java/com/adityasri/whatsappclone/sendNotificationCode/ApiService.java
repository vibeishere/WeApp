package com.adityasri.whatsappclone.sendNotificationCode;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAgyYlXM8:APA91bHaAvXT-Ne-1I6O-qcrSw4nYtPpU-90gWltJYiYKD5Ih1u377MksQOGQIX4HmcogQk1mvDNyY1X11qbm41DdlgZZjng-SxMzPtSEknYz1GTq4dsOMODZhHXYD2LHha_OyQACrLt"
            }
    )

    @POST("fcm/send")
    Call<Response> sendNotification(@Body NotificationSender body);
}
