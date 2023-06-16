package com.example.maincontroller.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SuccessBannerPrinter implements ApplicationListener<ApplicationReadyEvent> {

    private static final String[] BANNER = {
            "         __                 __             .___                                                 \n"
                    + "  _______/  |______ ________/  |_  ____   __| _/   ________ __   ____  ____  ____   ______ ______\n"
                    + " /  ___/\\   __\\__  \\\\_  __ \\   __\\/ __ \\ / __ |   /  ___/  |  \\_/ ___\\/ ___\\/ __ \\ /  ___//  ___/\n"
                    + " \\___ \\  |  |  / __ \\|  | \\/|  | \\  ___// /_/ |   \\___ \\|  |  /\\  \\__\\  \\__\\  ___/ \\___ \\ \\___ \\ \n"
                    + "/____  > |__| (____  /__|   |__|  \\___  >____ |  /____  >____/  \\___  >___  >___  >____  >____  >\n"
                    + "     \\/            \\/                 \\/     \\/       \\/            \\/    \\/    \\/     \\/     \\/ "
    };

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("application started success \n");

        for (String s : BANNER) {
            log.info(s);
        }
    }
}
