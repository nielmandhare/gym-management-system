-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               8.0.37 - MySQL Community Server - GPL
-- Server OS:                    Win64
-- HeidiSQL Version:             12.8.0.6908
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for gms
CREATE DATABASE IF NOT EXISTS `gms` /*!40100 DEFAULT CHARACTER SET utf8mb3 */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `gms`;

-- Dumping structure for table gms.admin
CREATE TABLE IF NOT EXISTS `admin` (
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
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb3;

-- Data exporting was unselected.

-- Dumping structure for table gms.equipment
CREATE TABLE IF NOT EXISTS `equipment` (
  `equipment_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `purchase_date` date DEFAULT NULL,
  `status` enum('GOOD','UNDER MAINTENANCE','DAMAGED','RETIRED') CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `serial_number` varchar(255) DEFAULT NULL,
  `warranty_exp` date DEFAULT NULL,
  PRIMARY KEY (`equipment_id`),
  UNIQUE KEY `serial_number_UNIQUE` (`serial_number`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb3;

-- Data exporting was unselected.

-- Dumping structure for table gms.maintenance_log
CREATE TABLE IF NOT EXISTS `maintenance_log` (
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

-- Data exporting was unselected.

-- Dumping structure for table gms.member
CREATE TABLE IF NOT EXISTS `member` (
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
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb3;

-- Data exporting was unselected.

-- Dumping structure for table gms.membership
CREATE TABLE IF NOT EXISTS `membership` (
  `membership_type_id` int NOT NULL AUTO_INCREMENT,
  `membership_type` enum('DAYPASS','MONTHLY','ANNUAL') NOT NULL,
  `price` decimal(10,2) NOT NULL,
  PRIMARY KEY (`membership_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3;

-- Data exporting was unselected.

-- Dumping structure for table gms.member_attendance
CREATE TABLE IF NOT EXISTS `member_attendance` (
  `attendance_id` int NOT NULL AUTO_INCREMENT,
  `member_member_id` int NOT NULL,
  `date` date NOT NULL DEFAULT (curdate()),
  `check_in_time` time NOT NULL DEFAULT (curtime()),
  PRIMARY KEY (`attendance_id`),
  KEY `fk_member_attendance_member1_idx` (`member_member_id`),
  CONSTRAINT `fk_member_attendance_member1` FOREIGN KEY (`member_member_id`) REFERENCES `member` (`member_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=77 DEFAULT CHARSET=utf8mb3;

-- Data exporting was unselected.

-- Dumping structure for table gms.order_history
CREATE TABLE IF NOT EXISTS `order_history` (
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
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb3;

-- Data exporting was unselected.

-- Dumping structure for table gms.payment_history
CREATE TABLE IF NOT EXISTS `payment_history` (
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
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb3;

-- Data exporting was unselected.

-- Dumping structure for table gms.personal_training
CREATE TABLE IF NOT EXISTS `personal_training` (
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

-- Data exporting was unselected.

-- Dumping structure for table gms.product
CREATE TABLE IF NOT EXISTS `product` (
  `product_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `stock` int NOT NULL DEFAULT '0',
  `description` text,
  PRIMARY KEY (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb3;

-- Data exporting was unselected.

-- Dumping structure for table gms.progress_track
CREATE TABLE IF NOT EXISTS `progress_track` (
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
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb3;

-- Data exporting was unselected.

-- Dumping structure for table gms.status
CREATE TABLE IF NOT EXISTS `status` (
  `status_id` int NOT NULL AUTO_INCREMENT,
  `status_status` tinyint NOT NULL,
  PRIMARY KEY (`status_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3;

-- Data exporting was unselected.

-- Dumping structure for table gms.trainer
CREATE TABLE IF NOT EXISTS `trainer` (
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
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb3;

-- Data exporting was unselected.

-- Dumping structure for table gms.trainer_attendance
CREATE TABLE IF NOT EXISTS `trainer_attendance` (
  `attendance_id` int NOT NULL AUTO_INCREMENT,
  `trainer_trainer_id` int NOT NULL,
  `date` date NOT NULL,
  `check_in_time` time NOT NULL,
  `check_out_time` time NOT NULL,
  PRIMARY KEY (`attendance_id`),
  KEY `fk_trainer_attendance_trainer1_idx` (`trainer_trainer_id`),
  CONSTRAINT `fk_trainer_attendance_trainer1` FOREIGN KEY (`trainer_trainer_id`) REFERENCES `trainer` (`trainer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb3;

-- Data exporting was unselected.

-- Dumping structure for table gms.workout_plan
CREATE TABLE IF NOT EXISTS `workout_plan` (
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
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb3;

-- Data exporting was unselected.

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
