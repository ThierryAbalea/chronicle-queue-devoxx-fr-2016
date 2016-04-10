package com.github.thierryabalea.ticket_sales.framework;

import com.github.thierryabalea.ticket_sales.api.Message;

public interface Handler
{
    void onMessage(Message message);
}
