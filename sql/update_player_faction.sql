﻿USE snowisland;

UPDATE player SET faction = '天灾使者' WHERE faction = '天灾使者';

ALTER TABLE player MODIFY COLUMN faction ENUM('统治者', '反叛者', '冒险者', '天灾使者', '平民') NOT NULL;

SELECT id, name, faction FROM player;