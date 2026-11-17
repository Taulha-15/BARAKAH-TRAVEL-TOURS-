-- Seed 9 simulated customer accounts so CustomerAccounts has at least 10 records.
-- Run in the Azure SQL query editor against BarakahTravelD.
-- Passwords are stored ONLY as ASP.NET Core Identity PasswordHasher (V3) hashes.
-- These are test accounts for the prototype; delete them before real use:
--   DELETE FROM CustomerAccounts WHERE Email LIKE '%@barakah.test';  (or the customers' own emails)
SET NOCOUNT ON;

DECLARE @h TABLE (n INT PRIMARY KEY, hash NVARCHAR(500));
INSERT INTO @h (n, hash) VALUES
    (1, 'AQAAAAEAAYagAAAAEKlAK7A19kvcngK9CLckmeShZKZzE6aT+hP1nwAZz8uJXyzuVzqrRo2wipJ52NclMA=='),
    (2, 'AQAAAAEAAYagAAAAEDUrrs4wfBhb9l74hJO4jBmhZa8UaKqUFfw8M4z6F+ybYxZW33Pecx4kleDDpRajIg=='),
    (3, 'AQAAAAEAAYagAAAAEERpsIewY9EZNM2GPJphdpAyVSPtTt4lmKHcsRjKYATfu0G6UuYmbc0FJZXrdzYuiA=='),
    (4, 'AQAAAAEAAYagAAAAEKTJdWnALv4ZhcT/wlW6z9DFrBl8skvrGHAGgW8rTSrrX2YMNAI7vX98BztGDSeWFg=='),
    (5, 'AQAAAAEAAYagAAAAEMq24N9eUiedZyjReqepVl5T0Rd99Ik01XYLqLbNGUuyf7ybfFxwE5Ylgk3DGbQKPw=='),
    (6, 'AQAAAAEAAYagAAAAEK/OWUmYf8TH1yACcnwY26MRsKJJQ/Yb0D159r8t4lHbqt9L/7USX132L9APY0h4cg=='),
    (7, 'AQAAAAEAAYagAAAAEIRk7ymT2wKfPzut9Lg7lS3vREJBvi3Wa106WhNoi1iBofvQO1G9OurixgOJ+kLazg=='),
    (8, 'AQAAAAEAAYagAAAAEFM/mSIvka1XEUvz2h06u0MANJq+VYNp4N7uamNvE0QErW/FJ1sGjhmm+Vx9mW9YnQ=='),
    (9, 'AQAAAAEAAYagAAAAEHO7qXU2TkTfz3Wube9ivxibiuScHFEmLPFzUJnf/9jgarGfLyf2ZiFvrxjCvIOuMQ==');

;WITH missing AS (
    SELECT c.CustomerID,
           COALESCE(NULLIF(LTRIM(RTRIM(c.Email)), ''), CONCAT('customer', c.CustomerID, '@barakah.test')) AS Email,
           ROW_NUMBER() OVER (ORDER BY c.CustomerID) AS n
    FROM Customers c
    WHERE NOT EXISTS (SELECT 1 FROM CustomerAccounts a WHERE a.CustomerID = c.CustomerID)
)
INSERT INTO CustomerAccounts (CustomerID, Email, PasswordHash, IsActive, CreatedAt)
SELECT m.CustomerID, m.Email, h.hash, 1, SYSUTCDATETIME()
FROM missing m
JOIN @h h ON h.n = m.n
WHERE m.n <= 9
  AND NOT EXISTS (SELECT 1 FROM CustomerAccounts x WHERE x.Email = m.Email);

SELECT COUNT(*) AS CustomerAccountsCount FROM CustomerAccounts;
