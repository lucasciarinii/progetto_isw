-- ============================================
-- TEST DATA — 2-player games
-- ============================================
INSERT INTO game (num_players) VALUES (2); -- id 1
INSERT INTO game (num_players) VALUES (2); -- id 2
INSERT INTO game (num_players) VALUES (2); -- id 3
INSERT INTO game (num_players) VALUES (2); -- id 4
INSERT INTO game (num_players) VALUES (2); -- id 5
INSERT INTO game (num_players) VALUES (2); -- id 6
INSERT INTO game (num_players) VALUES (2); -- id 7
INSERT INTO game (num_players) VALUES (2); -- id 8
INSERT INTO game (num_players) VALUES (2); -- id 9
INSERT INTO game (num_players) VALUES (2); -- id 10
INSERT INTO game (num_players) VALUES (2); -- id 11
INSERT INTO game (num_players) VALUES (2); -- id 12

-- ============================================
-- game 1: luca vs edo → luca vince
INSERT INTO game_result VALUES (1, 'luca',   45, 1);
INSERT INTO game_result VALUES (1, 'edo',    38, 2);

-- game 2: daniel vs leo → leo vince
INSERT INTO game_result VALUES (2, 'daniel', 30, 2);
INSERT INTO game_result VALUES (2, 'leo',    41, 1);

-- game 3: luca vs daniel → luca vince
INSERT INTO game_result VALUES (3, 'luca',   50, 1);
INSERT INTO game_result VALUES (3, 'daniel', 44, 2);

-- game 4: edo vs leo → edo vince
INSERT INTO game_result VALUES (4, 'edo',    37, 1);
INSERT INTO game_result VALUES (4, 'leo',    33, 2);

-- game 5: luca vs leo → leo vince
INSERT INTO game_result VALUES (5, 'luca',   28, 2);
INSERT INTO game_result VALUES (5, 'leo',    35, 1);

-- game 6: edo vs daniel → daniel vince
INSERT INTO game_result VALUES (6, 'edo',    22, 2);
INSERT INTO game_result VALUES (6, 'daniel', 29, 1);

-- game 7: luca vs edo → luca vince
INSERT INTO game_result VALUES (7, 'luca',   55, 1);
INSERT INTO game_result VALUES (7, 'edo',    48, 2);

-- game 8: daniel vs leo → pareggio vittorie, si distingue per avg_score → leo vince
INSERT INTO game_result VALUES (8, 'daniel', 40, 2);
INSERT INTO game_result VALUES (8, 'leo',    47, 1);

-- game 9: luca vs daniel → daniel vince
INSERT INTO game_result VALUES (9,  'luca',   31, 2);
INSERT INTO game_result VALUES (9,  'daniel', 36, 1);

-- game 10: edo vs leo → edo vince
INSERT INTO game_result VALUES (10, 'edo',    43, 1);
INSERT INTO game_result VALUES (10, 'leo',    39, 2);

-- game 11: luca vs leo → luca vince
INSERT INTO game_result VALUES (11, 'luca',   60, 1);
INSERT INTO game_result VALUES (11, 'leo',    52, 2);

-- game 12: edo vs daniel → pareggio vittorie tra edo e daniel
INSERT INTO game_result VALUES (12, 'edo',    51, 1);
INSERT INTO game_result VALUES (12, 'daniel', 45, 2);