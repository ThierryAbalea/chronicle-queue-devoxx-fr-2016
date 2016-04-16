package com.github.thierryabalea.ticket_sales.udp.translate;

import com.github.thierryabalea.ticket_sales.udp.EventType;
import com.github.thierryabalea.ticket_sales.api.command.Poll;
import com.github.thierryabalea.ticket_sales.udp.Message;

public class PollTranslator {

    public static void translateTo(Message message, long sequence, long accountId, long version) {
        message.type = EventType.POLL;
        message.event = new Poll(accountId, version);
    }
}
