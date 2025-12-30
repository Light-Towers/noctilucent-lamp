#!/bin/bash
# setting up prerequisites

#bin/elasticsearch-plugin install analysis-icu
#bin/elasticsearch-plugin install analysis-smartcn
#bin/elasticsearch-plugin install --batch https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v7.10.2/elasticsearch-analysis-ik-7.10.2.zip
#bin/elasticsearch-plugin install --batch https://github.com/KennFalcon/elasticsearch-analysis-hanlp/releases/download/v7.10.2/elasticsearch-analysis-hanlp-7.10.2.zip

#bin/elasticsearch-plugin install file:///apps/elasticsearch-analysis-ik-7.10.2.zip
#bin/elasticsearch-plugin install file:///apps/elasticsearch-analysis-dynamic-synonym-7.10.2.zip
#bin/elasticsearch-plugin install file:///apps/elasticsearch-analysis-pinyin-7.10.2.zip
#bin/elasticsearch-plugin install file:///apps/elasticsearch-analysis-hanlp-7.10.2.zip

exec /usr/local/bin/docker-entrypoint.sh elasticsearch

