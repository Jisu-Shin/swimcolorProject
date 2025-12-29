-- H2도 MySQL과 동일한 시퀀스 테이블 사용
CREATE TABLE IF NOT EXISTS sequences (
    name VARCHAR(32) NOT NULL PRIMARY KEY,
    currval BIGINT NOT NULL DEFAULT 0
);

-- 초기 데이터 삽입
INSERT IGNORE INTO sequences (name, currval) VALUES ('swimsuit-id-seq', 0);
INSERT IGNORE INTO sequences (name, currval) VALUES ('swimcap-id-seq', 0);