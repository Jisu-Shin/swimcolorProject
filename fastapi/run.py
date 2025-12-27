import uvicorn

if __name__ == "__main__":
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8000,
        reload=True
    )

# https://swim.co.kr/categories/918698/products?childCategoryNo=919173&brands=%255B43160579%255D&pageNumber=1&categoryNos=%255B%255D