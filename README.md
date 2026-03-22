**Digital Wallet System**
A microservices-based digital wallet system built with Java Spring Boot. 
The system is split into two independent services that communicate over HTTP using OpenFeign.

**Services**
wallet-service - 8081 - wallet_db 
transaction-service - 8082 - transaction_db 

**Tech Stack**
- Java 17
- Spring Boot 3.4
- Spring Data JPA
- PostgreSQL
- OpenFeign (inter-service communication)
- Lombok
- Gradle (multi-module)

**Prerequisites**
- Java 17+
- PostgreSQL running locally
- Gradle

**Database Setup**
Connect to PostgreSQL and create the two databases:

CREATE DATABASE wallet_db;
CREATE DATABASE transaction_db;


**Configuration**
Update the database password in both services under:

wallet-service/src/main/resources/application.properties
transaction-service/src/main/resources/application.properties

Both services must be running simultaneously for transfers and withdrawals to work.

## API Endpoints

### Wallet Service (port 8081)

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/wallets | Create a new wallet |
| GET | /api/wallets/{id} | Get wallet by ID |

### Transaction Service (port 8082)

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/transactions/deposit | Deposit funds |
| POST | /api/transactions/withdraw | Withdraw funds |
| POST | /api/transactions/transfer | Transfer between wallets |
| GET | /api/transactions/history/{walletId} | Get transaction history |

## Sample Requests

### Create Wallet

POST http://localhost:8081/api/wallets
{
    "ownerName": "Paul Mwangi",
    "ownerEmail": "paul@task.co.ke"
}


### Deposit

POST http://localhost:8082/api/transactions/deposit
{
    "walletId": "your-wallet-uuid",
    "amount": 5000.00,
    "description": "Initial deposit"
}


### Withdraw

POST http://localhost:8082/api/transactions/withdraw
{
    "walletId": "your-wallet-uuid",
    "amount": 1000.00,
    "description": "ATM withdrawal"
}


### Transfer

POST http://localhost:8082/api/transactions/transfer
{
    "sourceWalletId": "source-wallet-uuid",
    "targetWalletId": "target-wallet-uuid",
    "amount": 500.00,
    "description": "Sending funds"
}


## Architecture Notes

- Each service owns its own database — no shared data store
- Services communicate via HTTP using OpenFeign
- Balance updates use optimistic locking (`@Version`) to handle concurrent requests safely
- All money values use `BigDecimal` to avoid floating point precision issues
- Distributed transactions (e.g. transfer rollback) are handled with basic compensation logic.
  A Saga pattern would be the production-grade approach for this.
