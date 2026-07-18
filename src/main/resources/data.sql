-- Наполнение таблицы рейтингов MPA (id сгенерируются автоматически от 1 до 5)
INSERT INTO rating (rating) SELECT 'G' WHERE NOT EXISTS (SELECT 1 FROM rating WHERE rating = 'G');
INSERT INTO rating (rating) SELECT 'PG' WHERE NOT EXISTS (SELECT 1 FROM rating WHERE rating = 'PG');
INSERT INTO rating (rating) SELECT 'PG-13' WHERE NOT EXISTS (SELECT 1 FROM rating WHERE rating = 'PG-13');
INSERT INTO rating (rating) SELECT 'R' WHERE NOT EXISTS (SELECT 1 FROM rating WHERE rating = 'R');
INSERT INTO rating (rating) SELECT 'NC-17' WHERE NOT EXISTS (SELECT 1 FROM rating WHERE rating = 'NC-17');

-- Наполнение таблицы жанров (id от 1 до 6)
INSERT INTO genre (genre) SELECT 'Комедия' WHERE NOT EXISTS (SELECT 1 FROM genre WHERE genre = 'Комедия');
INSERT INTO genre (genre) SELECT 'Драма' WHERE NOT EXISTS (SELECT 1 FROM genre WHERE genre = 'Драма');
INSERT INTO genre (genre) SELECT 'Мультфильм' WHERE NOT EXISTS (SELECT 1 FROM genre WHERE genre = 'Мультфильм');
INSERT INTO genre (genre) SELECT 'Триллер' WHERE NOT EXISTS (SELECT 1 FROM genre WHERE genre = 'Триллер');
INSERT INTO genre (genre) SELECT 'Документальный' WHERE NOT EXISTS (SELECT 1 FROM genre WHERE genre = 'Документальный');
INSERT INTO genre (genre) SELECT 'Боевик' WHERE NOT EXISTS (SELECT 1 FROM genre WHERE genre = 'Боевик');
