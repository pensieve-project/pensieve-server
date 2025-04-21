from cassandra.cluster import Cluster
from cassandra.query import SimpleStatement
from uuid import uuid4, uuid1, UUID
from faker import Faker
import random
from datetime import datetime
import os
import json

PHOTO_DIR = './images'
THEMES_JSON_PATH = './themes.json'

with open(THEMES_JSON_PATH) as f:
    raw_themes = json.load(f)

theme_map = {}
for theme in raw_themes:
    theme_id = uuid4()
    image_path = os.path.join(PHOTO_DIR, theme['image_path'])
    with open(image_path, 'rb') as img_file:
        theme_map[theme_id] = {
            "title": theme['title'],
            "photo": img_file.read()
        }

cluster = Cluster(['127.0.0.1'], port=9042)
session = cluster.connect('pensieve')

session.execute("""
    CREATE TYPE IF NOT EXISTS point_type (
        latitude double,
        longitude double
    )
""")

keyspace = cluster.metadata.keyspaces['pensieve']
point_type = keyspace.user_types['point_type']

fake = Faker()

def generate_random_coords():
    return {
        'latitude': random.uniform(59.5, 60.0),
        'longitude': random.uniform(30.0, 30.5)
    }

# PROFILES
authors = [uuid4() for _ in range(50)]
for author_id in authors:
    description = fake.text(max_nb_chars=150)
    liked_themes_ids = []
    liked_posts_ids = []

    session.execute(
        "INSERT INTO profiles (authorId, avatar, description, likedThemesIds, likedPostsIds) VALUES (%s, %s, %s, %s, %s)",
        (author_id, None, description, liked_posts_ids, liked_themes_ids)
    )

# THEMES
themes = []
for theme_id, data in theme_map.items():
    author_id = random.choice(authors)
    title = data['title']
    timestamp = fake.date_time_between(start_date='-2y', end_date='now')
    themes.append(theme_id)

    session.execute(
        "INSERT INTO themes (themeId, authorId, title, timeStamp) VALUES (%s, %s, %s, %s)",
        (theme_id, author_id, title, timestamp)
    )

# POSTS
posts = []
for theme_id in themes:
    author_id = random.choice(authors)
    post_id = uuid4()
    photo_blob = theme_map[theme_id]['photo']
    text = fake.paragraph(nb_sentences=3)
    timestamp = fake.date_time_between(start_date='-1y', end_date='now')
    coords = generate_random_coords()
    likes = 0
    comments = 0
    posts.append((post_id, author_id, theme_id))

    query = """
        INSERT INTO posts
            (themeId, authorId, postId, photo, text, timeStamp, location, likesCount, commentsCount)
        VALUES (%s, %s, %s, %s, %s, %s, {latitude: %s, longitude: %s}, %s, %s)
    """

    session.execute(
        query,
        (theme_id, author_id, post_id, photo_blob, text, timestamp,
         coords['latitude'], coords['longitude'], likes, comments)
    )

# LIKES
for _ in range(100):
    author_id = random.choice(authors)
    post = random.choice(posts)
    post_id, author_id, theme_id = post

    session.execute(
        "UPDATE profiles SET likedPostsIds = likedPostsIds + [%s] WHERE authorId = %s",
        (post_id, author_id)
    )

    row = session.execute(
        "SELECT likesCount FROM posts WHERE themeId = %s AND authorId = %s AND postId = %s",
        (theme_id, author_id, post_id)
    ).one()
    if row:
        new_likes = row.likescount + 1
        session.execute(
            "UPDATE posts SET likesCount = %s WHERE themeId = %s AND authorId = %s AND postId = %s",
            (new_likes, theme_id, author_id, post_id)
        )

# COMMENTS
for _ in range(100):
    post = random.choice(posts)
    post_id, author_id, theme_id = post
    commenter_id = random.choice(authors)
    comment_id = uuid1()
    text = fake.sentence()

    session.execute(
        "INSERT INTO comments (postId, commentId, authorId, text) VALUES (%s, %s, %s, %s)",
        (post_id, comment_id, commenter_id, text)
    )

    row = session.execute(
        "SELECT commentsCount FROM posts WHERE themeId = %s AND authorId = %s AND postId = %s",
        (theme_id, author_id, post_id)
    ).one()
    if row:
        new_comments = row.commentscount + 1
        session.execute(
            "UPDATE posts SET commentsCount = %s WHERE themeId = %s AND authorId = %s AND postId = %s",
            (new_comments, theme_id, author_id, post_id)
        )