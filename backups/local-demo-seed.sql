-- Idempotent demo data for the existing gms schema (MySQL 8.0+).
-- No permanent tables or columns are added. Fixed dates are anchored to 2026-10-07
-- so rerunning this file does not shift history or duplicate attendance rows.
USE gms;

INSERT IGNORE INTO status (status_id,status_status) VALUES (1,1),(2,0);
INSERT IGNORE INTO membership (membership_type_id,membership_type,price)
VALUES (1,'DAYPASS',500),(2,'MONTHLY',3000),(3,'ANNUAL',30000);

-- Preserve the pre-existing admin credentials and ID.
INSERT IGNORE INTO admin (name,mobile,username,password,status_status_id)
VALUES ('admin','0770000001','admin','MIGRATED',1);

CREATE TEMPORARY TABLE demo_members (
 mobile CHAR(10) PRIMARY KEY, name VARCHAR(45) NOT NULL, joined_days INT NOT NULL,
 membership_type ENUM('DAYPASS','MONTHLY','ANNUAL') NOT NULL,
 status_id INT NOT NULL, paid TINYINT NOT NULL,
 height_cm DECIMAL(5,2) NOT NULL, weight_kg DECIMAL(5,2) NOT NULL,
 body_fat DECIMAL(4,2) NOT NULL
) ENGINE=MEMORY;
INSERT INTO demo_members VALUES
('9876501002','user',110,'MONTHLY',1,1,174,72.4,18.5),
('9876501003','Shruti Patil',180,'ANNUAL',1,1,163,59.8,24.0),
('9876501004','Tanisha Kulkarni',45,'MONTHLY',1,1,160,56.2,25.0),
('9876501005','Atharva Joshi',35,'DAYPASS',1,1,178,76.5,19.0),
('9876501006','Tasbiya Shaikh',26,'MONTHLY',1,0,158,61.0,28.0),
('9876501007','Pradynesh Pawar',200,'ANNUAL',1,1,172,70.2,20.0),
('9876501008','Aarav Mehta',220,'MONTHLY',2,0,180,82.5,24.5),
('9876501009','Ananya Iyer',75,'MONTHLY',1,1,165,57.4,23.0),
('9876501010','Rohan Gupta',20,'DAYPASS',1,1,176,74.0,18.0),
('9876501011','Kavya Nair',270,'ANNUAL',1,1,162,58.1,23.5),
('9876501012','Ishaan Shah',15,'MONTHLY',1,0,169,67.8,21.0),
('9876501013','Meera Rao',97,'MONTHLY',1,1,157,54.6,24.0),
('9876501014','Aditya Menon',17,'DAYPASS',1,1,181,84.0,23.5),
('9876501015','Aditi Verma',300,'ANNUAL',2,0,164,62.7,27.0),
('9876501016','Vihaan Kapoor',52,'MONTHLY',1,1,175,73.2,19.5),
('9876501017','Saanvi Reddy',130,'MONTHLY',1,1,159,55.8,24.0),
('9876501018','Kabir Chawla',10,'DAYPASS',1,1,177,79.3,22.0),
('9876501019','Diya Banerjee',61,'MONTHLY',1,1,161,60.4,26.0),
('9876501020','Arjun Sinha',110,'ANNUAL',1,1,183,86.0,24.0),
('9876501021','Neha Bhosale',150,'MONTHLY',2,0,156,58.2,27.5),
('9876501022','Siddharth Jain',27,'MONTHLY',1,1,171,69.6,20.5),
('9876501023','Pooja Pillai',7,'DAYPASS',1,1,166,63.0,25.5),
('9876501024','Omkar Jadhav',83,'ANNUAL',1,1,179,77.1,21.5),
('9876501025','Fatima Khan',240,'MONTHLY',2,0,160,64.0,29.0);

-- Upgrade the original one-row demo member in place so its dependent records survive.
UPDATE member SET name='Niel Mandhare',mobile='9876501001',
 joined_date=DATE_SUB('2026-10-07',INTERVAL 173 DAY),
 health_info='General fitness and strength training goals.',payment_status=1,
 membership_type_membership_type_id=2,status_status_id=1
WHERE member_id=1;
INSERT INTO member (name,mobile,joined_date,health_info,payment_status,membership_type_membership_type_id,status_status_id)
SELECT p.name,p.mobile,DATE_SUB('2026-10-07',INTERVAL p.joined_days DAY),
 'General fitness goals; no reported limitations.',p.paid,m.membership_type_id,p.status_id
FROM demo_members p JOIN membership m ON m.membership_type=p.membership_type
ON DUPLICATE KEY UPDATE name=VALUES(name),joined_date=VALUES(joined_date),
 health_info=VALUES(health_info),payment_status=VALUES(payment_status),
 membership_type_membership_type_id=VALUES(membership_type_membership_type_id),
 status_status_id=VALUES(status_status_id);

CREATE TEMPORARY TABLE demo_trainers (
 name VARCHAR(45) PRIMARY KEY,mobile CHAR(10) NOT NULL,nic CHAR(12) NOT NULL,
 hired_days INT NOT NULL,status_id INT NOT NULL,profile_key VARCHAR(45) NOT NULL
) ENGINE=MEMORY;
INSERT INTO demo_trainers VALUES
('Shruti Patil','9876512002','990000000002',420,1,'trainer_profile_shruti'),
('Tanisha Kulkarni','9876512003','990000000003',310,1,'trainer_profile_tanisha'),
('Atharva Joshi','9876512004','990000000004',250,1,'trainer_profile_atharva'),
('Tasbiya Shaikh','9876512005','990000000005',170,1,'trainer_profile_tasbiya'),
('Pradynesh Pawar','9876512006','990000000006',95,2,'trainer_profile_pradynesh');
-- Trainer username/password columns are required by this schema; PROFILE_ONLY is not an app login.
UPDATE trainer SET name='trainer',mobile='9876512001',nic='990000000001',
 hired_date=DATE_SUB('2026-10-07',INTERVAL 520 DAY),username='trainer_profile_niel',
 password='PROFILE_ONLY',status_status_id=1 WHERE trainer_id=1;
INSERT INTO trainer (name,mobile,nic,hired_date,username,password,status_status_id)
SELECT name,mobile,nic,DATE_SUB('2026-10-07',INTERVAL hired_days DAY),profile_key,'PROFILE_ONLY',status_id
FROM demo_trainers
ON DUPLICATE KEY UPDATE name=VALUES(name),nic=VALUES(nic),hired_date=VALUES(hired_date),
 password=VALUES(password),status_status_id=VALUES(status_status_id);

-- The schema has no demographic/contact, expiry-date, quantity, or specialty columns.
-- Keep health_info to general fitness notes and use only the fields that actually exist.

CREATE TEMPORARY TABLE demo_products (
 name VARCHAR(100) PRIMARY KEY,price DECIMAL(10,2) NOT NULL,stock INT NOT NULL,description VARCHAR(255) NOT NULL
) ENGINE=MEMORY;
INSERT INTO demo_products VALUES
('Whey Protein Bar - Chocolate 60 g',180,34,'Single-serving chocolate protein snack.'),
('Electrolyte Drink - Lime 500 ml',90,42,'Lime sports hydration drink.'),
('Stainless Steel Shaker Bottle 700 ml',500,18,'Leak-resistant shaker with mixing ball.'),
('Training Gloves - Medium',650,12,'Padded palm training gloves.'),
('Resistance Band - Medium',350,21,'Medium resistance latex training band.'),
('Energy Drink - Original 250 ml',120,30,'Caffeinated energy drink, 250 ml can.'),
('Gym Towel - Cotton',280,25,'Quick-dry cotton training towel.'),
('Whey Protein Sachet - Vanilla 30 g',150,28,'Single-serve vanilla whey sachet.');
-- Replace the starter placeholder product while preserving its ID and any sales reference.
UPDATE product SET name='Stainless Steel Shaker Bottle 700 ml',price=500,stock=18,
 description='Leak-resistant shaker with mixing ball.' WHERE product_id=1;
INSERT INTO product (name,price,stock,description)
SELECT p.name,p.price,p.stock,p.description FROM demo_products p
WHERE NOT EXISTS (SELECT 1 FROM product x WHERE x.name=p.name);

CREATE TEMPORARY TABLE demo_equipment (
 name VARCHAR(45) PRIMARY KEY,purchase_days INT NOT NULL,status ENUM('GOOD','UNDER MAINTENANCE','DAMAGED','RETIRED') NOT NULL,
 serial_number VARCHAR(40) NOT NULL,warranty_years INT NOT NULL
) ENGINE=MEMORY;
INSERT INTO demo_equipment VALUES
('Commercial Treadmill',420,'GOOD','GMS-EQ-1002',2),
('Olympic Bench Press',390,'GOOD','GMS-EQ-1003',2),
('Hex Dumbbell Rack 2.5-25 kg',365,'GOOD','GMS-EQ-1004',1),
('Olympic Barbell 20 kg',330,'GOOD','GMS-EQ-1005',1),
('Power Squat Rack',310,'UNDER MAINTENANCE','GMS-EQ-1006',2),
('Upright Exercise Bike',280,'GOOD','GMS-EQ-1007',2),
('Dual Cable Crossover Machine',255,'GOOD','GMS-EQ-1008',2),
('45 Degree Leg Press',230,'GOOD','GMS-EQ-1009',2),
('Lat Pulldown Machine',215,'GOOD','GMS-EQ-1010',2),
('Smith Machine',195,'GOOD','GMS-EQ-1011',2),
('Commercial Rowing Machine',130,'DAMAGED','GMS-EQ-1012',1);
UPDATE equipment SET name='Commercial Treadmill',purchase_date=DATE_SUB('2026-10-07',INTERVAL 460 DAY),
 status='GOOD',serial_number='GMS-EQ-1001',warranty_exp=DATE_ADD(DATE_SUB('2026-10-07',INTERVAL 460 DAY),INTERVAL 3 YEAR)
WHERE equipment_id=1;
INSERT INTO equipment (name,purchase_date,status,serial_number,warranty_exp)
SELECT e.name,DATE_SUB('2026-10-07',INTERVAL e.purchase_days DAY),e.status,e.serial_number,
 DATE_ADD(DATE_SUB('2026-10-07',INTERVAL e.purchase_days DAY),INTERVAL e.warranty_years YEAR)
FROM demo_equipment e
WHERE NOT EXISTS (SELECT 1 FROM equipment x WHERE x.serial_number=e.serial_number);

CREATE TEMPORARY TABLE demo_maintenance (
 serial_number VARCHAR(40) PRIMARY KEY,description VARCHAR(255) NOT NULL,days_ago INT NOT NULL,
 cost DECIMAL(10,2) NOT NULL,maintained_by VARCHAR(45) NOT NULL
) ENGINE=MEMORY;
INSERT INTO demo_maintenance VALUES
('GMS-EQ-1001','Quarterly belt alignment and safety inspection',12,1850,'Rakesh Electricals'),
('GMS-EQ-1002','Lubricated deck and calibrated incline motor',28,3200,'Rakesh Electricals'),
('GMS-EQ-1003','Replaced bench pad and checked frame bolts',43,2400,'Vijay Fitness Service'),
('GMS-EQ-1006','Replaced rack safety catches; inspection pending',5,1250,'Vijay Fitness Service'),
('GMS-EQ-1008','Inspected cable pulleys and replaced one cable',36,4600,'Vijay Fitness Service'),
('GMS-EQ-1009','Greased leg press rails and checked foot plate',58,1750,'Vijay Fitness Service'),
('GMS-EQ-1012','Motor fault inspection; unit tagged out of service',8,900,'Rakesh Electricals');
INSERT INTO maintenance_log (equipment_equipment_id,description,date,cost,maintained_by)
SELECT e.equipment_id,x.description,DATE_ADD(DATE_SUB('2026-10-07',INTERVAL x.days_ago DAY),INTERVAL 9 HOUR),x.cost,x.maintained_by
FROM demo_maintenance x JOIN equipment e ON e.serial_number=x.serial_number
WHERE NOT EXISTS (SELECT 1 FROM maintenance_log l WHERE l.equipment_equipment_id=e.equipment_id AND l.description=x.description);

-- Historical member progress: three plausible measurements with gradual change.
UPDATE progress_track SET date=DATE_SUB('2026-10-07',INTERVAL 90 DAY),height_cm=175,
 weight_kg=71.2,body_fat=18.8,bmi=23.25
WHERE progress_id=1 AND member_member_id=1;
INSERT INTO progress_track (date,height_cm,weight_kg,body_fat,bmi,member_member_id)
SELECT DATE_SUB('2026-10-07',INTERVAL r.days_ago DAY),r.height_cm,r.weight_kg,r.body_fat,
 ROUND(r.weight_kg/POW(r.height_cm/100,2),2),m.member_id
FROM (
 SELECT '9876501001' mobile,175 height_cm,70.6 weight_kg,18.4 body_fat,60 days_ago UNION ALL
 SELECT '9876501001',175,70.0,18.0,30 UNION ALL
 SELECT '9876501002',174,73.6,19.1,90 UNION ALL SELECT '9876501002',174,73.0,18.8,60 UNION ALL SELECT '9876501002',174,72.4,18.5,30 UNION ALL
 SELECT '9876501003',163,60.2,24.2,90 UNION ALL SELECT '9876501003',163,60.0,24.1,60 UNION ALL SELECT '9876501003',163,59.8,24.0,30 UNION ALL
 SELECT '9876501004',160,57.0,25.4,90 UNION ALL SELECT '9876501004',160,56.6,25.2,60 UNION ALL SELECT '9876501004',160,56.2,25.0,30 UNION ALL
 SELECT '9876501005',178,77.1,19.2,90 UNION ALL SELECT '9876501005',178,76.8,19.1,60 UNION ALL SELECT '9876501005',178,76.5,19.0,30 UNION ALL
 SELECT '9876501006',158,61.4,28.2,90 UNION ALL SELECT '9876501006',158,61.2,28.1,60 UNION ALL SELECT '9876501006',158,61.0,28.0,30 UNION ALL
 SELECT '9876501007',172,71.0,20.3,90 UNION ALL SELECT '9876501007',172,70.6,20.2,60 UNION ALL SELECT '9876501007',172,70.2,20.0,30 UNION ALL
 SELECT '9876501008',180,82.5,24.5,90 UNION ALL SELECT '9876501008',180,82.5,24.5,60 UNION ALL SELECT '9876501008',180,82.5,24.5,30 UNION ALL
 SELECT '9876501009',165,58.0,23.4,90 UNION ALL SELECT '9876501009',165,57.7,23.2,60 UNION ALL SELECT '9876501009',165,57.4,23.0,30 UNION ALL
 SELECT '9876501010',176,74.4,18.2,90 UNION ALL SELECT '9876501010',176,74.2,18.1,60 UNION ALL SELECT '9876501010',176,74.0,18.0,30 UNION ALL
 SELECT '9876501011',162,58.4,23.7,90 UNION ALL SELECT '9876501011',162,58.2,23.6,60 UNION ALL SELECT '9876501011',162,58.1,23.5,30 UNION ALL
 SELECT '9876501012',169,67.8,21.0,90 UNION ALL SELECT '9876501012',169,67.8,21.0,60 UNION ALL SELECT '9876501012',169,67.8,21.0,30 UNION ALL
 SELECT '9876501013',157,55.2,24.4,90 UNION ALL SELECT '9876501013',157,54.9,24.2,60 UNION ALL SELECT '9876501013',157,54.6,24.0,30 UNION ALL
 SELECT '9876501014',181,84.2,23.6,90 UNION ALL SELECT '9876501014',181,84.1,23.6,60 UNION ALL SELECT '9876501014',181,84.0,23.5,30 UNION ALL
 SELECT '9876501015',164,62.7,27.0,90 UNION ALL SELECT '9876501015',164,62.7,27.0,60 UNION ALL SELECT '9876501015',164,62.7,27.0,30 UNION ALL
 SELECT '9876501016',175,74.0,19.8,90 UNION ALL SELECT '9876501016',175,73.6,19.7,60 UNION ALL SELECT '9876501016',175,73.2,19.5,30 UNION ALL
 SELECT '9876501017',159,56.4,24.3,90 UNION ALL SELECT '9876501017',159,56.1,24.2,60 UNION ALL SELECT '9876501017',159,55.8,24.0,30 UNION ALL
 SELECT '9876501018',177,79.3,22.0,90 UNION ALL SELECT '9876501018',177,79.3,22.0,60 UNION ALL SELECT '9876501018',177,79.3,22.0,30 UNION ALL
 SELECT '9876501019',161,61.0,26.2,90 UNION ALL SELECT '9876501019',161,60.7,26.1,60 UNION ALL SELECT '9876501019',161,60.4,26.0,30 UNION ALL
 SELECT '9876501020',183,86.6,24.3,90 UNION ALL SELECT '9876501020',183,86.3,24.2,60 UNION ALL SELECT '9876501020',183,86.0,24.0,30 UNION ALL
 SELECT '9876501021',156,58.2,27.5,90 UNION ALL SELECT '9876501021',156,58.2,27.5,60 UNION ALL SELECT '9876501021',156,58.2,27.5,30 UNION ALL
 SELECT '9876501022',171,70.0,20.7,90 UNION ALL SELECT '9876501022',171,69.8,20.6,60 UNION ALL SELECT '9876501022',171,69.6,20.5,30 UNION ALL
 SELECT '9876501023',166,63.0,25.5,90 UNION ALL SELECT '9876501023',166,63.0,25.5,60 UNION ALL SELECT '9876501023',166,63.0,25.5,30 UNION ALL
 SELECT '9876501024',179,77.7,21.8,90 UNION ALL SELECT '9876501024',179,77.4,21.7,60 UNION ALL SELECT '9876501024',179,77.1,21.5,30 UNION ALL
 SELECT '9876501025',160,64.0,29.0,90 UNION ALL SELECT '9876501025',160,64.0,29.0,60 UNION ALL SELECT '9876501025',160,64.0,29.0,30
) r JOIN member m ON m.mobile=r.mobile
WHERE NOT EXISTS (SELECT 1 FROM progress_track p WHERE p.member_member_id=m.member_id AND p.date=DATE_SUB('2026-10-07',INTERVAL r.days_ago DAY));

-- Keep the member payment flag in step with the demo payment history.
UPDATE member m JOIN demo_members d ON d.mobile=m.mobile SET m.payment_status=d.paid;
UPDATE payment_history p JOIN member m ON m.member_id=p.member_member_id
SET p.date='2026-10-01 10:15:00',p.paid_amount=3000,p.paid_method='CARD',p.reference_no='GMS-DEMO-PAY-1001'
WHERE p.reference_no IN ('DEMO-PAY-001','GMS-DEMO-PAY-1001') AND m.mobile='9876501001';
INSERT INTO payment_history (member_member_id,date,paid_amount,paid_method,reference_no)
SELECT m.member_id,
 DATE_ADD(DATE_SUB('2026-10-07',INTERVAL LEAST(GREATEST(d.joined_days-1,1),CASE MOD(CAST(RIGHT(d.mobile,1) AS UNSIGNED),5) WHEN 0 THEN 2 WHEN 1 THEN 5 WHEN 2 THEN 9 WHEN 3 THEN 12 ELSE 23 END) DAY),INTERVAL 10 HOUR),
 ms.price,ELT(1+MOD(CAST(RIGHT(d.mobile,1) AS UNSIGNED),4),'CASH','CARD','ONLINE TRANSFER','OTHER'),
 CONCAT('GMS-DEMO-PAY-',RIGHT(d.mobile,4))
FROM demo_members d JOIN member m ON m.mobile=d.mobile
JOIN membership ms ON ms.membership_type=m.membership_type_membership_type_id
WHERE d.paid=1 AND NOT EXISTS(SELECT 1 FROM payment_history p WHERE p.reference_no=CONCAT('GMS-DEMO-PAY-',RIGHT(d.mobile,4)));

UPDATE order_history SET date='2026-09-22 12:30:00',quantity=2,unit_price=250,total_amount=500,
 paid_method='CASH',reference_no='GMS-DEMO-SALE-001'
WHERE reference_no='DEMO-ORDER-001';
CREATE TEMPORARY TABLE demo_sales (
 sale_no INT PRIMARY KEY,product_name VARCHAR(100) NOT NULL,days_ago INT NOT NULL,quantity INT NOT NULL,method ENUM('CASH','CARD','ONLINE TRANSFER','OTHER') NOT NULL
) ENGINE=MEMORY;
INSERT INTO demo_sales VALUES
(2,'Whey Protein Bar - Chocolate 60 g',3,2,'CASH'),(3,'Electrolyte Drink - Lime 500 ml',4,1,'ONLINE TRANSFER'),
(4,'Stainless Steel Shaker Bottle 700 ml',5,1,'CARD'),(5,'Training Gloves - Medium',7,1,'CARD'),
(6,'Resistance Band - Medium',9,2,'CASH'),(7,'Energy Drink - Original 250 ml',10,2,'CASH'),
(8,'Gym Towel - Cotton',12,1,'ONLINE TRANSFER'),(9,'Whey Protein Sachet - Vanilla 30 g',14,3,'CARD'),
(10,'Whey Protein Bar - Chocolate 60 g',16,1,'CASH'),(11,'Electrolyte Drink - Lime 500 ml',18,2,'CASH'),
(12,'Stainless Steel Shaker Bottle 700 ml',20,1,'ONLINE TRANSFER'),(13,'Training Gloves - Medium',22,1,'CARD'),
(14,'Resistance Band - Medium',25,1,'CASH'),(15,'Energy Drink - Original 250 ml',28,3,'CASH'),
(16,'Gym Towel - Cotton',32,2,'CARD'),(17,'Whey Protein Sachet - Vanilla 30 g',38,2,'ONLINE TRANSFER'),
(18,'Whey Protein Bar - Chocolate 60 g',45,2,'CASH'),(19,'Electrolyte Drink - Lime 500 ml',53,1,'CARD'),
(20,'Stainless Steel Shaker Bottle 700 ml',62,1,'CASH');
INSERT INTO order_history (product_product_id,date,quantity,unit_price,total_amount,paid_method,reference_no)
SELECT p.product_id,DATE_ADD(DATE_SUB('2026-10-07',INTERVAL s.days_ago DAY),INTERVAL (10+MOD(s.sale_no,8)) HOUR),
 s.quantity,p.price,p.price*s.quantity,s.method,CONCAT('GMS-DEMO-SALE-',LPAD(s.sale_no,3,'0'))
FROM demo_sales s JOIN product p ON p.name=s.product_name
WHERE NOT EXISTS(SELECT 1 FROM order_history o WHERE o.reference_no=CONCAT('GMS-DEMO-SALE-',LPAD(s.sale_no,3,'0')));

-- Attendance across the prior 90 days. Members attend at varied intervals; no member is
-- logged every day. Weekday check-in times vary with the member's demo mobile suffix.
INSERT INTO member_attendance (member_member_id,date,check_in_time)
WITH RECURSIVE days (days_ago) AS (
 SELECT 0 UNION ALL SELECT days_ago+1 FROM days WHERE days_ago<89
)
SELECT m.member_id,DATE_SUB('2026-10-07',INTERVAL d.days_ago DAY),
 ADDTIME('06:00:00',SEC_TO_TIME(MOD(CAST(RIGHT(m.mobile,2) AS UNSIGNED)*971+d.days_ago*337,43200)))
FROM member m JOIN (SELECT '9876501001' mobile,173 joined_days UNION ALL SELECT mobile,joined_days FROM demo_members) ids ON ids.mobile=m.mobile
CROSS JOIN days d
WHERE d.days_ago<ids.joined_days
 AND MOD(d.days_ago+CAST(RIGHT(m.mobile,2) AS UNSIGNED),CASE WHEN m.membership_type_membership_type_id=1 THEN 5 ELSE CASE MOD(CAST(RIGHT(m.mobile,1) AS UNSIGNED),4) WHEN 0 THEN 3 WHEN 1 THEN 5 WHEN 2 THEN 7 ELSE 10 END END)=0
 AND NOT EXISTS(SELECT 1 FROM member_attendance a WHERE a.member_member_id=m.member_id AND a.date=DATE_SUB('2026-10-07',INTERVAL d.days_ago DAY));

-- Six reusable weekly workout plans in the schema's three existing levels.
UPDATE workout_plan SET day1='Goblet squats 3x10; push-ups 3x8',day2='Brisk walk 25 min; mobility',
 day3='Dumbbell rows 3x10; glute bridges 3x12',day4='Rest or easy cycle 20 min',
 day5='Split squats 3x8; incline press 3x10',day6='Full-body stretch 20 min',day7='Rest'
WHERE plan_id=1 AND day1='Walk';
INSERT INTO workout_plan (level,day1,day2,day3,day4,day5,day6,day7)
SELECT 'BEGINNER','Chair squats 3x10; wall push-ups 3x10','Walk 20 min','Band rows 3x12; hip hinges 3x10','Rest','Step-ups 3x8 each side; dead bug 3x8','Easy cycle 15 min','Rest'
WHERE NOT EXISTS(SELECT 1 FROM workout_plan WHERE day1='Chair squats 3x10; wall push-ups 3x10');
INSERT INTO workout_plan (level,day1,day2,day3,day4,day5,day6,day7)
SELECT 'INTERMEDIATE','Squat 4x8; bench press 4x8','Row 25 min; core','Deadlift 3x6; split squat 3x8','Rest','Overhead press 4x8; pulldown 4x10','Easy cycle 25 min','Rest'
WHERE NOT EXISTS(SELECT 1 FROM workout_plan WHERE day1='Squat 4x8; bench press 4x8');
INSERT INTO workout_plan (level,day1,day2,day3,day4,day5,day6,day7)
SELECT 'INTERMEDIATE','Incline walk 30 min; mobility','Goblet squat 3x12; cable row 3x12','Cycle intervals 6x1 min','Rest','Leg press 3x10; chest press 3x10','Walk 35 min','Rest'
WHERE NOT EXISTS(SELECT 1 FROM workout_plan WHERE day1='Incline walk 30 min; mobility');
INSERT INTO workout_plan (level,day1,day2,day3,day4,day5,day6,day7)
SELECT 'ADVANCED','Squat 5x5; bench press 5x5','Recovery walk 30 min','Deadlift 4x4; pull-up 4x6','Rest','Front squat 4x6; overhead press 4x6','Row intervals 5x500 m','Rest'
WHERE NOT EXISTS(SELECT 1 FROM workout_plan WHERE day1='Squat 5x5; bench press 5x5');
INSERT INTO workout_plan (level,day1,day2,day3,day4,day5,day6,day7)
SELECT 'ADVANCED','Bike intervals 8x90 sec','Bench press 4x6; row 4x8','Easy cycle 25 min','Squat 4x6; Romanian deadlift 3x8','Conditioning circuit 4 rounds','Mobility 25 min','Rest'
WHERE NOT EXISTS(SELECT 1 FROM workout_plan WHERE day1='Bike intervals 8x90 sec');

-- Trainer shifts and personal training use only existing, valid trainer/member references.
INSERT INTO trainer_attendance (trainer_trainer_id,date,check_in_time,check_out_time)
WITH RECURSIVE days (days_ago) AS (
 SELECT 0 UNION ALL SELECT days_ago+1 FROM days WHERE days_ago<44
)
SELECT t.trainer_id,DATE_SUB('2026-10-07',INTERVAL d.days_ago DAY),
 MAKETIME(6+MOD(t.trainer_id,3),MOD(t.trainer_id*7+d.days_ago,60),0),
 MAKETIME(14+MOD(t.trainer_id,4),MOD(t.trainer_id*11+d.days_ago,60),0)
FROM trainer t CROSS JOIN days d
WHERE t.username LIKE 'trainer_profile_%' AND DAYOFWEEK(DATE_SUB('2026-10-07',INTERVAL d.days_ago DAY)) BETWEEN 2 AND 6
 AND MOD(d.days_ago+t.trainer_id,3)=0
 AND NOT EXISTS(SELECT 1 FROM trainer_attendance a WHERE a.trainer_trainer_id=t.trainer_id AND a.date=DATE_SUB('2026-10-07',INTERVAL d.days_ago DAY));

CREATE TEMPORARY TABLE demo_training (
 mobile CHAR(10) PRIMARY KEY,trainer_key VARCHAR(45) NOT NULL,start_days INT NOT NULL,
 session_count INT NOT NULL,completed INT NOT NULL,status ENUM('ACTIVE','COMPLETED','CANCELLED') NOT NULL
) ENGINE=MEMORY;
INSERT INTO demo_training VALUES
('9876501002','trainer_profile_niel',52,12,8,'ACTIVE'),
('9876501003','trainer_profile_shruti',75,16,16,'COMPLETED'),
('9876501004','trainer_profile_tanisha',24,12,5,'ACTIVE'),
('9876501006','trainer_profile_atharva',19,10,4,'ACTIVE'),
('9876501007','trainer_profile_tasbiya',120,16,16,'COMPLETED'),
('9876501009','trainer_profile_pradynesh',41,12,7,'ACTIVE'),
('9876501016','trainer_profile_niel',33,12,6,'ACTIVE'),
('9876501020','trainer_profile_atharva',82,16,16,'COMPLETED');
INSERT INTO personal_training (member_member_id,trainer_trainer_id,start_date,end_date,session_price,total_sessions,sessions_completed,status)
SELECT m.member_id,t.trainer_id,DATE_SUB('2026-10-07',INTERVAL x.start_days DAY),
 IF(x.status='COMPLETED',DATE_SUB('2026-10-07',INTERVAL (x.start_days-28) DAY),NULL),1500,x.session_count,x.completed,x.status
FROM demo_training x JOIN member m ON m.mobile=x.mobile JOIN trainer t ON t.username=x.trainer_key
WHERE NOT EXISTS(SELECT 1 FROM personal_training pt WHERE pt.member_member_id=m.member_id
 AND pt.trainer_trainer_id=t.trainer_id AND pt.start_date=DATE_SUB('2026-10-07',INTERVAL x.start_days DAY));

DROP TEMPORARY TABLE demo_training;
DROP TEMPORARY TABLE demo_maintenance;
DROP TEMPORARY TABLE demo_equipment;
DROP TEMPORARY TABLE demo_sales;
DROP TEMPORARY TABLE demo_products;
DROP TEMPORARY TABLE demo_trainers;
DROP TEMPORARY TABLE demo_members;
