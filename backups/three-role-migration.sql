USE gms;
-- Incremental extension of app_user; no existing profiles or records are recreated.
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema='gms' AND table_name='app_user' AND constraint_name='app_user_chk_1'),'ALTER TABLE app_user DROP CHECK app_user_chk_1','SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @ddl = IF(NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema='gms' AND table_name='app_user' AND column_name='member_id'),'ALTER TABLE app_user ADD member_id INT NULL UNIQUE, ADD CONSTRAINT fk_app_user_member FOREIGN KEY(member_id) REFERENCES member(member_id) ON DELETE CASCADE','SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
ALTER TABLE app_user MODIFY role ENUM('ADMIN','TRAINER','MEMBER') NOT NULL;
SET @ddl = IF(NOT EXISTS(SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema='gms' AND table_name='app_user' AND constraint_name='chk_app_user_role_profile'),'ALTER TABLE app_user ADD CONSTRAINT chk_app_user_role_profile CHECK ((role=''ADMIN'' AND trainer_id IS NULL AND member_id IS NULL) OR (role=''TRAINER'' AND trainer_id IS NOT NULL AND member_id IS NULL AND admin_id IS NULL) OR (role=''MEMBER'' AND member_id IS NOT NULL AND trainer_id IS NULL AND admin_id IS NULL))','SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

