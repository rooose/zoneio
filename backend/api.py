from flask import Flask, request, jsonify, make_response
from flask_api import status
from flask_sqlalchemy import SQLAlchemy
from werkzeug.security import generate_password_hash, check_password_hash
import datetime
from functools import wraps
import jwt
import uuid
import os
import sqlite3

app = Flask(__name__)
app.config.from_object(os.environ['APP_SETTINGS'])
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
db = SQLAlchemy(app)

from models import Coordinate, User

def token_required(f):
    @wraps(f)
    def decorator(*args, **kwargs):
        token = None

        if 'x-access-tokens' in request.headers:
            token = request.headers['x-access-tokens']

        if not token:
            return jsonify({'message': 'a valid token is missing'})

        try:
            data = jwt.decode(token, app.config[SECRET_KEY])
            current_user = Users.query.filter_by(public_id=data['public_id']).first()
        except:
            return jsonify({'message': 'token is invalid'})

        return f(current_user, *args, **kwargs)
    return decorator


# GET

@app.route('/', methods=['GET'])
def hello_world():
    return 'This is the zone development server :)'


@app.route('/register', methods=['POST'])
def signup_user():
    data = request.get_json()
    error = None

    if not data['username']:
        error = 'Username is required.'
    elif not data['password']:
        error = 'Password is required.'
    elif db.session.query(User.id).filter_by(name=data['username']).scalar() is not None:
        error = f'User {data["username"]} is already registered.'

    if error is None:
        hashed_password = generate_password_hash(data['password'], method='sha256')

        new_user = User(name=data['name'], password=hashed_password, admin=False)
        db.session.add(new_user)
        db.session.commit()

        return jsonify({'message': 'registered successfully', 'status': status.HTTP_200_OK})
    else:
        return jsonify({'message': 'could not register user', 'error': error, 'status': status.HTTP_400_BAD_REQUEST})


@app.route('/login', methods=['POST'])
def login_user():
    auth = request.authorization
    error = None

    if not auth or not auth.username or not auth.password:
        return make_response('could not verify', status.HTTP_401_UNAUTHORIZED, {'WWW.Authentication': 'Basic realm: "login required"'})

    user = Users.query.filter_by(name=auth.username).first()
    if user is None:
        error = "Invalid username"

    if error is None and check_password_hash(user.password, auth.password):
        token = jwt.encode({'public_id': user.public_id, 'exp' : datetime.datetime.utcnow() + datetime.timedelta(minutes=30)}, app.config['SECRET_KEY'])
        return jsonify({'token' : token.decode('UTF-8')})
    elif error is None:
        error = "Invalid password"

    return make_response(error,  status.HTTP_401_UNAUTHORIZED, {'WWW.Authentication': 'Basic realm: "login required"'})


@app.route('/coordinates', methods=['POST'])
@token_required
def create_coords(current_user):
    data = request.get_json()
    new_coords = Coordinates(user_id=current_user.id, latitude=data['x'], longitude=data['y'], timestamp=data['timestamp'])
    db.session.add(new_coords)
    db.session.commit()

    return jsonify({'message' : 'new coordinates added'})

@app.errorhandler(404)
def page_not_found(e):
    return "<h1>404</h1><p>The resource could not be found.</p>", 404

if __name__ == '__main__':
    app.run()