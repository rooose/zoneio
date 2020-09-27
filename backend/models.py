from app import db
from sqlalchemy.dialects.postgresql import JSON
import datetime


class Coordinate(db.Model):
    __tablename__ = 'coordinate'
    
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, ForeignKey('user.id'))
    latitude = db.Column(db.Float, nullable=False))
    longitude = db.Column(db.Float, nullable=False))
    timestamp = db.Column(db.DateTime, nullable=False, default=datetime.datetime.now)

    def __init__(self, user_id, latitude, longitude):
        self.user_id = user_id
        self.latitude = latitude
        self.longitude = longitude

    def __repr__(self):
        return f'<user_id {self.user_id}: ({self.latitude}, {self.longitude}) - {self.timestamp}>'


class User(db.Model):
    __tablename__ = 'user'

    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(50))
    password = db.Column(db.String(50))
    admin = db.Column(db.Boolean)

    def __init__(self, username, password, admin):
        self.username = username
        self.password = password
        self.admin = admin

    def __repr__(self):
        return f'<username {self.username}: is admin? {self.admin}>