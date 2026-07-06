-- dialect: MySQL
-- noinspection SqlDialectInspection
ALTER TABLE `user`
    ADD COLUMN `deleted_at` datetime NULL COMMENT '탈퇴 처리 일시' AFTER `updated_at`;
