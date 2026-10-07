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
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb3;
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
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `equipment`
--

LOCK TABLES `equipment` WRITE;
/*!40000 ALTER TABLE `equipment` DISABLE KEYS */;
INSERT INTO `equipment` VALUES (1,'Commercial Treadmill','2025-07-04','GOOD','GMS-EQ-1001','2028-07-04'),(21,'Commercial Treadmill','2025-08-13','GOOD','GMS-EQ-1002','2027-08-13'),(22,'Olympic Bench Press','2025-09-12','GOOD','GMS-EQ-1003','2027-09-12'),(23,'Hex Dumbbell Rack 2.5-25 kg','2025-10-07','GOOD','GMS-EQ-1004','2026-10-07'),(24,'Olympic Barbell 20 kg','2025-11-11','GOOD','GMS-EQ-1005','2026-11-11'),(25,'Power Squat Rack','2025-12-01','UNDER MAINTENANCE','GMS-EQ-1006','2027-12-01'),(26,'Upright Exercise Bike','2025-12-31','GOOD','GMS-EQ-1007','2027-12-31'),(27,'Dual Cable Crossover Machine','2026-01-25','GOOD','GMS-EQ-1008','2028-01-25'),(28,'45 Degree Leg Press','2026-02-19','GOOD','GMS-EQ-1009','2028-02-19'),(29,'Lat Pulldown Machine','2026-03-06','GOOD','GMS-EQ-1010','2028-03-06'),(30,'Smith Machine','2026-03-26','GOOD','GMS-EQ-1011','2028-03-26'),(31,'Commercial Rowing Machine','2026-05-30','DAMAGED','GMS-EQ-1012','2027-05-30');
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
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `maintenance_log`
--

LOCK TABLES `maintenance_log` WRITE;
/*!40000 ALTER TABLE `maintenance_log` DISABLE KEYS */;
INSERT INTO `maintenance_log` VALUES (18,1,'Quarterly belt alignment and safety inspection','2026-09-25 09:00:00',1850.00,'Rakesh Electricals'),(19,21,'Lubricated deck and calibrated incline motor','2026-09-09 09:00:00',3200.00,'Rakesh Electricals'),(20,22,'Replaced bench pad and checked frame bolts','2026-08-25 09:00:00',2400.00,'Vijay Fitness Service'),(21,25,'Replaced rack safety catches; inspection pending','2026-10-02 09:00:00',1250.00,'Vijay Fitness Service'),(22,27,'Inspected cable pulleys and replaced one cable','2026-09-01 09:00:00',4600.00,'Vijay Fitness Service'),(23,28,'Greased leg press rails and checked foot plate','2026-08-10 09:00:00',1750.00,'Vijay Fitness Service'),(24,31,'Motor fault inspection; unit tagged out of service','2026-09-29 09:00:00',900.00,'Rakesh Electricals');
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
) ENGINE=InnoDB AUTO_INCREMENT=88 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member`
--

LOCK TABLES `member` WRITE;
/*!40000 ALTER TABLE `member` DISABLE KEYS */;
INSERT INTO `member` VALUES (1,'Niel Mandhare','9876501001','2026-04-17 00:00:00','General fitness and strength training goals.',1,2,1),(48,'Vedant Deshmukh','9876501002','2026-06-19 00:00:00','General fitness goals; no reported limitations.',1,2,1),(49,'Shruti Patil','9876501003','2026-04-10 00:00:00','General fitness goals; no reported limitations.',1,3,1),(50,'Tanisha Kulkarni','9876501004','2026-08-23 00:00:00','General fitness goals; no reported limitations.',1,2,1),(51,'Atharva Joshi','9876501005','2026-09-02 00:00:00','General fitness goals; no reported limitations.',1,1,1),(52,'Tasbiya Shaikh','9876501006','2026-09-11 00:00:00','General fitness goals; no reported limitations.',0,2,1),(53,'Pradynesh Pawar','9876501007','2026-03-21 00:00:00','General fitness goals; no reported limitations.',1,3,1),(54,'Aarav Mehta','9876501008','2026-03-01 00:00:00','General fitness goals; no reported limitations.',0,2,2),(55,'Ananya Iyer','9876501009','2026-07-24 00:00:00','General fitness goals; no reported limitations.',1,2,1),(56,'Rohan Gupta','9876501010','2026-09-17 00:00:00','General fitness goals; no reported limitations.',1,1,1),(57,'Kavya Nair','9876501011','2026-01-10 00:00:00','General fitness goals; no reported limitations.',1,3,1),(58,'Ishaan Shah','9876501012','2026-09-22 00:00:00','General fitness goals; no reported limitations.',0,2,1),(59,'Meera Rao','9876501013','2026-07-02 00:00:00','General fitness goals; no reported limitations.',1,2,1),(60,'Aditya Menon','9876501014','2026-09-20 00:00:00','General fitness goals; no reported limitations.',1,1,1),(61,'Aditi Verma','9876501015','2025-12-11 00:00:00','General fitness goals; no reported limitations.',0,3,2),(62,'Vihaan Kapoor','9876501016','2026-08-16 00:00:00','General fitness goals; no reported limitations.',1,2,1),(63,'Saanvi Reddy','9876501017','2026-05-30 00:00:00','General fitness goals; no reported limitations.',1,2,1),(64,'Kabir Chawla','9876501018','2026-09-27 00:00:00','General fitness goals; no reported limitations.',1,1,1),(65,'Diya Banerjee','9876501019','2026-08-07 00:00:00','General fitness goals; no reported limitations.',1,2,1),(66,'Arjun Sinha','9876501020','2026-06-19 00:00:00','General fitness goals; no reported limitations.',1,3,1),(67,'Neha Bhosale','9876501021','2026-05-10 00:00:00','General fitness goals; no reported limitations.',0,2,2),(68,'Siddharth Jain','9876501022','2026-09-10 00:00:00','General fitness goals; no reported limitations.',1,2,1),(69,'Pooja Pillai','9876501023','2026-09-30 00:00:00','General fitness goals; no reported limitations.',1,1,1),(70,'Omkar Jadhav','9876501024','2026-07-16 00:00:00','General fitness goals; no reported limitations.',1,3,1),(71,'Fatima Khan','9876501025','2026-02-09 00:00:00','General fitness goals; no reported limitations.',0,2,2);
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
) ENGINE=InnoDB AUTO_INCREMENT=608 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member_attendance`
--

LOCK TABLES `member_attendance` WRITE;
/*!40000 ALTER TABLE `member_attendance` DISABLE KEYS */;
INSERT INTO `member_attendance` VALUES (81,1,'2026-07-10','14:36:04'),(82,1,'2026-07-15','14:07:59'),(83,1,'2026-07-20','13:39:54'),(84,1,'2026-07-25','13:11:49'),(85,1,'2026-07-30','12:43:44'),(86,1,'2026-08-04','12:15:39'),(87,1,'2026-08-09','11:47:34'),(88,1,'2026-08-14','11:19:29'),(89,1,'2026-08-19','10:51:24'),(90,1,'2026-08-24','10:23:19'),(91,1,'2026-08-29','09:55:14'),(92,1,'2026-09-03','09:27:09'),(93,1,'2026-09-08','08:59:04'),(94,1,'2026-09-13','08:30:59'),(95,1,'2026-09-18','08:02:54'),(96,1,'2026-09-23','07:34:49'),(97,1,'2026-09-28','07:06:44'),(98,1,'2026-10-03','06:38:39'),(99,48,'2026-07-10','14:52:15'),(100,48,'2026-07-17','14:12:56'),(101,48,'2026-07-24','13:33:37'),(102,48,'2026-07-31','12:54:18'),(103,48,'2026-08-07','12:14:59'),(104,48,'2026-08-14','11:35:40'),(105,48,'2026-08-21','10:56:21'),(106,48,'2026-08-28','10:17:02'),(107,48,'2026-09-04','09:37:43'),(108,48,'2026-09-11','08:58:24'),(109,48,'2026-09-18','08:19:05'),(110,48,'2026-09-25','07:39:46'),(111,48,'2026-10-02','07:00:27'),(112,49,'2026-07-12','14:57:12'),(113,49,'2026-07-22','14:01:02'),(114,49,'2026-08-01','13:04:52'),(115,49,'2026-08-11','12:08:42'),(116,49,'2026-08-21','11:12:32'),(117,49,'2026-08-31','10:16:22'),(118,49,'2026-09-10','09:20:12'),(119,49,'2026-09-20','08:24:02'),(120,49,'2026-09-30','07:27:52'),(121,50,'2026-08-24','11:11:52'),(122,50,'2026-08-27','10:55:01'),(123,50,'2026-08-30','10:38:10'),(124,50,'2026-09-02','10:21:19'),(125,50,'2026-09-05','10:04:28'),(126,50,'2026-09-08','09:47:37'),(127,50,'2026-09-11','09:30:46'),(128,50,'2026-09-14','09:13:55'),(129,50,'2026-09-17','08:57:04'),(130,50,'2026-09-20','08:40:13'),(131,50,'2026-09-23','08:23:22'),(132,50,'2026-09-26','08:06:31'),(133,50,'2026-09-29','07:49:40'),(134,50,'2026-10-02','07:32:49'),(135,50,'2026-10-05','07:15:58'),(136,51,'2026-09-07','10:09:25'),(137,51,'2026-09-12','09:41:20'),(138,51,'2026-09-17','09:13:15'),(139,51,'2026-09-22','08:45:10'),(140,51,'2026-09-27','08:17:05'),(141,51,'2026-10-02','07:49:00'),(142,51,'2026-10-07','07:20:55'),(143,52,'2026-09-15','09:40:40'),(144,52,'2026-09-22','09:01:21'),(145,52,'2026-09-29','08:22:02'),(146,52,'2026-10-06','07:42:43'),(147,53,'2026-07-16','15:39:28'),(148,53,'2026-07-26','14:43:18'),(149,53,'2026-08-05','13:47:08'),(150,53,'2026-08-15','12:50:58'),(151,53,'2026-08-25','11:54:48'),(152,53,'2026-09-04','10:58:38'),(153,53,'2026-09-14','10:02:28'),(154,53,'2026-09-24','09:06:18'),(155,53,'2026-10-04','08:10:08'),(156,54,'2026-07-11','16:23:44'),(157,54,'2026-07-14','16:06:53'),(158,54,'2026-07-17','15:50:02'),(159,54,'2026-07-20','15:33:11'),(160,54,'2026-07-23','15:16:20'),(161,54,'2026-07-26','14:59:29'),(162,54,'2026-07-29','14:42:38'),(163,54,'2026-08-01','14:25:47'),(164,54,'2026-08-04','14:08:56'),(165,54,'2026-08-07','13:52:05'),(166,54,'2026-08-10','13:35:14'),(167,54,'2026-08-13','13:18:23'),(168,54,'2026-08-16','13:01:32'),(169,54,'2026-08-19','12:44:41'),(170,54,'2026-08-22','12:27:50'),(171,54,'2026-08-25','12:10:59'),(172,54,'2026-08-28','11:54:08'),(173,54,'2026-08-31','11:37:17'),(174,54,'2026-09-03','11:20:26'),(175,54,'2026-09-06','11:03:35'),(176,54,'2026-09-09','10:46:44'),(177,54,'2026-09-12','10:29:53'),(178,54,'2026-09-15','10:13:02'),(179,54,'2026-09-18','09:56:11'),(180,54,'2026-09-21','09:39:20'),(181,54,'2026-09-24','09:22:29'),(182,54,'2026-09-27','09:05:38'),(183,54,'2026-09-30','08:48:47'),(184,54,'2026-10-03','08:31:56'),(185,54,'2026-10-06','08:15:05'),(186,55,'2026-07-28','15:04:26'),(187,55,'2026-08-02','14:36:21'),(188,55,'2026-08-07','14:08:16'),(189,55,'2026-08-12','13:40:11'),(190,55,'2026-08-17','13:12:06'),(191,55,'2026-08-22','12:44:01'),(192,55,'2026-08-27','12:15:56'),(193,55,'2026-09-01','11:47:51'),(194,55,'2026-09-06','11:19:46'),(195,55,'2026-09-11','10:51:41'),(196,55,'2026-09-16','10:23:36'),(197,55,'2026-09-21','09:55:31'),(198,55,'2026-09-26','09:27:26'),(199,55,'2026-10-01','08:59:21'),(200,55,'2026-10-06','08:31:16'),(201,56,'2026-09-20','10:17:19'),(202,56,'2026-09-23','10:00:28'),(203,56,'2026-09-26','09:43:37'),(204,56,'2026-09-29','09:26:46'),(205,56,'2026-10-02','09:09:55'),(206,56,'2026-10-05','08:53:04'),(207,57,'2026-07-10','17:17:54'),(208,57,'2026-07-15','16:49:49'),(209,57,'2026-07-20','16:21:44'),(210,57,'2026-07-25','15:53:39'),(211,57,'2026-07-30','15:25:34'),(212,57,'2026-08-04','14:57:29'),(213,57,'2026-08-09','14:29:24'),(214,57,'2026-08-14','14:01:19'),(215,57,'2026-08-19','13:33:14'),(216,57,'2026-08-24','13:05:09'),(217,57,'2026-08-29','12:37:04'),(218,57,'2026-09-03','12:08:59'),(219,57,'2026-09-08','11:40:54'),(220,57,'2026-09-13','11:12:49'),(221,57,'2026-09-18','10:44:44'),(222,57,'2026-09-23','10:16:39'),(223,57,'2026-09-28','09:48:34'),(224,57,'2026-10-03','09:20:29'),(225,58,'2026-09-28','10:04:45'),(226,58,'2026-10-05','09:25:26'),(227,59,'2026-07-12','17:39:02'),(228,59,'2026-07-22','16:42:52'),(229,59,'2026-08-01','15:46:42'),(230,59,'2026-08-11','14:50:32'),(231,59,'2026-08-21','13:54:22'),(232,59,'2026-08-31','12:58:12'),(233,59,'2026-09-10','12:02:02'),(234,59,'2026-09-20','11:05:52'),(235,59,'2026-09-30','10:09:42'),(236,60,'2026-09-21','11:16:26'),(237,60,'2026-09-24','10:59:35'),(238,60,'2026-09-27','10:42:44'),(239,60,'2026-09-30','10:25:53'),(240,60,'2026-10-03','10:09:02'),(241,60,'2026-10-06','09:52:11'),(242,61,'2026-07-14','06:00:10'),(243,61,'2026-07-19','17:32:05'),(244,61,'2026-07-24','17:04:00'),(245,61,'2026-07-29','16:35:55'),(246,61,'2026-08-03','16:07:50'),(247,61,'2026-08-08','15:39:45'),(248,61,'2026-08-13','15:11:40'),(249,61,'2026-08-18','14:43:35'),(250,61,'2026-08-23','14:15:30'),(251,61,'2026-08-28','13:47:25'),(252,61,'2026-09-02','13:19:20'),(253,61,'2026-09-07','12:51:15'),(254,61,'2026-09-12','12:23:10'),(255,61,'2026-09-17','11:55:05'),(256,61,'2026-09-22','11:27:00'),(257,61,'2026-09-27','10:58:55'),(258,61,'2026-10-02','10:30:50'),(259,61,'2026-10-07','10:02:45'),(260,62,'2026-08-21','14:42:55'),(261,62,'2026-08-28','14:03:36'),(262,62,'2026-09-04','13:24:17'),(263,62,'2026-09-11','12:44:58'),(264,62,'2026-09-18','12:05:39'),(265,62,'2026-09-25','11:26:20'),(266,62,'2026-10-02','10:47:01'),(267,63,'2026-07-16','06:21:18'),(268,63,'2026-07-26','17:25:08'),(269,63,'2026-08-05','16:28:58'),(270,63,'2026-08-15','15:32:48'),(271,63,'2026-08-25','14:36:38'),(272,63,'2026-09-04','13:40:28'),(273,63,'2026-09-14','12:44:18'),(274,63,'2026-09-24','11:48:08'),(275,63,'2026-10-04','10:51:58'),(276,64,'2026-09-28','11:41:51'),(277,64,'2026-10-01','11:25:00'),(278,64,'2026-10-04','11:08:09'),(279,64,'2026-10-07','10:51:18'),(280,65,'2026-08-12','16:22:01'),(281,65,'2026-08-17','15:53:56'),(282,65,'2026-08-22','15:25:51'),(283,65,'2026-08-27','14:57:46'),(284,65,'2026-09-01','14:29:41'),(285,65,'2026-09-06','14:01:36'),(286,65,'2026-09-11','13:33:31'),(287,65,'2026-09-16','13:05:26'),(288,65,'2026-09-21','12:37:21'),(289,65,'2026-09-26','12:09:16'),(290,65,'2026-10-01','11:41:11'),(291,65,'2026-10-06','11:13:06'),(292,66,'2026-07-11','07:37:56'),(293,66,'2026-07-14','07:21:05'),(294,66,'2026-07-17','07:04:14'),(295,66,'2026-07-20','06:47:23'),(296,66,'2026-07-23','06:30:32'),(297,66,'2026-07-26','06:13:41'),(298,66,'2026-07-29','17:56:50'),(299,66,'2026-08-01','17:39:59'),(300,66,'2026-08-04','17:23:08'),(301,66,'2026-08-07','17:06:17'),(302,66,'2026-08-10','16:49:26'),(303,66,'2026-08-13','16:32:35'),(304,66,'2026-08-16','16:15:44'),(305,66,'2026-08-19','15:58:53'),(306,66,'2026-08-22','15:42:02'),(307,66,'2026-08-25','15:25:11'),(308,66,'2026-08-28','15:08:20'),(309,66,'2026-08-31','14:51:29'),(310,66,'2026-09-03','14:34:38'),(311,66,'2026-09-06','14:17:47'),(312,66,'2026-09-09','14:00:56'),(313,66,'2026-09-12','13:44:05'),(314,66,'2026-09-15','13:27:14'),(315,66,'2026-09-18','13:10:23'),(316,66,'2026-09-21','12:53:32'),(317,66,'2026-09-24','12:36:41'),(318,66,'2026-09-27','12:19:50'),(319,66,'2026-09-30','12:02:59'),(320,66,'2026-10-03','11:46:08'),(321,66,'2026-10-06','11:29:17'),(322,67,'2026-07-10','07:59:44'),(323,67,'2026-07-15','07:31:39'),(324,67,'2026-07-20','07:03:34'),(325,67,'2026-07-25','06:35:29'),(326,67,'2026-07-30','06:07:24'),(327,67,'2026-08-04','17:39:19'),(328,67,'2026-08-09','17:11:14'),(329,67,'2026-08-14','16:43:09'),(330,67,'2026-08-19','16:15:04'),(331,67,'2026-08-24','15:46:59'),(332,67,'2026-08-29','15:18:54'),(333,67,'2026-09-03','14:50:49'),(334,67,'2026-09-08','14:22:44'),(335,67,'2026-09-13','13:54:39'),(336,67,'2026-09-18','13:26:34'),(337,67,'2026-09-23','12:58:29'),(338,67,'2026-09-28','12:30:24'),(339,67,'2026-10-03','12:02:19'),(340,68,'2026-09-17','13:48:22'),(341,68,'2026-09-24','13:09:03'),(342,68,'2026-10-01','12:29:44'),(343,70,'2026-07-18','08:03:21'),(344,70,'2026-07-21','07:46:30'),(345,70,'2026-07-24','07:29:39'),(346,70,'2026-07-27','07:12:48'),(347,70,'2026-07-30','06:55:57'),(348,70,'2026-08-02','06:39:06'),(349,70,'2026-08-05','06:22:15'),(350,70,'2026-08-08','06:05:24'),(351,70,'2026-08-11','17:48:33'),(352,70,'2026-08-14','17:31:42'),(353,70,'2026-08-17','17:14:51'),(354,70,'2026-08-20','16:58:00'),(355,70,'2026-08-23','16:41:09'),(356,70,'2026-08-26','16:24:18'),(357,70,'2026-08-29','16:07:27'),(358,70,'2026-09-01','15:50:36'),(359,70,'2026-09-04','15:33:45'),(360,70,'2026-09-07','15:16:54'),(361,70,'2026-09-10','15:00:03'),(362,70,'2026-09-13','14:43:12'),(363,70,'2026-09-16','14:26:21'),(364,70,'2026-09-19','14:09:30'),(365,70,'2026-09-22','13:52:39'),(366,70,'2026-09-25','13:35:48'),(367,70,'2026-09-28','13:18:57'),(368,70,'2026-10-01','13:02:06'),(369,70,'2026-10-04','12:45:15'),(370,70,'2026-10-07','12:28:24'),(371,71,'2026-07-14','08:42:00'),(372,71,'2026-07-19','08:13:55'),(373,71,'2026-07-24','07:45:50'),(374,71,'2026-07-29','07:17:45'),(375,71,'2026-08-03','06:49:40'),(376,71,'2026-08-08','06:21:35'),(377,71,'2026-08-13','17:53:30'),(378,71,'2026-08-18','17:25:25'),(379,71,'2026-08-23','16:57:20'),(380,71,'2026-08-28','16:29:15'),(381,71,'2026-09-02','16:01:10'),(382,71,'2026-09-07','15:33:05'),(383,71,'2026-09-12','15:05:00'),(384,71,'2026-09-17','14:36:55'),(385,71,'2026-09-22','14:08:50'),(386,71,'2026-09-27','13:40:45'),(387,71,'2026-10-02','13:12:40'),(388,71,'2026-10-07','12:44:35'),(592,56,'2026-09-22','10:06:05'),(593,56,'2026-09-27','09:38:00'),(594,56,'2026-10-07','08:41:50'),(595,60,'2026-09-26','10:48:21'),(596,60,'2026-10-01','10:20:16'),(597,64,'2026-09-30','11:30:37'),(598,64,'2026-10-05','11:02:32'),(599,69,'2026-10-05','12:23:27');
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
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_history`
--

LOCK TABLES `order_history` WRITE;
/*!40000 ALTER TABLE `order_history` DISABLE KEYS */;
INSERT INTO `order_history` VALUES (20,1,'2026-09-22 12:30:00',2,250.00,500.00,'CASH','GMS-DEMO-SALE-001'),(25,21,'2026-10-04 12:00:00',2,180.00,360.00,'CASH','GMS-DEMO-SALE-002'),(26,22,'2026-10-03 13:00:00',1,90.00,90.00,'ONLINE TRANSFER','GMS-DEMO-SALE-003'),(27,1,'2026-10-02 14:00:00',1,500.00,500.00,'CARD','GMS-DEMO-SALE-004'),(28,23,'2026-09-30 15:00:00',1,650.00,650.00,'CARD','GMS-DEMO-SALE-005'),(29,24,'2026-09-28 16:00:00',2,350.00,700.00,'CASH','GMS-DEMO-SALE-006'),(30,25,'2026-09-27 17:00:00',2,120.00,240.00,'CASH','GMS-DEMO-SALE-007'),(31,26,'2026-09-25 10:00:00',1,280.00,280.00,'ONLINE TRANSFER','GMS-DEMO-SALE-008'),(32,27,'2026-09-23 11:00:00',3,150.00,450.00,'CARD','GMS-DEMO-SALE-009'),(33,21,'2026-09-21 12:00:00',1,180.00,180.00,'CASH','GMS-DEMO-SALE-010'),(34,22,'2026-09-19 13:00:00',2,90.00,180.00,'CASH','GMS-DEMO-SALE-011'),(35,1,'2026-09-17 14:00:00',1,500.00,500.00,'ONLINE TRANSFER','GMS-DEMO-SALE-012'),(36,23,'2026-09-15 15:00:00',1,650.00,650.00,'CARD','GMS-DEMO-SALE-013'),(37,24,'2026-09-12 16:00:00',1,350.00,350.00,'CASH','GMS-DEMO-SALE-014'),(38,25,'2026-09-09 17:00:00',3,120.00,360.00,'CASH','GMS-DEMO-SALE-015'),(39,26,'2026-09-05 10:00:00',2,280.00,560.00,'CARD','GMS-DEMO-SALE-016'),(40,27,'2026-08-30 11:00:00',2,150.00,300.00,'ONLINE TRANSFER','GMS-DEMO-SALE-017'),(41,21,'2026-08-23 12:00:00',2,180.00,360.00,'CASH','GMS-DEMO-SALE-018'),(42,22,'2026-08-15 13:00:00',1,90.00,90.00,'CARD','GMS-DEMO-SALE-019'),(43,1,'2026-08-06 14:00:00',1,500.00,500.00,'CASH','GMS-DEMO-SALE-020');
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
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_history`
--

LOCK TABLES `payment_history` WRITE;
/*!40000 ALTER TABLE `payment_history` DISABLE KEYS */;
INSERT INTO `payment_history` VALUES (20,1,'2026-09-07 10:15:00',3000.00,'CARD','GMS-DEMO-PAY-1001'),(25,69,'2026-10-01 10:00:00',500.00,'OTHER','GMS-DEMO-PAY-1023'),(26,64,'2026-09-28 10:00:00',500.00,'CASH','GMS-DEMO-PAY-1018'),(27,60,'2026-09-21 10:00:00',500.00,'CASH','GMS-DEMO-PAY-1014'),(28,56,'2026-09-19 10:00:00',500.00,'CASH','GMS-DEMO-PAY-1010'),(29,51,'2026-09-24 10:00:00',500.00,'CARD','GMS-DEMO-PAY-1005'),(30,68,'2026-09-11 10:00:00',3000.00,'ONLINE TRANSFER','GMS-DEMO-PAY-1022'),(31,65,'2026-09-10 10:00:00',3000.00,'CARD','GMS-DEMO-PAY-1019'),(32,63,'2026-09-12 10:00:00',3000.00,'OTHER','GMS-DEMO-PAY-1017'),(33,62,'2026-09-13 10:00:00',3000.00,'ONLINE TRANSFER','GMS-DEMO-PAY-1016'),(34,59,'2026-09-16 10:00:00',3000.00,'OTHER','GMS-DEMO-PAY-1013'),(35,55,'2026-09-20 10:00:00',3000.00,'CARD','GMS-DEMO-PAY-1009'),(36,50,'2026-09-25 10:00:00',3000.00,'CASH','GMS-DEMO-PAY-1004'),(37,48,'2026-09-27 10:00:00',3000.00,'ONLINE TRANSFER','GMS-DEMO-PAY-1002'),(38,70,'2026-09-05 10:00:00',30000.00,'CASH','GMS-DEMO-PAY-1024'),(39,66,'2026-09-09 10:00:00',30000.00,'CASH','GMS-DEMO-PAY-1020'),(40,57,'2026-09-18 10:00:00',30000.00,'CARD','GMS-DEMO-PAY-1011'),(41,53,'2026-09-22 10:00:00',30000.00,'OTHER','GMS-DEMO-PAY-1007'),(42,49,'2026-09-26 10:00:00',30000.00,'OTHER','GMS-DEMO-PAY-1003');
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
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `personal_training`
--

LOCK TABLES `personal_training` WRITE;
/*!40000 ALTER TABLE `personal_training` DISABLE KEYS */;
INSERT INTO `personal_training` VALUES (19,48,1,'2026-08-16',NULL,1500.00,12,8,'ACTIVE'),(20,49,25,'2026-07-24','2026-08-21',1500.00,16,16,'COMPLETED'),(21,50,26,'2026-09-13',NULL,1500.00,12,5,'ACTIVE'),(22,52,27,'2026-09-18',NULL,1500.00,10,4,'ACTIVE'),(23,53,28,'2026-06-09','2026-07-07',1500.00,16,16,'COMPLETED'),(24,55,29,'2026-08-27',NULL,1500.00,12,7,'ACTIVE'),(25,62,1,'2026-09-04',NULL,1500.00,12,6,'ACTIVE'),(26,66,27,'2026-07-17','2026-08-14',1500.00,16,16,'COMPLETED');
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
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES (1,'Stainless Steel Shaker Bottle 700 ml',500.00,18,'Leak-resistant shaker with mixing ball.'),(21,'Whey Protein Bar - Chocolate 60 g',180.00,34,'Single-serving chocolate protein snack.'),(22,'Electrolyte Drink - Lime 500 ml',90.00,42,'Lime sports hydration drink.'),(23,'Training Gloves - Medium',650.00,12,'Padded palm training gloves.'),(24,'Resistance Band - Medium',350.00,21,'Medium resistance latex training band.'),(25,'Energy Drink - Original 250 ml',120.00,30,'Caffeinated energy drink, 250 ml can.'),(26,'Gym Towel - Cotton',280.00,25,'Quick-dry cotton training towel.'),(27,'Whey Protein Sachet - Vanilla 30 g',150.00,28,'Single-serve vanilla whey sachet.');
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
) ENGINE=InnoDB AUTO_INCREMENT=166 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `progress_track`
--

LOCK TABLES `progress_track` WRITE;
/*!40000 ALTER TABLE `progress_track` DISABLE KEYS */;
INSERT INTO `progress_track` VALUES (33,'2026-10-07',175.00,70.00,18.00,22.86,1),(38,'2026-08-08',175.00,70.60,18.40,23.05,1),(39,'2026-09-07',175.00,70.00,18.00,22.86,1),(40,'2026-07-09',174.00,73.60,19.10,24.31,48),(41,'2026-08-08',174.00,73.00,18.80,24.11,48),(42,'2026-09-07',174.00,72.40,18.50,23.91,48),(43,'2026-07-09',163.00,60.20,24.20,22.66,49),(44,'2026-08-08',163.00,60.00,24.10,22.58,49),(45,'2026-09-07',163.00,59.80,24.00,22.51,49),(46,'2026-07-09',160.00,57.00,25.40,22.27,50),(47,'2026-08-08',160.00,56.60,25.20,22.11,50),(48,'2026-09-07',160.00,56.20,25.00,21.95,50),(49,'2026-07-09',178.00,77.10,19.20,24.33,51),(50,'2026-08-08',178.00,76.80,19.10,24.24,51),(51,'2026-09-07',178.00,76.50,19.00,24.14,51),(52,'2026-07-09',158.00,61.40,28.20,24.60,52),(53,'2026-08-08',158.00,61.20,28.10,24.52,52),(54,'2026-09-07',158.00,61.00,28.00,24.44,52),(55,'2026-07-09',172.00,71.00,20.30,24.00,53),(56,'2026-08-08',172.00,70.60,20.20,23.86,53),(57,'2026-09-07',172.00,70.20,20.00,23.73,53),(58,'2026-07-09',180.00,82.50,24.50,25.46,54),(59,'2026-08-08',180.00,82.50,24.50,25.46,54),(60,'2026-09-07',180.00,82.50,24.50,25.46,54),(61,'2026-07-09',165.00,58.00,23.40,21.30,55),(62,'2026-08-08',165.00,57.70,23.20,21.19,55),(63,'2026-09-07',165.00,57.40,23.00,21.08,55),(64,'2026-07-09',176.00,74.40,18.20,24.02,56),(65,'2026-08-08',176.00,74.20,18.10,23.95,56),(66,'2026-09-07',176.00,74.00,18.00,23.89,56),(67,'2026-07-09',162.00,58.40,23.70,22.25,57),(68,'2026-08-08',162.00,58.20,23.60,22.18,57),(69,'2026-09-07',162.00,58.10,23.50,22.14,57),(70,'2026-07-09',169.00,67.80,21.00,23.74,58),(71,'2026-08-08',169.00,67.80,21.00,23.74,58),(72,'2026-09-07',169.00,67.80,21.00,23.74,58),(73,'2026-07-09',157.00,55.20,24.40,22.39,59),(74,'2026-08-08',157.00,54.90,24.20,22.27,59),(75,'2026-09-07',157.00,54.60,24.00,22.15,59),(76,'2026-07-09',181.00,84.20,23.60,25.70,60),(77,'2026-08-08',181.00,84.10,23.60,25.67,60),(78,'2026-09-07',181.00,84.00,23.50,25.64,60),(79,'2026-07-09',164.00,62.70,27.00,23.31,61),(80,'2026-08-08',164.00,62.70,27.00,23.31,61),(81,'2026-09-07',164.00,62.70,27.00,23.31,61),(82,'2026-07-09',175.00,74.00,19.80,24.16,62),(83,'2026-08-08',175.00,73.60,19.70,24.03,62),(84,'2026-09-07',175.00,73.20,19.50,23.90,62),(85,'2026-07-09',159.00,56.40,24.30,22.31,63),(86,'2026-08-08',159.00,56.10,24.20,22.19,63),(87,'2026-09-07',159.00,55.80,24.00,22.07,63),(88,'2026-07-09',177.00,79.30,22.00,25.31,64),(89,'2026-08-08',177.00,79.30,22.00,25.31,64),(90,'2026-09-07',177.00,79.30,22.00,25.31,64),(91,'2026-07-09',161.00,61.00,26.20,23.53,65),(92,'2026-08-08',161.00,60.70,26.10,23.42,65),(93,'2026-09-07',161.00,60.40,26.00,23.30,65),(94,'2026-07-09',183.00,86.60,24.30,25.86,66),(95,'2026-08-08',183.00,86.30,24.20,25.77,66),(96,'2026-09-07',183.00,86.00,24.00,25.68,66),(97,'2026-07-09',156.00,58.20,27.50,23.92,67),(98,'2026-08-08',156.00,58.20,27.50,23.92,67),(99,'2026-09-07',156.00,58.20,27.50,23.92,67),(100,'2026-07-09',171.00,70.00,20.70,23.94,68),(101,'2026-08-08',171.00,69.80,20.60,23.87,68),(102,'2026-09-07',171.00,69.60,20.50,23.80,68),(103,'2026-07-09',166.00,63.00,25.50,22.86,69),(104,'2026-08-08',166.00,63.00,25.50,22.86,69),(105,'2026-09-07',166.00,63.00,25.50,22.86,69),(106,'2026-07-09',179.00,77.70,21.80,24.25,70),(107,'2026-08-08',179.00,77.40,21.70,24.16,70),(108,'2026-09-07',179.00,77.10,21.50,24.06,70),(109,'2026-07-09',160.00,64.00,29.00,25.00,71),(110,'2026-08-08',160.00,64.00,29.00,25.00,71),(111,'2026-09-07',160.00,64.00,29.00,25.00,71);
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
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trainer`
--

LOCK TABLES `trainer` WRITE;
/*!40000 ALTER TABLE `trainer` DISABLE KEYS */;
INSERT INTO `trainer` VALUES (1,'Niel Mandhare','9876512001','990000000001','2025-05-05','trainer_profile_niel','PROFILE_ONLY',1),(25,'Shruti Patil','9876512002','990000000002','2025-08-13','trainer_profile_shruti','PROFILE_ONLY',1),(26,'Tanisha Kulkarni','9876512003','990000000003','2025-12-01','trainer_profile_tanisha','PROFILE_ONLY',1),(27,'Atharva Joshi','9876512004','990000000004','2026-01-30','trainer_profile_atharva','PROFILE_ONLY',1),(28,'Tasbiya Shaikh','9876512005','990000000005','2026-04-20','trainer_profile_tasbiya','PROFILE_ONLY',1),(29,'Pradynesh Pawar','9876512006','990000000006','2026-07-04','trainer_profile_pradynesh','PROFILE_ONLY',2);
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
) ENGINE=InnoDB AUTO_INCREMENT=147 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trainer_attendance`
--

LOCK TABLES `trainer_attendance` WRITE;
/*!40000 ALTER TABLE `trainer_attendance` DISABLE KEYS */;
INSERT INTO `trainer_attendance` VALUES (20,27,'2026-08-26','06:51:00','17:39:00'),(21,27,'2026-09-01','06:45:00','17:33:00'),(22,27,'2026-09-04','06:42:00','17:30:00'),(23,27,'2026-09-07','06:39:00','17:27:00'),(24,27,'2026-09-10','06:36:00','17:24:00'),(25,27,'2026-09-16','06:30:00','17:18:00'),(26,27,'2026-09-22','06:24:00','17:12:00'),(27,27,'2026-09-25','06:21:00','17:09:00'),(28,27,'2026-09-28','06:18:00','17:06:00'),(29,27,'2026-10-01','06:15:00','17:03:00'),(30,27,'2026-10-07','06:09:00','17:57:00'),(31,1,'2026-08-24','07:51:00','15:55:00'),(32,1,'2026-08-27','07:48:00','15:52:00'),(33,1,'2026-09-02','07:42:00','15:46:00'),(34,1,'2026-09-08','07:36:00','15:40:00'),(35,1,'2026-09-11','07:33:00','15:37:00'),(36,1,'2026-09-14','07:30:00','15:34:00'),(37,1,'2026-09-17','07:27:00','15:31:00'),(38,1,'2026-09-23','07:21:00','15:25:00'),(39,1,'2026-09-29','07:15:00','15:19:00'),(40,1,'2026-10-02','07:12:00','15:16:00'),(41,1,'2026-10-05','07:09:00','15:13:00'),(42,29,'2026-08-25','08:06:00','15:02:00'),(43,29,'2026-08-28','08:03:00','15:59:00'),(44,29,'2026-08-31','08:00:00','15:56:00'),(45,29,'2026-09-03','08:57:00','15:53:00'),(46,29,'2026-09-09','08:51:00','15:47:00'),(47,29,'2026-09-15','08:45:00','15:41:00'),(48,29,'2026-09-18','08:42:00','15:38:00'),(49,29,'2026-09-21','08:39:00','15:35:00'),(50,29,'2026-09-24','08:36:00','15:32:00'),(51,29,'2026-09-30','08:30:00','15:26:00'),(52,29,'2026-10-06','08:24:00','15:20:00'),(53,25,'2026-08-24','07:39:00','15:19:00'),(54,25,'2026-08-27','07:36:00','15:16:00'),(55,25,'2026-09-02','07:30:00','15:10:00'),(56,25,'2026-09-08','07:24:00','15:04:00'),(57,25,'2026-09-11','07:21:00','15:01:00'),(58,25,'2026-09-14','07:18:00','15:58:00'),(59,25,'2026-09-17','07:15:00','15:55:00'),(60,25,'2026-09-23','07:09:00','15:49:00'),(61,25,'2026-09-29','07:03:00','15:43:00'),(62,25,'2026-10-02','07:00:00','15:40:00'),(63,25,'2026-10-05','07:57:00','15:37:00'),(64,26,'2026-08-25','08:45:00','16:29:00'),(65,26,'2026-08-28','08:42:00','16:26:00'),(66,26,'2026-08-31','08:39:00','16:23:00'),(67,26,'2026-09-03','08:36:00','16:20:00'),(68,26,'2026-09-09','08:30:00','16:14:00'),(69,26,'2026-09-15','08:24:00','16:08:00'),(70,26,'2026-09-18','08:21:00','16:05:00'),(71,26,'2026-09-21','08:18:00','16:02:00'),(72,26,'2026-09-24','08:15:00','16:59:00'),(73,26,'2026-09-30','08:09:00','16:53:00'),(74,26,'2026-10-06','08:03:00','16:47:00'),(75,28,'2026-08-24','07:00:00','14:52:00'),(76,28,'2026-08-27','07:57:00','14:49:00'),(77,28,'2026-09-02','07:51:00','14:43:00'),(78,28,'2026-09-08','07:45:00','14:37:00'),(79,28,'2026-09-11','07:42:00','14:34:00'),(80,28,'2026-09-14','07:39:00','14:31:00'),(81,28,'2026-09-17','07:36:00','14:28:00'),(82,28,'2026-09-23','07:30:00','14:22:00'),(83,28,'2026-09-29','07:24:00','14:16:00'),(84,28,'2026-10-02','07:21:00','14:13:00'),(85,28,'2026-10-05','07:18:00','14:10:00');
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
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `workout_plan`
--

LOCK TABLES `workout_plan` WRITE;
/*!40000 ALTER TABLE `workout_plan` DISABLE KEYS */;
INSERT INTO `workout_plan` VALUES (16,'BEGINNER','Walk','Strength','Rest','Cardio','Strength','Stretch','Rest'),(18,'BEGINNER','Chair squats 3x10; wall push-ups 3x10','Walk 20 min','Band rows 3x12; hip hinges 3x10','Rest','Step-ups 3x8 each side; dead bug 3x8','Easy cycle 15 min','Rest'),(19,'INTERMEDIATE','Squat 4x8; bench press 4x8','Row 25 min; core','Deadlift 3x6; split squat 3x8','Rest','Overhead press 4x8; pulldown 4x10','Easy cycle 25 min','Rest'),(20,'INTERMEDIATE','Incline walk 30 min; mobility','Goblet squat 3x12; cable row 3x12','Cycle intervals 6x1 min','Rest','Leg press 3x10; chest press 3x10','Walk 35 min','Rest'),(21,'ADVANCED','Squat 5x5; bench press 5x5','Recovery walk 30 min','Deadlift 4x4; pull-up 4x6','Rest','Front squat 4x6; overhead press 4x6','Row intervals 5x500 m','Rest'),(22,'ADVANCED','Bike intervals 8x90 sec','Bench press 4x6; row 4x8','Easy cycle 25 min','Squat 4x6; Romanian deadlift 3x8','Conditioning circuit 4 rounds','Mobility 25 min','Rest');
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

-- Dump completed on 2026-10-07 11:57:27
