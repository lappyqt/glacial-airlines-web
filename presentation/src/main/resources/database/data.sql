-- Схема flight
INSERT INTO flight.airport(id, iata_code, name, city, country, offset_utc) VALUES
    (1, 'DME', 'Домодедово', 'Москва', 'Россия', +3),
    (2, 'SVX', 'Кольцово', 'Екатеринбург', 'Россия', +5),
    (3, 'FRA', 'Франкфурт-на-Майне', 'Франкфурт-на-Майне', 'Германия', +2);

INSERT INTO flight.route(id, departure_airport_id, arrival_airport_id) VALUES
    (1, 1, 2),
    (2, 1, 3),
    (3, 2, 1),
    (4, 2, 3),
    (5, 3, 1),
    (6, 3, 2);

INSERT INTO flight.aircraft(id, registration_number, model, economy_seats, emergency_seats, business_seats) VALUES
    (1, 'RA-73598', 'Airbus A320', 66, 12, 36),
    (2, 'RA-73599', 'Boeing 737 MAX', 96, 12, 12);

-- Airbus A320
INSERT INTO flight.seat (id, aircraft_id, seat_number, row_number, seat_letter, seat_class) VALUES
    -- Бизнес (ряды 1-6)
    (1,  1,'1A',1,'A','BUSINESS'),(2,  1,'1B',1,'B','BUSINESS'),(3,  1,'1C',1,'C','BUSINESS'),
    (4,  1,'1D',1,'D','BUSINESS'),(5,  1,'1E',1,'E','BUSINESS'),(6,  1,'1F',1,'F','BUSINESS'),
    (7,  1,'2A',2,'A','BUSINESS'),(8,  1,'2B',2,'B','BUSINESS'),(9,  1,'2C',2,'C','BUSINESS'),
    (10, 1,'2D',2,'D','BUSINESS'),(11, 1,'2E',2,'E','BUSINESS'),(12, 1,'2F',2,'F','BUSINESS'),
    (13, 1,'3A',3,'A','BUSINESS'),(14, 1,'3B',3,'B','BUSINESS'),(15, 1,'3C',3,'C','BUSINESS'),
    (16, 1,'3D',3,'D','BUSINESS'),(17, 1,'3E',3,'E','BUSINESS'),(18, 1,'3F',3,'F','BUSINESS'),
    (19, 1,'4A',4,'A','BUSINESS'),(20, 1,'4B',4,'B','BUSINESS'),(21, 1,'4C',4,'C','BUSINESS'),
    (22, 1,'4D',4,'D','BUSINESS'),(23, 1,'4E',4,'E','BUSINESS'),(24, 1,'4F',4,'F','BUSINESS'),
    (25, 1,'5A',5,'A','BUSINESS'),(26, 1,'5B',5,'B','BUSINESS'),(27, 1,'5C',5,'C','BUSINESS'),
    (28, 1,'5D',5,'D','BUSINESS'),(29, 1,'5E',5,'E','BUSINESS'),(30, 1,'5F',5,'F','BUSINESS'),
    (31, 1,'6A',6,'A','BUSINESS'),(32, 1,'6B',6,'B','BUSINESS'),(33, 1,'6C',6,'C','BUSINESS'),
    (34, 1,'6D',6,'D','BUSINESS'),(35, 1,'6E',6,'E','BUSINESS'),(36, 1,'6F',6,'F','BUSINESS'),
    -- Эконом (ряды 7-11)
    (37, 1,'7A', 7,'A','ECONOMY'),(38, 1,'7B', 7,'B','ECONOMY'),(39, 1,'7C', 7,'C','ECONOMY'),
    (40, 1,'7D', 7,'D','ECONOMY'),(41, 1,'7E', 7,'E','ECONOMY'),(42, 1,'7F', 7,'F','ECONOMY'),
    (43, 1,'8A', 8,'A','ECONOMY'),(44, 1,'8B', 8,'B','ECONOMY'),(45, 1,'8C', 8,'C','ECONOMY'),
    (46, 1,'8D', 8,'D','ECONOMY'),(47, 1,'8E', 8,'E','ECONOMY'),(48, 1,'8F', 8,'F','ECONOMY'),
    (49, 1,'9A', 9,'A','ECONOMY'),(50, 1,'9B', 9,'B','ECONOMY'),(51, 1,'9C', 9,'C','ECONOMY'),
    (52, 1,'9D', 9,'D','ECONOMY'),(53, 1,'9E', 9,'E','ECONOMY'),(54, 1,'9F', 9,'F','ECONOMY'),
    (55, 1,'10A',10,'A','ECONOMY'),(56, 1,'10B',10,'B','ECONOMY'),(57, 1,'10C',10,'C','ECONOMY'),
    (58, 1,'10D',10,'D','ECONOMY'),(59, 1,'10E',10,'E','ECONOMY'),(60, 1,'10F',10,'F','ECONOMY'),
    (61, 1,'11A',11,'A','ECONOMY'),(62, 1,'11B',11,'B','ECONOMY'),(63, 1,'11C',11,'C','ECONOMY'),
    (64, 1,'11D',11,'D','ECONOMY'),(65, 1,'11E',11,'E','ECONOMY'),(66, 1,'11F',11,'F','ECONOMY'),
    -- Аварийные (ряды 12, 14)
    (67, 1,'12A',12,'A','EMERGENCY'),(68, 1,'12B',12,'B','EMERGENCY'),(69, 1,'12C',12,'C','EMERGENCY'),
    (70, 1,'12D',12,'D','EMERGENCY'),(71, 1,'12E',12,'E','EMERGENCY'),(72, 1,'12F',12,'F','EMERGENCY'),
    (73, 1,'14A',14,'A','EMERGENCY'),(74, 1,'14B',14,'B','EMERGENCY'),(75, 1,'14C',14,'C','EMERGENCY'),
    (76, 1,'14D',14,'D','EMERGENCY'),(77, 1,'14E',14,'E','EMERGENCY'),(78, 1,'14F',14,'F','EMERGENCY'),
    -- Эконом (ряды 15-20)
    (79, 1,'15A',15,'A','ECONOMY'),(80, 1,'15B',15,'B','ECONOMY'),(81, 1,'15C',15,'C','ECONOMY'),
    (82, 1,'15D',15,'D','ECONOMY'),(83, 1,'15E',15,'E','ECONOMY'),(84, 1,'15F',15,'F','ECONOMY'),
    (85, 1,'16A',16,'A','ECONOMY'),(86, 1,'16B',16,'B','ECONOMY'),(87, 1,'16C',16,'C','ECONOMY'),
    (88, 1,'16D',16,'D','ECONOMY'),(89, 1,'16E',16,'E','ECONOMY'),(90, 1,'16F',16,'F','ECONOMY'),
    (91, 1,'17A',17,'A','ECONOMY'),(92, 1,'17B',17,'B','ECONOMY'),(93, 1,'17C',17,'C','ECONOMY'),
    (94, 1,'17D',17,'D','ECONOMY'),(95, 1,'17E',17,'E','ECONOMY'),(96, 1,'17F',17,'F','ECONOMY'),
    (97, 1,'18A',18,'A','ECONOMY'),(98, 1,'18B',18,'B','ECONOMY'),(99, 1,'18C',18,'C','ECONOMY'),
    (100,1,'18D',18,'D','ECONOMY'),(101,1,'18E',18,'E','ECONOMY'),(102,1,'18F',18,'F','ECONOMY'),
    (103,1,'19A',19,'A','ECONOMY'),(104,1,'19B',19,'B','ECONOMY'),(105,1,'19C',19,'C','ECONOMY'),
    (106,1,'19D',19,'D','ECONOMY'),(107,1,'19E',19,'E','ECONOMY'),(108,1,'19F',19,'F','ECONOMY'),
    (109,1,'20A',20,'A','ECONOMY'),(110,1,'20B',20,'B','ECONOMY'),(111,1,'20C',20,'C','ECONOMY'),
    (112,1,'20D',20,'D','ECONOMY'),(113,1,'20E',20,'E','ECONOMY'),(114,1,'20F',20,'F','ECONOMY');

-- Boeing 737 MAX
INSERT INTO flight.seat (id, aircraft_id, seat_number, row_number, seat_letter, seat_class) VALUES
    -- Бизнес (ряды 1-2)
    (115,2,'1A',1,'A','BUSINESS'),(116,2,'1B',1,'B','BUSINESS'),(117,2,'1C',1,'C','BUSINESS'),
    (118,2,'1D',1,'D','BUSINESS'),(119,2,'1E',1,'E','BUSINESS'),(120,2,'1F',1,'F','BUSINESS'),
    (121,2,'2A',2,'A','BUSINESS'),(122,2,'2B',2,'B','BUSINESS'),(123,2,'2C',2,'C','BUSINESS'),
    (124,2,'2D',2,'D','BUSINESS'),(125,2,'2E',2,'E','BUSINESS'),(126,2,'2F',2,'F','BUSINESS'),
    -- Эконом (ряды 3-12)
    (127,2,'3A', 3,'A','ECONOMY'),(128,2,'3B', 3,'B','ECONOMY'),(129,2,'3C', 3,'C','ECONOMY'),
    (130,2,'3D', 3,'D','ECONOMY'),(131,2,'3E', 3,'E','ECONOMY'),(132,2,'3F', 3,'F','ECONOMY'),
    (133,2,'4A', 4,'A','ECONOMY'),(134,2,'4B', 4,'B','ECONOMY'),(135,2,'4C', 4,'C','ECONOMY'),
    (136,2,'4D', 4,'D','ECONOMY'),(137,2,'4E', 4,'E','ECONOMY'),(138,2,'4F', 4,'F','ECONOMY'),
    (139,2,'5A', 5,'A','ECONOMY'),(140,2,'5B', 5,'B','ECONOMY'),(141,2,'5C', 5,'C','ECONOMY'),
    (142,2,'5D', 5,'D','ECONOMY'),(143,2,'5E', 5,'E','ECONOMY'),(144,2,'5F', 5,'F','ECONOMY'),
    (145,2,'6A', 6,'A','ECONOMY'),(146,2,'6B', 6,'B','ECONOMY'),(147,2,'6C', 6,'C','ECONOMY'),
    (148,2,'6D', 6,'D','ECONOMY'),(149,2,'6E', 6,'E','ECONOMY'),(150,2,'6F', 6,'F','ECONOMY'),
    (151,2,'7A', 7,'A','ECONOMY'),(152,2,'7B', 7,'B','ECONOMY'),(153,2,'7C', 7,'C','ECONOMY'),
    (154,2,'7D', 7,'D','ECONOMY'),(155,2,'7E', 7,'E','ECONOMY'),(156,2,'7F', 7,'F','ECONOMY'),
    (157,2,'8A', 8,'A','ECONOMY'),(158,2,'8B', 8,'B','ECONOMY'),(159,2,'8C', 8,'C','ECONOMY'),
    (160,2,'8D', 8,'D','ECONOMY'),(161,2,'8E', 8,'E','ECONOMY'),(162,2,'8F', 8,'F','ECONOMY'),
    (163,2,'9A', 9,'A','ECONOMY'),(164,2,'9B', 9,'B','ECONOMY'),(165,2,'9C', 9,'C','ECONOMY'),
    (166,2,'9D', 9,'D','ECONOMY'),(167,2,'9E', 9,'E','ECONOMY'),(168,2,'9F', 9,'F','ECONOMY'),
    (169,2,'10A',10,'A','ECONOMY'),(170,2,'10B',10,'B','ECONOMY'),(171,2,'10C',10,'C','ECONOMY'),
    (172,2,'10D',10,'D','ECONOMY'),(173,2,'10E',10,'E','ECONOMY'),(174,2,'10F',10,'F','ECONOMY'),
    (175,2,'11A',11,'A','ECONOMY'),(176,2,'11B',11,'B','ECONOMY'),(177,2,'11C',11,'C','ECONOMY'),
    (178,2,'11D',11,'D','ECONOMY'),(179,2,'11E',11,'E','ECONOMY'),(180,2,'11F',11,'F','ECONOMY'),
    (181,2,'12A',12,'A','ECONOMY'),(182,2,'12B',12,'B','ECONOMY'),(183,2,'12C',12,'C','ECONOMY'),
    (184,2,'12D',12,'D','ECONOMY'),(185,2,'12E',12,'E','ECONOMY'),(186,2,'12F',12,'F','ECONOMY'),
    -- Аварийные (ряды 13-14)
    (187,2,'13A',13,'A','EMERGENCY'),(188,2,'13B',13,'B','EMERGENCY'),(189,2,'13C',13,'C','EMERGENCY'),
    (190,2,'13D',13,'D','EMERGENCY'),(191,2,'13E',13,'E','EMERGENCY'),(192,2,'13F',13,'F','EMERGENCY'),
    (193,2,'14A',14,'A','EMERGENCY'),(194,2,'14B',14,'B','EMERGENCY'),(195,2,'14C',14,'C','EMERGENCY'),
    (196,2,'14D',14,'D','EMERGENCY'),(197,2,'14E',14,'E','EMERGENCY'),(198,2,'14F',14,'F','EMERGENCY'),
    -- Эконом (ряды 15-20)
    (199,2,'15A',15,'A','ECONOMY'),(200,2,'15B',15,'B','ECONOMY'),(201,2,'15C',15,'C','ECONOMY'),
    (202,2,'15D',15,'D','ECONOMY'),(203,2,'15E',15,'E','ECONOMY'),(204,2,'15F',15,'F','ECONOMY'),
    (205,2,'16A',16,'A','ECONOMY'),(206,2,'16B',16,'B','ECONOMY'),(207,2,'16C',16,'C','ECONOMY'),
    (208,2,'16D',16,'D','ECONOMY'),(209,2,'16E',16,'E','ECONOMY'),(210,2,'16F',16,'F','ECONOMY'),
    (211,2,'17A',17,'A','ECONOMY'),(212,2,'17B',17,'B','ECONOMY'),(213,2,'17C',17,'C','ECONOMY'),
    (214,2,'17D',17,'D','ECONOMY'),(215,2,'17E',17,'E','ECONOMY'),(216,2,'17F',17,'F','ECONOMY'),
    (217,2,'18A',18,'A','ECONOMY'),(218,2,'18B',18,'B','ECONOMY'),(219,2,'18C',18,'C','ECONOMY'),
    (220,2,'18D',18,'D','ECONOMY'),(221,2,'18E',18,'E','ECONOMY'),(222,2,'18F',18,'F','ECONOMY'),
    (223,2,'19A',19,'A','ECONOMY'),(224,2,'19B',19,'B','ECONOMY'),(225,2,'19C',19,'C','ECONOMY'),
    (226,2,'19D',19,'D','ECONOMY'),(227,2,'19E',19,'E','ECONOMY'),(228,2,'19F',19,'F','ECONOMY'),
    (229,2,'20A',20,'A','ECONOMY'),(230,2,'20B',20,'B','ECONOMY'),(231,2,'20C',20,'C','ECONOMY'),
    (232,2,'20D',20,'D','ECONOMY'),(233,2,'20E',20,'E','ECONOMY'),(234,2,'20F',20,'F','ECONOMY');

INSERT INTO flight.flight(id, route_id, aircraft_id, flight_number, departure_time, arrival_time, status) VALUES
    -- Airbus A320: DME - SVX (внутренние)
    (1,  1, 1, 'GA 101', '2026-05-26 06:00:00+03', '2026-05-26 10:10:00+05', 'SCHEDULED'),
    (2,  3, 1, 'GA 102', '2026-05-26 10:00:00+05', '2026-05-26 10:10:00+03', 'SCHEDULED'),
    (3,  1, 1, 'GA 101', '2026-05-28 06:00:00+03', '2026-05-28 10:10:00+05', 'SCHEDULED'),
    (4,  3, 1, 'GA 102', '2026-05-28 10:00:00+05', '2026-05-28 10:10:00+03', 'SCHEDULED'),
    (5,  1, 1, 'GA 101', '2026-06-02 06:00:00+03', '2026-06-02 10:10:00+05', 'SCHEDULED'),
    (6,  3, 1, 'GA 102', '2026-06-02 10:00:00+05', '2026-06-02 10:10:00+03', 'SCHEDULED'),
    (7,  1, 1, 'GA 101', '2026-06-09 06:00:00+03', '2026-06-09 10:10:00+05', 'SCHEDULED'),
    (8,  3, 1, 'GA 102', '2026-06-09 10:00:00+05', '2026-06-09 10:10:00+03', 'SCHEDULED'),
    -- Два самолёта: SVX - FRA
    (9,  4, 1, 'GA 201', '2026-06-05 08:00:00+05', '2026-06-05 12:30:00+02', 'SCHEDULED'),
    (10, 4, 2, 'GA 203', '2026-06-05 14:00:00+05', '2026-06-05 18:30:00+02', 'SCHEDULED'),
    (11, 6, 1, 'GA 202', '2026-06-10 09:00:00+02', '2026-06-10 19:30:00+05', 'SCHEDULED'),
    (12, 6, 2, 'GA 204', '2026-06-10 16:00:00+02', '2026-06-11 02:30:00+05', 'SCHEDULED'),

    -- Boeing 737 MAX: DME - FRA
    (13, 2, 2, 'GA 203', '2026-06-03 09:00:00+03', '2026-06-03 12:00:00+02', 'SCHEDULED'),
    (14, 5, 2, 'GA 204', '2026-06-03 13:00:00+02', '2026-06-03 18:00:00+03', 'SCHEDULED'),
    (15, 2, 2, 'GA 203', '2026-06-17 09:00:00+03', '2026-06-17 12:00:00+02', 'SCHEDULED'),
    (16, 5, 2, 'GA 204', '2026-06-17 13:00:00+02', '2026-06-17 18:00:00+03', 'SCHEDULED');

INSERT INTO flight.flight_inventory(id, flight_id, seat_class, available_seats, price, child_seat_discount) VALUES
    -- Рейс 1 (GA 101, 26 мая, Airbus)
    (1,  1, 'ECONOMY',   66, 8500.00,  0.75),
    (2,  1, 'EMERGENCY', 12, 8900.00,  0.75),
    (3,  1, 'BUSINESS',  36, 25000.00, 0.75),
    -- Рейс 2 (GA 102, 26 мая, Airbus)
    (4,  2, 'ECONOMY',   66, 8500.00,  0.75),
    (5,  2, 'EMERGENCY', 12, 8900.00,  0.75),
    (6,  2, 'BUSINESS',  36, 25000.00, 0.75),
    -- Рейс 3 (GA 101, 28 мая, Airbus)
    (7,  3, 'ECONOMY',   66, 8500.00,  0.75),
    (8,  3, 'EMERGENCY', 12, 8900.00,  0.75),
    (9,  3, 'BUSINESS',  36, 25000.00, 0.75),
    -- Рейс 4 (GA 102, 28 мая, Airbus)
    (10, 4, 'ECONOMY',   66, 8500.00,  0.75),
    (11, 4, 'EMERGENCY', 12, 8900.00,  0.75),
    (12, 4, 'BUSINESS',  36, 25000.00, 0.75),
    -- Рейс 5 (GA 101, 2 июня, Airbus)
    (13, 5, 'ECONOMY',   66, 8500.00,  0.75),
    (14, 5, 'EMERGENCY', 12, 8900.00,  0.75),
    (15, 5, 'BUSINESS',  36, 25000.00, 0.75),
    -- Рейс 6 (GA 102, 2 июня, Airbus)
    (16, 6, 'ECONOMY',   66, 8500.00,  0.75),
    (17, 6, 'EMERGENCY', 12, 8900.00,  0.75),
    (18, 6, 'BUSINESS',  36, 25000.00, 0.75),
    -- Рейс 7 (GA 101, 9 июня, Airbus)
    (19, 7, 'ECONOMY',   66, 8500.00,  0.75),
    (20, 7, 'EMERGENCY', 12, 8900.00,  0.75),
    (21, 7, 'BUSINESS',  36, 25000.00, 0.75),
    -- Рейс 8 (GA 102, 9 июня, Airbus)
    (22, 8, 'ECONOMY',   66, 8500.00,  0.75),
    (23, 8, 'EMERGENCY', 12, 8900.00,  0.75),
    (24, 8, 'BUSINESS',  36, 25000.00, 0.75),

    -- Рейс 9 (GA 201, 5 июня, Airbus, SVX-FRA)
    (25, 9, 'ECONOMY',   66, 45000.00, 0.75),
    (26, 9, 'EMERGENCY', 12, 47000.00, 0.75),
    (27, 9, 'BUSINESS',  36, 120000.00,0.75),
    -- Рейс 10 (GA 203, 5 июня, Boeing, SVX-FRA)
    (28, 10,'ECONOMY',   96, 42000.00, 0.75),
    (29, 10,'EMERGENCY', 12, 44000.00, 0.75),
    (30, 10,'BUSINESS',  12, 115000.00,0.75),
    -- Рейс 11 (GA 202, 10 июня, Airbus, FRA-SVX)
    (31, 11,'ECONOMY',   66, 45000.00, 0.75),
    (32, 11,'EMERGENCY', 12, 47000.00, 0.75),
    (33, 11,'BUSINESS',  36, 120000.00,0.75),
    -- Рейс 12 (GA 204, 10 июня, Boeing, FRA-SVX)
    (34, 12,'ECONOMY',   96, 42000.00, 0.75),
    (35, 12,'EMERGENCY', 12, 44000.00, 0.75),
    (36, 12,'BUSINESS',  12, 115000.00,0.75),
    -- Рейс 13 (GA 203, 3 июня, Boeing, DME-FRA)
    (37, 13,'ECONOMY',   96, 47000.00, 0.75),
    (38, 13,'EMERGENCY', 12, 49000.00, 0.75),
    (39, 13,'BUSINESS',  12, 100000.00,0.75),
    -- Рейс 14 (GA 204, 3 июня, Boeing, FRA-DME)
    (40, 14,'ECONOMY',   96, 47000.00, 0.75),
    (41, 14,'EMERGENCY', 12, 49000.00, 0.75),
    (42, 14,'BUSINESS',  12, 100000.00,0.75),
    -- Рейс 15 (GA 203, 17 июня, Boeing, DME-FRA)
    (43, 15,'ECONOMY',   96, 47000.00, 0.75),
    (44, 15,'EMERGENCY', 12, 49000.00, 0.75),
    (45, 15,'BUSINESS',  12, 100000.00,0.75),
    -- Рейс 16 (GA 204, 17 июня, Boeing, FRA-DME)
    (46, 16,'ECONOMY',   96, 47000.00, 0.75),
    (47, 16,'EMERGENCY', 12, 49000.00, 0.75),
    (48, 16,'BUSINESS',  12, 100000.00,0.75);

-- Рейсы Airbus A320
INSERT INTO flight.seat_availability(id, flight_id, seat_id, status)
    SELECT ROW_NUMBER() OVER() + 114 * 0, 1, s.id, 'AVAILABLE' FROM flight.seat s WHERE s.aircraft_id = 1;
INSERT INTO flight.seat_availability(id, flight_id, seat_id, status)
    SELECT ROW_NUMBER() OVER() + 114 * 1, 2, s.id, 'AVAILABLE' FROM flight.seat s WHERE s.aircraft_id = 1;
INSERT INTO flight.seat_availability(id, flight_id, seat_id, status)
    SELECT ROW_NUMBER() OVER() + 114 * 2, 3, s.id, 'AVAILABLE' FROM flight.seat s WHERE s.aircraft_id = 1;
INSERT INTO flight.seat_availability(id, flight_id, seat_id, status)
    SELECT ROW_NUMBER() OVER() + 114 * 3, 4, s.id, 'AVAILABLE' FROM flight.seat s WHERE s.aircraft_id = 1;
INSERT INTO flight.seat_availability(id, flight_id, seat_id, status)
    SELECT ROW_NUMBER() OVER() + 114 * 4, 5, s.id, 'AVAILABLE' FROM flight.seat s WHERE s.aircraft_id = 1;
INSERT INTO flight.seat_availability(id, flight_id, seat_id, status)
    SELECT ROW_NUMBER() OVER() + 114 * 5, 6, s.id, 'AVAILABLE' FROM flight.seat s WHERE s.aircraft_id = 1;
INSERT INTO flight.seat_availability(id, flight_id, seat_id, status)
    SELECT ROW_NUMBER() OVER() + 114 * 6, 7, s.id, 'AVAILABLE' FROM flight.seat s WHERE s.aircraft_id = 1;
INSERT INTO flight.seat_availability(id, flight_id, seat_id, status)
    SELECT ROW_NUMBER() OVER() + 114 * 7, 8, s.id, 'AVAILABLE' FROM flight.seat s WHERE s.aircraft_id = 1;
INSERT INTO flight.seat_availability(id, flight_id, seat_id, status)
    SELECT ROW_NUMBER() OVER() + 114 * 8, 9, s.id, 'AVAILABLE' FROM flight.seat s WHERE s.aircraft_id = 1;
INSERT INTO flight.seat_availability(id, flight_id, seat_id, status)
    SELECT ROW_NUMBER() OVER() + 114 * 9, 11, s.id, 'AVAILABLE' FROM flight.seat s WHERE s.aircraft_id = 1;

-- Рейсы Boeing 737 MAX
INSERT INTO flight.seat_availability(id, flight_id, seat_id, status)
    SELECT ROW_NUMBER() OVER() + 114 * 10 + 120 * 0, 10, s.id, 'AVAILABLE' FROM flight.seat s WHERE s.aircraft_id = 2;
INSERT INTO flight.seat_availability(id, flight_id, seat_id, status)
    SELECT ROW_NUMBER() OVER() + 114 * 10 + 120 * 1, 12, s.id, 'AVAILABLE' FROM flight.seat s WHERE s.aircraft_id = 2;
INSERT INTO flight.seat_availability(id, flight_id, seat_id, status)
    SELECT ROW_NUMBER() OVER() + 114 * 10 + 120 * 2, 13, s.id, 'AVAILABLE' FROM flight.seat s WHERE s.aircraft_id = 2;
INSERT INTO flight.seat_availability(id, flight_id, seat_id, status)
    SELECT ROW_NUMBER() OVER() + 114 * 10 + 120 * 3, 14, s.id, 'AVAILABLE' FROM flight.seat s WHERE s.aircraft_id = 2;
INSERT INTO flight.seat_availability(id, flight_id, seat_id, status)
    SELECT ROW_NUMBER() OVER() + 114 * 10 + 120 * 4, 15, s.id, 'AVAILABLE' FROM flight.seat s WHERE s.aircraft_id = 2;
INSERT INTO flight.seat_availability(id, flight_id, seat_id, status)
    SELECT ROW_NUMBER() OVER() + 114 * 10 + 120 * 5, 16, s.id, 'AVAILABLE' FROM flight.seat s WHERE s.aircraft_id = 2;

-- Рейс 9
UPDATE flight.seat_availability SET status = 'OCCUPIED'
WHERE flight_id = 9 AND seat_id IN (
    SELECT id FROM flight.seat WHERE aircraft_id = 1
    AND seat_number IN ('1A','1B','1C','2D','2E','3A','3F',
                        '7A','7B','8C','8D','9E','10A','10F',
                        '12A','12B','12D','14C','14E',
                        '15A','16B','17C','18D','19E','20F')
);

-- Рейс 10
UPDATE flight.seat_availability SET status = 'OCCUPIED'
WHERE flight_id = 10 AND seat_id IN (
    SELECT id FROM flight.seat WHERE aircraft_id = 2
    AND seat_number IN ('1A','1B','1D','2C','2E',
                        '3A','3B','4C','5D','6E','7F','8A','9B','10C',
                        '13A','13B','13D','14C','14F',
                        '15A','16B','17C','18D','19E','20F')
);

-- Рейс 9
UPDATE flight.flight_inventory SET available_seats = available_seats - 7
WHERE flight_id = 9 AND seat_class = 'BUSINESS';

UPDATE flight.flight_inventory SET available_seats = available_seats - 13
WHERE flight_id = 9 AND seat_class = 'ECONOMY';

UPDATE flight.flight_inventory SET available_seats = available_seats - 5
WHERE flight_id = 9 AND seat_class = 'EMERGENCY';

-- Рейс 10
UPDATE flight.flight_inventory SET available_seats = available_seats - 5
WHERE flight_id = 10 AND seat_class = 'BUSINESS';

UPDATE flight.flight_inventory SET available_seats = available_seats - 15
WHERE flight_id = 10 AND seat_class = 'ECONOMY';

UPDATE flight.flight_inventory SET available_seats = available_seats - 5
WHERE flight_id = 10 AND seat_class = 'EMERGENCY';

-- Схема account
INSERT INTO account.loyalty_account(id, miles, created_at) VALUES
   (1, 1644, '2026-01-15 10:00:00+00'),
   (2, 320,  '2026-03-22 14:00:00+00'),
   (3, 5429, '2026-05-21 17:43:00+00'),
   (4, 0,    '2026-02-10 09:15:00+00'),
   (5, 2100, '2026-01-28 11:30:00+00'),
   (6, 780,  '2026-04-05 16:20:00+00'),
   (7, 0,    '2026-05-01 08:00:00+00'),
   (8, 3300, '2025-12-10 12:00:00+00'),
   (9, 150,  '2026-03-17 19:45:00+00'),
   (10, 0,   '2026-05-10 14:00:00+00');

INSERT INTO account.passenger(id, first_name, last_name, middle_name, gender, birth_date, document_type, document_number, contact_email, contact_phone)
    VALUES
    (1,  'Матвей',    'Воронов',    'Денисович',  'MALE',   '1993-04-21', 'PASSPORT_RUSSIAN', '4323 544217', 'm.voronov@gmail.com',       '+79164328811'),
    (2,  'Элина',     'Савицкая',   'Романовна',  'FEMALE', '1989-06-01', 'PASSPORT_RUSSIAN', '5676 457568', 'e.savitskaya@outlook.com',  '+79257113455'),
    (3,  'Родион',    'Кравцов',    'Игоревич',   'MALE',   '2001-11-07', 'PASSPORT_RUSSIAN', '9667 369343', 'r.kravtsov@gmail.com',      '+79031456699'),
    (4,  'Анастасия', 'Белова',     'Олеговна',   'FEMALE', '1997-03-14', 'PASSPORT_RUSSIAN', '7712 334521', 'a.belova@gmail.com',        '+79652341122'),
    (5,  'Дмитрий',   'Орлов',      'Павлович',   'MALE',   '1985-09-30', 'PASSPORT_RUSSIAN', '3341 887654', 'd.orlov@yandex.ru',         '+79031122334'),
    (6,  'Юлия',      'Морозова',   'Александровна','FEMALE','1992-12-05','PASSPORT_RUSSIAN', '6654 221133', 'yu.morozova@gmail.com',     '+79167788990'),
    (7,  'Артём',     'Соколов',    'Витальевич', 'MALE',   '2000-07-19', 'INTERNATIONAL',    '75 1234567',  'a.sokolov@gmail.com',       '+79993344556'),
    (8,  'Наталья',   'Громова',    'Сергеевна',  'FEMALE', '1978-02-28', 'PASSPORT_RUSSIAN', '2298 556677', 'n.gromova@outlook.com',     '+79251234567'),
    (9,  'Кирилл',    'Захаров',    'Михайлович', 'MALE',   '1995-10-11', 'PASSPORT_RUSSIAN', '5543 112233', 'k.zakharov@gmail.com',      '+79671122334'),
    (10, 'Виктория',  'Лебедева',   'Андреевна',  'FEMALE', '2003-05-22', 'PASSPORT_RUSSIAN', '8821 998877', 'v.lebedeva@yandex.ru',      '+79031987654');

INSERT INTO
    account.user_account(id, email, phone_number, last_name, first_name, middle_name, password_hash, created_at, email_verified, auth_provider, loyalty_account_id, passenger_id)
    VALUES
    (1,  'm.voronov@gmail.com',     '+79164328811', 'Воронов',  'Матвей',    'Денисович',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhy', '2026-01-15 10:00:00+00', true,  'EMAIL', 1,  1),
    (2,  'e.savitskaya@outlook.com','+79257113455', 'Савицкая', 'Элина',     'Романовна',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhy', '2026-03-22 14:00:00+00', true,  'EMAIL', 2,  2),
    (3,  'r.kravtsov@gmail.com',    '+79031456699', 'Кравцов',  'Родион',    'Игоревич',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhy', '2026-05-21 17:43:00+00', true,  'EMAIL', 3,  3),
    (4,  'a.belova@gmail.com',      '+79652341122', 'Белова',   'Анастасия', 'Олеговна',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhy', '2026-02-10 09:15:00+00', true,  'EMAIL', 4,  4),
    (5,  'd.orlov@yandex.ru',       '+79031122334', 'Орлов',    'Дмитрий',   'Павлович',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhy', '2026-01-28 11:30:00+00', true,  'EMAIL', 5,  5),
    (6,  'yu.morozova@gmail.com',   '+79167788990', 'Морозова', 'Юлия',      'Александровна', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhy', '2026-04-05 16:20:00+00', true,  'EMAIL', 6,  6),
    (7,  'a.sokolov@gmail.com',     '+79993344556', 'Соколов',  'Артём',     'Витальевич',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhy', '2026-05-01 08:00:00+00', false, 'EMAIL', 7,  7),
    (8,  'n.gromova@outlook.com',   '+79251234567', 'Громова',  'Наталья',   'Сергеевна',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhy', '2025-12-10 12:00:00+00', true,  'EMAIL', 8,  8),
    (9,  'k.zakharov@gmail.com',    '+79671122334', 'Захаров',  'Кирилл',    'Михайлович',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhy', '2026-03-17 19:45:00+00', true,  'EMAIL', 9,  9),
    (10, 'v.lebedeva@yandex.ru',    '+79031987654', 'Лебедева', 'Виктория',  'Андреевна',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhy', '2026-05-10 14:00:00+00', true,  'EMAIL', 10, 10);

-- Схема booking
INSERT INTO booking.additional_service(id, additional_service_type, name, description, price, is_active) VALUES
    (1, 'MEAL', 'Питание на борту', 'Горячее блюдо и напиток', 800.00, false),
    (2, 'REFUND', 'Возврат билетов', 'За 24 часа до вылета', 7600.00, false),
    (3, 'EXTRA_BAGGAGE', 'Увеличенный вес багажа', '+10 кг к весу одной сумки', 3400.00, false);

INSERT INTO
    booking.booking_order(id, user_account_id, outbound_flight_id, return_flight_id, seat_class, status, total_price, miles_spent, miles_earned, contact_email, contact_phone, created_at, booking_expires_at)
    VALUES
    (1, 1,  1,  2,    'BUSINESS', 'BOOKED',    17000.00,  1644, 34,  'm.voronov@gmail.com',     '+79164328811', '2026-05-01 10:00:00+00', '2026-05-01 11:00:00+00'),
    (2, 2,  9,  11,   'ECONOMY',  'PAID',      90000.00,  320,  563, 'e.savitskaya@outlook.com','+79257113455', '2026-04-20 14:00:00+00', '2026-04-20 15:00:00+00'),
    (3, 3,  2,  1,    'ECONOMY',  'BOOKED',    17800.00,  0,    35,  'r.kravtsov@gmail.com',    '+79031456699', '2026-05-10 09:00:00+00', '2026-05-10 10:00:00+00'),
    (4, 4,  3,  4,    'ECONOMY',  'PAID',      17000.00,  0,    34,  'a.belova@gmail.com',      '+79652341122', '2026-04-15 11:00:00+00', '2026-04-15 12:00:00+00'),
    (5, 5,  5,  6,    'BUSINESS', 'PAID',      50000.00,  0,    250, 'd.orlov@yandex.ru',       '+79031122334', '2026-04-10 08:00:00+00', '2026-04-10 09:00:00+00'),
    (6, 6,  9,  NULL, 'ECONOMY',  'PAID',      45000.00,  0,    225, 'yu.morozova@gmail.com',   '+79167788990', '2026-05-05 16:00:00+00', '2026-05-05 17:00:00+00'),
    (7, 8,  10, 12,   'BUSINESS', 'COMPLETED', 230000.00, 0,    1150, 'n.gromova@outlook.com',   '+79251234567', '2026-03-01 12:00:00+00', '2026-03-01 13:00:00+00');

INSERT INTO
    booking.order_passenger(id, order_id, passenger_id, passenger_type, last_name, first_name, middle_name, gender, birth_date, document_type, document_number, outbound_seat_id)
    VALUES
    (1, 1, 1, 'ADULT', 'Воронов',  'Матвей',    'Денисович',     'MALE',   '1993-04-21', 'PASSPORT_RUSSIAN', '4323 544217', 34),
    (2, 2, 2, 'ADULT', 'Савицкая', 'Элина',     'Романовна',     'FEMALE', '1989-06-01', 'PASSPORT_RUSSIAN', '5676 457568', 3),
    (3, 3, 3, 'ADULT', 'Кравцов',  'Родион',    'Игоревич',      'MALE',   '2001-11-07', 'PASSPORT_RUSSIAN', '9667 369343', 56),
    (4, 4, 4, 'ADULT', 'Белова',   'Анастасия', 'Олеговна',      'FEMALE', '1997-03-14', 'PASSPORT_RUSSIAN', '7712 334521', 78),
    (5, 5, 5, 'ADULT', 'Орлов',    'Дмитрий',   'Павлович',      'MALE',   '1985-09-30', 'PASSPORT_RUSSIAN', '3341 887654', 12),
    (6, 6, 6, 'ADULT', 'Морозова', 'Юлия',      'Александровна', 'FEMALE', '1992-12-05', 'PASSPORT_RUSSIAN', '6654 221133', 42),
    (7, 7, 8, 'ADULT', 'Громова',  'Наталья',   'Сергеевна',     'FEMALE', '1978-02-28', 'PASSPORT_RUSSIAN', '2298 556677', 116);

INSERT INTO booking.order_services(order_id, service_id) VALUES
    (2, 1),
    (2, 3),
    (5, 1),
    (6, 2),
    (7, 1),
    (7, 3);

INSERT INTO account.loyalty_transaction(id, loyalty_account_id, order_id, transaction_type, miles, created_at) VALUES
    (1, 1, 1, 'SPENT', 1644, '2026-05-01 10:00:00+00'),
    (2, 1, 1, 'EARNED', 34,  '2026-05-01 10:00:00+00'),
    (3, 2, 2, 'SPENT', 320,  '2026-04-20 14:00:00+00'),
    (4, 2, 2, 'EARNED', 563, '2026-04-20 14:00:00+00'),
    (5, 3, 3, 'EARNED', 35,  '2026-05-10 09:00:00+00'),
    (6, 4, 4, 'EARNED', 34,  '2026-04-15 11:00:00+00'),
    (7, 5, 5, 'EARNED', 250, '2026-04-10 08:00:00+00'),
    (8, 6, 6, 'EARNED', 225, '2026-05-05 16:00:00+00'),
    (9, 8, 7, 'EARNED', 1150,'2026-03-01 12:00:00+00');

SELECT setval('public.loyalty_account_id_seq',       (SELECT MAX(id) FROM account.loyalty_account));
SELECT setval('public.passenger_id_seq',             (SELECT MAX(id) FROM account.passenger));
SELECT setval('public.account_id_seq',               (SELECT MAX(id) FROM account.user_account));
SELECT setval('public.loyalty_transaction_seq',      (SELECT MAX(id) FROM account.loyalty_transaction));

SELECT setval('public.airport_id_seq',               (SELECT MAX(id) FROM flight.airport));
SELECT setval('public.route_id_seq',                 (SELECT MAX(id) FROM flight.route));
SELECT setval('public.aircraft_id_seq',              (SELECT MAX(id) FROM flight.aircraft));
SELECT setval('public.seat_id_seq',                  (SELECT MAX(id) FROM flight.seat));
SELECT setval('public.flight_id_seq',                (SELECT MAX(id) FROM flight.flight));
SELECT setval('public.flight_inventory_id_seq',      (SELECT MAX(id) FROM flight.flight_inventory));
SELECT setval('public.seat_availability_id_seq',     (SELECT MAX(id) FROM flight.seat_availability));

SELECT setval('public.additional_service_id_seq',    (SELECT MAX(id) FROM booking.additional_service));
SELECT setval('public.booking_order_id_seq',         (SELECT MAX(id) FROM booking.booking_order));
SELECT setval('public.order_passenger_id_seq',       (SELECT MAX(id) FROM booking.order_passenger));