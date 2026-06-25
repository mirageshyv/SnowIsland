UPDATE ark_config SET daily_wood_limit = 50.00, daily_metal_limit = 30.00 WHERE id = 1;
SELECT id, daily_wood_limit, daily_metal_limit, daily_asphalt_limit FROM ark_config WHERE id = 1;