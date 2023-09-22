```bash
/opt/cloudera/parcels/CDH/lib/spark3/bin/spark-submit \
 --queue app_enterprise_info \
 --deploy-mode client \
 --master yarn \
 --driver-memory 16g \
  --num-executors 4 \
  --executor-cores 4 \
  --executor-memory 16g \
  --conf spark.default.parallelism=100 \
   /root/yangjinhua/firstApp2.py
```

