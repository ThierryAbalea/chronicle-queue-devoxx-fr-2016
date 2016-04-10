package com.github.thierryabalea.ticket_sales.api;

import javolution.io.Struct;

public class AllocationApproved extends Struct
{
    public final Signed64 accountId = new Signed64();
    public final Signed64 requestId = new Signed64();
    public final Signed32 numSeats = new Signed32();
}
