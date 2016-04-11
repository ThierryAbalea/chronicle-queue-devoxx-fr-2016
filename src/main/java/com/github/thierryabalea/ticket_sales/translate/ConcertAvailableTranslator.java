package com.github.thierryabalea.ticket_sales.translate;

import com.github.thierryabalea.ticket_sales.api.ConcertCreated;
import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.Message;
import com.github.thierryabalea.ticket_sales.api.SectionSeating;
import com.github.thierryabalea.ticket_sales.domain.Concert;
import com.github.thierryabalea.ticket_sales.domain.Seating;
import com.github.thierryabalea.ticket_sales.domain.Section;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class ConcertAvailableTranslator {

    public static void translateTo(Message message, long sequence, Concert concert) {
        List<SectionSeating> sectionSeatings = concert.getSeatingMap().entrySet().stream()
                .map(sectionSeatingEntry -> {
                    Section section = sectionSeatingEntry.getKey();
                    Seating seating = sectionSeatingEntry.getValue();
                    return new SectionSeating(
                            section.getId(),
                            section.getName(),
                            section.getPrice(),
                            seating.getAvailableSeats()
                    );
                }).collect(toList());

        ConcertCreated concertCreated = new ConcertCreated(
                concert.getId(),
                sequence,
                concert.getName(),
                concert.getVenue(),
                (short) sectionSeatings.size(),
                sectionSeatings);

        message.type = EventType.CONCERT_CREATED;
        message.event = concertCreated;

    }
}
