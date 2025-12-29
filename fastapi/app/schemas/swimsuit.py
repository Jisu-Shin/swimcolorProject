from pydantic import BaseModel

class SwimsuitRequest(BaseModel):
    swimsuitId: str
    colors: list[str]