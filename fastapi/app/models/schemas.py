from sqlalchemy import Column, DateTime, ForeignKey, Integer, String, UniqueConstraint
# from app.database import Base


# class Swimsuit(Base):
#     id = Column(String(10), primary_key=True)
#     name = Column(String(255))
#     image_url = Column(String(255))
#     product_url = Column(String(255))
#     brand = Column(String(255))
#     price = Column(Integer)
#
# class SwimsuitPalette(Base):
#     swimsuit_id = Column(String(10), primary_key=True)
#     colors = Column(String(255))
#
# class Swimcap(Base):
#     id = Column(String(10), primary_key=True)
#     name = Column(String(255))
#     image_url = Column(String(255))
#     product_url = Column(String(255))
#     brand = Column(String(255))
#     price = Column(Integer)
#
# class SwimcapPalette(Base):
#     swimcap_id = Column(String(10), primary_key=True)
#     colors = Column(String(255))