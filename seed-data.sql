-- Transportation Management System - Seed Data
-- Run this script on your cloud PostgreSQL database to populate initial data
-- Excludes: users, reservations, seat_holds (user-specific data)

-- Disable foreign key checks temporarily
SET session_replication_role = 'replica';

-- Clear existing data (optional - uncomment if needed)
-- TRUNCATE TABLE timetable_days, timetable_stops, timetables, route_stops, routes, fare_policies, stations RESTART IDENTITY CASCADE;

-- =====================================================
-- STATIONS
-- =====================================================
INSERT INTO stations (id, address, description, latitude, longitude, name, status) VALUES
(1, 'Autogara Codreanu, Străpungerea Silvestru, Gară, Iași, Iași Metropolitan Area, Iași, 700132, Romania', '', 47.16678798507194, 27.570525117810035, 'Iasi - Autogara Codreanu ', 'ACTIVE'),
(2, 'Filaret Bus Station, 1, Piața Gara Filaret, Filaret, Sector 5, Bucharest, 040542, Romania', '', 44.4155328, 26.0921098, 'Bucuresti - Autogara Filaret', 'ACTIVE'),
(3, 'Strada Traian Vuia, Hărbărie, Zamca, Suceava, 678543, Romania', '', 47.66121796007154, 26.25255290159871, 'Suceava - Autogara', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- =====================================================
-- ROUTES
-- =====================================================
INSERT INTO routes (id, description, distance, duration_minutes, vehiclecapacity, vehicle_class, destination_station_id, origin_station_id) VALUES
(1, NULL, 326.7, 280, 50, 'STANDARD', 2, 1),
(2, NULL, 326.7, 280, 50, 'STANDARD', 1, 2)
ON CONFLICT (id) DO NOTHING;

-- =====================================================
-- ROUTE STOPS
-- =====================================================
INSERT INTO route_stops (id, cumulative_distance, cumulative_duration_minutes, distance_from_previous, duration_minutes_from_previous, sequence_order, route_id, station_id) VALUES
(1, 0, 0, 0, 0, 0, 1, 1),
(2, 326.7, 280, 326.7, 280, 1, 1, 2),
(3, 0, 0, 0, 0, 0, 2, 2),
(4, 326.7, 280, 326.7, 280, 1, 2, 1)
ON CONFLICT (id) DO NOTHING;

-- =====================================================
-- TIMETABLES
-- =====================================================
INSERT INTO timetables (id, description, end_date, start_date, route_id) VALUES
(1, 'Morning Bus', '2026-02-06', '2026-01-03', 1),
(2, 'Bus', '2026-02-25', '2026-01-01', 2)
ON CONFLICT (id) DO NOTHING;

-- =====================================================
-- TIMETABLE DAYS
-- =====================================================
INSERT INTO timetable_days (timetable_id, day_of_week) VALUES
(1, 'Mon'),
(1, 'Wed'),
(1, 'Thu'),
(1, 'Fri'),
(1, 'Sat'),
(1, 'Sun'),
(2, 'Mon'),
(2, 'Tue'),
(2, 'Wed'),
(2, 'Thu'),
(2, 'Fri'),
(2, 'Sat'),
(2, 'Sun')
ON CONFLICT DO NOTHING;

-- =====================================================
-- TIMETABLE STOPS
-- =====================================================
INSERT INTO timetable_stops (id, arrival_time, departure_time, sequence_order, station_id, timetable_id) VALUES
(3, '2026-01-03 21:51:00', '2026-01-03 21:51:00', 0, 1, 1),
(4, '2026-01-03 02:31:00', NULL, 1, 2, 1),
(5, '2026-01-01 23:32:00', '2026-01-01 23:32:00', 0, 2, 2),
(6, '2026-01-01 04:12:00', NULL, 1, 1, 2)
ON CONFLICT (id) DO NOTHING;

-- =====================================================
-- RESET SEQUENCES
-- =====================================================
SELECT setval('stations_id_seq', (SELECT COALESCE(MAX(id), 0) + 1 FROM stations), false);
SELECT setval('routes_id_seq', (SELECT COALESCE(MAX(id), 0) + 1 FROM routes), false);
SELECT setval('route_stops_id_seq', (SELECT COALESCE(MAX(id), 0) + 1 FROM route_stops), false);
SELECT setval('timetables_id_seq', (SELECT COALESCE(MAX(id), 0) + 1 FROM timetables), false);
SELECT setval('timetable_stops_id_seq', (SELECT COALESCE(MAX(id), 0) + 1 FROM timetable_stops), false);

-- Re-enable foreign key checks
SET session_replication_role = 'origin';

-- Verify data
SELECT 'stations' as table_name, COUNT(*) as count FROM stations
UNION ALL SELECT 'routes', COUNT(*) FROM routes
UNION ALL SELECT 'route_stops', COUNT(*) FROM route_stops
UNION ALL SELECT 'timetables', COUNT(*) FROM timetables
UNION ALL SELECT 'timetable_stops', COUNT(*) FROM timetable_stops
UNION ALL SELECT 'timetable_days', COUNT(*) FROM timetable_days;
