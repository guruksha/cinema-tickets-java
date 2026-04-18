# Cinema Tickets

Java implementation of the DWP cinema tickets coding exercise.

## Overview

The service handles ticket purchase requests, validates them against the business rules, calculates the total cost and number of seats needed, then calls the third-party payment and reservation services.

## Business Rules

- Account ID must be greater than zero
- At least one ticket must be requested
- Child and infant tickets require at least one adult ticket
- Infants cannot outnumber adults (one lap per infant)
- Maximum of 25 tickets per transaction
- Infants are free and get no seat
- Adults: £25, Children: £15, Infants: £0

## Running the Tests

Requires Java 21 and Maven.

```bash
mvn test
```

18 test cases covering valid purchases, invalid accounts, missing adults, infant/adult ratio, and ticket quantity limits.

## Approach

I split the logic into focused private methods to keep `purchaseTickets()` readable , it just validates, calculates, and calls the services in order. Constructor injection is used for the two third-party services so they can be mocked in tests.

I added an infant-to-adult ratio check since the rules state infants sit on an adult's lap , it felt like an implied constraint worth enforcing.