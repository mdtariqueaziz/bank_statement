Project Overview: Bank Statement Service
This project is a high-performance REST API built with Spring Boot 3 and PostgreSQL. Its primary purpose is to securely serve bank statements to users while handling massive scale. It is designed to support up to 10,000 requests per second and a potential customer base of 50 million users.

Here are the key highlights described in the README:

1. Core Functionality
The application provides two main capabilities:

Authentication (/api/v1/auth/login): Secure login that issues a JWT (JSON Web Token) for stateless session management.
Statement Retrieval (/api/v1/statements/{accountId}): The core endpoint that fetches bank transactions for a specific account within a given date range. It returns not only the individual transactions but also a calculated summary (Total Credits, Total Debits, Net Balance).
2. Built for Extreme Performance
The README highlights several advanced architectural decisions designed to prevent database crashes and ensure low-latency responses:

Database Partitioning: The statements table is partitioned by date. This means when a user searches for January statements, the database completely ignores data from other months, speeding up the query exponentially.
Covering Indexes: It uses composite indexes (e.g., account_id + transaction_date) so the database can fulfill the query using the index alone, without having to load the actual table rows.
Caching Strategy: It utilizes Caffeine Cache to store frequently accessed data in memory, allowing response times of ~50 nanoseconds for "hot" accounts.
Connection Pooling: Uses HikariCP to manage a pool of database connections, ensuring threads don't wait long to talk to the DB.
3. Enterprise Security
Security is baked in from the ground up:

Stateless JWT: Uses HS512 algorithm for token generation with a strict 1-hour expiration.
Password Hashing: Uses BCrypt with a strength of 12 to securely store passwords.
Server-Side Logout: Unlike simple JWT apps, this app stores tokens in the DB and nullifies them upon logout, preventing token reuse.
Endpoint Protection: Implements method-level security (@PreAuthorize) to ensure only users with the correct roles can access specific data.
