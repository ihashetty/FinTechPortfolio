package com.niveshtrack.portfolio.controller;

import com.niveshtrack.portfolio.dto.request.CreateSipRequest;
import com.niveshtrack.portfolio.dto.response.SipInstructionDTO;
import com.niveshtrack.portfolio.security.UserDetailsServiceImpl;
import com.niveshtrack.portfolio.service.MutualFundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SIP (Systematic Investment Plan) management endpoints.
 */
@RestController
@RequestMapping("/api/sip")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "SIP", description = "Systematic Investment Plan management")
public class SipController extends BaseController {

    private final MutualFundService mutualFundService;

    public SipController(UserDetailsServiceImpl userDetailsService,
                         MutualFundService mutualFundService) {
        super(userDetailsService);
        this.mutualFundService = mutualFundService;
    }

    @GetMapping
    @Operation(summary = "Get all SIPs",
               description = "Returns all SIP instructions for the authenticated user.")
    public ResponseEntity<List<SipInstructionDTO>> getUserSips() {
        return ResponseEntity.ok(mutualFundService.getUserSIPs(getCurrentUserId()));
    }

    @PostMapping
    @Operation(summary = "Create a new SIP",
               description = "Sets up a recurring SIP for a mutual fund. First installment is executed immediately.")
    public ResponseEntity<SipInstructionDTO> createSip(
            @Valid @RequestBody CreateSipRequest request) {
        SipInstructionDTO sip = mutualFundService.createSIP(getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(sip);
    }

    @DeleteMapping("/{sipId}")
    @Operation(summary = "Cancel a SIP",
               description = "Deactivates a SIP instruction. No further installments will be executed.")
    public ResponseEntity<Void> cancelSip(@PathVariable Long sipId) {
        mutualFundService.cancelSIP(getCurrentUserId(), sipId);
        return ResponseEntity.noContent().build();
    }
}
