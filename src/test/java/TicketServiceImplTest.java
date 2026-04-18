
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketServiceImplTest {

    private TicketPaymentService paymentService;
    private SeatReservationService reservationService;
    private TicketServiceImpl ticketService;

    @BeforeEach
    void setUp() {
        paymentService = mock(TicketPaymentService.class);
        reservationService = mock(SeatReservationService.class);
        ticketService = new TicketServiceImpl(paymentService, reservationService);
    }

    //Happy Path Tests
    @Test
    void shouldProcessValidAdultTicketPurchase() {
        ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2));

        verify(paymentService).makePayment(1L, 50);       // 2 adults × £25
        verify(reservationService).reserveSeat(1L, 2);    // 2 seats
    }

    @Test
    void shouldProcessAdultAndChildTickets() {
        ticketService.purchaseTickets(1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3));

        verify(paymentService).makePayment(1L, 95);       // (2×25) + (3×15)
        verify(reservationService).reserveSeat(1L, 5);    // 2 adults + 3 children
    }

    @Test
    void shouldNotChargeOrAllocateSeatForInfants() {
        ticketService.purchaseTickets(1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1));

        verify(paymentService).makePayment(1L, 25);       // only adult pays
        verify(reservationService).reserveSeat(1L, 1);    // infant gets no seat
    }

    @Test
    void shouldAllowMaximumOf25Tickets() {
        ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 25));

        verify(paymentService).makePayment(1L, 625);      // 25 × £25
        verify(reservationService).reserveSeat(1L, 25);
    }

    @Test
    void shouldAllowMultipleInfantsUpToAdultCount() {
        ticketService.purchaseTickets(1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 3),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 3));

        verify(paymentService).makePayment(1L, 75);
        verify(reservationService).reserveSeat(1L, 3);// 3 adults only
    }

    // Invalid Account Tests
    @Test
    void shouldRejectAccountIdOfZero() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(0L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)));
    }

    @Test
    void shouldRejectNegativeAccountId() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(-1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)));
    }

    @Test
    void shouldRejectNullAccountId() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(null, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)));
    }

    //No Tickets Tests
    @Test
    void shouldRejectEmptyTicketRequest() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L));
    }

    @Test
    void shouldRejectNullTicketRequest() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, (TicketTypeRequest[]) null));
    }

    //No Adult Tests
    @Test
    void shouldRejectChildTicketWithoutAdult() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2)));
    }

    @Test
    void shouldRejectInfantTicketWithoutAdult() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)));
    }

    //Infant/Adult Ratio Tests
    @Test
    void shouldRejectMoreInfantsThanAdults() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                        new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2)));
    }

    //Quantity Limit Tests
    @Test
    void shouldRejectMoreThan25Tickets() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26)));
    }

    @Test
    void shouldRejectMoreThan25TicketsAcrossTypes() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10),
                        new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 10),
                        new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 6)));
    }
    @Test
    void shouldRejectZeroTicketQuantity() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 0)));
    }
    @Test
    void shouldRejectNullTicketInsideRequestArray() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, null, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)));
    }

    //No Interaction Tests
    @Test
    void shouldNotCallServicesOnInvalidRequest() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(0L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)));

        verifyNoInteractions(paymentService, reservationService);
    }
}