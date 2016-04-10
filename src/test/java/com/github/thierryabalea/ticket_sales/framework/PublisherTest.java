package com.github.thierryabalea.ticket_sales.framework;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

import com.github.thierryabalea.ticket_sales.api.RejectionReason;
import com.github.thierryabalea.ticket_sales.api.TicketPurchase;
import com.github.thierryabalea.ticket_sales.translate.ConcertAvailableTranslator;
import com.github.thierryabalea.ticket_sales.translate.PurchaseApprovedTranslator;
import com.github.thierryabalea.ticket_sales.translate.PurchaseRejectedTranslator;
import com.github.thierryabalea.ticket_sales.translate.SectionUpdatedTranslator;
import org.junit.Before;
import org.junit.Test;

import com.lmax.disruptor.EventPublisher;
import com.github.thierryabalea.ticket_sales.api.Message;
import com.github.thierryabalea.ticket_sales.domain.Concert;

public class PublisherTest
{
    private EventPublisher<Message> eventPublisher;
    private Publisher publisher;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setup()
    {
        eventPublisher = mock(EventPublisher.class);
        publisher = new Publisher(eventPublisher);
    }
    
    @Test
    public void shouldPublishPurchasedApproved()
    {
        publisher.onPurchaseApproved(new TicketPurchase());
        verify(eventPublisher).publishEvent(argThat(notNullValue(PurchaseApprovedTranslator.class)));
    }
    
    @Test
    public void shouldPublishConcertAvailable()
    {
        publisher.onConcertAvailable(mock(Concert.class));
        verify(eventPublisher).publishEvent(argThat(notNullValue(ConcertAvailableTranslator.class)));
    }
    
    @Test
    public void shouldPublishPurchaseRejected()
    {
        publisher.onPurchaseRejected(RejectionReason.CONCERT_DOES_NOT_EXIST, new TicketPurchase());
        verify(eventPublisher).publishEvent(argThat(notNullValue(PurchaseRejectedTranslator.class)));
    }
    
    @Test
    public void shouldSectionUpdated()
    {
        publisher.onSectionUpdated(1, 2, 3);
        verify(eventPublisher).publishEvent(argThat(notNullValue(SectionUpdatedTranslator.class)));
    }
}
