from sqlalchemy.orm import Session
from app.db.models import Swimsuit, Swimcap

##### fastapi 에서는 조회만 가능함 #####

def get_swimsuits(db: Session):
    return db.query(Swimsuit).all()

def get_swimsuit(db: Session, id: str):
    return db.query(Swimsuit).filter(Swimsuit.id == id).first()

def get_swimcaps(db: Session):
    return db.query(Swimcap).all()

def get_swimcap(db: Session, id: str):
    return db.query(Swimcap).filter(Swimcap.id == id).first()