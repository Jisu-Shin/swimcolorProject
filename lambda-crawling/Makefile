.PHONY: build run test stop logs build-run

build:
	docker build \
	    -f Dockerfile \
	    -t crawling-lambda .

run:
	docker run \
	    -v ~/.aws:/root/.aws \
	    -d --name crawling-lambda-test \
	    -p 9000:8080 \
	    --env-file .env \
	    crawling-lambda

test:
	curl -X POST "http://localhost:9000/2015-03-31/functions/function/invocations" \
		-H "Content-Type: application/json" \
		-d '{"url": "https://swim.co.kr/categories/918698/products?childCategoryNo=919173&brands=%255B43160588%252C43160568%255D&pageNumber=1"}'

build-run: build run