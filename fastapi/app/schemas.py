from sqlalchemy import Column, DateTime, ForeignKey, Integer, String, UniqueConstraint
from app.database import Base


class Swimsuit(Base):
    __tablename__ = 'swimsuit'
    id = Column(String(10), primary_key=True)
    name = Column(String(255))
    image_url = Column(String(255))
    product_url = Column(String(255))
    brand = Column(String(255))
    price = Column(Integer)

class SwimsuitPalette(Base):
    __tablename__ = 'swimsuitpalette'
    id = Column(String(10), primary_key=True)
    swimsuit_id = Column(String(10))
    colors = Column(String(255))

class Swimcap(Base):
    __tablename__ = 'swimcap'
    id = Column(String(10), primary_key=True)
    name = Column(String(255))
    image_url = Column(String(255))
    product_url = Column(String(255))
    brand = Column(String(255))
    price = Column(Integer)

class SwimcapPalette(Base):
    __tablename__ = 'swimcappalette'
    id = Column(String(10), primary_key=True)
    swimcap_id = Column(String(10))
    colors = Column(String(255))