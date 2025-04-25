from cassandra.cluster import Cluster
import psycopg2

# CASSANDRA
cluster = Cluster(['127.0.0.1'], port=9042)
session = cluster.connect('pensieve')

tables_to_truncate = [
    'posts',
    'themes',
    'profiles',
    'comments',
    'subscriptions_by_subscriber',
    'subscribers_by_target',
    'user_feed',
    'vip_posts'
]

for table in tables_to_truncate:
    session.execute(f'TRUNCATE {table};')

# POSTGRES
postgres_tables = [
    'users'
]

conn = psycopg2.connect(
    dbname='pensieveDatabase',
    user='postgres',
    password='postgres',
    host='localhost'
)
cursor = conn.cursor()

for table in postgres_tables:
    cursor.execute(f'TRUNCATE TABLE {table} CASCADE;')

conn.commit()
cursor.close()
conn.close()
