-- listings.price (VARCHAR) → price_amount (NUMERIC)
ALTER TABLE listings ADD COLUMN price_amount NUMERIC(12, 2) NOT NULL DEFAULT 0;
ALTER TABLE listings DROP COLUMN price;
ALTER TABLE listings ALTER COLUMN price_amount DROP DEFAULT;

-- reservations.arrival (VARCHAR) → arrival_date (DATE)
ALTER TABLE reservations ADD COLUMN arrival_date DATE NOT NULL DEFAULT CURRENT_DATE;
ALTER TABLE reservations DROP COLUMN arrival;
ALTER TABLE reservations ALTER COLUMN arrival_date DROP DEFAULT;

-- reservations.payout (VARCHAR) → payout_amount (NUMERIC)
ALTER TABLE reservations ADD COLUMN payout_amount NUMERIC(12, 2) NOT NULL DEFAULT 0;
ALTER TABLE reservations DROP COLUMN payout;
ALTER TABLE reservations ALTER COLUMN payout_amount DROP DEFAULT;
