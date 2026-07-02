WITH ranked_active_addresses AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY id) AS row_number
    FROM user_address
    WHERE is_active = true
)
UPDATE user_address
SET is_active = false
WHERE id IN (
    SELECT id
    FROM ranked_active_addresses
    WHERE row_number > 1
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_user_address_one_active_per_user
ON user_address (user_id)
WHERE is_active = true;
