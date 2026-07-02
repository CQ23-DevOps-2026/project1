DELETE FROM order_item
WHERE order_id IN (
    SELECT id
    FROM (
        SELECT id,
               ROW_NUMBER() OVER (PARTITION BY checkout_id ORDER BY id DESC) AS row_number
        FROM "order"
        WHERE checkout_id IS NOT NULL
    ) ranked_orders
    WHERE row_number > 1
);

DELETE FROM "order"
WHERE id IN (
    SELECT id
    FROM (
        SELECT id,
               ROW_NUMBER() OVER (PARTITION BY checkout_id ORDER BY id DESC) AS row_number
        FROM "order"
        WHERE checkout_id IS NOT NULL
    ) ranked_orders
    WHERE row_number > 1
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_order_checkout_id
ON "order" (checkout_id)
WHERE checkout_id IS NOT NULL;
