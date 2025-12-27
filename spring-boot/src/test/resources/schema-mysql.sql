CREATE TABLE sequences (
    name VARCHAR(32) NOT NULL PRIMARY KEY,
    currval BIGINT UNSIGNED NOT NULL DEFAULT 0
) ENGINE=InnoDB;

-- 초기 데이터 삽입
INSERT INTO sequences (name, currval) VALUES ('swimsuit-id-seq', 0);
INSERT INTO sequences (name, currval) VALUES ('swimcap-id-seq', 0);