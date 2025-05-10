package ro.ase.acs.mind_path.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class to enable scheduling in the application.
 * This allows using @Scheduled annotations on methods.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // No additional beans needed, the @EnableScheduling annotation does the job
}