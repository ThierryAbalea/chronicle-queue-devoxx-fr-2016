package com.github.thierryabalea.ticket_sales;

import com.github.thierryabalea.ticket_sales.api.command.CreateConcert;
import com.github.thierryabalea.ticket_sales.api.SectionSeating;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

public class ConcertFactory {

    public static List<CreateConcert> createConcerts() {
        CreateConcert createConcert1 = new CreateConcert(
                1L,
                0,
                "Red Hot Chili Peppers",
                "Albert Hall",
                (short) 8,
                asList(
                        new SectionSeating(1, "Section A", 58.50F, Integer.MAX_VALUE),
                        new SectionSeating(2, "Section B", 63.50F, Integer.MAX_VALUE),
                        new SectionSeating(3, "Section C", 45.50F, Integer.MAX_VALUE),
                        new SectionSeating(4, "Section D", 67.50F, Integer.MAX_VALUE),
                        new SectionSeating(5, "Section E", 38.00F, Integer.MAX_VALUE),
                        new SectionSeating(6, "Section F", 31.95F, Integer.MAX_VALUE),
                        new SectionSeating(7, "Section G", 58.50F, Integer.MAX_VALUE),
                        new SectionSeating(8, "Section H", 78.89F, Integer.MAX_VALUE))
        );

        CreateConcert createConcert2 = new CreateConcert(
                2L,
                1,
                "Gomez",
                "Wembley Park",
                (short) 8,
                asList(
                        new SectionSeating(1, "Section A", 58.50F, Integer.MAX_VALUE),
                        new SectionSeating(2, "Section B", 63.50F, Integer.MAX_VALUE),
                        new SectionSeating(3, "Section C", 45.50F, Integer.MAX_VALUE),
                        new SectionSeating(4, "Section D", 67.50F, Integer.MAX_VALUE),
                        new SectionSeating(5, "Section E", 38.00F, Integer.MAX_VALUE),
                        new SectionSeating(6, "Section F", 66.55F, Integer.MAX_VALUE),
                        new SectionSeating(7, "Section G", 31.95F, Integer.MAX_VALUE),
                        new SectionSeating(8, "Section H", 78.89F, Integer.MAX_VALUE)
                )
        );

        return Arrays.asList(createConcert1, createConcert2);
    }
}
