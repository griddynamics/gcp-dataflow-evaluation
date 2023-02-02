import pyspark
import pyspark.sql.functions as f
import re
import datetime

def fix_date(date):
    try:
        datum = re.split('/|-|,|:| ', date)
        return datetime.date(int(datum[2]), int(datum[1]), int(datum[0])).strftime('%d/%m/%Y')
    except:
        pass

key_file = '...'
order_path = '...'
user_path = '...'

# spark-submit requires spark.jars.packages=‘org.apache.spark:spark-avro_2.12:3.1.3’ for successful reading of avro files
spark = pyspark.sql.SparkSession.builder\
    .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem") \
    .config("spark.hadoop.google.cloud.auth.service.account.enable", "true") \
    .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", key_file) \
    .getOrCreate()


orders = spark.read..format("avro").load(order_path)

users = spark.read.format("avro").load(user_path)

# Filtering order type
orders = orders.filter((orders.order_type == 'BRICK AND MORTAR') | (orders.order_type == 'ONLINE'))

# Fixing shipping info if order is null
orders = orders.withColumn('shipping_info', f.when(orders.order_type == 'ONLINE', orders.shipping_info) \
                                    .otherwise(None))

orders = orders.filter(((orders.order_type == 'ONLINE') & (orders.shipping_info.isNotNull()) & (orders.billing_info.isNotNull())) | ((orders.order_type != 'ONLINE') & (orders.billing_info.isNotNull())))

orders = orders.filter((orders.amount > 0 )& (orders.price > 0))

orders = orders.select('*', 'billing_info.*', 'shipping_info.*').drop('billing_info', 'shipping_info')

orders = orders.filter(~(orders.payment_method == 'CASH') & (orders.card_number.isNotNull()))

orders_date_fix = f.udf(lambda x: fix_date(x))

orders = orders.withColumn('order_date', orders_date_fix('order_date'))

orders = orders.join(users, 'user_id', 'outer')

orders = orders.withColumn('sum', orders.amount * orders.price)

orders1 = orders.groupBy('code').sum('sum')

orders = orders.groupBy('order_id').agg(f.sum(orders.sum).alias('sum'), f.first('order_type').alias('order_type'))

orders2 = orders.groupBy('order_type').agg(f.sum(orders.sum), f.count(orders.sum))

orders2.printSchema()

orders2.show()

