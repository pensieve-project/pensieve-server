from cassandra.cluster import Cluster

cluster = Cluster(['188.187.122.19'], port=9042)
session = cluster.connect('pensieve')

tables_to_truncate = [
    'posts',
    'themes',
    'profiles',
    'comments'
]

for table in tables_to_truncate:
    session.execute(f'TRUNCATE {table};')