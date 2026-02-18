# Entity Relationship Diagram (ERD)
## Wells Fargo Investment Management System

### Entities & Relationships

```
┌─────────────────────────┐          ┌─────────────────────────┐
│   FINANCIAL_ADVISOR     │          │         CLIENT           │
├─────────────────────────┤          ├─────────────────────────┤
│ PK advisor_id (BIGINT)  │──┐       │ PK client_id (BIGINT)   │
│    first_name (VARCHAR)  │  │       │ FK advisor_id (BIGINT)  │──┐
│    last_name (VARCHAR)   │  │  1:N  │    first_name (VARCHAR)  │  │
│    email (VARCHAR) [UQ]  │  ├──────>│    last_name (VARCHAR)   │  │
│    phone (VARCHAR)       │  │       │    email (VARCHAR) [UQ]  │  │
│    employee_id (VARCHAR) │  │       │    phone (VARCHAR)       │  │
│    created_at (TIMESTAMP)│──┘       │    address (VARCHAR)     │  │
│    updated_at (TIMESTAMP)│         │    created_at (TIMESTAMP)│  │
└─────────────────────────┘          │    updated_at (TIMESTAMP)│  │
                                      └─────────────────────────┘  │
                                                                    │
                              ┌──────────────────────────────────────┘
                              │
                              │       ┌─────────────────────────┐
                              │       │       PORTFOLIO          │
                              │       ├─────────────────────────┤
                              │  1:1  │ PK portfolio_id (BIGINT)│
                              └──────>│ FK client_id (BIGINT)   │──┐
                                      │    name (VARCHAR)        │  │
                                      │    created_at (TIMESTAMP)│  │
                                      │    updated_at (TIMESTAMP)│  │
                                      └─────────────────────────┘  │
                                                                    │
                              ┌──────────────────────────────────────┘
                              │
                              │       ┌──────────────────────────────┐
                              │       │         SECURITY              │
                              │       ├──────────────────────────────┤
                              │  1:N  │ PK security_id (BIGINT)      │
                              └──────>│ FK portfolio_id (BIGINT)     │
                                      │    name (VARCHAR)             │
                                      │    category (VARCHAR)         │
                                      │    purchase_date (DATE)       │
                                      │    purchase_price (DECIMAL)   │
                                      │    quantity (INTEGER)         │
                                      │    created_at (TIMESTAMP)     │
                                      │    updated_at (TIMESTAMP)     │
                                      └──────────────────────────────┘
```

### Relationships Summary

| Relationship                  | Type | Description                                    |
|-------------------------------|------|------------------------------------------------|
| Financial Advisor → Client    | 1:N  | Each advisor can have many clients              |
| Client → Portfolio            | 1:1  | Each client has exactly one portfolio            |
| Portfolio → Security          | 1:N  | Each portfolio can contain zero or more securities|

### Cardinality Details

- **Financial Advisor to Client**: One financial advisor manages many clients. Each client belongs to exactly one advisor.
- **Client to Portfolio**: Each client has exactly one portfolio. Each portfolio belongs to exactly one client.
- **Portfolio to Security**: A portfolio may contain zero or more securities. Each security belongs to exactly one portfolio.

### Entity Attributes

#### FINANCIAL_ADVISOR
| Attribute   | Type        | Constraints        | Description                     |
|-------------|-------------|--------------------|---------------------------------|
| advisor_id  | BIGINT      | PK, AUTO_INCREMENT | Unique identifier               |
| first_name  | VARCHAR(50) | NOT NULL           | Advisor's first name            |
| last_name   | VARCHAR(50) | NOT NULL           | Advisor's last name             |
| email       | VARCHAR(100)| NOT NULL, UNIQUE   | Advisor's email address         |
| phone       | VARCHAR(20) | NOT NULL           | Advisor's phone number          |
| employee_id | VARCHAR(20) | NOT NULL, UNIQUE   | Wells Fargo employee identifier |
| created_at  | TIMESTAMP   | NOT NULL           | Record creation timestamp       |
| updated_at  | TIMESTAMP   | NOT NULL           | Record last update timestamp    |

#### CLIENT
| Attribute   | Type        | Constraints        | Description                     |
|-------------|-------------|--------------------|---------------------------------|
| client_id   | BIGINT      | PK, AUTO_INCREMENT | Unique identifier               |
| advisor_id  | BIGINT      | FK, NOT NULL       | References financial_advisor    |
| first_name  | VARCHAR(50) | NOT NULL           | Client's first name             |
| last_name   | VARCHAR(50) | NOT NULL           | Client's last name              |
| email       | VARCHAR(100)| NOT NULL, UNIQUE   | Client's email address          |
| phone       | VARCHAR(20) |                    | Client's phone number           |
| address     | VARCHAR(255)|                    | Client's mailing address        |
| created_at  | TIMESTAMP   | NOT NULL           | Record creation timestamp       |
| updated_at  | TIMESTAMP   | NOT NULL           | Record last update timestamp    |

#### PORTFOLIO
| Attribute    | Type        | Constraints        | Description                    |
|--------------|-------------|--------------------|---------------------------------|
| portfolio_id | BIGINT      | PK, AUTO_INCREMENT | Unique identifier              |
| client_id    | BIGINT      | FK, NOT NULL, UQ   | References client (one-to-one) |
| name         | VARCHAR(100)| NOT NULL           | Portfolio display name          |
| created_at   | TIMESTAMP   | NOT NULL           | Record creation timestamp       |
| updated_at   | TIMESTAMP   | NOT NULL           | Record last update timestamp    |

#### SECURITY
| Attribute      | Type          | Constraints        | Description                  |
|----------------|---------------|--------------------|---------------------------------|
| security_id    | BIGINT        | PK, AUTO_INCREMENT | Unique identifier            |
| portfolio_id   | BIGINT        | FK, NOT NULL       | References portfolio         |
| name           | VARCHAR(100)  | NOT NULL           | Security name (e.g. AAPL)    |
| category       | VARCHAR(50)   | NOT NULL           | Category (Stock, Bond, etc.) |
| purchase_date  | DATE          | NOT NULL           | Date of purchase             |
| purchase_price | DECIMAL(15,2) | NOT NULL           | Price at purchase            |
| quantity       | INTEGER       | NOT NULL           | Number of units held         |
| created_at     | TIMESTAMP     | NOT NULL           | Record creation timestamp    |
| updated_at     | TIMESTAMP     | NOT NULL           | Record last update timestamp |
