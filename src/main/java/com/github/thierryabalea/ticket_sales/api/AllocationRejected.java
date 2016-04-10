package com.github.thierryabalea.ticket_sales.api;

import javolution.io.Struct;

public class AllocationRejected extends Struct
{
    public final Signed64 accountId = new Signed64();
    public final Signed64 requestId = new Signed64();
    public final Enum32<RejectionReason> reason = new Enum32<RejectionReason>(RejectionReason.values());
}
