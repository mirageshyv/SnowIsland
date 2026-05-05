-- Run against existing DB when skill.faction is missing or blank (fixes Enum mapping errors).
USE snowisland;

-- If your table has no faction column yet, Hibernate may have added it as VARCHAR; normalize empty values:
UPDATE skill SET faction = '平民' WHERE faction IS NULL OR TRIM(faction) = '';
