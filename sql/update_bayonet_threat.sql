UPDATE weapon SET threat_level = 2, remark = '军用制式刺刀，长约20厘米。威胁值2。' WHERE id = 4;
SELECT id, name, threat_level, remark FROM weapon WHERE id = 4;