from pydantic import BaseModel

class CrawlRequest(BaseModel):
    url: str

class CrawlResponse(BaseModel):
    products: list