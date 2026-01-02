from pydantic import BaseModel

class CrawlRequest(BaseModel):
    logId: int
    crawlingUrl: str
    callbackUrl: str

class CrawlResponse(BaseModel):
    logId: int
    crawlStatus: str
    errorMsg: str
    products: list