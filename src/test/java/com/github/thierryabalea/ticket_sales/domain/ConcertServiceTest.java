package com.github.thierryabalea.ticket_sales.domain;

import com.github.thierryabalea.ticket_sales.api.ConcertCreated;
import com.github.thierryabalea.ticket_sales.api.RejectionReason;
import com.github.thierryabalea.ticket_sales.api.SectionSeating;
import com.github.thierryabalea.ticket_sales.api.TicketPurchase;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.util.Arrays;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ConcertServiceTest {
    private ConcertServiceListener listener;
    private ConcertService concertService;

    @Before
    public void setup() {
        listener = mock(ConcertServiceListener.class);
        concertService = new ConcertService(listener);
    }

    @Test
    public void shouldSendNotifyOnNewConcertCreated() {
        ConcertCreated concertCreated = singeSectionConcert();
        concertService.on(concertCreated);

        verify(listener).onConcertAvailable(argThat(isConcert(concertCreated)));
    }

    @Test
    public void shouldNotifyOfSeatsAllocated() throws Exception {
        ConcertCreated concertCreated = singeSectionConcert();
        concertService.on(concertCreated);
        long concertId = concertCreated.concertId;
        long sectionId = concertCreated.sections.get(0).sectionId;

        TicketPurchase ticketPurchase = new TicketPurchase(
                concertId,
                sectionId,
                4,
                7L,
                11L
        );

        int seatsAvailable = concertCreated.sections.get(0).seats - ticketPurchase.numSeats;

        concertService.on(ticketPurchase);

        verify(listener).onPurchaseApproved(ticketPurchase);
        verify(listener).onSectionUpdated(concertId, sectionId, seatsAvailable);
    }

    @Test
    public void shouldNotifyFailureOnNonExistentConcert() throws Exception {
        ConcertCreated concertCreated = singeSectionConcert();
        concertService.on(concertCreated);

        TicketPurchase ticketPurchase = new TicketPurchase(
                999999999999L,
                concertCreated.sections.get(0).sectionId,
                4,
                7L,
                11L
        );

        concertService.on(ticketPurchase);

        verify(listener).onPurchaseRejected(RejectionReason.CONCERT_DOES_NOT_EXIST, ticketPurchase);
    }

    @Test
    public void shouldNotifyFailureOnNonExistentSection() throws Exception {
        ConcertCreated concertCreated = singeSectionConcert();
        concertService.on(concertCreated);

        TicketPurchase ticketPurchase = new TicketPurchase(
                concertCreated.concertId,
                99999999999L,
                4,
                7L,
                11L
        );

        concertService.on(ticketPurchase);

        verify(listener).onPurchaseRejected(RejectionReason.SECTION_DOES_NOT_EXIST, ticketPurchase);
    }

    @Test
    public void shouldRejectOrderIfNotSeatsAvailable() throws Exception {
        ConcertCreated concertCreated = singeSectionConcert();
        concertService.on(concertCreated);

        TicketPurchase ticketPurchase = new TicketPurchase(
                concertCreated.concertId,
                concertCreated.sections.get(0).sectionId,
                concertCreated.sections.get(0).seats,
                7L,
                11L
        );

        concertService.on(ticketPurchase);

        verify(listener).onPurchaseRejected(RejectionReason.NOT_ENOUGH_SEATS, ticketPurchase);
    }

    private ConcertCreated singeSectionConcert() {
        ConcertCreated concertCreated = new ConcertCreated(
                12345L,
                0,
                "Red Hot Chili Peppers",
                "Albert Hall",
                (short) 1,
                Arrays.asList(new SectionSeating(5, "East", 75.50F, 100))
        );
        return concertCreated;
    }

    public static Matcher<Concert> isConcert(final ConcertCreated concertCreated) {
        return new TypeSafeMatcher<Concert>() {
            public void describeTo(Description description) {
                description.appendText("id:").appendValue(concertCreated.concertId);
                description.appendText(", name:").appendValue(concertCreated.name);
                description.appendText(", venue:").appendValue(concertCreated.venue);
            }

            @Override
            public boolean matchesSafely(Concert concert) {
                if (concert == null) {
                    return false;
                }

                boolean result = true;
                result &= concertCreated.concertId == concert.getId();
                result &= concertCreated.name.equals(concert.getName());
                result &= concertCreated.venue.equals(concert.getVenue());

                for (int i = 0, n = concertCreated.numSections; i < n; i++) {
                    SectionSeating sectionSeating = concertCreated.sections.get(i);
                    Section section = concert.getSection(sectionSeating.sectionId);
                    Seating seating = concert.getSeating(section);

                    result &= sectionSeating.name.equals(section.getName());
                    result &= sectionSeating.seats == seating.getAvailableSeats();
                }

                return result;
            }
        };
    }
}
