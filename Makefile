.DEFAULT_GOAL := package

.PHONY: login
login:
	heroku login
	heroku container:login

.PHONY: package
package:
	mvn clean package jib:dockerBuild

.PHONY: deploy
deploy: package
	docker image tag clover-search:$$(grep '<version>' pom.xml | head -n 1 | perl -wp -e 's!.+>(.+)<.+!$$1!') registry.heroku.com/clover-search/web
	docker image push registry.heroku.com/clover-search/web

.PHONY: release
release: deploy
	heroku container:release web --app clover-search
