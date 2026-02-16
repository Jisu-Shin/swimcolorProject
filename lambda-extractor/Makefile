.PHONY: build run test stop logs build-run

build:
	docker build \
	    -f Dockerfile \
	    -t lambda-extractor .

run:
	docker run \
	    -v ~/.aws:/root/.aws \
	    -d --name lambda-extractor-test \
	    -p 9001:8080 \
	    --env-file .env \
	    lambda-extractor

test:
	curl -X POST "http://localhost:9001/2015-03-31/functions/function/invocations" \
		-H "Content-Type: application/json" \
		-d @test.json

build-run: build run