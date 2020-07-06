package ceng.gui.sportmate;

import ceng.gui.sportmate.notification.MyResponse;
import ceng.gui.sportmate.notification.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAActtJjCQ:APA91bGFM0Qk4mYlsnNMFypoMOFBKkJmotm9rGhVY6-q3J8KqvfkNqn2gFG_fe6-oV_cWGopzkQLWBu6qe2o4c_YJW96x-cHMr1KZF-yTJEtYkAahjaDdMxXFYfIbNexD-rWzggQF47G"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}