from sqlalchemy import Column, Integer, String
from app.db.connection import Base


class Swimsuit(Base):
    __tablename__ = 'swimsuit'
    id = Column(String(10), primary_key=True)
    name = Column(String(255))
    image_url = Column(String(255))
    product_url = Column(String(255))
    brand = Column(String(255))
    price = Column(Integer)

class SwimsuitPalette(Base):
    __tablename__ = 'swimsuit_palette'
    swimsuit_id = Column(String(10), primary_key=True)
    colors = Column(String(255), primary_key=True)

class Swimcap(Base):
    __tablename__ = 'swimcap'
    id = Column(String(10), primary_key=True)
    name = Column(String(255))
    image_url = Column(String(255))
    product_url = Column(String(255))
    brand = Column(String(255))
    price = Column(Integer)

class SwimcapPalette(Base):
    __tablename__ = 'swimcap_palette'
    swimcap_id = Column(String(10), primary_key=True)
    colors = Column(String(255), primary_key=True)