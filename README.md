# Phonebook REST API

### About
Реализован http сервер, предоставляющий REST API для работы с телефонным справочником со
следующим функционалом:
- добавление телефона (строка) и имя (строка) в справочник
- получиние списка ранее добавленных вхождений в справочник (кортеж
<id, имя, номер>) в виде json
- изменение значения телефона и/или имени во вхождении выбранному по id
- удаление из справочника вхождения по id
- поиск всех вхождений по подстроке имени и/или по подстроке номера

Хранение данных реализовано в базе данных postgres, поднятой в docker контейнере.

Добавлено кэширование запрашиваемых из БД данных в памяти
приложения. Кэш ограниченного размера с ttl - временем жизни записи. 
Параметр ttl выбран таким образом, что считаем допустимым некоторое время отдавать пользователю устаревшую инофрмацию. 
Поддерживается консистеность - обновление записи только при успешных действиях с базой данных.

Так же реализовано клиентское веб-приложение - <https://github.com/slysheva/phonebook-web-client>

### Running

Linux
```bash
make clear run-pg && sbt run
```

Приложение будет поднято на HTTP порту 9000 -  <http://localhost:9000/>.   
### Usage

- добавить телефон (строка) и имя (строка) в справочник
 
```
POST /phones/createNewPhone 
```

- получить список ранее добавленных вхождений в справочник (кортеж <id, ссылка, имя, номер>) в виде json
```
GET /phones
```
- получить вхождение по id
```
GET /phone/{id}
``` 
- изменить значения телефона или имени во вхождении выбранному по id
```
POST /phone/{id}
``` 
- удалить из справочника вхождение по id
``` 
DELETE /phone/{id}
``` 
- поиск всех вхождений по подстроке имени и/или номера
``` 
GET /phones/searchBySubstr?nameSubstring=<...>&phoneSubstring=<...>
``` 

### Example of usage
- добавить запись
```bash
$ curl  -d 'name=Sasha&phoneNumber=3458945' http://localhost:9000/phones/createNewPhone -X POST
```

- получить список записей
```
$ curl http://localhost:9000/phones 
```
- пример response 
```
[{"id":"57a3e90a-d743-40aa-bba5-6863d4d4a327","name":"Sasha","number":"3458945"},
{"id":"a215ee5a-b253-447d-a07c-e74610fb263d","name":"Lena","number":"8874738745"}]
```
- редактировать запись по id
```
$ curl  -d 'name=Dasha&phoneNumber=777' http://localhost:9000/phone/57a3e90a-d743-40aa-bba5-6863d4d4a327 -X POST
```
- поиск записей по подстроке
```
$ curl http://localhost:9000/phones/searchBySubstr?nameSubstring=asha
```
- пример response 
```
[{"id":"a34f93b8-cbd6-41ce-99cf-dfab427dd892","name":"Sasha","number":"777"},
{"id":"57a3e90a-d743-40aa-bba5-6863d4d4a327","name":"Dasha","number":"777"}]
```
- удалить запись по id
```
$ curl http://localhost:9000/phone/57a3e90a-d743-40aa-bba5-6863d4d4a327 -X DELETE
```
