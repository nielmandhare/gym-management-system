-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: gms
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
  `admin_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `mobile` varchar(10) NOT NULL,
  `username` varchar(45) NOT NULL,
  `password` varchar(45) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `status_status_id` int NOT NULL,
  PRIMARY KEY (`admin_id`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  KEY `fk_admin_status_idx` (`status_status_id`),
  CONSTRAINT `fk_admin_status` FOREIGN KEY (`status_status_id`) REFERENCES `status` (`status_id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (20,'Local Admin','0770000001','admin','1234','2026-10-07 05:59:09',1);
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `equipment`
--

DROP TABLE IF EXISTS `equipment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `equipment` (
  `equipment_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `purchase_date` date DEFAULT NULL,
  `status` enum('GOOD','UNDER MAINTENANCE','DAMAGED','RETIRED') CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `serial_number` varchar(255) DEFAULT NULL,
  `warranty_exp` date DEFAULT NULL,
  PRIMARY KEY (`equipment_id`),
  UNIQUE KEY `serial_number_UNIQUE` (`serial_number`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `equipment`
--

LOCK TABLES `equipment` WRITE;
/*!40000 ALTER TABLE `equipment` DISABLE KEYS */;
INSERT INTO `equipment` VALUES (1,'Demo Treadmill','2026-10-07','GOOD','DEMO-EQ-001','2027-10-07');
/*!40000 ALTER TABLE `equipment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `maintenance_log`
--

DROP TABLE IF EXISTS `maintenance_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `maintenance_log` (
  `log_id` int NOT NULL AUTO_INCREMENT,
  `equipment_equipment_id` int NOT NULL,
  `description` text NOT NULL,
  `date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `cost` decimal(10,2) NOT NULL,
  `maintained_by` varchar(255) NOT NULL,
  PRIMARY KEY (`log_id`),
  KEY `fk_maintenance_log_equipment1_idx` (`equipment_equipment_id`),
  CONSTRAINT `fk_maintenance_log_equipment1` FOREIGN KEY (`equipment_equipment_id`) REFERENCES `equipment` (`equipment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `maintenance_log`
--

LOCK TABLES `maintenance_log` WRITE;
/*!40000 ALTER TABLE `maintenance_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `maintenance_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `member`
--

DROP TABLE IF EXISTS `member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member` (
  `member_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `mobile` varchar(10) NOT NULL,
  `joined_date` datetime NOT NULL DEFAULT (now()),
  `health_info` text,
  `payment_status` tinyint NOT NULL DEFAULT '0',
  `membership_type_membership_type_id` int NOT NULL DEFAULT '2',
  `status_status_id` int NOT NULL DEFAULT '1',
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `mobile_UNIQUE` (`mobile`),
  KEY `fk_member_status1_idx` (`status_status_id`),
  KEY `fk_member_membership_type1_idx` (`membership_type_membership_type_id`),
  CONSTRAINT `fk_member_membership_type1` FOREIGN KEY (`membership_type_membership_type_id`) REFERENCES `membership` (`membership_type_id`),
  CONSTRAINT `fk_member_status1` FOREIGN KEY (`status_status_id`) REFERENCES `status` (`status_id`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member`
--

LOCK TABLES `member` WRITE;
/*!40000 ALTER TABLE `member` DISABLE KEYS */;
INSERT INTO `member` VALUES (1,'Demo Member','0770000002','2026-10-07 11:29:09','Development sample',1,2,1);
/*!40000 ALTER TABLE `member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `member_attendance`
--

DROP TABLE IF EXISTS `member_attendance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member_attendance` (
  `attendance_id` int NOT NULL AUTO_INCREMENT,
  `member_member_id` int NOT NULL,
  `date` date NOT NULL DEFAULT (curdate()),
  `check_in_time` time NOT NULL DEFAULT (curtime()),
  PRIMARY KEY (`attendance_id`),
  KEY `fk_member_attendance_member1_idx` (`member_member_id`),
  CONSTRAINT `fk_member_attendance_member1` FOREIGN KEY (`member_member_id`) REFERENCES `member` (`member_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=81 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member_attendance`
--

LOCK TABLES `member_attendance` WRITE;
/*!40000 ALTER TABLE `member_attendance` DISABLE KEYS */;
/*!40000 ALTER TABLE `member_attendance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `membership`
--

DROP TABLE IF EXISTS `membership`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `membership` (
  `membership_type_id` int NOT NULL AUTO_INCREMENT,
  `membership_type` enum('DAYPASS','MONTHLY','ANNUAL') NOT NULL,
  `price` decimal(10,2) NOT NULL,
  PRIMARY KEY (`membership_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `membership`
--

LOCK TABLES `membership` WRITE;
/*!40000 ALTER TABLE `membership` DISABLE KEYS */;
INSERT INTO `membership` VALUES (1,'DAYPASS',500.00),(2,'MONTHLY',3000.00),(3,'ANNUAL',30000.00);
/*!40000 ALTER TABLE `membership` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_history`
--

DROP TABLE IF EXISTS `order_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_history` (
  `order_id` int NOT NULL AUTO_INCREMENT,
  `product_product_id` int NOT NULL,
  `date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `quantity` int NOT NULL DEFAULT '1',
  `unit_price` decimal(10,2) NOT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  `paid_method` enum('CASH','CARD','ONLINE TRANSFER','OTHER') NOT NULL,
  `reference_no` varchar(100) NOT NULL,
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `reference_no_UNIQUE` (`reference_no`),
  KEY `fk_order_history_product1_idx` (`product_product_id`),
  CONSTRAINT `fk_order_history_product1` FOREIGN KEY (`product_product_id`) REFERENCES `product` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_history`
--

LOCK TABLES `order_history` WRITE;
/*!40000 ALTER TABLE `order_history` DISABLE KEYS */;
INSERT INTO `order_history` VALUES (20,1,'2026-10-07 11:29:09',1,500.00,500.00,'CASH','DEMO-ORDER-001');
/*!40000 ALTER TABLE `order_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_history`
--

DROP TABLE IF EXISTS `payment_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_history` (
  `payment_id` int NOT NULL AUTO_INCREMENT,
  `member_member_id` int NOT NULL,
  `date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `paid_amount` decimal(10,2) NOT NULL,
  `paid_method` enum('CASH','CARD','ONLINE TRANSFER','OTHER') NOT NULL,
  `reference_no` varchar(100) NOT NULL,
  PRIMARY KEY (`payment_id`),
  UNIQUE KEY `reference_no_UNIQUE` (`reference_no`),
  KEY `fk_payment_history_member1_idx` (`member_member_id`),
  CONSTRAINT `fk_payment_history_member1` FOREIGN KEY (`member_member_id`) REFERENCES `member` (`member_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_history`
--

LOCK TABLES `payment_history` WRITE;
/*!40000 ALTER TABLE `payment_history` DISABLE KEYS */;
INSERT INTO `payment_history` VALUES (20,1,'2026-10-07 11:29:09',3000.00,'CASH','DEMO-PAY-001');
/*!40000 ALTER TABLE `payment_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `personal_training`
--

DROP TABLE IF EXISTS `personal_training`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `personal_training` (
  `pt_id` int NOT NULL AUTO_INCREMENT,
  `member_member_id` int NOT NULL,
  `trainer_trainer_id` int NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `session_price` decimal(10,2) NOT NULL,
  `total_sessions` int NOT NULL,
  `sessions_completed` int DEFAULT '0',
  `status` enum('ACTIVE','COMPLETED','CANCELLED') DEFAULT 'ACTIVE',
  PRIMARY KEY (`pt_id`),
  KEY `fk_personal_training_trainer1_idx` (`trainer_trainer_id`),
  KEY `fk_personal_training_member1_idx` (`member_member_id`),
  CONSTRAINT `fk_personal_training_member1` FOREIGN KEY (`member_member_id`) REFERENCES `member` (`member_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_personal_training_trainer1` FOREIGN KEY (`trainer_trainer_id`) REFERENCES `trainer` (`trainer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `personal_training`
--

LOCK TABLES `personal_training` WRITE;
/*!40000 ALTER TABLE `personal_training` DISABLE KEYS */;
/*!40000 ALTER TABLE `personal_training` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `product_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `stock` int NOT NULL DEFAULT '0',
  `description` text,
  PRIMARY KEY (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES (1,'Demo Water Bottle',500.00,10,'Sample inventory');
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `progress_track`
--

DROP TABLE IF EXISTS `progress_track`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `progress_track` (
  `progress_id` int NOT NULL AUTO_INCREMENT,
  `date` date NOT NULL DEFAULT (now()),
  `height_cm` decimal(5,2) DEFAULT NULL,
  `weight_kg` decimal(5,2) DEFAULT NULL,
  `body_fat` decimal(4,2) DEFAULT NULL,
  `bmi` decimal(4,2) DEFAULT NULL,
  `member_member_id` int NOT NULL,
  PRIMARY KEY (`progress_id`),
  KEY `fk_progress_track_member1_idx` (`member_member_id`),
  CONSTRAINT `fk_progress_track_member1` FOREIGN KEY (`member_member_id`) REFERENCES `member` (`member_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `progress_track`
--

LOCK TABLES `progress_track` WRITE;
/*!40000 ALTER TABLE `progress_track` DISABLE KEYS */;
INSERT INTO `progress_track` VALUES (33,'2026-10-07',175.00,70.00,18.00,22.86,1);
/*!40000 ALTER TABLE `progress_track` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `status`
--

DROP TABLE IF EXISTS `status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `status` (
  `status_id` int NOT NULL AUTO_INCREMENT,
  `status_status` tinyint NOT NULL,
  PRIMARY KEY (`status_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `status`
--

LOCK TABLES `status` WRITE;
/*!40000 ALTER TABLE `status` DISABLE KEYS */;
INSERT INTO `status` VALUES (1,1),(2,0);
/*!40000 ALTER TABLE `status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trainer`
--

DROP TABLE IF EXISTS `trainer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trainer` (
  `trainer_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `mobile` varchar(10) NOT NULL,
  `nic` varchar(12) NOT NULL,
  `hired_date` date NOT NULL,
  `username` varchar(45) NOT NULL,
  `password` varchar(45) NOT NULL,
  `status_status_id` int NOT NULL,
  PRIMARY KEY (`trainer_id`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  UNIQUE KEY `nic_UNIQUE` (`nic`),
  UNIQUE KEY `mobile` (`mobile`),
  KEY `fk_trainer_status1_idx` (`status_status_id`),
  CONSTRAINT `fk_trainer_status1` FOREIGN KEY (`status_status_id`) REFERENCES `status` (`status_id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trainer`
--

LOCK TABLES `trainer` WRITE;
/*!40000 ALTER TABLE `trainer` DISABLE KEYS */;
INSERT INTO `trainer` VALUES (1,'Demo Trainer','0770000003','200000000001','2026-10-07','demo_trainer','Demo1234!',1);
/*!40000 ALTER TABLE `trainer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trainer_attendance`
--

DROP TABLE IF EXISTS `trainer_attendance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trainer_attendance` (
  `attendance_id` int NOT NULL AUTO_INCREMENT,
  `trainer_trainer_id` int NOT NULL,
  `date` date NOT NULL,
  `check_in_time` time NOT NULL,
  `check_out_time` time NOT NULL,
  PRIMARY KEY (`attendance_id`),
  KEY `fk_trainer_attendance_trainer1_idx` (`trainer_trainer_id`),
  CONSTRAINT `fk_trainer_attendance_trainer1` FOREIGN KEY (`trainer_trainer_id`) REFERENCES `trainer` (`trainer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trainer_attendance`
--

LOCK TABLES `trainer_attendance` WRITE;
/*!40000 ALTER TABLE `trainer_attendance` DISABLE KEYS */;
/*!40000 ALTER TABLE `trainer_attendance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `workout_plan`
--

DROP TABLE IF EXISTS `workout_plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `workout_plan` (
  `plan_id` int NOT NULL AUTO_INCREMENT,
  `level` enum('BEGINNER','INTERMEDIATE','ADVANCED') CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `day1` text,
  `day2` text,
  `day3` text,
  `day4` text,
  `day5` text,
  `day6` text,
  `day7` text,
  PRIMARY KEY (`plan_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `workout_plan`
--

LOCK TABLES `workout_plan` WRITE;
/*!40000 ALTER TABLE `workout_plan` DISABLE KEYS */;
INSERT INTO `workout_plan` VALUES (16,'BEGINNER','Walk','Strength','Rest','Cardio','Strength','Stretch','Rest');
/*!40000 ALTER TABLE `workout_plan` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-07 11:38:34
