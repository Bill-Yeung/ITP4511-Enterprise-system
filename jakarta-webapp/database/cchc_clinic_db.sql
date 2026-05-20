SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

CREATE DATABASE IF NOT EXISTS `cchc_clinic_db`;
USE `cchc_clinic_db`;


CREATE TABLE `clinics` (
  `clinic_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `location` varchar(100) NOT NULL,
  `is_queue_enabled` tinyint(1) DEFAULT 1,
  PRIMARY KEY (`clinic_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `clinics` (`clinic_id`, `name`, `location`, `is_queue_enabled`) VALUES
(1, 'Chai Wan Community Clinic', 'Chai Wan', 1),
(2, 'Tseung Kwan O Community Clinic', 'Tseung Kwan O', 1),
(3, 'Sha Tin Community Clinic', 'Sha Tin', 1),
(4, 'Tuen Mun Community Clinic', 'Tuen Mun', 1),
(5, 'Tsing Yi Community Clinic', 'Tsing Yi', 1);

CREATE TABLE `clinic_hours` (
  `clinic_hours_id` int NOT NULL AUTO_INCREMENT,
  `clinic_id` int NOT NULL,
  `day_of_week` enum('Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday') NOT NULL,
  `open_time` time NOT NULL,
  `close_time` time NOT NULL,
  PRIMARY KEY (`clinic_hours_id`),
  KEY `clinic_id` (`clinic_id`),
  CONSTRAINT `clinic_hours_ibfk_1` FOREIGN KEY (`clinic_id`) REFERENCES `clinics` (`clinic_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `clinic_hours` (`clinic_id`, `day_of_week`, `open_time`, `close_time`) VALUES
(1, 'Monday', '08:00:00', '13:00:00'),
(1, 'Monday', '14:00:00', '18:00:00'),
(1, 'Tuesday', '08:00:00', '13:00:00'),
(1, 'Tuesday', '14:00:00', '18:00:00'),
(1, 'Wednesday', '08:00:00', '13:00:00'),
(1, 'Wednesday', '14:00:00', '18:00:00'),
(1, 'Thursday', '08:00:00', '13:00:00'),
(1, 'Thursday', '14:00:00', '18:00:00'),
(1, 'Friday', '08:00:00', '13:00:00'),
(1, 'Friday', '14:00:00', '18:00:00'),
(1, 'Saturday', '08:00:00', '13:00:00'),
(2, 'Monday', '08:00:00', '13:00:00'),
(2, 'Monday', '14:00:00', '18:00:00'),
(2, 'Tuesday', '08:00:00', '13:00:00'),
(2, 'Tuesday', '14:00:00', '18:00:00'),
(2, 'Wednesday', '08:00:00', '13:00:00'),
(2, 'Wednesday', '14:00:00', '18:00:00'),
(2, 'Thursday', '08:00:00', '13:00:00'),
(2, 'Thursday', '14:00:00', '18:00:00'),
(2, 'Friday', '08:00:00', '13:00:00'),
(2, 'Friday', '14:00:00', '18:00:00'),
(2, 'Saturday', '08:00:00', '13:00:00'),
(3, 'Monday', '08:00:00', '13:00:00'),
(3, 'Monday', '14:00:00', '17:00:00'),
(3, 'Tuesday', '08:00:00', '13:00:00'),
(3, 'Tuesday', '14:00:00', '17:00:00'),
(3, 'Wednesday', '08:00:00', '13:00:00'),
(3, 'Wednesday', '14:00:00', '17:00:00'),
(3, 'Thursday', '08:00:00', '13:00:00'),
(3, 'Thursday', '14:00:00', '17:00:00'),
(3, 'Friday', '08:00:00', '13:00:00'),
(3, 'Friday', '14:00:00', '17:00:00'),
(3, 'Saturday', '08:00:00', '13:00:00'),
(4, 'Monday', '09:00:00', '13:00:00'),
(4, 'Monday', '14:00:00', '18:00:00'),
(4, 'Tuesday', '09:00:00', '13:00:00'),
(4, 'Tuesday', '14:00:00', '18:00:00'),
(4, 'Wednesday', '09:00:00', '13:00:00'),
(4, 'Wednesday', '14:00:00', '18:00:00'),
(4, 'Thursday', '09:00:00', '13:00:00'),
(4, 'Thursday', '14:00:00', '18:00:00'),
(4, 'Friday', '09:00:00', '13:00:00'),
(4, 'Friday', '14:00:00', '18:00:00'),
(5, 'Monday', '09:00:00', '13:00:00'),
(5, 'Monday', '14:00:00', '17:00:00'),
(5, 'Tuesday', '09:00:00', '13:00:00'),
(5, 'Tuesday', '14:00:00', '17:00:00'),
(5, 'Wednesday', '09:00:00', '13:00:00'),
(5, 'Wednesday', '14:00:00', '17:00:00'),
(5, 'Thursday', '09:00:00', '13:00:00'),
(5, 'Thursday', '14:00:00', '17:00:00'),
(5, 'Friday', '09:00:00', '13:00:00'),
(5, 'Friday', '14:00:00', '17:00:00');


CREATE TABLE `services` (
  `service_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  PRIMARY KEY (`service_id`),
  UNIQUE KEY `uq_services_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `services` (`service_id`, `name`, `description`) VALUES
(1, 'General Consultation', 'Outpatient consultation with a general practitioner for common illnesses and follow-ups.'),
(2, 'Vaccination', 'Administration of vaccines including flu, Hepatitis B, and COVID-19 boosters.'),
(3, 'Basic Health Screening', 'Includes blood pressure, BMI, blood glucose, and cholesterol checks.'),
(4, 'Blood Test', 'Fasting or non-fasting blood draw for laboratory analysis.');


CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` enum('Patient','Staff','Admin') NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(20) NOT NULL DEFAULT '',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role`, `full_name`, `email`, `phone`, `created_at`) VALUES
(1, 'admin_01', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Admin', 'Alice Wong', 'alice.wong@cchc.hk', '+852-2100-0001', '2026-03-10 06:00:00'),
(2, 'admin_cw', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Admin', 'Chai Wan Admin', 'admin.cw@cchc.hk', '+852-2100-0002', '2026-03-10 06:00:00'),
(3, 'staff_cw', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Staff', 'Carol Lam', 'carol.lam@cchc.hk', '+852-2100-1001', '2026-03-10 06:00:00'),
(4, 'staff_tko', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Staff', 'David Ng', 'david.ng@cchc.hk', '+852-2100-1002', '2026-03-10 06:00:00'),
(5, 'staff_st', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Staff', 'Eva Cheung', 'eva.cheung@cchc.hk', '+852-2100-1003', '2026-03-10 06:00:00'),
(6, 'staff_tm', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Staff', 'Frank Yip', 'frank.yip@cchc.hk', '+852-2100-1004', '2026-03-10 06:00:00'),
(7, 'staff_ty', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Staff', 'Grace Ho', 'grace.ho@cchc.hk', '+852-2100-1005', '2026-03-10 06:00:00'),
(8, 'patient_01', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Patient', 'Henry Tsang', 'henry.tsang@gmail.com', '+852-9100-2001', '2026-03-10 06:00:00'),
(9, 'patient_02', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Patient', 'Iris Kwok', 'iris.kwok@gmail.com', '+852-9100-2002', '2026-03-10 06:00:00'),
(10, 'patient_03', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Patient', 'James Liu', 'james.liu@gmail.com', '+852-9100-2003', '2026-03-10 06:00:00'),
(11, 'patient_04', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Patient', 'Karen Fong', 'karen.fong@gmail.com', '+852-9100-2004', '2026-03-10 06:00:00'),
(12, 'patient_05', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Patient', 'Leo Mak', 'leo.mak@gmail.com', '+852-9100-2005', '2026-03-10 06:00:00'),
(13, 'patient_06', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Patient', 'Mandy Sin', 'mandy.sin@gmail.com', '+852-9100-2006', '2026-03-10 06:00:00'),
(14, 'patient_07', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Patient', 'Nathan Hui', 'nathan.hui@gmail.com', '+852-9100-2007', '2026-03-10 06:00:00'),
(15, 'patient_08', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Patient', 'Olivia Poon', 'olivia.poon@gmail.com', '+852-9100-2008', '2026-03-10 06:00:00'),
(16, 'patient_09', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Patient', 'Peter Siu', 'peter.siu@gmail.com', '+852-9100-2009', '2026-03-10 06:00:00'),
(17, 'patient_10', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Patient', 'Queen Tam', 'queen.tam@gmail.com', '+852-9100-2010', '2026-03-10 06:00:00'),
(18, 'dr_cw', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Staff', 'Dr. Raymond Lee', 'raymond.lee@cchc.hk', '+852-2100-3001', '2026-03-10 06:00:00'),
(19, 'dr_tko', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Staff', 'Dr. Susan Ma', 'susan.ma@cchc.hk', '+852-2100-3002', '2026-03-10 06:00:00'),
(20, 'dr_st', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Staff', 'Dr. Kevin Tse', 'kevin.tse@cchc.hk', '+852-2100-3003', '2026-03-10 06:00:00'),
(21, 'dr_tm', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Staff', 'Dr. Wendy Lau', 'wendy.lau@cchc.hk', '+852-2100-3004', '2026-03-10 06:00:00'),
(22, 'dr_ty', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Staff', 'Dr. Patrick Yeung', 'patrick.yeung@cchc.hk', '+852-2100-3005', '2026-03-10 06:00:00'),
(23, 'dr_cw2',  '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Staff', 'Dr. Angela Chow', 'angela.chow@cchc.hk', '+852-2100-3006', '2026-03-10 06:00:00'),
(24, 'dr_cw3',  '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Staff', 'Dr. Michael Wu', 'michael.wu@cchc.hk', '+852-2100-3007', '2026-03-10 06:00:00'),
(25, 'dr_tko2', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Staff', 'Dr. Tommy Lam', 'tommy.lam@cchc.hk', '+852-2100-3008', '2026-03-10 06:00:00'),
(26, 'dr_tko3', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Staff', 'Dr. Jessica Ng', 'jessica.ng@cchc.hk', '+852-2100-3009', '2026-03-10 06:00:00'),
(27, 'dr_st2',  '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Staff', 'Dr. Stephanie Yuen', 'stephanie.yuen@cchc.hk', '+852-2100-3010', '2026-03-10 06:00:00'),
(28, 'dr_st3',  '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Staff', 'Dr. Daniel Cheng', 'daniel.cheng@cchc.hk', '+852-2100-3011', '2026-03-10 06:00:00'),
(29, 'dr_tm2',  '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Staff', 'Dr. Gary Fung', 'gary.fung@cchc.hk', '+852-2100-3012', '2026-03-10 06:00:00'),
(30, 'dr_tm3',  '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Staff', 'Dr. Hannah Ip', 'hannah.ip@cchc.hk', '+852-2100-3013', '2026-03-10 06:00:00'),
(31, 'dr_ty2',  '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Staff', 'Dr. Victor Chan', 'victor.chan@cchc.hk', '+852-2100-3014', '2026-03-10 06:00:00'),
(32, 'dr_ty3',  '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW', 'Staff', 'Dr. Bella Leung', 'bella.leung@cchc.hk', '+852-2100-3015', '2026-03-10 06:00:00');

CREATE TABLE `patient_profiles` (
  `user_id` int NOT NULL,
  `id_number` varchar(255) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `gender` enum('Male','Female','Other') DEFAULT NULL,
  `address` varchar(512) NOT NULL DEFAULT '',
  `emergency_contact_name` varchar(100) DEFAULT NULL,
  `emergency_contact_phone` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `patient_profiles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `patient_profiles` (`user_id`, `id_number`, `date_of_birth`, `gender`, `address`, `emergency_contact_name`, `emergency_contact_phone`) VALUES
(8, 'fWFN5NZ3sK9vSY3rtqtEOQ==', '1985-06-15', 'Male', 'sRIB7fl5YM+R4oBjUl/UNBrV0HLOm20gPQm7pPosfCc=', 'Mary Tsang', '+852-9100-8001'),
(9, '5n/P98JuDpfImBYcaxLmng==', '1990-03-22', 'Female', '29pxHGwaScgZ5XWFLbgf84VvnHt2ITqdpafjO6LW+/RgZUGNiO787uDqQE00C+HD', 'Tom Kwok', '+852-9100-8002'),
(10, 'b5u1/iqh2gZYCQTt3BwkYg==', '1978-11-08', 'Male', 'mQLORnXtS5w71hAZuKa26CxnFWSE1LymVDO0uuIifmE=', 'Lisa Liu', '+852-9100-8003'),
(11, 'MBOpPo1vYWCtjj4qoWUmRQ==', '1995-01-30', 'Female', 'V8K/4//dDOrLzRc6IAki9yVWeky5aog0zf7KbFJklbo=', 'David Fong', '+852-9100-8004'),
(12, 'L35ywJ+py2xe7FC9lFR8hg==', '1988-09-12', 'Male', 'OUib3RpkXLPJHyyHOmQMSdYDR7Vzp+APyBdE7VfyV6w5s3aoSgedtv0E2DpPubv5', 'Jenny Mak', '+852-9100-8005'),
(13, 'Ub0iRoAi/TjT5NWIPUl7mA==', '1992-07-05', 'Female', 'lC8pH9RF4PzzqY4BVXROV/S6H2oQafOZ++XZYHeqGvE=', 'Raymond Sin', '+852-9100-8006'),
(14, 'AcXl7X9X1OEw0ir0cbMXog==', '2000-04-18', 'Male', 'iPixip5SpzY+uFCpDLGeJNWkscUPQG8Khrj7vSomZTRgZUGNiO787uDqQE00C+HD', 'Angela Hui', '+852-9100-8007'),
(15, 'y06iVFATxbj2KsXD7OaMmw==', '1983-12-25', 'Female', '9PhR9GcSb6ipjbOiZa1TFwMBi1xt3r60cMbtdI62b6o=', 'Kenneth Poon', '+852-9100-8008'),
(16, 'xE+Xuem3cK3KZF49caUo0g==', '1975-08-03', 'Male', 'JcUjvRm/VTVkJNNZF4KDKJcMATLh/yf4VgdSYMPxLqE=', 'Betty Siu', '+852-9100-8009'),
(17, 'VtV3ysIJ1GH58iQ5zq9OHQ==', '1998-02-14', 'Female', 'eAmU/g6Nv8yPDKchEEWkRSwirJBo4dhsayfzahp/9Sk=', 'George Tam', '+852-9100-8010');

CREATE TABLE `staff_profiles` (
  `user_id` int NOT NULL,
  `clinic_id` int NOT NULL,
  `position` enum('Doctor','Nurse','Front Desk') NOT NULL DEFAULT 'Front Desk',
  PRIMARY KEY (`user_id`),
  KEY `clinic_id` (`clinic_id`),
  CONSTRAINT `staff_profiles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `staff_profiles_ibfk_2` FOREIGN KEY (`clinic_id`) REFERENCES `clinics` (`clinic_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `staff_profiles` (`user_id`, `clinic_id`, `position`) VALUES
(3, 1, 'Front Desk'),
(4, 2, 'Front Desk'),
(5, 3, 'Nurse'),
(6, 4, 'Nurse'),
(7, 5, 'Front Desk'),
(18, 1, 'Doctor'),
(23, 1, 'Doctor'),
(24, 1, 'Doctor'),
(19, 2, 'Doctor'),
(25, 2, 'Doctor'),
(26, 2, 'Doctor'),
(20, 3, 'Doctor'),
(27, 3, 'Doctor'),
(28, 3, 'Doctor'),
(21, 4, 'Doctor'),
(29, 4, 'Doctor'),
(30, 4, 'Doctor'),
(22, 5, 'Doctor'),
(31, 5, 'Doctor'),
(32, 5, 'Doctor');

CREATE TABLE `doctor_schedules` (
  `schedule_id` int NOT NULL AUTO_INCREMENT,
  `doctor_id` int NOT NULL,
  `day_of_week` enum('Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday') NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  PRIMARY KEY (`schedule_id`),
  UNIQUE KEY `doctor_day` (`doctor_id`, `day_of_week`),
  CONSTRAINT `doctor_schedules_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `staff_profiles` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `doctor_schedules` (`doctor_id`, `day_of_week`, `start_time`, `end_time`) VALUES
(18, 'Monday', '09:00:00', '17:00:00'),
(18, 'Tuesday', '09:00:00', '17:00:00'),
(18, 'Wednesday', '09:00:00', '17:00:00'),
(18, 'Thursday', '09:00:00', '17:00:00'),
(18, 'Friday', '09:00:00', '17:00:00'),
(19, 'Monday', '09:00:00', '17:00:00'),
(19, 'Tuesday', '09:00:00', '17:00:00'),
(19, 'Wednesday', '09:00:00', '17:00:00'),
(19, 'Thursday', '09:00:00', '17:00:00'),
(19, 'Friday', '09:00:00', '17:00:00'),
(19, 'Saturday', '09:00:00', '13:00:00'),
(20, 'Monday', '09:00:00', '17:00:00'),
(20, 'Tuesday', '09:00:00', '17:00:00'),
(20, 'Wednesday', '09:00:00', '17:00:00'),
(20, 'Thursday', '09:00:00', '17:00:00'),
(20, 'Friday', '09:00:00', '17:00:00'),
(20, 'Saturday', '09:00:00', '13:00:00'),
(21, 'Monday', '09:00:00', '17:00:00'),
(21, 'Tuesday', '09:00:00', '17:00:00'),
(21, 'Wednesday', '09:00:00', '17:00:00'),
(21, 'Thursday', '09:00:00', '17:00:00'),
(21, 'Friday', '09:00:00', '17:00:00'),
(22, 'Monday', '09:00:00', '17:00:00'),
(22, 'Tuesday', '09:00:00', '17:00:00'),
(22, 'Wednesday', '09:00:00', '17:00:00'),
(22, 'Thursday', '09:00:00', '17:00:00'),
(22, 'Friday', '09:00:00', '17:00:00'),
(23, 'Monday', '09:00:00', '17:00:00'),
(23, 'Tuesday', '09:00:00', '17:00:00'),
(23, 'Wednesday', '09:00:00', '17:00:00'),
(23, 'Thursday', '09:00:00', '17:00:00'),
(23, 'Friday', '09:00:00', '17:00:00'),
(23, 'Saturday', '09:00:00', '13:00:00'),
(24, 'Monday', '09:00:00', '17:00:00'),
(24, 'Tuesday', '09:00:00', '17:00:00'),
(24, 'Wednesday', '09:00:00', '17:00:00'),
(24, 'Thursday', '09:00:00', '17:00:00'),
(25, 'Monday', '09:00:00', '17:00:00'),
(25, 'Tuesday', '09:00:00', '17:00:00'),
(25, 'Wednesday', '09:00:00', '17:00:00'),
(25, 'Thursday', '09:00:00', '17:00:00'),
(25, 'Friday', '09:00:00', '17:00:00'),
(25, 'Saturday', '09:00:00', '13:00:00'),
(26, 'Tuesday', '09:00:00', '17:00:00'),
(26, 'Wednesday', '09:00:00', '17:00:00'),
(26, 'Thursday', '09:00:00', '17:00:00'),
(26, 'Friday', '09:00:00', '17:00:00'),
(26, 'Saturday', '09:00:00', '13:00:00'),
(27, 'Monday', '09:00:00', '17:00:00'),
(27, 'Tuesday', '09:00:00', '17:00:00'),
(27, 'Wednesday', '09:00:00', '17:00:00'),
(27, 'Thursday', '09:00:00', '17:00:00'),
(27, 'Friday', '09:00:00', '17:00:00'),
(27, 'Saturday', '09:00:00', '13:00:00'),
(28, 'Monday', '09:00:00', '17:00:00'),
(28, 'Tuesday', '09:00:00', '17:00:00'),
(28, 'Wednesday', '09:00:00', '17:00:00'),
(29, 'Monday', '09:00:00', '17:00:00'),
(29, 'Tuesday', '09:00:00', '17:00:00'),
(29, 'Wednesday', '09:00:00', '17:00:00'),
(29, 'Thursday', '09:00:00', '17:00:00'),
(29, 'Friday', '09:00:00', '17:00:00'),
(30, 'Monday', '09:00:00', '17:00:00'),
(30, 'Wednesday', '09:00:00', '17:00:00'),
(30, 'Friday', '09:00:00', '17:00:00'),
(31, 'Monday', '09:00:00', '17:00:00'),
(31, 'Tuesday', '09:00:00', '17:00:00'),
(31, 'Wednesday', '09:00:00', '17:00:00'),
(31, 'Thursday', '09:00:00', '17:00:00'),
(31, 'Friday', '09:00:00', '17:00:00'),
(32, 'Tuesday', '09:00:00', '17:00:00'),
(32, 'Wednesday', '09:00:00', '17:00:00'),
(32, 'Thursday', '09:00:00', '17:00:00'),
(32, 'Friday', '09:00:00', '17:00:00');

CREATE TABLE `admin_profiles` (
  `user_id` int NOT NULL,
  `clinic_id` int DEFAULT NULL,
  `admin_level` enum('System','Clinic') NOT NULL DEFAULT 'System',
  PRIMARY KEY (`user_id`),
  KEY `clinic_id` (`clinic_id`),
  CONSTRAINT `admin_profiles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `admin_profiles_ibfk_2` FOREIGN KEY (`clinic_id`) REFERENCES `clinics` (`clinic_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `admin_profiles` (`user_id`, `clinic_id`, `admin_level`) VALUES
(1, NULL, 'System'),
(2, 1, 'Clinic');


CREATE TABLE `clinic_services` (
  `clinic_service_id` int NOT NULL AUTO_INCREMENT,
  `clinic_id` int NOT NULL,
  `service_id` int NOT NULL,
  `quota_per_slot` int DEFAULT 5,
  `slot_duration_mins` int DEFAULT 30,
  `requires_approval` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`clinic_service_id`),
  UNIQUE KEY `clinic_id` (`clinic_id`, `service_id`),
  KEY `service_id` (`service_id`),
  CONSTRAINT `clinic_services_ibfk_1` FOREIGN KEY (`clinic_id`) REFERENCES `clinics` (`clinic_id`) ON DELETE CASCADE,
  CONSTRAINT `clinic_services_ibfk_2` FOREIGN KEY (`service_id`) REFERENCES `services` (`service_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `clinic_services` (`clinic_service_id`, `clinic_id`, `service_id`, `quota_per_slot`, `slot_duration_mins`, `requires_approval`) VALUES
(1, 1, 1, 8, 30, 0),
(2, 1, 2, 5, 20, 0),
(3, 1, 3, 6, 30, 0),
(4, 1, 4, 4, 15, 1),
(5, 2, 1, 8, 30, 0),
(6, 2, 2, 5, 20, 0),
(7, 2, 3, 6, 30, 0),
(8, 2, 4, 4, 15, 1),
(9, 3, 1, 10, 30, 0),
(10, 3, 2, 6, 20, 0),
(11, 3, 3, 8, 30, 0),
(12, 3, 4, 5, 15, 1),
(13, 4, 1, 8, 30, 0),
(14, 4, 2, 5, 20, 0),
(15, 4, 3, 5, 30, 0),
(16, 5, 1, 6, 30, 0),
(17, 5, 2, 4, 20, 0),
(18, 5, 3, 5, 30, 0);


CREATE TABLE `appointments` (
  `appointment_id` int NOT NULL AUTO_INCREMENT,
  `patient_id` int NOT NULL,
  `clinic_service_id` int NOT NULL,
  `doctor_id` int DEFAULT NULL,
  `appointment_date` date NOT NULL,
  `time_slot` time NOT NULL,
  `status` enum('Pending','Booked','Arrived','Completed','No-show','Cancelled') DEFAULT 'Booked',
  `cancel_reason` varchar(255) DEFAULT NULL,
  `cancelled_by` int DEFAULT NULL,
  `remarks` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`appointment_id`),
  UNIQUE KEY `patient_id` (`patient_id`, `appointment_date`, `time_slot`),
  KEY `clinic_service_id` (`clinic_service_id`),
  KEY `doctor_id` (`doctor_id`),
  KEY `cancelled_by` (`cancelled_by`),
  CONSTRAINT `appointments_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `appointments_ibfk_2` FOREIGN KEY (`clinic_service_id`) REFERENCES `clinic_services` (`clinic_service_id`) ON DELETE CASCADE,
  CONSTRAINT `appointments_ibfk_3` FOREIGN KEY (`cancelled_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL,
  CONSTRAINT `appointments_ibfk_4` FOREIGN KEY (`doctor_id`) REFERENCES `staff_profiles` (`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=81 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `appointments` (`appointment_id`, `patient_id`, `clinic_service_id`, `doctor_id`, `appointment_date`, `time_slot`, `status`, `cancel_reason`, `cancelled_by`, `remarks`, `created_at`) VALUES
(1, 8, 1, 18, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:00:00', 'Completed', NULL, NULL, 'Follow-up in 2 weeks recommended.', NOW()),
(2, 9, 5, 19, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:30:00', 'Completed', NULL, NULL, 'Routine check, all clear.', NOW()),
(3, 10, 9, NULL, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '10:00:00', 'Completed', NULL, NULL, NULL, NOW()),
(4, 11, 2, 18, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '10:30:00', 'Completed', NULL, NULL, 'Flu vaccine administered.', NOW()),
(5, 12, 6, NULL, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '11:00:00', 'Completed', NULL, NULL, NULL, NOW()),
(6, 13, 13, 21, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:00:00', 'Completed', NULL, NULL, NULL, NOW()),
(7, 14, 16, 22, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:30:00', 'Completed', NULL, NULL, NULL, NOW()),
(8, 15, 3, NULL, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '10:00:00', 'Completed', NULL, NULL, 'Blood pressure slightly elevated. Monitor.', NOW()),
(9, 16, 10, 20, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '10:30:00', 'Completed', NULL, NULL, NULL, NOW()),
(10, 17, 14, 21, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '11:00:00', 'Completed', NULL, NULL, 'Hepatitis B booster given.', NOW()),
(11, 8, 5, 19, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:00:00', 'No-show', NULL, NULL, NULL, NOW()),
(12, 9, 9, NULL, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:30:00', 'No-show', NULL, NULL, NULL, NOW()),
(13, 10, 1, 18, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '10:00:00', 'No-show', NULL, NULL, NULL, NOW()),
(14, 11, 13, NULL, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '10:30:00', 'No-show', NULL, NULL, NULL, NOW()),
(15, 12, 2, 18, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '11:00:00', 'Cancelled', 'Personal reasons', 12, NULL, NOW()),
(16, 13, 6, NULL, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '11:30:00', 'Cancelled', 'Recovered before appointment', 13, NULL, NOW()),
(17, 14, 3, 18, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:00:00', 'Cancelled', 'Doctor unavailable', 3, NULL, NOW()),
(18, 15, 7, NULL, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:30:00', 'Cancelled', 'Service suspended for the day', 4, NULL, NOW()),
(19, 16, 9, 20, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '10:00:00', 'Completed', NULL, NULL, 'Prescribed antibiotics for throat infection.', NOW()),
(20, 17, 1, NULL, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '10:30:00', 'Completed', NULL, NULL, NULL, NOW()),
(21, 8, 6, 19, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '11:00:00', 'Completed', NULL, NULL, NULL, NOW()),
(22, 9, 2, 18, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '11:30:00', 'Completed', NULL, NULL, 'COVID-19 booster administered.', NOW()),
(23, 8, 1, 18, CURDATE(), '09:00:00', 'Arrived', NULL, NULL, NULL, NOW()),
(24, 9, 5, 19, CURDATE(), '09:30:00', 'Arrived', NULL, NULL, NULL, NOW()),
(25, 10, 9, NULL, CURDATE(), '10:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(26, 11, 13, 21, CURDATE(), '10:30:00', 'Booked', NULL, NULL, NULL, NOW()),
(27, 12, 16, NULL, CURDATE(), '11:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(28, 13, 2, 18, CURDATE(), '11:30:00', 'Booked', NULL, NULL, NULL, NOW()),
(29, 16, 3, NULL, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:00:00', 'Completed', NULL, NULL, NULL, NOW()),
(30, 17, 10, 20, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:30:00', 'Completed', NULL, NULL, NULL, NOW()),
(31, 8, 14, 21, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '10:00:00', 'Completed', NULL, NULL, NULL, NOW()),
(32, 9, 17, NULL, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '10:30:00', 'Completed', NULL, NULL, NULL, NOW()),
(33, 10, 4, 18, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '11:00:00', 'Completed', NULL, NULL, NULL, NOW()),
(34, 11, 8, NULL, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:00:00', 'Completed', NULL, NULL, NULL, NOW()),
(35, 12, 11, 20, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:30:00', 'Completed', NULL, NULL, NULL, NOW()),
(36, 13, 15, NULL, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '10:00:00', 'Completed', NULL, NULL, NULL, NOW()),
(37, 14,  1, 18, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(38, 15,  1, 23, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(39, 16,  1, 24, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(40, 17,  1, 18, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(41,  8,  1, 23, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(42,  9,  1, NULL, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(43, 10,  1, 18, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(44, 11,  1, 23, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(45, 12,  1, 24, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(46, 13,  1, 18, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(47, 14,  1, 23, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(48, 15,  1, NULL, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(49, 16,  1, 24, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(50, 17,  1, 18, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(51,  8,  1, 23, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:30:00', 'Booked', NULL, NULL, NULL, NOW()),
(52,  9,  1, 24, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:30:00', 'Booked', NULL, NULL, NULL, NOW()),
(53, 10,  1, 18, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:30:00', 'Booked', NULL, NULL, NULL, NOW()),
(54, 11,  1, 23, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:30:00', 'Booked', NULL, NULL, NULL, NOW()),
(55, 12,  1, 24, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:30:00', 'Booked', NULL, NULL, NULL, NOW()),
(56, 13,  1, NULL, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:30:00', 'Booked', NULL, NULL, NULL, NOW()),
(57, 14,  1, 18, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:30:00', 'Booked', NULL, NULL, NULL, NOW()),
(58, 15,  1, 23, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:30:00', 'Booked', NULL, NULL, NULL, NOW()),
(59, 16,  1, 18, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(60, 17,  1, 23, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(61,  8,  1, 24, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(62,  9,  1, 18, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(63, 10,  1, 23, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(64, 11, 5, 19, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(65, 12, 5, 25, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(66, 13, 5, 26, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(67, 14, 5, 19, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(68, 15, 5, 25, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(69, 16, 5, NULL, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(70, 17, 5, 26, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(71,  8, 5, 19, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(72,  9, 5, 25, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:30:00', 'Booked', NULL, NULL, NULL, NOW()),
(73, 10, 5, 19, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:30:00', 'Booked', NULL, NULL, NULL, NOW()),
(74, 11, 5, 26, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:30:00', 'Booked', NULL, NULL, NULL, NOW()),
(75, 12, 5, 25, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:30:00', 'Booked', NULL, NULL, NULL, NOW()),
(76, 13, 2, 18, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '08:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(77, 14, 2, 23, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '08:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(78, 15, 2, 24, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '08:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(79, 16, 2, 18, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '08:00:00', 'Booked', NULL, NULL, NULL, NOW()),
(80, 17, 2, 23, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '08:00:00', 'Booked', NULL, NULL, NULL, NOW());


CREATE TABLE `walkin_queues` (
  `queue_id` int NOT NULL AUTO_INCREMENT,
  `patient_id` int NOT NULL,
  `clinic_service_id` int NOT NULL,
  `queue_date` date NOT NULL,
  `queue_number` int NOT NULL,
  `status` enum('Waiting','Called','Skipped','Served') DEFAULT 'Waiting',
  `joined_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`queue_id`),
  UNIQUE KEY `uq_patient_service_date` (`patient_id`, `clinic_service_id`, `queue_date`),
  UNIQUE KEY `uq_queue_num` (`clinic_service_id`, `queue_date`, `queue_number`),
  KEY `clinic_service_id` (`clinic_service_id`),
  CONSTRAINT `walkin_queues_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `walkin_queues_ibfk_2` FOREIGN KEY (`clinic_service_id`) REFERENCES `clinic_services` (`clinic_service_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `walkin_queues` (`queue_id`, `patient_id`, `clinic_service_id`, `queue_date`, `queue_number`, `status`, `joined_at`) VALUES
(1, 8, 1, '2026-03-10', 1, 'Served', '2026-03-10 06:00:00'),
(2, 9, 1, '2026-03-10', 2, 'Served', '2026-03-10 06:00:00'),
(3, 10, 1, '2026-03-10', 3, 'Called', '2026-03-10 06:00:00'),
(4, 11, 1, '2026-03-10', 4, 'Waiting', '2026-03-10 06:00:00'),
(5, 12, 1, '2026-03-10', 5, 'Waiting', '2026-03-10 06:00:00'),
(6, 13, 6, '2026-03-10', 1, 'Served', '2026-03-10 06:00:00'),
(7, 14, 6, '2026-03-10', 2, 'Called', '2026-03-10 06:00:00'),
(8, 15, 6, '2026-03-10', 3, 'Waiting', '2026-03-10 06:00:00'),
(9, 16, 9, '2026-03-10', 1, 'Served', '2026-03-10 06:00:00'),
(10, 17, 9, '2026-03-10', 2, 'Skipped', '2026-03-10 06:00:00'),
(11, 8, 9, '2026-03-10', 3, 'Waiting', '2026-03-10 06:00:00'),
(12, 9, 15, '2026-03-10', 1, 'Called', '2026-03-10 06:00:00'),
(13, 10, 15, '2026-03-10', 2, 'Waiting', '2026-03-10 06:00:00'),
(14, 11, 1, '2026-03-09', 1, 'Served', '2026-03-10 06:00:00'),
(15, 12, 5, '2026-03-09', 1, 'Served', '2026-03-10 06:00:00'),
(16, 13, 9, '2026-03-09', 1, 'Served', '2026-03-10 06:00:00');


INSERT IGNORE INTO `appointments` (`patient_id`, `clinic_service_id`, `doctor_id`, `appointment_date`, `time_slot`, `status`) VALUES
(10, 1, 18, CURDATE(), '09:00:00', 'Arrived'),
(11, 5, 19, CURDATE(), '09:30:00', 'Arrived'),
(12, 9, NULL, CURDATE(), '10:00:00', 'Booked'),
(13, 13, 21, CURDATE(), '10:30:00', 'Booked'),
(14, 16, NULL, CURDATE(), '11:00:00', 'Booked');

INSERT IGNORE INTO `walkin_queues` (`patient_id`, `clinic_service_id`, `queue_date`, `queue_number`, `status`) VALUES
(8,  1, CURDATE(), 1, 'Served'),
(9,  1, CURDATE(), 2, 'Called'),
(10, 1, CURDATE(), 3, 'Waiting'),
(11, 1, CURDATE(), 4, 'Waiting'),
(13, 6, CURDATE(), 1, 'Called'),
(14, 6, CURDATE(), 2, 'Waiting'),
(16, 9, CURDATE(), 1, 'Served'),
(17, 9, CURDATE(), 2, 'Waiting'),
(12, 15, CURDATE(), 1, 'Called'),
(15, 15, CURDATE(), 2, 'Waiting');


CREATE TABLE `notifications` (
  `notification_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `message` text NOT NULL,
  `type` enum('Appointment','Queue','System','Reminder') NOT NULL,
  `is_read` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`notification_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `notifications` (`notification_id`, `user_id`, `message`, `type`, `is_read`, `created_at`) VALUES
(1, 10, 'Your appointment at Chai Wan Community Clinic at 09:00 has been confirmed.', 'Appointment', 0, NOW()),
(2, 11, 'Your appointment at Tseung Kwan O Community Clinic at 09:30 has been confirmed.', 'Appointment', 0, NOW()),
(3, 12, 'Your appointment at Sha Tin Community Clinic at 10:00 has been confirmed.', 'Appointment', 1, NOW()),
(4, 13, 'Your appointment at Tuen Mun Community Clinic at 10:30 has been confirmed.', 'Appointment', 1, NOW()),
(5, 12, 'Reminder: You have an upcoming appointment at Sha Tin Community Clinic at 10:00.', 'Reminder', 0, NOW()),
(6, 13, 'Reminder: You have an upcoming appointment at Tuen Mun Community Clinic at 10:30.', 'Reminder', 0, NOW()),
(7, 14, 'Reminder: You have an upcoming appointment at Tsing Yi Community Clinic at 11:00.', 'Reminder', 1, NOW()),
(8, 14, 'Your appointment at Chai Wan Community Clinic has been cancelled by the clinic. Reason: Doctor unavailable.', 'Appointment', 1, NOW()),
(9, 15, 'Your appointment at Tseung Kwan O Community Clinic has been cancelled by the clinic. Reason: Service suspended for the day.', 'Appointment', 1, NOW()),
(10, 12, 'Your appointment has been successfully cancelled.', 'Appointment', 1, NOW()),
(11, 13, 'Your appointment has been successfully cancelled.', 'Appointment', 1, NOW()),
(12, 10, 'Queue update: You are now number 3 in the queue at Chai Wan Community Clinic. You have been called.', 'Queue', 0, NOW()),
(13, 11, 'Queue update: You are number 4 in the queue at Chai Wan Community Clinic. Estimated wait: 20 minutes.', 'Queue', 0, NOW()),
(14, 14, 'Queue update: You are number 2 in the queue at Tseung Kwan O Community Clinic. You have been called.', 'Queue', 0, NOW()),
(15, 9, 'Queue update: You are number 1 in the queue at Tuen Mun Community Clinic. You have been called.', 'Queue', 0, NOW()),
(16, 8, 'You were marked as No-show for your appointment at Tseung Kwan O Community Clinic.', 'Appointment', 1, NOW()),
(17, 9, 'You were marked as No-show for your appointment at Sha Tin Community Clinic.', 'Appointment', 1, NOW()),
(18, 10, 'You were marked as No-show for your appointment at Chai Wan Community Clinic.', 'Appointment', 1, NOW());


CREATE TABLE `audit_logs` (
  `log_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `action_description` text NOT NULL,
  `action_timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`log_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `audit_logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `audit_logs` (`log_id`, `user_id`, `action_description`, `action_timestamp`) VALUES
(1, 3, 'Marked patient Henry Tsang (patient01) as Arrived for appointment #23.', NOW()),
(2, 4, 'Marked patient Iris Kwok (patient02) as Arrived for appointment #24.', NOW()),
(3, 3, 'Called queue number 3 (James Liu) at Chai Wan – General Consultation.', NOW()),
(4, 5, 'Marked queue number 2 (Queen Tam) as Skipped at Sha Tin – General Consultation.', NOW()),
(5, 4, 'Called queue number 2 (Nathan Hui) at Tseung Kwan O – Vaccination.', NOW()),
(6, 6, 'Called queue number 1 (Iris Kwok) at Tuen Mun – Basic Health Screening.', NOW()),
(7, 3, 'Cancelled appointment #17 for Nathan Hui. Reason: Doctor unavailable.', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(8, 4, 'Cancelled appointment #18 for Olivia Poon. Reason: Service suspended for the day.', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9, 5, 'Marked patient Peter Siu (patient09) as Completed after General Consultation.', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(10, 1, 'Created new staff account: grace.ho@cchc.hk (Staff - Tsing Yi Community Clinic).', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(11, 1, 'Disabled walk-in queue for Tsing Yi Community Clinic.', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(12, 2, 'Updated quota for Sha Tin - General Consultation from 8 to 10 per slot.', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(13, 1, 'Generated no-show report for recent appointments.', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(14, 2, 'Updated opening hours for Sha Tin Community Clinic to 08:00-17:00.', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(15, 1, 'Reset password for patient account: patient05 (Leo Mak).', DATE_SUB(NOW(), INTERVAL 1 DAY));


CREATE TABLE `settings` (
  `setting_key` varchar(50) NOT NULL,
  `setting_value` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `settings` (`setting_key`, `setting_value`, `description`) VALUES
('max_active_bookings', '8', 'Maximum number of active (Booked/Arrived) appointments per patient'),
('cancellation_cutoff_hours', '2', 'Minimum hours before appointment that patient can cancel'),
('reschedule_cutoff_hours', '24', 'Minimum hours before appointment that patient can reschedule');


CREATE TABLE `password_reset_tokens` (
  `token_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `otp_code` varchar(6) NOT NULL,
  `expires_at` datetime NOT NULL,
  `used` tinyint NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`token_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `password_reset_tokens_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `incidents` (
  `incident_id`      int NOT NULL AUTO_INCREMENT,
  `reported_by`      int NOT NULL,
  `clinic_id`        int NOT NULL,
  `patient_id`       int DEFAULT NULL,
  `appointment_id`   int DEFAULT NULL,
  `category`         enum('Equipment','Patient','Safety','IT','Other') NOT NULL DEFAULT 'Other',
  `severity`         enum('Low','Medium','High','Critical') NOT NULL DEFAULT 'Low',
  `description`      text NOT NULL,
  `status`           enum('Open','InProgress','Resolved') NOT NULL DEFAULT 'Open',
  `resolution_notes` text DEFAULT NULL,
  `created_at`       timestamp NOT NULL DEFAULT current_timestamp(),
  `resolved_at`      timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`incident_id`),
  KEY `idx_inc_clinic_status` (`clinic_id`, `status`),
  KEY `idx_inc_patient` (`patient_id`),
  KEY `idx_inc_reporter` (`reported_by`),
  KEY `idx_inc_appt` (`appointment_id`),
  CONSTRAINT `incidents_ibfk_1` FOREIGN KEY (`reported_by`)    REFERENCES `users` (`user_id`)              ON DELETE CASCADE,
  CONSTRAINT `incidents_ibfk_2` FOREIGN KEY (`clinic_id`)      REFERENCES `clinics` (`clinic_id`)          ON DELETE CASCADE,
  CONSTRAINT `incidents_ibfk_3` FOREIGN KEY (`patient_id`)     REFERENCES `users` (`user_id`)              ON DELETE SET NULL,
  CONSTRAINT `incidents_ibfk_4` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`appointment_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `incidents` (`incident_id`, `reported_by`, `clinic_id`, `patient_id`, `appointment_id`, `category`, `severity`, `description`, `status`, `resolution_notes`, `created_at`, `resolved_at`) VALUES
(1, 3, 1, NULL, NULL, 'Equipment', 'Medium', 'Printer offline at front desk; tickets cannot be printed.', 'Resolved', 'Rebooted printer and replaced toner; test prints OK.', NOW() - INTERVAL 4 HOUR, NOW() - INTERVAL 3 HOUR),
(2, 4, 2, 9, 11, 'Patient', 'Low', 'Patient became dizzy while waiting; attended by nurse; no further action required.', 'Resolved', 'Patient monitored and discharged; advised GP follow-up.', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
(3, 18, 1, NULL, NULL, 'IT', 'High', 'System timeout errors on booking page causing delays and duplicate bookings.', 'InProgress', NULL, NOW() - INTERVAL 1 HOUR, NULL),
(4, 1, 3, 16, 29, 'Safety', 'Critical', 'Strong gas smell near pharmacy area; evacuation carried out.', 'Resolved', 'Evacuation, maintenance fixed loose gas connector; facility cleared.', NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 9 DAY),
(5, 2, 2, NULL, NULL, 'Other', 'Low', 'Lost-and-found: sunglasses reported at front desk.', 'Open', NULL, NOW() - INTERVAL 6 HOUR, NULL),
(6, 7, 5, NULL, NULL, 'Equipment', 'Low', 'Hand sanitizer dispenser in waiting area empty.', 'Resolved', 'Refilled dispenser and ordered extra stock.', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 2 DAY),
(7, 3, 1, 8, 23, 'Patient', 'Medium', 'Complaint about long wait time and missed alert for appointment #23.', 'Open', NULL, NOW() - INTERVAL 30 MINUTE, NULL);


DELETE FROM walkin_queues WHERE queue_date = CURDATE();

INSERT INTO walkin_queues (patient_id, clinic_service_id, queue_date, queue_number, status, joined_at) VALUES
  ( 8, 1, CURDATE(), 1, 'Served',  NOW() - INTERVAL 90 MINUTE),
  ( 9, 1, CURDATE(), 2, 'Served',  NOW() - INTERVAL 75 MINUTE),
  (10, 1, CURDATE(), 3, 'Skipped', NOW() - INTERVAL 60 MINUTE),
  (11, 1, CURDATE(), 4, 'Called',  NOW() - INTERVAL 45 MINUTE),
  (12, 1, CURDATE(), 5, 'Waiting', NOW() - INTERVAL 30 MINUTE),
  (13, 1, CURDATE(), 6, 'Waiting', NOW() - INTERVAL 15 MINUTE),
  (14, 1, CURDATE(), 7, 'Waiting', NOW() - INTERVAL  5 MINUTE),
  (15, 9, CURDATE(), 1, 'Served',  NOW() - INTERVAL 80 MINUTE),
  (16, 9, CURDATE(), 2, 'Called',  NOW() - INTERVAL 40 MINUTE),
  (17, 9, CURDATE(), 3, 'Waiting', NOW() - INTERVAL 10 MINUTE);

DELETE FROM notifications WHERE user_id IN (1, 3, 8, 9);

INSERT INTO notifications (user_id, type, message, is_read, created_at) VALUES
  (8, 'Appointment', CONCAT('Your appointment at Chai Wan Community Clinic on ',
       DATE_FORMAT(CURDATE() + INTERVAL 2 DAY, '%Y-%m-%d'),
       ' 10:00 has been CONFIRMED.'),                                  0, NOW() - INTERVAL  5 MINUTE),
  (8, 'Reminder',    'Reminder: you have an upcoming appointment tomorrow at 09:30 (General Consultation, Chai Wan).',
                                                                       0, NOW() - INTERVAL 30 MINUTE),
  (8, 'Queue',       'You have joined the walk-in queue for Chai Wan - General Consultation. Your number is #1.',
                                                                       1, NOW() - INTERVAL 90 MINUTE),
  (8, 'Queue',       'Your queue ticket #1 has been called. Please proceed to the consultation room.',
                                                                       0, NOW() - INTERVAL 70 MINUTE),
  (8, 'Appointment', CONCAT('Your appointment on ',
       DATE_FORMAT(CURDATE() - INTERVAL 1 DAY, '%Y-%m-%d'),
       ' has been CANCELLED by the clinic. Reason: Doctor unavailable.'), 1, NOW() - INTERVAL 1 DAY);

INSERT INTO notifications (user_id, type, message, is_read, created_at) VALUES
  (9, 'Appointment', CONCAT('Your reschedule request has been APPROVED. New time: ',
       DATE_FORMAT(CURDATE() + INTERVAL 3 DAY, '%Y-%m-%d'), ' 14:30.'), 0, NOW() - INTERVAL 15 MINUTE),
  (9, 'Reminder',    'Reminder: appointment in 24 hours at Tseung Kwan O Community Clinic.',
                                                                       0, NOW() - INTERVAL  2 HOUR);

INSERT INTO notifications (user_id, type, message, is_read, created_at) VALUES
  (3, 'System', 'Patient Henry Tsang joined the walk-in queue (#1, General Consultation).',
                                                                       0, NOW() - INTERVAL 90 MINUTE),
  (3, 'System', 'A new booking request requires your approval for Basic Health Screening.',
                                                                       0, NOW() - INTERVAL 20 MINUTE),
  (3, 'System', 'Incident #4 (Doctor unavailable) has been marked as Resolved by admin.',
                                                                       1, NOW() - INTERVAL 4 HOUR);

INSERT INTO notifications (user_id, type, message, is_read, created_at) VALUES
  (1, 'System',   'Daily report ready: 5 no-shows recorded across all clinics yesterday.',
                                                                       0, NOW() - INTERVAL 1 HOUR),
  (1, 'System',   'New incident reported by staff_cw: "Printer offline at front desk".',
                                                                       0, NOW() - INTERVAL 25 MINUTE),
  (1, 'Reminder', 'Public holiday next Monday - please confirm clinic schedules are updated.',
                                                                       1, NOW() - INTERVAL 6 HOUR);

INSERT IGNORE INTO users (username, password_hash, role, full_name, email, phone) VALUES
  ('demo_patient', '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW',
   'Patient', 'Demo Patient', 'demo.patient@cchc.hk', '+852-9000-0001'),
  ('demo_staff',   '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW',
   'Staff',   'Demo Staff',   'demo.staff@cchc.hk',   '+852-9000-0002'),
  ('demo_admin',   '$2a$10$b9McRnFW05H.midNrv.EZe6frHoPHMip.CV8I4ih7vXv9D1xCtTmW',
   'Admin',   'Demo Admin',   'demo.admin@cchc.hk',   '+852-9000-0003');


COMMIT;
