#Phonebook REST API


### Running

```bash
sbt run
```

Play will start up on the HTTP port at <http://localhost:9000/>.   
### Usage

- добавить телефон (строка) и имя (строка) в справочник
 
```
POST /phones/createNewPhone 
```

- получить список ранее добавленных вхождений в справочник (кортеж <id, ссылка, имя, номер>) в виде json
```
GET /phones
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
