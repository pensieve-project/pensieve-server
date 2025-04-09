from cassandra.cluster import Cluster
from cassandra.query import SimpleStatement
from uuid import uuid4, uuid1
from faker import Faker
import random
from datetime import datetime

cluster = Cluster(['127.0.0.1'], port=9042)
session = cluster.connect('pensieve')

fake = Faker()

# PROFILES
authors = [uuid4() for _ in range(50)]
for author_id in authors:
    description = fake.text(max_nb_chars=150)
    liked_posts_ids = []

    session.execute(
        "INSERT INTO profiles (authorId, description, likedPostsIds) VALUES (%s, %s, %s)",
        (author_id, description, liked_posts_ids)
    )

# THEMES
themes = []
for _ in range(10):
    theme_id = uuid4()
    author_id = random.choice(authors)
    title = fake.sentence(nb_words=3)[:-1]
    timestamp = fake.date_time_between(start_date='-2y', end_date='now')
    themes.append(theme_id)

    session.execute(
        "INSERT INTO themes (themeId, authorId, title, timeStamp) VALUES (%s, %s, %s, %s)",
        (theme_id, author_id, title, timestamp)
    )

# POSTS
posts = []
for _ in range(50):
    theme_id = random.choice(themes)
    author_id = random.choice(authors)
    post_id = uuid4()
    text = fake.paragraph(nb_sentences=3)
    timestamp = fake.date_time_between(start_date='-1y', end_date='now')
    likes = 0
    comments = 0
    posts.append((post_id, author_id, theme_id))

    session.execute(
        "INSERT INTO posts (themeId, authorId, postId, photo, text, timeStamp, likesCount, commentsCount) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)",
        (theme_id, author_id, post_id, None, text, timestamp, likes, comments)
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