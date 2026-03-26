package com.airnest.backend.reservation.controller;

import com.airnest.backend.common.exception.InvalidRequestException;
import com.airnest.backend.reservation.dto.ReservationListResponse;
import com.airnest.backend.reservation.dto.UpdateReservationStatusRequest;
import com.airnest.backend.reservation.dto.UpdateReservationStatusResponse;
import com.airnest.backend.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reservations", description = "Guest reservation management APIs")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Operation(
        summary = "List all reservations",
        description = "Retrieve all guest reservations for the authenticated host"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reservations retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping
    public ReservationListResponse listReservations() {
        return reservationService.listReservations();
    }

    @Operation(
        summary = "Update reservation status",
        description = "Update the status of a specific reservation (Preparing, Ready, Checked in, Checked out)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    @PatchMapping("/{id}/status")
    public UpdateReservationStatusResponse updateStatus(
        @Parameter(description = "Reservation ID", example = "1") @PathVariable("id") String id,
        @Valid @RequestBody UpdateReservationStatusRequest request
    ) {
        return new UpdateReservationStatusResponse(
            reservationService.updateStatus(parseId(id, "Invalid reservation id."), request.status())
        );
    }

    private Long parseId(String rawId, String errorMessage) {
        try {
            long numericId = Long.parseLong(rawId);
            if (numericId <= 0) {
                throw new InvalidRequestException(errorMessage);
            }
            return numericId;
        } catch (NumberFormatException exception) {
            throw new InvalidRequestException(errorMessage);
        }
    }
}
