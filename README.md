# java-filmorate
Template repository for Filmorate project.


Ссылка на БД
https://dbdiagram.io/d/6a4e59f536d348d12097c80f

Запросы из ТЗ

Получение всех фильмов SELECT name
FROM film

Получение всех пользователей
SELECT name
FROM user

Топ 10 наиболее популярных фильмов
SELECT f.name
FROM film AS f
JOIN likes AS l ON f.film_id=l.film_id
GROUP BY f.name
ORDER BY COUNT(l.user_id) DESC
LIMIT 10

Общие друзья пользователей 1 и 2
SELECT friend_id
FROM friends
WHERE user_id IN (1, 2)
GROUP BY friend_id
HAVING COUNT(*) > 1