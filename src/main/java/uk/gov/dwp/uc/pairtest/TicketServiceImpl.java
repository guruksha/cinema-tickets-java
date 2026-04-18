package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */

    private record PurchaseTotals(int totalAmount, int totalSeats) {}
    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;
    private static final int ADULT_PRICE = 25;
    private static final int CHILD_PRICE = 15;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        validateAccount(accountId);
        validateRequest(ticketTypeRequests);
        PurchaseTotals totals = calculateTotals(ticketTypeRequests);
        ticketPaymentService.makePayment(accountId,totals.totalAmount());
        seatReservationService.reserveSeat(accountId,totals.totalSeats());

    }
    private PurchaseTotals calculateTotals(TicketTypeRequest[] requests)
    {
        int adult=0,child=0,infant=0;
        for(TicketTypeRequest req: requests) {
            if (req == null) {
                throw new InvalidPurchaseException("Ticket request cannot be null");
            }
            if (req.getTicketType()==null){
                throw new InvalidPurchaseException("Ticket type can't be null");
            }
            if (req.getNoOfTickets() <= 0) {
                throw new InvalidPurchaseException("Ticket quantity cannot be negative or 0 ");
            }
            switch (req.getTicketType()) {
                case ADULT -> adult += req.getNoOfTickets();
                case CHILD -> child += req.getNoOfTickets();
                case INFANT -> infant += req.getNoOfTickets();
            }
        }
        validateBusinessRules(adult,child,infant);
        int totalAmount=((adult*ADULT_PRICE)+(child*CHILD_PRICE));
        int totalSeats=adult+child;
        return new PurchaseTotals(totalAmount,totalSeats);
    }
    private void validateAccount(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException("Account Id is invalid");
        }
    }

    private void validateRequest(TicketTypeRequest[] requests){
            if(requests==null || requests.length==0 ) {
                throw new InvalidPurchaseException("Ticket requests cannot be null or empty");
            }
        }

    private void validateBusinessRules(int adult,int child,int infant){
            if(adult==0 && (child>0 || infant>0))
                throw new InvalidPurchaseException("Children and infants must be accompanied by an adult");
            if(infant>adult)
                throw new InvalidPurchaseException("Each infant needs atleast one adult to attend the movies ");
            if(adult+child+infant>25)
                throw new InvalidPurchaseException("Ticket quatity cannot be more than 25");

        }


    }


