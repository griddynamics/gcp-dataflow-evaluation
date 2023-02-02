# Dataflow vs Apache Spark Benchmark

## Running Dataflow
    mvn -Pdataflow-runner compile exec:java \
          -Dexec.mainClass=org.apache.beam.examples.PipelineJavaJSON \
          -Dexec.args=“--project=gcp-project \
                      --gcpTempLocation=gs://TEMPORARY_STORAGE \
                      --runner=DataflowRunner \
                      --region=europe-west4”
                      
## Running spark job on a Dataproc cluster
### Creating a cluster
    gcloud dataproc clusters create cluster_name \
        --image-version=2.0 \
        --region=europe-west4 \
        --enable-component-gateway \
        --master-machine-type=n2-standard-8 \
        --num-workers=2 \
        --worker-machine-type=n2-standard-8 \
        --properties=${PROPERTIES} \
        --optional-components=DOCKER

### Submitting pyspark job to the cluster
    gcloud dataproc jobs submit pyspark \
        --cluster=cluster_name \
        --region=europe-west4 \
        spark_pipeline_avro.py \
        --properties spark.jars.packages=‘org.apache.spark:spark-avro_2.12:3.1.3’

