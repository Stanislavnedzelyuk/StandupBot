package ru.hh;

import okhttp3.*;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import java.io.IOException;
import java.util.Random;

public class StandupBot {
    private static final String[] STANDUP_LEADS = {"Женя", "Стас", "Сева", "Леша", "Армас", "Аня", "Босс"};
    private static final String MATTERMOST_URL = "https://mattermost.pyn.ru/api/v4/posts";
    private static final String CHANNEL_ID = "где взять id канала, его надо сюда вписать";
    private static final String BEARER_TOKEN = "сюда нужен токен";

    private static final OkHttpClient client = new OkHttpClient();

    public static void main(String[] args) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(StandupJob.class)
                .withIdentity("standupJob", "group1").build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("standupTrigger", "group1")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 9 ? * MON-FRI"))
                .build();

        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.start();
        scheduler.scheduleJob(job, trigger);
    }

    public static class StandupJob implements Job {
        public void execute(JobExecutionContext context) throws JobExecutionException {
            Random rand = new Random();
            String lead = STANDUP_LEADS[rand.nextInt(STANDUP_LEADS.length)];

            String json = "{\n" +
                    "\"channel_id\": \"" + CHANNEL_ID + "\",\n" +
                    "\"message\": \"Сегодня стендап будет вести " + lead + ".\"\n" +
                    "}";

            Request request = new Request.Builder()
                    .url(MATTERMOST_URL)
                    .post(RequestBody.create(
                            MediaType.parse("application/json; charset=utf-8"),
                            json))
                    .addHeader("Authorization", "Bearer " + BEARER_TOKEN)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                System.out.println(response.body().string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

