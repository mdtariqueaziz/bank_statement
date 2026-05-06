INSERT INTO users (username, password, email, role, enabled)
VALUES (
    'admin',
    '$2a$12$tKfYKsBxGYfZH0BsOVWBL.JJx8Qx9RbXvF5uZk7QeJNzCf0m8UOLW',
    'admin@bank.com',
    'ADMIN',
    TRUE
) ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password, email, role, enabled)
VALUES (
    'user',
    '$2a$12$tKfYKsBxGYfZH0BsOVWBL.JJx8Qx9RbXvF5uZk7QeJNzCf0m8UOLW',
    'user@bank.com',
    'USER',
    TRUE
) ON CONFLICT (username) DO NOTHING;

INSERT INTO statements (account_id, transaction_date, amount, type, description, balance, reference_number, currency)
VALUES
    ('ACC001', '2025-01-05', 50000.00, 'CREDIT', 'Salary January',       150000.00, 'TXN2025010501', 'INR'),
    ('ACC001', '2025-01-08', 2500.00,  'DEBIT',  'Electricity Bill',     147500.00, 'TXN2025010801', 'INR'),
    ('ACC001', '2025-01-12', 1200.00,  'DEBIT',  'Netflix Subscription', 146300.00, 'TXN2025011201', 'INR'),
    ('ACC001', '2025-01-15', 10000.00, 'DEBIT',  'Rent Payment',        136300.00, 'TXN2025011501', 'INR'),
    ('ACC001', '2025-01-20', 5000.00,  'CREDIT', 'Freelance Income',    141300.00, 'TXN2025012001', 'INR'),
    ('ACC001', '2025-02-01', 50000.00, 'CREDIT', 'Salary February',     191300.00, 'TXN2025020101', 'INR'),
    ('ACC001', '2025-02-10', 3200.00,  'DEBIT',  'Grocery Shopping',    188100.00, 'TXN2025021001', 'INR'),
    ('ACC001', '2025-02-14', 8000.00,  'DEBIT',  'Insurance Premium',   180100.00, 'TXN2025021401', 'INR'),
    ('ACC001', '2025-03-01', 50000.00, 'CREDIT', 'Salary March',        230100.00, 'TXN2025030101', 'INR'),
    ('ACC001', '2025-03-05', 15000.00, 'DEBIT',  'Travel Booking',      215100.00, 'TXN2025030501', 'INR')
ON CONFLICT (reference_number) DO NOTHING;

INSERT INTO statements (account_id, transaction_date, amount, type, description, balance, reference_number, currency)
VALUES
    ('ACC002', '2025-01-03', 75000.00, 'CREDIT', 'Salary Credit',       75000.00,  'TXN2025010302', 'INR'),
    ('ACC002', '2025-01-10', 5000.00,  'DEBIT',  'EMI Payment',         70000.00,  'TXN2025011002', 'INR'),
    ('ACC002', '2025-02-03', 75000.00, 'CREDIT', 'Salary Credit',       145000.00, 'TXN2025020302', 'INR'),
    ('ACC002', '2025-02-15', 25000.00, 'DEBIT',  'Medical Expenses',    120000.00, 'TXN2025021502', 'INR'),
    ('ACC002', '2025-03-03', 75000.00, 'CREDIT', 'Salary Credit',       195000.00, 'TXN2025030302', 'INR')
ON CONFLICT (reference_number) DO NOTHING;
