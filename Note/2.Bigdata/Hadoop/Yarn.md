# 删除处于ACCEPTED状态的任务
for i in  `yarn application  -list | grep -w  ACCEPTED | awk '{print $1}' | grep application_`; do yarn  application -kill $i; done

# 批量kill掉yarn上所有正在running的任务

for i in  `yarn application  -list | grep -w  RUNNING | awk '{print $1}' | grep application_`; do yarn  application -kill $i; done