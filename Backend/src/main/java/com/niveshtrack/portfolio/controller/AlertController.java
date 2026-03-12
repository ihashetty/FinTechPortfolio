package com.niveshtrack.portfolio.controller;

import com.niveshtrack.portfolio.dto.request.CreateAlertRequest;
import com.niveshtrack.portfolio.dto.response.PriceAlertDTO;
import com.niveshtrack.portfolio.security.UserDetailsServiceImpl;
import com.niveshtrack.portfolio.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Price alert CRUD endpoints.
 */
@RestController
@RequestMapping("/api/alerts")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Price Alerts", description = "Stock price alert management APIs")
public class AlertController extends BaseController {

    private final AlertService alertService;

    public AlertController(UserDetailsServiceImpl userDetailsService,
                            AlertService alertService) {
        super(userDetailsService);
        this.alertService = alertService;
    }

    @GetMapping
    @Operation(summary = "Get all alerts",
               description = "Returns all price alerts (active and triggered) for the user.")
    public ResponseEntity<List<PriceAlertDTO>> getAlerts() {
        return ResponseEntity.ok(alertService.getAlerts(getCurrentUserId()));
    }

    @PostMapping
    @Operation(summary = "Create price alert",
               description = "Sets an alert to trigger when the stock price goes ABOVE or BELOW a target.")
    public ResponseEntity<PriceAlertDTO> createAlert(@Valid @RequestBody CreateAlertRequest request) {
        PriceAlertDTO alert = alertService.createAlert(getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(alert);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a price alert")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        alertService.deleteAlert(getCurrentUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
