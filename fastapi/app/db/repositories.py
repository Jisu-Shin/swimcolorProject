from sqlalchemy.orm import Session
from sqlalchemy import func
from .models import Swimsuit, Swimcap, SwimsuitPalette, SwimcapPalette

##### fastapi 에서는 조회만 가능함 #####

def get_swimsuits(db: Session):
    return db.query(Swimsuit).all()

def get_swimsuit(db: Session, id: str):
    return db.query(Swimsuit).filter(Swimsuit.id == id).first()

def get_swimcaps(db: Session):
    return db.query(Swimcap).all()

def get_all_swimcap_pallete(db: Session):
    return db.query(SwimcapPalette).all()

def get_swimcap(db: Session, id: str):
    return db.query(Swimcap).filter(Swimcap.id == id).first()

def get_swimsuits_no_pallete(db: Session, limit: int = 100):
    return db.query(Swimsuit).outerjoin(
        SwimsuitPalette, Swimsuit.id == SwimsuitPalette.swimsuit_id
    ).filter(SwimsuitPalette.colors == None).limit(limit).all()

def get_swimcaps_no_pallete(db: Session, limit: int = 100):
    return db.query(Swimcap).outerjoin(
        SwimcapPalette, Swimcap.id == SwimcapPalette.swimcap_id
    ).filter(SwimcapPalette.colors == None).limit(limit).all()

def get_random_swimsuits(db: Session, limit: int = 30):
    return db.query(Swimsuit).outerjoin(
        SwimsuitPalette, Swimsuit.id == SwimsuitPalette.swimsuit_id
    ).filter(SwimsuitPalette.colors != None).order_by(func.random()).limit(limit).all()

def get_random_swimcaps(db: Session, limit: int = 30):
    return db.query(Swimcap).outerjoin(
        SwimcapPalette, Swimcap.id == SwimcapPalette.swimcap_id
    ).filter(SwimcapPalette.colors != None).order_by(func.random()).limit(limit).all()
