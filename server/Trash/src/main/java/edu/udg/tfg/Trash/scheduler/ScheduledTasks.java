package edu.udg.tfg.Trash.scheduler;

import edu.udg.tfg.Trash.services.TrashService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;


@Component
public class ScheduledTasks {

    @Autowired
    private TrashService trashService;

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(cron = "0 0 0 * * ?")
    public void removeExpiredRecords() {
        trashService.removeExpiredRecords();
    }

    // Second scheduled task to be executed at 12 AM every day
    @Scheduled(cron = "0 0 0 * * ?")
    public void performTaskTwo() {
        trashService.cleanRecords();
    }
}
