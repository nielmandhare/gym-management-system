USE gms;
-- Local coursework credentials: admin, trainer and user1; password 123.
-- Salted hashes only. Subsequent setup never resets existing passwords.
INSERT IGNORE INTO app_user(username,display_name,mobile,password_hash,role,admin_id)
SELECT 'admin','admin',mobile,'pbkdf2$600000$P0o3jgC89AM85Q1CmnwbNQ==$fED0xdivr8yFL4hg/tO4MUbMuZaGPoVuja9cUIUjElI=','ADMIN',admin_id
FROM admin WHERE username='admin';
UPDATE admin a JOIN app_user u ON u.admin_id=a.admin_id SET a.password='MIGRATED' WHERE a.username='admin';
INSERT IGNORE INTO app_user(username,display_name,mobile,password_hash,role,trainer_id)
SELECT 'trainer','trainer',mobile,'pbkdf2$600000$gdIJ1VeQps7WjWUbvlq79g==$DjexjOTqHKPO3ubnOMUZSRVHEZQbn7Ge71WASVUPCoY=','TRAINER',trainer_id
FROM trainer WHERE mobile='9876512001';
UPDATE app_user SET username='user1',display_name='user1' WHERE username='user' AND role='MEMBER';
INSERT IGNORE INTO app_user(username,display_name,mobile,password_hash,role,member_id)
SELECT 'user1','user1',mobile,'pbkdf2$600000$3RFGzUCxdBLLHAPaiVBSCA==$AOP5q0X29Dee+iQaAGNrcj4zatHgwm3Z+VkghfrS7PM=','MEMBER',member_id
FROM member WHERE mobile='9876501002';
