from pydantic import BaseModel

class SwimsuitRequest(BaseModel):
    swimsuit_id: str
    swimsuit_color: list