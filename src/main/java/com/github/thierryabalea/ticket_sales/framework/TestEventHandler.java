package com.github.thierryabalea.ticket_sales.framework;

import com.lmax.disruptor.EventHandler;
import com.github.thierryabalea.ticket_sales.api.Message;

public class TestEventHandler implements EventHandler<Message>
{
    public void onEvent(Message message, long sequence, boolean endOfBatch) throws Exception
    {
        System.out.println("Message Type: " + message.type.get());
    }
}
