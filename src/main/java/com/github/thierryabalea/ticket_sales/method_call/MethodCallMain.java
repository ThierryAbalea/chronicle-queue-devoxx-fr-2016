package com.github.thierryabalea.ticket_sales.method_call;

import com.github.thierryabalea.ticket_sales.api.AllocationApproved;
import com.github.thierryabalea.ticket_sales.api.AllocationRejected;
import com.github.thierryabalea.ticket_sales.api.ConcertCreated;
import com.github.thierryabalea.ticket_sales.api.SectionUpdated;
import com.github.thierryabalea.ticket_sales.api.TicketPurchase;
import com.github.thierryabalea.ticket_sales.api.service.EventHandler;
import com.github.thierryabalea.ticket_sales.domain.ConcertService;
import com.github.thierryabalea.ticket_sales.web.RequestWebServer;
import com.github.thierryabalea.ticket_sales.web.ResponseWebServer;
import com.github.thierryabalea.ticket_sales.web.json.TicketPurchaseFromJson;
import org.rapidoid.http.fast.On;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MethodCallMain {

    private static final ExecutorService webExecutor = Executors.newSingleThreadExecutor();
    private static final ExecutorService concertServiceExecutor = Executors.newSingleThreadExecutor();

    public static void main(String[] args) throws Exception {
        On.port(7070);

        ResponseWebServer responseWebServer = new ResponseWebServer();
        ResponseWebServer.PollHandler pollHandler =
                (accountId, version) -> webExecutor.execute(() -> responseWebServer.onPoll(accountId, version));
        responseWebServer.init(pollHandler);

        SingleThreadEventHandlerProxy proxy = new SingleThreadEventHandlerProxy(responseWebServer);
        ConcertService concertService = new ConcertService(proxy);

        RequestWebServer.JsonRequestHandler requestHandler = request -> {
            TicketPurchase ticketPurchase = TicketPurchaseFromJson.fromJson(request);
            concertServiceExecutor.execute(() -> concertService.onTicketPurchase(ticketPurchase));
        };
        RequestWebServer requestWebServer =
                new RequestWebServer(requestHandler);
        requestWebServer.init();

        concertServiceExecutor.execute(() -> SeedClient.createConcerts(concertService));
    }

    private static class SingleThreadEventHandlerProxy implements EventHandler {

        private final EventHandler eventHandler;

        public SingleThreadEventHandlerProxy(EventHandler eventHandler) {
            this.eventHandler = eventHandler;
        }

        @Override
        public void onConcertAvailable(ConcertCreated concertCreated) {
            webExecutor.submit(() -> eventHandler.onConcertAvailable(concertCreated));
        }

        @Override
        public void onAllocationApproved(AllocationApproved allocationApproved) {
            webExecutor.submit(() -> eventHandler.onAllocationApproved(allocationApproved));
        }

        @Override
        public void onAllocationRejected(AllocationRejected allocationRejected) {
            webExecutor.submit(() -> eventHandler.onAllocationRejected(allocationRejected));
        }

        @Override
        public void onSectionUpdated(SectionUpdated sectionUpdated) {
            webExecutor.submit(() -> eventHandler.onSectionUpdated(sectionUpdated));
        }
    }
}
