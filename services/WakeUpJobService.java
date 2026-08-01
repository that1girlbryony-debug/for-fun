package com.dev.test.myfirstapp.services;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import com.dev.test.myfirstapp.storage.SmsCache;

public class WakeUpJobService extends JobService {
    private static final int JOB_ID = 2001;

    @Override
    public boolean onStartJob(JobParameters params) {
        SmsCache.flushCache(this);
        scheduleNextWakeUp();
        jobFinished(params, false);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        scheduleNextWakeUp();
        return true;
    }

    private void scheduleNextWakeUp() {
        ComponentName componentName = new ComponentName(this, WakeUpJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, componentName)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPersisted(true)
            .setPeriodic(15 * 60 * 1000)
            .setBackoffCriteria(30000, JobInfo.BACKOFF_POLICY_LINEAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            builder.setImportantWhileForeground(true);
        }
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) jobScheduler.schedule(builder.build());
    }
}