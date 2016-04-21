-- MySQL dump 10.13  Distrib 5.7.9, for Win64 (x86_64)
--
-- Host: 192.168.1.159    Database: yourstorydb
-- ------------------------------------------------------
-- Server version	5.5.44-0+deb8u1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `accounts`
--

DROP TABLE IF EXISTS `accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `accounts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(13) NOT NULL DEFAULT '',
  `password` varchar(128) NOT NULL DEFAULT '',
  `salt` varchar(128) DEFAULT NULL,
  `pin` varchar(10) DEFAULT NULL,
  `pic` varchar(26) DEFAULT NULL,
  `loggedin` tinyint(4) NOT NULL DEFAULT '0',
  `lastlogin` timestamp NULL DEFAULT NULL,
  `createdat` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `birthday` date NOT NULL DEFAULT '0000-00-00',
  `banned` tinyint(1) NOT NULL DEFAULT '0',
  `banreason` text,
  `gm` tinyint(1) NOT NULL DEFAULT '0',
  `macs` tinytext,
  `nxCredit` int(11) DEFAULT NULL,
  `maplePoint` int(11) DEFAULT NULL,
  `nxPrepaid` int(11) DEFAULT NULL,
  `characterslots` tinyint(2) NOT NULL DEFAULT '5',
  `gender` tinyint(2) NOT NULL DEFAULT '10',
  `tempban` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `greason` tinyint(4) NOT NULL DEFAULT '0',
  `tos` tinyint(1) NOT NULL DEFAULT '0',
  `sitelogged` text,
  `webadmin` int(1) DEFAULT '0',
  `nick` varchar(20) DEFAULT NULL,
  `mute` int(1) DEFAULT '0',
  `email` varchar(45) DEFAULT NULL,
  `ip` text,
  `rewardpoints` int(11) NOT NULL DEFAULT '0',
  `hwid` varchar(12) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `ranking1` (`id`,`banned`,`gm`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `accounts`
--

LOCK TABLES `accounts` WRITE;
/*!40000 ALTER TABLE `accounts` DISABLE KEYS */;
INSERT INTO `accounts` VALUES (1,'Galtdyn','a2f12231daec27a39a4d8e5c7aff4ce24cd47215',NULL,NULL,NULL,0,NULL,'2016-04-20 18:02:10','1990-01-01',0,NULL,0,NULL,NULL,NULL,NULL,5,10,'0000-00-00 00:00:00',0,0,'1461175409',0,NULL,0,'galtdyn@gmail.com','77.168.73.141',0,''),(4,'KimJung','5de994aedb930b5b72590d02fe092c38a077da79',NULL,NULL,NULL,0,NULL,'2016-04-21 11:16:52','1990-01-01',0,NULL,0,NULL,NULL,NULL,NULL,5,10,'0000-00-00 00:00:00',0,0,NULL,0,NULL,0,'l.tong@student.fontys.nl','145.93.93.10',0,''),(5,'Bash','806764243244696220e85bda434a5e52fdddd648',NULL,NULL,'000000',0,'2016-04-21 17:41:13','2016-04-21 17:38:43','1990-01-01',0,NULL,0,'D8-CB-8A-80-71-1D, 00-50-56-C0-00-08, 00-50-56-C0-00-01',NULL,NULL,NULL,5,0,'0000-00-00 00:00:00',0,1,'1461260482',0,NULL,0,'bash@bash.nl','192.168.1.1',0,'A081-2ED8');
/*!40000 ALTER TABLE `accounts` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-04-21 19:41:26
