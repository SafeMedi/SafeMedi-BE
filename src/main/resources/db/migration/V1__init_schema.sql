CREATE TABLE `atc_group_master` (
    `atc_code`    varchar(20)  PRIMARY KEY COMMENT 'ATC 코드 (PK)',
    `atc_name_ko` varchar(255) COMMENT 'ATC 한글명',
    `atc_name_en` varchar(255) COMMENT 'ATC 영문명',
    `atc_level`   int          COMMENT 'ATC 분류 단계'
) COMMENT 'ATC 분류 마스터';

CREATE TABLE `ingredient_master` (
    `ingredient_code` varchar(50)  PRIMARY KEY COMMENT '성분 코드 (PK)',
    `ingredient_name` varchar(255) COMMENT '성분명'
) COMMENT '약품 성분 마스터';

CREATE TABLE `drug_master` (
    `drug_code` varchar(50)  PRIMARY KEY COMMENT '제품 코드 (PK)',
    `drug_name` varchar(255) COMMENT '제품명',
    `atc_code`  varchar(20)  COMMENT '대표 ATC 코드'
) COMMENT '의약품(제품) 마스터';

CREATE TABLE `ingredient_atc_map` (
    `id`              bigint      PRIMARY KEY AUTO_INCREMENT COMMENT 'PK',
    `ingredient_code` varchar(50) COMMENT '성분 코드',
    `atc_code`        varchar(20) COMMENT 'ATC 코드'
) COMMENT '성분-ATC 다대다 매핑 테이블';

CREATE TABLE `drug_ingredient_map` (
    `id`              bigint      PRIMARY KEY AUTO_INCREMENT COMMENT 'PK',
    `drug_code`       varchar(50) COMMENT '제품 코드',
    `ingredient_code` varchar(50) COMMENT '성분 코드'
) COMMENT '의약품-성분 다대다 매핑 테이블';

CREATE TABLE `user` (
    `id`                    bigint      PRIMARY KEY AUTO_INCREMENT COMMENT '사용자 식별자 (PK)',
    `nickname`              varchar(50) COMMENT '사용자 닉네임',
    `social_provider`       ENUM('GOOGLE') COMMENT '소셜 로그인 제공자',
    `social_id`             varchar(255) UNIQUE COMMENT '소셜 로그인 고유 식별자',
    `invite_code`           varchar(20)  COMMENT '가족 초대 코드',
    `is_tutorial_completed` boolean      DEFAULT false COMMENT '튜토리얼 완료 여부',
    `birth_date`            date        COMMENT '생년월일',
    `gender`                ENUM('MALE', 'FEMALE') COMMENT '성별',
    `height`                int         COMMENT '키(cm)',
    `weight`                int         COMMENT '몸무게(kg)',
    `blood_type`            ENUM('A', 'B', 'O', 'AB') COMMENT '혈액형(ABO)',
    `rh_type`               ENUM('PLUS', 'MINUS') DEFAULT 'PLUS' COMMENT '혈액형(Rh)',
    `diseases`              text        COMMENT '기저질환 목록',
    `device_token`          varchar(255) COMMENT '푸시 알림용 디바이스 토큰',
    `device_type`           varchar(20)  COMMENT '디바이스 OS 환경(iOS, Android 등)',
    `is_my_reminder_on`     boolean      DEFAULT true COMMENT '내 복약 알림 수신 여부',
    `is_family_reminder_on` boolean      DEFAULT true COMMENT '가족 복약 알림 수신 여부',
    `is_missed_alert_on`    boolean      DEFAULT true COMMENT '미복약 경고 알림 수신 여부',
    `created_at`            datetime     COMMENT '계정 생성 일시',
    `updated_at`            datetime     COMMENT '계정 정보 수정 일시'
) COMMENT '사용자 기본 정보';

CREATE TABLE `user_allergy` (
    `id`                  bigint       PRIMARY KEY AUTO_INCREMENT COMMENT 'PK',
    `user_id`             bigint       COMMENT '사용자 식별자',
    `allergy_type`        ENUM('INGREDIENT', 'FOOD', 'CUSTOM') COMMENT '알러지 유형(성분/음식/커스텀)',
    `allergy_value`       varchar(50)  COMMENT 'ATC 코드, 성분 코드 또는 직접 입력 값',
    `allergy_name`        varchar(255) COMMENT '화면 표시용 알러지명',
    `registered_via_drug` varchar(50)  COMMENT '원인 약물 코드(FK, 해당 약물로 인해 등록된 경우)'
) COMMENT '사용자별 알러지 정보';

CREATE TABLE `family` (
    `id`                bigint      PRIMARY KEY AUTO_INCREMENT COMMENT 'PK',
    `user_id`           bigint      COMMENT '나의 사용자 식별자',
    `connected_user_id` bigint      COMMENT '연결된 가족의 사용자 식별자',
    `relation`          varchar(50) COMMENT '가족 관계(예: 엄마, 아들 등)',
    `is_alert_consent`  boolean     DEFAULT true COMMENT '가족 알림 수신 동의 여부',
    `created_at`        datetime    COMMENT '가족 연결 생성 일시',
    `updated_at`        datetime    COMMENT '가족 변경 일시'

) COMMENT '가족 연결 정보';

CREATE TABLE `family_request` (
    `id`                bigint      PRIMARY KEY AUTO_INCREMENT COMMENT 'PK',
    `sender_id`         bigint      COMMENT '요청을 보낸 사용자 식별자',
    `receiver_id`       bigint      COMMENT '요청을 받은 사용자 식별자',
    `proposed_relation` varchar(50) COMMENT '제안한 가족 관계',
    `status`            ENUM('PENDING', 'ACCEPTED', 'REJECTED') DEFAULT 'PENDING' COMMENT '요청 상태(대기/수락/거절)',
    `created_at`      datetime    COMMENT '요청 발송 일시',
    `updated_at`        datetime    COMMENT '요청 상태 변경 일시'
) COMMENT '가족 연결 요청 내역';

CREATE TABLE `prescription` (
    `id`         bigint       PRIMARY KEY AUTO_INCREMENT COMMENT '처방전 식별자 (PK)',
    `user_id`    bigint       COMMENT '처방전 소유 사용자 식별자',
    `title`      varchar(255) COMMENT '처방전 별칭 (예: 감기약, 혈압약)',
    `start_date` date         COMMENT '복용 시작일',
    `end_date`   date         COMMENT '복용 종료일',
    `created_at` datetime     COMMENT '처방전 등록 일시',
    `updated_at` datetime     COMMENT '처방전 정보 수정 일시'
) COMMENT '처방전 기본 정보';

CREATE TABLE `prescription_drug` (
    `id`              bigint       PRIMARY KEY AUTO_INCREMENT COMMENT 'PK',
    `prescription_id` bigint       COMMENT '속한 처방전 식별자',
    `drug_name`       varchar(255) COMMENT '처방된 약품명',
    `drug_code`       varchar(50)  COMMENT '처방된 약품 코드',
    `atc_code`        varchar(20)  COMMENT '약품의 대표 ATC 코드'
) COMMENT '처방전에 포함된 약품 목록';

CREATE TABLE `prescription_time` (
    `id`              bigint PRIMARY KEY AUTO_INCREMENT COMMENT 'PK',
    `prescription_id` bigint COMMENT '속한 처방전 식별자',
    `take_time`       time   COMMENT '프론트에서 결정한 복용 시간(HH:mm)'
) COMMENT '처방전별 복용 시간 설정';

CREATE TABLE `medication_record` (
    `id`              bigint   PRIMARY KEY AUTO_INCREMENT COMMENT '복약 기록 식별자 (PK)',
    `user_id`         bigint   COMMENT '사용자 식별자',
    `prescription_id` bigint   COMMENT '대상 처방전 식별자',
    `scheduled_at`    datetime COMMENT '복용 예정 일시',
    `taken_at`        datetime COMMENT '실제 버튼 클릭(복용) 일시',
    `status`          ENUM('PENDING', 'SUCCESS', 'FAIL', 'SKIP') DEFAULT 'PENDING' COMMENT '복약 수행 상태(대기/성공/실패/스킵)'
) COMMENT '시간대별 복약 수행 기록';

CREATE TABLE `notification_log` (
    `id`         bigint       PRIMARY KEY AUTO_INCREMENT COMMENT '알림 식별자 (PK)',
    `user_id`    bigint       COMMENT '알림 수신 사용자 식별자',
    `type`       varchar(50)  COMMENT '알림 유형(복약안내, 가족요청 등)',
    `title`      varchar(255) COMMENT '알림 제목',
    `message`    text         COMMENT '알림 상세 메시지',
    `is_read`    boolean      DEFAULT false COMMENT '알림 읽음 처리 여부',
    `created_at` datetime     COMMENT '알림 생성 및 발송 일시',
    `updated_at` datetime     COMMENT '알림 상태 수정 일시'
) COMMENT '사용자별 알림 발송 및 읽음 이력';




ALTER TABLE `ingredient_atc_map`  ADD FOREIGN KEY (`ingredient_code`)   REFERENCES `ingredient_master` (`ingredient_code`);
ALTER TABLE `drug_ingredient_map` ADD FOREIGN KEY (`drug_code`)         REFERENCES `drug_master` (`drug_code`);
ALTER TABLE `drug_ingredient_map` ADD FOREIGN KEY (`ingredient_code`)   REFERENCES `ingredient_master` (`ingredient_code`);


ALTER TABLE `user_allergy`        ADD FOREIGN KEY (`user_id`)             REFERENCES `user` (`id`);
ALTER TABLE `user_allergy`        ADD FOREIGN KEY (`registered_via_drug`) REFERENCES `drug_master` (`drug_code`);


ALTER TABLE `family`              ADD FOREIGN KEY (`user_id`)             REFERENCES `user` (`id`);
ALTER TABLE `family`              ADD FOREIGN KEY (`connected_user_id`)   REFERENCES `user` (`id`);
ALTER TABLE `family_request`      ADD FOREIGN KEY (`sender_id`)           REFERENCES `user` (`id`);
ALTER TABLE `family_request`      ADD FOREIGN KEY (`receiver_id`)         REFERENCES `user` (`id`);


ALTER TABLE `prescription`        ADD FOREIGN KEY (`user_id`)             REFERENCES `user` (`id`);
ALTER TABLE `prescription_drug`   ADD FOREIGN KEY (`prescription_id`)     REFERENCES `prescription` (`id`);
ALTER TABLE `prescription_drug`   ADD FOREIGN KEY (`drug_code`)           REFERENCES `drug_master` (`drug_code`);
ALTER TABLE `prescription_time`   ADD FOREIGN KEY (`prescription_id`)     REFERENCES `prescription` (`id`);


ALTER TABLE `medication_record`   ADD FOREIGN KEY (`user_id`)             REFERENCES `user` (`id`);
ALTER TABLE `medication_record`   ADD FOREIGN KEY (`prescription_id`)     REFERENCES `prescription` (`id`);
ALTER TABLE `notification_log`    ADD FOREIGN KEY (`user_id`)             REFERENCES `user` (`id`);
