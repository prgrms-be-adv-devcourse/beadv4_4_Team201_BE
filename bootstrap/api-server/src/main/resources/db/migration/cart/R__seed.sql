-- Cart seed data (dev/staging)
TRUNCATE TABLE cart_items CASCADE;
TRUNCATE TABLE carts CASCADE;

INSERT INTO carts (id, member_id, created_at, updated_at, created_by, updated_by)
VALUES (1, 1, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 2, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 3, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (4, 4, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (5, 5, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (6, 6, NOW(), NOW(), 'SYSTEM', 'SYSTEM');

SELECT setval('carts_id_seq', 100, false);

INSERT INTO cart_items (id, cart_id, wishlist_item_id, amount, wishlist_item_status)
VALUES (1, 2, 9, 539100.00, 'IN_PROGRESS');

SELECT setval('cart_items_id_seq', 100, false);

-- Loadtest carts (only in loadtest schema)
DO $$
BEGIN
  IF '${is_staging}' = 'true' THEN
    INSERT INTO carts (id, member_id, created_at, updated_at, created_by, updated_by)
    VALUES
           (1001, 1001, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1002, 1002, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1003, 1003, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1004, 1004, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1005, 1005, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1006, 1006, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1007, 1007, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1008, 1008, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1009, 1009, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1010, 1010, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1011, 1011, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1012, 1012, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1013, 1013, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1014, 1014, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1015, 1015, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1016, 1016, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1017, 1017, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1018, 1018, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1019, 1019, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1020, 1020, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1021, 1021, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1022, 1022, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1023, 1023, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1024, 1024, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1025, 1025, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1026, 1026, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1027, 1027, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1028, 1028, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1029, 1029, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1030, 1030, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1031, 1031, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1032, 1032, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1033, 1033, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1034, 1034, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1035, 1035, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1036, 1036, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1037, 1037, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1038, 1038, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1039, 1039, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1040, 1040, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1041, 1041, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1042, 1042, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1043, 1043, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1044, 1044, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1045, 1045, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1046, 1046, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1047, 1047, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1048, 1048, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1049, 1049, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1050, 1050, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1051, 1051, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1052, 1052, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1053, 1053, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1054, 1054, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1055, 1055, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1056, 1056, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1057, 1057, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1058, 1058, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1059, 1059, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1060, 1060, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1061, 1061, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1062, 1062, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1063, 1063, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1064, 1064, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1065, 1065, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1066, 1066, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1067, 1067, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1068, 1068, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1069, 1069, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1070, 1070, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1071, 1071, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1072, 1072, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1073, 1073, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1074, 1074, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1075, 1075, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1076, 1076, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1077, 1077, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1078, 1078, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1079, 1079, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1080, 1080, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1081, 1081, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1082, 1082, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1083, 1083, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1084, 1084, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1085, 1085, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1086, 1086, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1087, 1087, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1088, 1088, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1089, 1089, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1090, 1090, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1091, 1091, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1092, 1092, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1093, 1093, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1094, 1094, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1095, 1095, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1096, 1096, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1097, 1097, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1098, 1098, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1099, 1099, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
           (1100, 1100, NOW(), NOW(), 'SYSTEM', 'SYSTEM');

    PERFORM setval('carts_id_seq', 1200, false);
  END IF;
END
$$;