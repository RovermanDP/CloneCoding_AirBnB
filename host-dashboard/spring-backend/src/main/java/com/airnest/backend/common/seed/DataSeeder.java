package com.airnest.backend.common.seed;

import com.airnest.backend.auth.entity.AppUser;
import com.airnest.backend.auth.entity.UserRole;
import com.airnest.backend.auth.repository.AppUserRepository;
import com.airnest.backend.inbox.entity.InboxThread;
import com.airnest.backend.inbox.entity.InboxThreadStatus;
import com.airnest.backend.inbox.entity.MessageSender;
import com.airnest.backend.inbox.repository.InboxThreadRepository;
import com.airnest.backend.listing.entity.Listing;
import com.airnest.backend.listing.entity.ListingStatus;
import com.airnest.backend.listing.repository.ListingRepository;
import com.airnest.backend.reservation.entity.Reservation;
import com.airnest.backend.reservation.entity.ReservationStatus;
import com.airnest.backend.reservation.repository.ReservationRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

    private final InboxThreadRepository inboxThreadRepository;
    private final ReservationRepository reservationRepository;
    private final ListingRepository listingRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String seedHostEmail;
    private final String seedHostPassword;
    private final String seedHostDisplayName;

    public DataSeeder(
        InboxThreadRepository inboxThreadRepository,
        ReservationRepository reservationRepository,
        ListingRepository listingRepository,
        AppUserRepository appUserRepository,
        PasswordEncoder passwordEncoder,
        @Value("${app.seed.host-email:}") String seedHostEmail,
        @Value("${app.seed.host-password:}") String seedHostPassword,
        @Value("${app.seed.host-display-name:Airnest Host}") String seedHostDisplayName
    ) {
        this.inboxThreadRepository = inboxThreadRepository;
        this.reservationRepository = reservationRepository;
        this.listingRepository = listingRepository;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedHostEmail = seedHostEmail;
        this.seedHostPassword = seedHostPassword;
        this.seedHostDisplayName = seedHostDisplayName;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedUsers();
        seedInboxThreads();
        seedReservations();
        seedListings();
    }

    private void seedUsers() {
        if (seedHostEmail.isBlank() || seedHostPassword.isBlank() || appUserRepository.existsByEmailIgnoreCase(seedHostEmail)) {
            return;
        }

        Instant now = Instant.now();
        appUserRepository.save(
            AppUser.create(
                seedHostEmail,
                passwordEncoder.encode(seedHostPassword),
                seedHostDisplayName,
                UserRole.HOST,
                true,
                now,
                now
            )
        );
    }

    private void seedInboxThreads() {
        if (inboxThreadRepository.count() > 0) {
            return;
        }

        InboxThread olivia = InboxThread.create(
            "Olivia",
            "Inquiry from Olivia",
            "1 guest / 6 nights / Sep 8 - Sep 14",
            "Room near city centre",
            InboxThreadStatus.AWAITING_REPLY,
            null,
            Instant.parse("2026-03-24T10:15:00Z")
        );

        InboxThread daniel = InboxThread.create(
            "Daniel",
            "Inquiry from Daniel",
            "1 guest / 38 nights / Sep 23 - Oct 31",
            "Seongsu Loft Residence",
            InboxThreadStatus.REPLIED,
            "Hello Daniel, I have shared parking details and the long-stay discount terms.",
            Instant.parse("2026-03-25T08:45:00Z")
        );
        daniel.addMessage(
            MessageSender.HOST,
            "Hello Daniel, I have shared parking details and the long-stay discount terms.",
            Instant.parse("2026-03-25T08:45:00Z")
        );

        InboxThread sophia = InboxThread.create(
            "Sophia",
            "Inquiry from Sophia",
            "1 guest / 30 nights / Sep 15 - Oct 15",
            "Yeonnam Garden Stay",
            InboxThreadStatus.AWAITING_REPLY,
            null,
            Instant.parse("2026-03-25T07:20:00Z")
        );

        inboxThreadRepository.saveAll(List.of(olivia, daniel, sophia));
    }

    private void seedReservations() {
        if (reservationRepository.count() > 0) {
            return;
        }

        LocalDate today = LocalDate.now();
        reservationRepository.saveAll(List.of(
            Reservation.create(
                "Olivia",
                "Room near city centre",
                today,
                new BigDecimal("1270000"),
                ReservationStatus.PREPARING,
                Instant.parse("2026-03-25T06:10:00Z")
            ),
            Reservation.create(
                "Daniel",
                "Seongsu Loft Residence",
                today.plusDays(1),
                new BigDecimal("4920000"),
                ReservationStatus.READY,
                Instant.parse("2026-03-25T06:20:00Z")
            ),
            Reservation.create(
                "Sophia",
                "Yeonnam Garden Stay",
                LocalDate.of(2026, 9, 15),
                new BigDecimal("3480000"),
                ReservationStatus.CHECKED_IN,
                Instant.parse("2026-03-25T06:30:00Z")
            )
        ));
    }

    private void seedListings() {
        if (listingRepository.count() > 0) {
            return;
        }

        listingRepository.saveAll(List.of(
            Listing.create(
                "Room near city centre",
                new BigDecimal("179000"),
                "Seoul",
                ListingStatus.PUBLISHED,
                Instant.parse("2026-03-25T05:30:00Z")
            ),
            Listing.create(
                "Seongsu Loft Residence",
                new BigDecimal("212000"),
                "Seoul",
                ListingStatus.PUBLISHED,
                Instant.parse("2026-03-25T05:40:00Z")
            ),
            Listing.create(
                "Yeonnam Garden Stay",
                new BigDecimal("198000"),
                "Seoul",
                ListingStatus.DRAFT,
                Instant.parse("2026-03-25T05:50:00Z")
            )
        ));
    }
}
