package com.niveshtrack.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;
import java.util.TimeZone;

/**
 * Main entry point for NiveshTrack Portfolio Management System.
 * A complete REST API backend for Indian retail stock investors (NSE/BSE).
 *
 * <p>Features:
 * <ul>
 *   <li>JWT-based authentication</li>
 *   <li>Transaction management with P&L calculations</li>
 *   <li>Portfolio analytics (XIRR, sector allocation, STCG/LTCG)</li>
 *   <li>Watchlist and price alert management</li>
 *   <li>Scheduled stock price updates and portfolio snapshots</li>
 * </ul>
 *
 * @author NiveshTrack Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableScheduling
public class PortfolioApplication {

    public static void main(String[] args) {
        // Force IST timezone for the JVM
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        SpringApplication.run(PortfolioApplication.class, args);
    }
}
