PROJECT=phonesdb
PROJ_TAG=app-postgresql

run-pg:
	docker build --tag=$(PROJ_TAG) ./docker/pg
	docker run -p 7432:5432 --name $(PROJECT) -e POSTGRES_PASSWORD=mysecretpassword -d $(PROJ_TAG)

clear:
	docker stop $(PROJECT) && docker rm $(PROJECT)
