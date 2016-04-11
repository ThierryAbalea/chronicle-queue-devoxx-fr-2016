package com.github.thierryabalea.ticket_sales.udp;

import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.TicketingEvent;
import com.lmax.disruptor.EventFactory;
import net.openhft.chronicle.wire.AbstractMarshallable;

public class Message extends AbstractMarshallable {
    public EventType type;
    public TicketingEvent event;

    @Override
    public String toString() {
        return "Message [type=" + type + "]";
    }

    public final static EventFactory<Message> FACTORY = Message::new;
}
