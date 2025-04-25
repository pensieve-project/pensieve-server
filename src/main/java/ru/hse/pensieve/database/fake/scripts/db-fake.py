from cassandra.cluster import Cluster
from cassandra.query import SimpleStatement
from uuid import uuid4, uuid1, UUID
from faker import Faker
import random
from datetime import datetime
import os
import json
import psycopg2
from psycopg2.extras import register_uuid

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

# POSTGRES CONNECTION
conn = psycopg2.connect(
    dbname='pensieveDatabase',
    user='postgres',
    password='postgres',
    host='localhost'
)
cursor = conn.cursor()
register_uuid()

# CASSANDRA CONNECTION
cluster = Cluster(['127.0.0.1'], port=9042)
session = cluster.connect('pensieve')

fake = Faker()

admin_id = UUID("b8855fe9-feb8-41a6-a367-47f576e340af")
authors = [admin_id] + [uuid4() for _ in range(50)]
# USERS
for author_id in authors[1:]:
    username = fake.user_name()
    email = fake.email()
    password_hash = fake.sha256()
    salt = fake.bothify(text='????????##??')

    cursor.execute(
        """INSERT INTO users (id, username, email, password_hash, salt)
           VALUES (%s, %s, %s, %s, %s)""",
        (author_id, username, email, password_hash, salt)
    )
conn.commit()
cursor.close()
conn.close()

# PROFILES
for author_id in authors:
    description = fake.text(max_nb_chars=150)
    liked_themes_ids = []
    liked_posts_ids = []

    session.execute(
        "INSERT INTO profiles (authorId, avatar, description, likedThemesIds, likedPostsIds, subscriptionsCount, subscribersCount, isVip) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)",
        (author_id, None, description, liked_posts_ids, liked_themes_ids, 0, 0, False)
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
    for _ in range(3):
        author_id = random.choice(authors)
        post_id = uuid4()
        photo_blob = theme_map[theme_id]['photo']
        text = fake.paragraph(nb_sentences=3)
        timestamp = fake.date_time_between(start_date='-1y', end_date='now')
        likes = 0
        comments = 0
        posts.append((post_id, author_id, theme_id))

        session.execute(
            "INSERT INTO posts (themeId, authorId, postId, photo, text, timeStamp, likesCount, commentsCount) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)",
            (theme_id, author_id, post_id, photo_blob, text, timestamp, likes, comments)
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

# SUBSCRIPTIONS
subscriptions = set()
num_subscriptions = 200
for _ in range(num_subscriptions):
    subscriber_id = random.choice(authors)
    target_id = random.choice(authors)

    while subscriber_id == target_id or (subscriber_id, target_id) in subscriptions:
        subscriber_id = random.choice(authors)
        target_id = random.choice(authors)

    subscriptions.add((subscriber_id, target_id))
    timestamp = fake.date_time_between(start_date='-1y', end_date='now')

    session.execute(
        "INSERT INTO subscriptions_by_subscriber (subscriberId, targetId, timeStamp) VALUES (%s, %s, %s)",
        (subscriber_id, target_id, timestamp)
    )

    session.execute(
        "INSERT INTO subscribers_by_target (targetId, subscriberId, timeStamp) VALUES (%s, %s, %s)",
        (target_id, subscriber_id, timestamp)
    )

    row = session.execute(
        "SELECT subscriptionsCount FROM profiles WHERE authorId = %s",
        (subscriber_id,)
    ).one()
    if row:
        new_subs = row.subscriptionscount + 1
        session.execute(
            "UPDATE profiles SET subscriptionsCount = %s WHERE authorId = %s",
            (new_subs, subscriber_id)
        )

    row = session.execute(
        "SELECT subscribersCount FROM profiles WHERE authorId = %s",
        (target_id,)
    ).one()
    if row:
        new_subs = row.subscriberscount + 1
        session.execute(
            "UPDATE profiles SET subscribersCount = %s WHERE authorId = %s",
            (new_subs, target_id)
        )
